package edu.rice.cs.hpcremote.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.eclipse.collections.api.map.primitive.IntIntMap;
import org.eclipse.collections.api.map.primitive.IntObjectMap;
import org.hpctoolkit.client_server_common.time.Timestamp;
import org.hpctoolkit.client_server_common.trace.TraceId;
import org.hpctoolkit.hpcclient.v1_0.FutureTraceSamplingSet;
import org.hpctoolkit.hpcclient.v1_0.HpcClient;
import org.hpctoolkit.hpcclient.v1_0.TraceSampling;

import edu.rice.cs.hpcbase.IProcessTimeline;
import edu.rice.cs.hpcbase.ITraceDataCollector;
import edu.rice.cs.hpcbase.ITraceDataCollector.TraceOption;
import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.experiment.extdata.IFilteredData;
import edu.rice.cs.hpcdata.util.ICallPath.ICallPathInfo;
import edu.rice.cs.hpctraceviewer.config.TracePreferenceManager;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

public class RemoteSpaceTimeDataController extends SpaceTimeDataController 
{
	private final HpcClient client;
	private final IFilteredData remoteTraceData;
	
	private FutureTraceSamplingSet samplingSet;
	private IntObjectMap<IdTuple> mapIntToIdTuple;
	private IntIntMap mapIntToLine;
	
	
	public RemoteSpaceTimeDataController(HpcClient client, IExperiment experiment) {
		super(experiment);
		
		this.client = client;
		remoteTraceData = createTraceData(experiment);
	}

	private IFilteredData createTraceData(IExperiment experiment) {		
		var idTupleType  = exp.getIdTupleType();

		try {
			var listIdTuples = exp.getThreadData().getIdTuples();
			exp.getThreadData().getParallelismLevel();
			
			return new RemoteFilteredData(listIdTuples, idTupleType);
		} catch (IOException e) {
			throw new IllegalAccessError(e.getMessage());
		}
	}
	
	@Override
	public String getName() {
		return "Remote: " + super.getExperiment().getName();
	}

	@Override
	public void closeDB() {
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
		var time1 = Timestamp.ofEpochNano(frame.begTime);
		var time2 = Timestamp.ofEpochNano(frame.endTime);
		
		samplingSet = client.sampleTracesAsync(setOfTraceId, time1, time2, getPixelHorizontal());
	}


	@Override
	public ITraceDataCollector getTraceDataCollector(int lineNum, IdTuple idTuple) {
		TraceOption traceOption = TraceOption.ORIGINAL_TRACE;
		var idtupleType = getExperiment().getIdTupleType();

		if (TracePreferenceManager.getGPUTraceExposure() && idTuple.isGPU(idtupleType))
			traceOption = TraceOption.REVEAL_GPU_TRACE;
		
		return new RemoteTraceDataCollector(lineNum, idTuple, getPixelHorizontal(), traceOption);
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

	
	static class RemoteProcessTimeline implements IProcessTimeline
	{
		private final SpaceTimeDataController traceData;
		private final Future<TraceSampling> traceSampling;

		private TraceSampling trace;
		private ITraceDataCollector traceDataCollector;
		
		RemoteProcessTimeline(SpaceTimeDataController traceData, Future<TraceSampling> traceSampling) {
			this.traceData = traceData;
			this.traceSampling = traceSampling;
		}
		
		@Override
		public void readInData() throws IOException {
			try {
				trace = traceSampling.get();
				var traceId = trace.getTraceId();
				var profile = traceId.toInt();
				traceDataCollector = traceData.getTraceDataCollector(line(), getProfileIdTuple());
			} catch (InterruptedException  | ExecutionException e) {
			    // Restore interrupted state...
			    Thread.currentThread().interrupt();
			}
		}

		@Override
		public long getTime(int sample) {
			return trace.getSamplesChronologicallyNonDescending().get(sample).getTimestamp().toEpochNano();
		}

		@Override
		public int getContextId(int sample) {
			return trace.getSamplesChronologicallyNonDescending().get(sample).getCallingContext().toInt();
		}

		@Override
		public void shiftTimeBy(long lowestStartingTime) {
			traceDataCollector.shiftTimeBy(lowestStartingTime);
		}

		@Override
		public ICallPathInfo getCallPathInfo(int sample) {
			var cpid = getContextId(sample);
			var map  = traceData.getScopeMap();
			return map.getCallPathInfo(cpid);
		}

		@Override
		public void copyDataFrom(IProcessTimeline other) {
			traceDataCollector.duplicate( ((RemoteProcessTimeline) other).traceDataCollector );
		}

		@Override
		public int size() {
			return trace.getSamplesChronologicallyNonDescending().size();
		}

		@Override
		public int line() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public IdTuple getProfileIdTuple() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int findMidpointBefore(long time, boolean usingMidpoint) {
			try {
				return traceDataCollector.findClosestSample(time, usingMidpoint);
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid time: " + time);
			}
		}

		@Override
		public boolean isEmpty() {
			return trace.getSamplesChronologicallyNonDescending().isEmpty();
		}

		@Override
		public boolean isGPU() {			
			return getProfileIdTuple().isGPU(traceData.getExperiment().getIdTupleType());
		}

		@Override
		public void dispose() {
			if (traceDataCollector != null)
				traceDataCollector.dispose();
			
			traceDataCollector = null;
		}
		
	}

	/***************************************************************
	 * 
	 * Remote base data specifically for remote data
	 *
	 ***************************************************************/
	public static class RemoteFilteredData implements IFilteredData
	{
		final IdTupleType idTupleType;
		final List<IdTuple> listOriginalIdTuples;
		
		List<IdTuple> listIdTuples;
		List<Integer> indexes;

		public RemoteFilteredData(List<IdTuple> listOriginalIdTuples, IdTupleType idTupleType) {
			this.listOriginalIdTuples = listOriginalIdTuples;
			this.idTupleType = idTupleType;
		}
		

		@Override
		public List<IdTuple> getListOfIdTuples(IdTupleOption option) {
			if (indexes == null) {
				// this can happen when we remove the filter and go back to the densed one.
				return listOriginalIdTuples;
			}
			if (listIdTuples != null) {
				return listIdTuples;
			}
			listIdTuples = new ArrayList<>();
			
			for (int i=0; i<indexes.size(); i++) {
				Integer index = indexes.get(i);
				IdTuple idTuple = listOriginalIdTuples.get(index);
				listIdTuples.add(idTuple);
			}
			return listIdTuples;
		}


		@Override
		public IdTupleType getIdTupleTypes() {
			return idTupleType;
		}

		@Override
		public int getNumberOfRanks() {
			return indexes.size();
		}

		@Override
		public int getFirstIncluded() {
			if (indexes == null || indexes.isEmpty())
				return 0;
			
			return indexes.get(0);
		}

		@Override
		public int getLastIncluded() {
			if (indexes == null || indexes.isEmpty())
				return 0;
			
			return indexes.get(indexes.size()-1);
		}

		@Override
		public boolean isDenseBetweenFirstAndLast() {
			if (indexes == null || indexes.isEmpty())
				return true;
			
			int size = indexes.size();
			return indexes.get(size-1)-indexes.get(0) == size-1;
		}

		@Override
		public boolean hasGPU() {
			var list = getListOfIdTuples(IdTupleOption.BRIEF);
			var gpuIdTuple = list.stream().filter( idt -> idt.isGPU(idTupleType)).findAny();
			return gpuIdTuple.isPresent();
		}

		@Override
		public void dispose() {
			indexes = null;
			
			if (listIdTuples != null)
				listIdTuples.clear();
			listIdTuples = null;
		}

		@Override
		public boolean isGPU(int rank) {
			if (indexes == null)
				return false;
			
			var index = indexes.get(rank);
			var list  = getListOfIdTuples(IdTupleOption.BRIEF);
			var idtuple = list.get(index);
			
			return idtuple.isGPU(idTupleType);
		}

		@Override
		public boolean isGoodFilter() {
			return getNumberOfRanks() > 0;
		}

		@Override
		public void setIncludeIndex(List<Integer> listOfIncludedIndex) {
			indexes = listOfIncludedIndex;
			listIdTuples = null;			
		}

		@Override
		public List<IdTuple> getDenseListIdTuple(IdTupleOption option) {
			return listOriginalIdTuples;
		}
	}
}
