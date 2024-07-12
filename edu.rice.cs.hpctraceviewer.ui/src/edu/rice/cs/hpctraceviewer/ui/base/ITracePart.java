// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctraceviewer.ui.base;

import edu.rice.cs.hpcbase.ui.IMainPart;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;

public interface ITracePart extends IMainPart, ITraceContext 
{
	void activateStatisticItem();
	
	SpaceTimeDataController getDataController();
}
