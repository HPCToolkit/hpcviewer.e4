package edu.rice.cs.hpcdata.tld.v4;


import java.io.IOException;
import java.util.List;

import edu.rice.cs.hpcdata.db.IdTuple;
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
		
		List<IdTuple> list = dataSummary.getIdTuple();
		
		final int num_ranks 	= Math.max(1, list.size());
		final double []metrics	= new double[num_ranks];
		
		// if there is no plot data in the database, we return an array of zeros
		// (assuming Java will initialize metrics[] with zeros)
		if (entry != null)
		{	
			for(DataPlotEntry e : entry)
			{
				int profile = list.get(e.tid).getProfileIndex();
				for (int i=0; i<list.size(); i++) {
					var idt = list.get(i);
					if (e.tid == idt.getProfileIndex()-1) {
						profile = i;
						break;
					}
				}
				
				// minus 1 because the index is based on profile number.
				// unfortunately, the profile number starts with number 1 instead of 0
				// the profile 0 is reserved for summary profile. sigh
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
}
