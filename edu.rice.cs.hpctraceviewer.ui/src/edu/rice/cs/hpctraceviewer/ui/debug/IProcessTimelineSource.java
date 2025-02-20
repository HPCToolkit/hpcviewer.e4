// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.debug;

import edu.rice.cs.hpcbase.IProcessTimeline;


public interface IProcessTimelineSource 
{
	IProcessTimeline getCurrentProcessTimeline();
}
