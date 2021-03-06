package com.graphbuilder.math;

/**
A node of an expression tree, represented by the symbol "/".
*/
public class DivNode extends OpNode {

	public DivNode(Expression leftChild, Expression rightChild) {
		super(leftChild, rightChild);
	}

	/**
	Divides the evaluation of the left side by the evaluation of the right side and returns the result.
	*/
	public double eval(VarMap v, FuncMap f) {
		double a = leftChild.eval(v, f);
		double b = rightChild.eval(v, f);
		// Laks 2008.05.16: check if the b is zero or not (to avoid an exception)
		if(b!=0)
			return a / b;
		else
			return 0;
	}

	public String getSymbol() {
		return "/";
	}
	
	public Expression duplicate() {
		DivNode n = new DivNode(leftChild.duplicate(), rightChild.duplicate());
		return n;
	}
}
