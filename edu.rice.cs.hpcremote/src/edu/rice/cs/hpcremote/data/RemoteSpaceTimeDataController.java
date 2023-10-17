package edu.rice.cs.hpcremote.data;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
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
	
	private IntObjectHashMap<IdTuple> mapIntToIdTuple;
	private IntIntHashMap mapIntToLine;
	
	
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

			mapIntToIdTuple = new IntObjectHashMap<>(listIdTuples.size());
			mapIntToLine = new IntIntHashMap(listIdTuples.size());
			
			for(int i=0; i<listIdTuples.size(); i++) {
				var idtuple = listIdTuples.get(i);
				mapIntToIdTuple.put(idtuple.getProfileIndex(), idtuple);
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
		return mapIntToIdTuple.get(profileIndex);
	}
	
	@Override
	public String getName() {
		return "Remote: " + super.getExperiment().getName();
	}

	
	@Override
	public void closeDB() {
		if (mapIntToIdTuple != null)
			mapIntToIdTuple.clear();
		
		if (mapIntToLine != null)
			mapIntToLine.clear();
		
		mapIntToIdTuple = null;
		mapIntToLine = null;
		samplingSet = null;
	}

	
	@Override
	public void fillTracesWithData(boolean changedBounds, int numThreadsToLaunch) throws IOException {
		if (!changedBounds)
			return;
		
		var traceAttr = getTraceDisplayAttribute();
		var pixelsV = traceAttr.getPixelVertical();

		var listIdTuples = getBaseData().getListOfIdTuples(IdTupleOption.BRIEF);
		Set<TraceId> setOfTraceId = null; 
		
		var numRanks = listIdTuples.size();
		if (numRanks > pixelsV) {
			// in case the number of ranks is bigger than the number of pixels,
			// we need to pick (or sample) which ranks need to be displayed.
			// A lazy way is to rely on the server to pick which ranks to be displayed. 
			
		} else {
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
			}
		}
		
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
		if (samplingSet.isDone())
			return null;
		
		Optional<Future<TraceSampling>> sampling = samplingSet.getAnyDoneSampling();
		if (sampling.isPresent()) {
			return new RemoteProcessTimeline(this, sampling.get());
		}
		return null;
	}
}
