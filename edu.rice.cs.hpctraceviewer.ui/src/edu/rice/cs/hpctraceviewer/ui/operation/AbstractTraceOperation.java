// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.operation;

import org.eclipse.core.commands.operations.AbstractOperation;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;

public abstract class AbstractTraceOperation extends AbstractOperation 
{
	private final SpaceTimeDataController data;

	public AbstractTraceOperation(SpaceTimeDataController data, String label) {
		super(label);
		this.data = data;
	}

	public SpaceTimeDataController getData() {
		return data;
	}
}
