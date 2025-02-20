// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcgraph.internal;

public class IdentityGraphTranlator implements IGraphTranslator {

	@Override
	public int getIndexTranslator(int xIndex) {
		return xIndex;
	}

}
