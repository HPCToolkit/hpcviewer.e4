// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctraceviewer.ui.base;

public interface ITraceViewAction {

	void home();

	void timeZoomIn();
	void timeZoomOut();
	
	void processZoomIn();
	void processZoomOut();

	void saveConfiguration();
	void openConfiguration();
	
	void goUp();
	void goDown();
	void goRight();
	void goLeft();
	
	boolean canProcessZoomIn();
	boolean canProcessZoomOut();

	boolean canTimeZoomIn();
	boolean canTimeZoomOut();
	
    boolean canGoRight();    
    boolean canGoLeft();
    boolean canGoUp();
    boolean canGoDown();
}
