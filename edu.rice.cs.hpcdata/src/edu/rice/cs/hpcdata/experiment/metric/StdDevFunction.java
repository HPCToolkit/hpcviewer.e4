package edu.rice.cs.hpcdata.experiment.metric;

import com.graphbuilder.math.func.Function;

/**
The average function.
*/
public class StdDevFunction implements Function {

	public StdDevFunction() {}

	/**
	Returns the standard deviation of the values in the array from [0, numParam).
	*/
	public double of(double[] d, int numParam) {
		double sum = 0, sqrsum = 0;
		double mean, stdev;

		for (int i = 0; i < numParam; i++) {
			sum += d[i];
			sqrsum += (d[i] * d[i]);
		}

		mean = sum / numParam;
		stdev = (sqrsum) / numParam - (mean * mean);
		return java.lang.Math.sqrt(stdev);
	}

	/**
	Returns true for 1 or more parameters, false otherwise.
	*/
	public boolean acceptNumParam(int numParam) {
		return numParam > 0;
	}

	public String toString() {
		return "stdev(x1, x2, ..., xn)";
	}
}