package edu.rice.cs.hpcdata.experiment.extdata;

import java.io.IOException;


public abstract class AbstractThreadDataCollection implements
		IThreadDataCollection {

	public double[] getEvenlySparseRankLabels() throws IOException {
		double []values = getRankLabels();
		int parallelism = getParallelismLevel();
		
		// we only spread the values if the parallelism is more than 1 
		
		if (parallelism>1) {
			int i = 0;
			while(i<values.length) {
				int numSiblings = 0;
				int rankFirst 	= (int) Math.floor(values[i]);
				
				int j = i+1;
				for(; j<values.length; j++) {
					int nextRank =  (int) Math.floor(values[j]);
					numSiblings++;
										
					if (nextRank > rankFirst) {
						break;
					} else if (j==values.length-1) {
						numSiblings++;
					}
				}
				for (int k=0; k<numSiblings; k++) {
					values[i+k] = rankFirst + ((double)k/numSiblings);
				}
				i = j;
			}
		}
		return values;
	}

}
