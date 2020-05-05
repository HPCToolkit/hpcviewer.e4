package edu.rice.cs.hpc.data.experiment.extdata;

import java.io.IOException;


public abstract class AbstractThreadDataCollection implements
		IThreadDataCollection {

	public double[] getEvenlySparseRankLabels() throws IOException {
		double []values = getRankLabels();
		int parallelism = getParallelismLevel();
		
		// we only spread the values if the parallelism is more than 1 
		
		if (parallelism>1) {
			
			for(int i=0; i<values.length; i++) {
				int num_siblings = 0;
				int rank_first 	 = (int) Math.floor(values[i]);
				
				int j = i+1;
				for(; j<values.length; j++) {
					int next_rank =  (int) Math.floor(values[j]);
					num_siblings++;
										
					if (next_rank > rank_first) {
						break;
					} else if (j==values.length-1) {
						num_siblings++;
					}
				}
				for (int k=0; k<num_siblings; k++) {
					values[i+k] = (double)rank_first + ((double)k/num_siblings);
				}
				i = j-1;
			}
		}
		return values;
	}

}
