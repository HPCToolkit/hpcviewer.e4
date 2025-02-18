// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcgraph.histogram;

import java.util.Arrays;


/************************************************************
 * Class to compute histogram data: bins, frequency, ...
 * @author laksonoadhianto
 *
 ************************************************************/
public class Histogram {
	
	// data for histogram graph
	private double freq[];
	private double axis_x[];
	
	// histogram property
	private double data_min, data_max, data_width;
	
	
	/***
	 * create histogram data
	 * 
	 * @param n_bins: number of bins
	 * @param data: array of data
	 */
	public Histogram(int n_bins, double data[]) {

		double sorted_data[] = data.clone();
		Arrays.sort(sorted_data);
		data_min = sorted_data[0];
		data_max = sorted_data[data.length-1];
		
		data_width = (data_max - data_min) / n_bins;
		
		freq = new double[n_bins];
		axis_x = new double[n_bins];
		
		//---------------------------------
		// initialize frequency and axis
		//---------------------------------
		for (int i=0; i<n_bins; i++) {
			freq[i] = 0.0;
			axis_x[i] = data_min + (i * data_width);
		}
		
		//---------------------------------
		// compute the frequency
		//---------------------------------
		for (int i=0; i<data.length; i++) {
			int pos = (int) ( (data[i]-data_min) / data_width);
			if (pos >= n_bins)
				pos = n_bins - 1;
			freq[pos]++;
		}
		
	}
	
	
	/***
	 * get the x-axis labels
	 * @return
	 */
	public double[] getAxisX() {
		return this.axis_x;
	}
	
	
	/****
	 * get the frequency values
	 * @return
	 */
	public double[] getAxisY() {
		return this.freq;
	}
	
	
	/****
	 * get the minimum data value
	 * @return
	 */
	public double min() {
		return data_min;
	}
	
	
	/****
	 * get the maximum data value
	 * @return
	 */
	public double max() {
		return data_max;
	}
	
	public double getWidth() {
		return data_width;
	}
}