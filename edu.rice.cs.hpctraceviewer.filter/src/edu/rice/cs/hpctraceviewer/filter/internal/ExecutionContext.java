// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.filter.internal;

import org.hpctoolkit.db.local.db.IdTuple;

public class ExecutionContext implements IExecutionContext
{
	private final IdTuple idTuple;
	private final int numSamples;
	
	
	public ExecutionContext(IdTuple executionContext, int numSamples) {
		this.idTuple = executionContext;
		this.numSamples = numSamples;
	}


	/**
	 * @return the executionContext
	 */
	public IdTuple getIdTuple() {
		return idTuple;
	}


	/**
	 * @return the numSamples
	 */
	public int getNumSamples() {
		return numSamples;
	}

}
