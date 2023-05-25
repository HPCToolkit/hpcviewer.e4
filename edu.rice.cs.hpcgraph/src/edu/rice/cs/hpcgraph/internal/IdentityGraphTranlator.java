package edu.rice.cs.hpcgraph.internal;

public class IdentityGraphTranlator implements IGraphTranslator {

	@Override
	public int getIndexTranslator(int xIndex) {
		return xIndex;
	}

}
