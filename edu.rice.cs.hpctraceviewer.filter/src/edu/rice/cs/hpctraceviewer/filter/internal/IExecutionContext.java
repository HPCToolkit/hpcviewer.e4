// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctraceviewer.filter.internal;

import edu.rice.cs.hpcdata.db.IdTuple;

public interface IExecutionContext 
{
	IdTuple getIdTuple();
	
	int getNumSamples();
}
