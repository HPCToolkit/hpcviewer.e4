package edu.rice.cs.hpcremote.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.hpctoolkit.client_server_common.time.Timestamp;
import org.hpctoolkit.hpcclient.v1_0.FutureTraceSamplingSet;
import org.hpctoolkit.hpcclient.v1_0.HpcClient;

import edu.rice.cs.hpcbase.ITraceDataCollector;
import edu.rice.cs.hpcbase.ITraceDataCollector.TraceOption;
import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.experiment.extdata.IFilteredData;
import edu.rice.cs.hpctraceviewer.config.TracePreferenceManager;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;

public class RemoteSpaceTimeDataController extends SpaceTimeDataController 
{
	private final HpcClient client;
	private FutureTraceSamplingSet samplingSet;

	public RemoteSpaceTimeDataController(HpcClient client, IExperiment experiment) {
		super(experiment);
		
		this.client = client;
	}

	@Override
	public String getName() {
		return "Remote: " + super.getExperiment().getName();
	}

	@Override
	public void closeDB() {
	}

	
	@Override
	public IFilteredData getTraceData() {
		try {
			var listIdTuples = exp.getThreadData().getIdTuples();
			var idTupleType  = exp.getIdTupleType();
			
			return new RemoteFilteredData(listIdTuples, idTupleType);
		} catch (IOException e) {
			throw new IllegalAccessError(e.getMessage());
		}
	}

	@Override
	public void fillTracesWithData(boolean changedBounds, int numThreadsToLaunch) throws IOException {
		if (!changedBounds)
			return;
		
		var traceAttr = getTraceDisplayAttribute();
		var frame = traceAttr.getFrame();
		var time1 = Timestamp.ofEpochNano(frame.begTime);
		var time2 = Timestamp.ofEpochNano(frame.endTime);
		
		samplingSet = client.sampleTracesAsync(traceAttr.getProcessInterval(), time1, time2, getPixelHorizontal());
	}


	@Override
	public ITraceDataCollector getTraceDataCollector(int lineNum, IdTuple idTuple) {
		TraceOption traceOption = TraceOption.ORIGINAL_TRACE;
		var idtupleType = getExperiment().getIdTupleType();

		if (TracePreferenceManager.getGPUTraceExposure() && idTuple.isGPU(idtupleType))
			traceOption = TraceOption.REVEAL_GPU_TRACE;

		//samplingSet.getAnyDoneSampling
		
		var dataCollector = new RemoteTraceDataCollector(client, idTuple, getPixelHorizontal(), traceOption);
		
		return dataCollector;
	}
	
	
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
		public void setListOfIdTuples(List<IdTuple> listIdTuples) {
			throw new IllegalAccessError();
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
		public int getNumLevels() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void dispose() {
			indexes = null;
			
			if (listIdTuples != null)
				listIdTuples.clear();
			listIdTuples = null;
		}

		@Override
		public long getLong(long position) throws IOException {
			throw new IllegalAccessError();
		}

		@Override
		public int getInt(long position) throws IOException {
			throw new IllegalAccessError();
		}

		@Override
		public int getRecordSize() {
			throw new IllegalAccessError();
		}

		@Override
		public long getMinLoc(int rank) {
			throw new IllegalAccessError();
		}

		@Override
		public long getMaxLoc(int rank) {
			throw new IllegalAccessError();
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
