package com.graphbuilder.math;

/**
 * check the value of comparison sign = or ==
 * 
 */
public class EqualNode extends OpNode {

	//private final boolean debug = true;
	
	public EqualNode(Expression leftChild, Expression rightChild) {
		super(leftChild, rightChild);
	}

	//@Override
	public String getSymbol() {
		return "==";
	}

	//@Override
	public double eval(VarMap v, FuncMap f) {
		double a = leftChild.eval(v, f);
		double b = rightChild.eval(v, f);
		if (Double.compare(a, b) == 0)
			return 0.0;
		else
			return 1.0;
	}

}
