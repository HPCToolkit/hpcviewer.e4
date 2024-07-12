// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcgraph.internal;

public class IdentityGraphTranlator implements IGraphTranslator {

	@Override
	public int getIndexTranslator(int xIndex) {
		return xIndex;
	}

}
