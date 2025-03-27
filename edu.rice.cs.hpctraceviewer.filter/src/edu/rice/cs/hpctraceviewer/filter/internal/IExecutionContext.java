// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.filter.internal;

import org.hpctoolkit.db.local.db.IdTuple;

public interface IExecutionContext 
{
	IdTuple getIdTuple();
	
	int getNumSamples();
}
