// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctraceviewer.ui.debug;

import edu.rice.cs.hpcbase.IProcessTimeline;


public interface IProcessTimelineSource 
{
	IProcessTimeline getCurrentProcessTimeline();
}
