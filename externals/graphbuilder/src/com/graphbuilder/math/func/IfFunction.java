package com.graphbuilder.math.func;

public class IfFunction implements Function {

	public double of(double[] param, int numParam) {
		
		final boolean condition = (0 == param[0]);
		double result = 0.0;
		
		if (condition) {
			result = param[1];
		} else if (numParam == 3) {
			result = param[2];
		}
		return result;
	}

	public boolean acceptNumParam(int numParam) {
		return (numParam==2 || numParam==3);
	}

	public String toString() {
		return "if(condition,val_if_true,val_if_false)";
	}

}
