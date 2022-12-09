package edu.rice.cs.hpcdata.tld.v4;


import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.collections.impl.map.mutable.primitive.LongLongHashMap;

import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;
import edu.rice.cs.hpcdata.db.version4.DataPlot;
import edu.rice.cs.hpcdata.db.version4.DataPlotEntry;
import edu.rice.cs.hpcdata.db.version4.DataSummary;
import edu.rice.cs.hpcdata.experiment.extdata.AbstractThreadDataCollection;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;

/*******************************************************************
 * 
 * Class to manage a collection of metric plot data for database v.4
 *
 *******************************************************************/
public class ThreadDataCollection4 extends AbstractThreadDataCollection
{
	private DataPlot    dataPlot;
	private DataSummary dataSummary;

	public void init(DataSummary dataSummary) {
		this.dataSummary = dataSummary;
	}
	
	@Override
	public void open(RootScope root, String directory) throws IOException {
		dataPlot = new DataPlot();
		dataPlot.open(directory);
	}

	@Override
	public boolean isAvailable() {
		return (dataPlot != null);
	}

	@Override
	public double[] getRankLabels() {		
		return dataSummary.getDoubleLableIdTuples();
	}


	@Override
	public String[] getRankStringLabels() throws IOException {
		var listIdTuples = dataSummary.getIdTuple(IdTupleOption.BRIEF);
		if (listIdTuples == null || listIdTuples.isEmpty())
			return new String[0];
		
		String []labels  = new String[listIdTuples.size()];
		for(int i=0; i<listIdTuples.size(); i++) {
			labels[i] = listIdTuples.get(i).toString(dataSummary.getIdTupleType());
		}
		return labels;
	}

	@Override
	public int getParallelismLevel() {
		return dataSummary.getParallelismLevels();
	}

	@Override
	public String getRankTitle() {
		var idtuples = dataSummary.getIdTuple();
		var isRank = idtuples.stream().anyMatch(idt -> idt.getIndex(IdTupleType.KIND_RANK) >= 0);
		var isThread = idtuples.stream().anyMatch(idt -> idt.getIndex(IdTupleType.KIND_THREAD) >= 0);
		
		if (isRank && isThread) 
			return "Rank.Thread";
		else if (isRank)
			return "Rank";
		else if (isThread)
			return "Thread";
		
		return "Context";
	}

	@Override
	public double getMetric(long nodeIndex, int metricIndex, IdTuple idtuple, int numMetrics) throws IOException {

		if (dataSummary == null)
			return 0.0d;

		return dataSummary.getMetric(idtuple, (int) nodeIndex, metricIndex);
	}

	@Override
	public double[] getMetrics(long nodeIndex, int metricIndex, int numMetrics)
			throws IOException 
			{
		final DataPlotEntry []entry = dataPlot.getPlotEntry((int) nodeIndex, metricIndex);
		
		var listWithoutGPUs = getIdTupleListWithoutGPU(dataSummary.getIdTupleType());
		
		final int num_ranks 	= Math.max(1, listWithoutGPUs.size()); // shouldn't include gpus
		final double []metrics	= new double[num_ranks];
		
		// if there is no plot data in the database, we return an array of zeros
		// (assuming Java will initialize metrics[] with zeros)
		if (entry != null)
		{	
			for(DataPlotEntry e : entry)
			{
				int profile = -1;
				for (int i=0; i<listWithoutGPUs.size(); i++) {
					var idt = listWithoutGPUs.get(i);
					if (e.tid == idt.getProfileIndex()) {
						profile = i;
						break;
					}
				}
				assert(profile >= 0);
				metrics[profile] = e.metval;
			}
		}
		return metrics;
	}

	@Override
	public void dispose() {
		try {
			dataPlot.dispose();
		} catch (IOException e) {
			// unused
		}
	}

	@Override
	public double[] getScopeMetrics(int threadId, int metricIndex,
			int numMetrics) throws IOException {

		// not implemented yet
		return new double[0];
	}

	@Override
	public List<IdTuple> getIdTuples() {
		return dataSummary.getIdTuple();
	}
	
	@Override
	public double[] getEvenlySparseRankLabels() throws IOException {
		var listIdTuple = dataSummary.getIdTuple();		
		
		// collect list of non-gpu id tuples
		var list = listIdTuple.stream()
							  .filter(idt -> !idt.isGPU(dataSummary.getIdTupleType()))
							  .collect(Collectors.toList());

		var hasProcess = listIdTuple.stream()
									.anyMatch(idt -> idt.getIndex(IdTupleType.KIND_RANK) >= 0);
		var hasThread  = listIdTuple.stream()
									.anyMatch(idt -> idt.getIndex(IdTupleType.KIND_THREAD) >= 0);
		var hasHybrid  = hasProcess && hasThread;
		
		// find the number of threads per rank. 
		// The number of threads will be used to distribute the thread id evenly.
		//
		// fix issue #261: in meta.db, it's possible different rank has different number of threads.
		// 	for instance, rank 0 has 10 threads, but rank 1 has 2 threads.
		var mapRankToNumThreads = new LongLongHashMap();
		var ranks = new double[list.size()];
		int i=0;
		
		for(var idt: list) {
			var proc   = idt.getIndex(IdTupleType.KIND_RANK);
			var thread = idt.getIndex(IdTupleType.KIND_THREAD);
			
			if (hasHybrid) {
				var numThreads = mapRankToNumThreads.getIfAbsent(proc, 0);
				var maxThreads = numThreads + 1;
				mapRankToNumThreads.put(proc, maxThreads);
			} else {
				// case for non hybrid
				if (hasProcess && proc >= 0)
					ranks[i] = proc;
				else if (hasThread && thread >= 0)
					ranks[i] = thread;
			}
			i++;
		}
		// if we only have one parallelism level, no need to complicate things.
		if (!hasHybrid)
			return ranks;
		
		// distribute the threads evenly depending on the number of threads of a specified rank
		// if rank 0 has 5 threads, its labels are: 0.0, 0.2, 0.4, 0.6, 0.8
		// if rank 1 has 2 threads, its labels are: 0.0, 0.5
		i=0;
		for(var idt: list) {
			var rank = idt.getIndex(IdTupleType.KIND_RANK);
			var numThreads = mapRankToNumThreads.get(idt.getIndex(IdTupleType.KIND_RANK));
			var thread = idt.getIndex(IdTupleType.KIND_THREAD);
			float distance = (float)thread / (float)numThreads;
			ranks[i] = rank + distance;
			i++;
		}
		return ranks;
	}
}
