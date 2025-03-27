// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcremote.trace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.hpctoolkit.db.protocol.time.Timestamp;
import org.hpctoolkit.db.protocol.trace.TraceId;
import org.hpctoolkit.db.client.BrokerClient;
import org.hpctoolkit.db.client.FutureTraceSamplingSet;
import org.hpctoolkit.db.client.TraceDataNotAvailableException;
import org.hpctoolkit.db.client.TraceSampling;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcbase.DebugUtil;
import edu.rice.cs.hpcbase.IProcessTimeline;
import edu.rice.cs.hpcbase.ITraceDataCollector;
import edu.rice.cs.hpcbase.ITraceDataCollector.TraceOption;
import org.hpctoolkit.db.local.db.IdTuple;
import org.hpctoolkit.db.local.experiment.IExperiment;
import edu.rice.cs.hpctraceviewer.config.TracePreferenceManager;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;


/**
 * A remote version of trace data
 * <p>
 * This class is NOT thread-safe.
 * <ul>
 *   <li> It only handles one database at a time, not multiple database (just like other classes)</li>
 *   <li> No support for concurrent request. Only one request at a time. </li>
 *   <li> If a previous request is still in process, it will wait until it's done</li>
 * </ul>
 */
public class RemoteSpaceTimeDataController extends SpaceTimeDataController 
{
	/**
	 * Protects multi-threaded access to this controller.
	 * <p>
	 * Controller instances are expected to be utilized by mutliple threads. This monitor should be used to
	 * enforce thread-safe access to any fields that are not otherwise thread-safe and/or enforce any mutual exclusion
	 * required to fulfill this class' contract.
	 */
	private final Object controllerMonitor = new Object();

	/**
	 * The client to use to contact the remote server that contains the source of truth for trace sampling information.
	 * Since {@code HpcClient} is not contractually thread-safe, synchronize on {@link #controllerMonitor} to ensure
	 * thread-safe operation. 
	 */
	private final BrokerClient client;

	/**
	 * A (future) description of sampled traces reflective of the most recent call to {@link #startTrace(int, boolean)}.
	 * A {@code null} value for this fields indicates that either {@code startTrace(...)} has never been invoked, or, 
	 * {@link #closeDB()} has been invoked more recently than the last invocation of {@code startTrace(...)}.
	 * <p>
	 * Since {@code FutureTraceSamplingSet} is not contractually thread-safe, synchronize on {@link #controllerMonitor}
	 * to ensure thread-safe operation.
	 */
	private FutureTraceSamplingSet sampledTraces;

	/**
	 * Tracks the subset of the samplings within {@link #sampledTraces} that have not yet been returned by 
	 * {@link #getNextTrace()} (in {@code IProcessTimeline} form). Note that this field eagerly transitions to
	 * {@code null} when {@code sampledTraces} transitions to {@code null}, but when {@code sampledTraces} transitions
	 * to {@code non-null} this field defers population until the next invocation of {@code getNextTrace()}.
	 */
	// n.b. the motivation for the lazy transition to non-null comes from the fact that the population of this field
	// via `unYieldedSampledTraces = sampledTraces.get().toJavaSet();` can throw a checked exception, but the method 
	// that populates `sampledTraces`, namely `startTrace(int numTraces, boolean changedBounds)`, is regulated by our
	// super class not to throw a checked exception. As such we have to defer population of this field to a method that 
	// can throw a checked exception, namely `getNextTrace()`.		
	private java.util.Set<Future<TraceSampling>> unYieldedSampledTraces;

	/**
	 * TODO: document
	 *
	 * Since {@code mapIntToLine} is not contractually thread-safe, synchronize on {@link #controllerMonitor} to ensure
	 * thread-safe operation.
	 */
	private IntIntHashMap mapIntToLine;

	/**
	 * TODO: document
	 *
	 * Synchronize on {@link #controllerMonitor} to ensure thread-safe operation.
	 */
	private boolean changedBounds;

	/**
	 * TODO: document
	 *
	 * Synchronize on {@link #controllerMonitor} to ensure thread-safe operation.
	 */
	private int currentLine;
	
	private AtomicInteger numCurrentQueries = new AtomicInteger(0);

	
	public RemoteSpaceTimeDataController(BrokerClient client, IExperiment experiment) throws IOException {
		super(experiment);

		sampledTraces = null;
		this.client = client;

		setTraceBeginAndEndTime(client, experiment);
		
		var listIdTuples = experiment.getThreadData().getIdTuples();
		var idTupleType  = experiment.getIdTupleType();
		
		setBaseData(new RemoteFilteredData(client, listIdTuples, idTupleType));
	}
	
	
	/****
	 * Initialize start and end time of the trace.
	 * This is to mimic IFileDb behavior to initialize trace attributes.
	 * 
	 * @param client
	 * @param experiment
	 * @throws IOException
	 */
	private void setTraceBeginAndEndTime(BrokerClient client, IExperiment experiment) throws IOException  {
		var attributes = experiment.getTraceAttribute();
		try {
			var minTime = client.getMinimumTraceSampleTimestamp();
			if (minTime.isPresent()) {
				attributes.dbTimeMin = minTime.get().toEpochNano();
			}
			
			var maxTime = client.getMaximumTraceSampleTimestamp();
			if (maxTime.isPresent()) {
				attributes.dbTimeMax = maxTime.get().toEpochNano();
			}
		} catch (InterruptedException e) {
		    // Restore interrupted state...
		    Thread.currentThread().interrupt();
		} catch (TraceDataNotAvailableException e) {
			// ignore
			LoggerFactory.getLogger(getClass()).error("Begin and end trace time: No data available from the server", e);
		}
	}
	
	
	/***
	 * Retrieve the trace line number given the trace profile index.
	 * Note: A trace line is the order of trace to be painted.
	 * 
	 * @param profileIndex
	 * 			The trace profile index in trace.db file
	 * @return
	 */
	public int getTraceLineFromProfile(int profileIndex) {
		synchronized (controllerMonitor) {
			// possible data concurrent here, but ...
			// It's totally okay to concurrently reading a map index.
			return mapIntToLine.get(profileIndex);
		}
	}

	
	@Override
	public String getName() {
		return "Remote: " + super.getExperiment().getName();
	}

	
	@Override
	public void closeDB() {
		// laks: do we need to sync this block?
		// Other than that, there is no harm to concurrently setting null for sampledTraces and unYieldedSampledTraces
		synchronized (controllerMonitor) { // ensure safe publication of new values of class fields
			if (mapIntToLine != null)
				mapIntToLine.clear();

			sampledTraces = null;
			unYieldedSampledTraces = null;
		}
	}

	
	@Override
	public void startTrace(int numTraces, boolean changedBounds) {

		// if we are in the middle of query, we need to wait until all previous queries are done.
		// Another solution is to fix hpcserver#92 issue by allowing the client to abort previous queries
		while(numCurrentQueries.get() > 0) {
			// dry out the old processes
			try {
				var process = getNextTrace();
				if (process == null)
					break;
				Thread.sleep(50);
			} catch (InterruptedException e) {
			    // Restore interrupted state...
			    Thread.currentThread().interrupt();
			} catch (Exception e) {
				LoggerFactory.getLogger(getClass()).error("Fail to start trace ", e);
			}
		}
		synchronized (controllerMonitor) { // ensure all threads see the most recent values of all fields
			this.changedBounds = changedBounds;
			
			if (!changedBounds) {
				currentLine = numTraces;
				return;
			}

			var traceAttr = getTraceDisplayAttribute();
			var pixelsV = traceAttr.getPixelVertical();

			// in case the number of ranks is bigger than the number of pixels,
			// we need to pick (or sample) which ranks need to be displayed.
			// A lazy way is to rely on the server to pick which ranks to be displayed.

			// collect id tuples to fit number of vertical pixels
			var numTracesToCollect = Math.min(numTraces, pixelsV);
			
			// use iterable list because vavr set sometimes doesn't want to add items
			List<TraceId> listTracesToCollect = new ArrayList<>(numTracesToCollect);
			
			// a map from a profile index to its trace line number
			mapIntToLine = new IntIntHashMap(numTracesToCollect);

			for(int i=0; i<numTracesToCollect; i++) {
				var idt = getProfileFromPixel(i);
				var profileIndex = idt.getProfileIndex() - 1;
				
				TraceId traceId = TraceId.make(profileIndex);
				listTracesToCollect.add(traceId);
				
				// create a map from the trace's profile index to the sorted trace order.
				// ideally this is done at the server level or the viewer can piggy-back the order 
				// within TraceId object.
				mapIntToLine.put(profileIndex, i);
			}
			Set<TraceId> setOfTraceId = HashSet.ofAll(listTracesToCollect);

			var traceAttributes = getExperiment().getTraceAttribute();
			var frame = traceAttr.getFrame();

			Timestamp time1 = Timestamp.ofEpochNano(frame.begTime + traceAttributes.dbTimeMin);
			Timestamp time2 = Timestamp.ofEpochNano(frame.endTime + traceAttributes.dbTimeMin);

			if (time1.isAfter(time2)) {
				// the time range is not initialized yet
				// this shouldn't happen
				try {
					var traceTimeMin = client.getMinimumTraceSampleTimestamp();
					var traceTimeMax = client.getMaximumTraceSampleTimestamp();
					if (traceTimeMin.isPresent()) {
						time1 = traceTimeMin.get();
					}
					if (traceTimeMax.isPresent()) {
						time2 = traceTimeMax.get();
					}
				} catch (InterruptedException | TraceDataNotAvailableException e) {
					LoggerFactory.getLogger(getClass()).error("Cannot retrieve time min/max", e);
					// Restore interrupted state...
					Thread.currentThread().interrupt();
				} catch (IOException e) {
					throw new IllegalAccessError(e.getMessage());
				}
			}

			/*
			 * Collect the trace sampling information from the remote server that will be yieled by incremental
			 * calls to `getNextTrace()`.
			 */
			DebugUtil.debugThread(getClass().getName(), String.format("num-traces: %d, num-samples: %d, time: %d - %d   %n", setOfTraceId.size(), getPixelHorizontal(), time1.toEpochNano(), time2.toEpochNano()));
			
			sampledTraces = client.sampleTracesAsync(setOfTraceId, time1, time2, getPixelHorizontal());
			unYieldedSampledTraces = null; // ensure any previous subset of `sampledTraces` is cleared.
			                               // see field contract for details
		}
		// mark that we add a new query.
		numCurrentQueries.incrementAndGet();
	}


	@Override
	public ITraceDataCollector getTraceDataCollector(int lineNum, IdTuple idTuple) {
		TraceOption traceOption = TraceOption.ORIGINAL_TRACE;
		var idtupleType = getExperiment().getIdTupleType();

		if (TracePreferenceManager.getGPUTraceExposure() && idTuple.isGPU(idtupleType))
			traceOption = TraceOption.REVEAL_GPU_TRACE;
		
		return new RemoteTraceDataCollectorPerProfile(getPixelHorizontal(), traceOption);
	}


	@Override
	public IProcessTimeline getNextTrace() throws Exception {
		synchronized (controllerMonitor)  { // ensure all threads see the most recent values of all fields

			// if we just want to reuse the existing trace data, there is no need to check the sampled traces
			// or any un-yield traces
			if (!changedBounds) {
				currentLine--;
				if (currentLine < 0) {
					done();
					return null;
				}
				return getProcessTimelineService().getProcessTimeline(currentLine);
			}

			if(sampledTraces == null) {
				done();
				throw new IllegalStateException("getNextTrace() may not be invoked prior to startTrace(...) or after" +
						                        " this controller is closed.");
			}

			/*
			 * If this is the first time `getNextTrace()` has been called since the most recent invocation of
			 * `startTrace(...)`, set `unYieldedSampledTraces` to the entire set of trace samplings in `sampledTraces`
			 * to reflect that none have yet been returned by this method since the last `startTrace(...)`.
			 *
			 * See field documentation of `unYieldedSampledTraces` as to why this is done here and not when
			 * `sampledTraces` is updated.
			 */
			if (unYieldedSampledTraces == null)
				unYieldedSampledTraces = sampledTraces.get().toJavaSet();

			/*
			 * If there are traces within `sampledTraces` that have not yet been yieled by this method, pick one
			 * arbitrarily and return it in `RemoteProcessTimeline` form. Otherwise, return `null`.
			 */
			Iterator<Future<TraceSampling>> unYieldedSampledTracesElements = unYieldedSampledTraces.iterator();
			if (unYieldedSampledTracesElements.hasNext())
			{
				Future<TraceSampling> sampledTrace = unYieldedSampledTracesElements.next();
				unYieldedSampledTracesElements.remove(); // `sampledTrace` is about to be yielded, so declare yielded
				return new RemoteProcessTimeline(this, sampledTrace);
			}
			done();
			
			return null;
		}
	}
	
	private void done() {
		numCurrentQueries.decrementAndGet();
		unYieldedSampledTraces = null;
		currentLine = 0;
	}
}
