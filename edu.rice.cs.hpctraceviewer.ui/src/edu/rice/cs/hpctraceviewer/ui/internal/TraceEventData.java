// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.internal;

import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;

public class TraceEventData 
{
	public final SpaceTimeDataController data;
	public final Object source;
	public final Object value;
	
	public TraceEventData(SpaceTimeDataController data, Object source, Object value) {
		this.data   = data;
		this.source = source;
		this.value  = value;
	}

}
