package com.graphbuilder.math.func;

/**
The square root function.

@see java.lang.Math#sqrt(double)
*/
public class SqrtFunction implements Function {

	public SqrtFunction() {}

	/**
	Returns the square root of the value at index location 0.
	*/
	public double of(double[] d, int numParam) {
		double root = d[0];
		//----------------------------------------------------------
		// hack: if the root is negative, we need to return 0
		//----------------------------------------------------------
		if (root< 0.0) {
			//System.err.println("neg sqrt " + root);
			return 0.0;
		}
		return java.lang.Math.sqrt(root);
	}

	/**
	Returns true only for 1 parameter, false otherwise.
	*/
	public boolean acceptNumParam(int numParam) {
		return numParam == 1;
	}

	public String toString() {
		return "sqrt(x)";
	}
}