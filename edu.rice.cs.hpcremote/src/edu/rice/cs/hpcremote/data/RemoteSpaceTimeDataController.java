package edu.rice.cs.hpcremote.data;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.hpctoolkit.client_server_common.time.Timestamp;
import org.hpctoolkit.client_server_common.trace.TraceId;
import org.hpctoolkit.hpcclient.v1_0.FutureTraceSamplingSet;
import org.hpctoolkit.hpcclient.v1_0.HpcClient;
import org.hpctoolkit.hpcclient.v1_0.TraceDataNotAvailableException;
import org.hpctoolkit.hpcclient.v1_0.TraceSampling;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcbase.IFilteredData;
import edu.rice.cs.hpcbase.IProcessTimeline;
import edu.rice.cs.hpcbase.ITraceDataCollector;
import edu.rice.cs.hpcbase.ITraceDataCollector.TraceOption;
import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.experiment.BaseExperiment;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.visitors.TraceScopeVisitor;
import edu.rice.cs.hpctraceviewer.config.TracePreferenceManager;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

public class RemoteSpaceTimeDataController extends SpaceTimeDataController 
{
	private final HpcClient client;
	
	private FutureTraceSamplingSet samplingSet;
	
	private IntIntHashMap mapIntToLine;
	
	private boolean changedBounds;
	private AtomicInteger currentLine;
	
	public RemoteSpaceTimeDataController(HpcClient client, IExperiment experiment) {
		super(experiment);
		
		createScopeMap((BaseExperiment) experiment);
		
		this.client = client;
		var remoteTraceData = createTraceData(experiment);
		super.setBaseData(remoteTraceData);
	}

	private IFilteredData createTraceData(IExperiment experiment) {		
		var idTupleType = experiment.getIdTupleType();

		try {
			var listIdTuples = experiment.getThreadData().getIdTuples();

			mapIntToLine = new IntIntHashMap(listIdTuples.size());
			
			for(int i=0; i<listIdTuples.size(); i++) {
				var idtuple = listIdTuples.get(i);
				mapIntToLine.put(idtuple.getProfileIndex(), i);
			}
			
			return new RemoteFilteredData(listIdTuples, idTupleType);
		} catch (IOException e) {
			throw new IllegalAccessError(e.getMessage());
		}
	}
	
	
	private void createScopeMap(BaseExperiment experiment) {

		var rootCCT = experiment.getRootScope(RootScopeType.CallingContextTree);

		// If we already computed the call-path map, we do not do it again.
		// It's harmless to recompute but it such a waste of CPU resources.

		if (rootCCT != null && experiment.getScopeMap() == null) {
			// needs to gather info about cct id and its depth
			// this is needed for traces
			TraceScopeVisitor visitor = new TraceScopeVisitor();
			rootCCT.dfsVisitScopeTree(visitor);

			experiment.setMaxDepth(visitor.getMaxDepth());
			experiment.setScopeMap(visitor.getCallPath());
		}
	}
	
	
	public int getTraceLineFromProfile(int profileIndex) {
		return mapIntToLine.get(profileIndex);
	}
	
	
	public IdTuple getIdTupleFromProfile(int profileIndex) {
		var line = mapIntToLine.get(profileIndex);
		return getProfileIndexToPaint(line);
	}
	
	@Override
	public String getName() {
		return "Remote: " + super.getExperiment().getName();
	}

	
	@Override
	public void closeDB() {
		if (mapIntToLine != null)
			mapIntToLine.clear();
		
		mapIntToLine = null;
		samplingSet = null;
	}

	
	@Override
	public void startTrace(int numTraces, boolean changedBounds) {
		this.changedBounds = changedBounds;
		
		if (!changedBounds) {
			currentLine = new AtomicInteger(numTraces);
			return;
		}

		var traceAttr = getTraceDisplayAttribute();
		var pixelsV = traceAttr.getPixelVertical();

		var remoteTraceData = getBaseData();
		var listIdTuples = remoteTraceData.getListOfIdTuples(IdTupleOption.BRIEF);
		
		Set<TraceId> setOfTraceId = null; 
		
		var numRanks = listIdTuples.size();
		if (numRanks > pixelsV) {
			// in case the number of ranks is bigger than the number of pixels,
			// we need to pick (or sample) which ranks need to be displayed.
			// A lazy way is to rely on the server to pick which ranks to be displayed. 
			setOfTraceId = HashSet.empty();
			float fraction = (float) numRanks / pixelsV;
			int line = 0;
			
			// collect id tuples to fit number of vertical pixels
			for(int i=0; i<pixelsV; i++) {
				var idt = listIdTuples.get(line);
				TraceId traceId = TraceId.make(idt.getProfileIndex());
				setOfTraceId.add(traceId);
				
				line += fraction;				
			}
			
		} else {
			// fast approach
			var setTraces = listIdTuples.stream().map(idt -> TraceId.make(idt.getProfileIndex())).collect(Collectors.toSet());
			setOfTraceId = HashSet.ofAll(setTraces);
		}
		
		var frame = traceAttr.getFrame();
		
		Timestamp time1 = Timestamp.ofEpochNano(frame.begTime);
		Timestamp time2 = Timestamp.ofEpochNano(frame.endTime);
		
		if (time1.isAfter(time2)) {
			// the time range is not initialized yet
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
		System.out.printf("num traces: %d, time: %d, %d %n", setOfTraceId.size(), time1.toEpochNano(), time2.toEpochNano());
		samplingSet = client.sampleTracesAsync(setOfTraceId, time1, time2, getPixelHorizontal());
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
		if (!changedBounds) {
			var line = currentLine.decrementAndGet();
			if (line < 0)
				return null;
			return getProcessTimelineService().getProcessTimeline(line);
		}
		
		if (samplingSet.isDone())
			return null;
		
		Optional<Future<TraceSampling>> sampling = samplingSet.getAnyDoneSampling(Duration.ofMillis(3));
		System.out.println("getNextTrace: " + sampling.isPresent());
		if (sampling.isPresent()) {
			return new RemoteProcessTimeline(this, sampling.get());
		}
		return null;
	}
}
