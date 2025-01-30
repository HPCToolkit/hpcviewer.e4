// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcbase.ui;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.widgets.Shell;

public class TimelineProgressMonitor {

	private AtomicInteger progress;
	private IStatusLineManager statusMgr;
	private IProgressMonitor monitor;
	

	public TimelineProgressMonitor(IStatusLineManager _statusMgr)
	{
		statusMgr = _statusMgr;
		monitor = statusMgr.getProgressMonitor();
		progress = new AtomicInteger();
	}
	
	public void beginProgress(int totalWork, String sMessage, String sTask, Shell shell)
	{
		progress.set(0);
		statusMgr.setMessage(sMessage);
		
		// quick fix to force UI to show the message.
		// we need a smarter way to do this. If the work is small, no need to refresh UI
		//shell.update();
		
		monitor.beginTask(sTask, totalWork);
	}
	
	public void announceProgress()
	{
		progress.getAndIncrement();
	}
	
	public void reportProgress()
	{
		int workDone = progress.getAndSet(0);
		if (workDone > 0)
			monitor.worked(workDone);
	}
	
	public void endProgress()
	{
		monitor.done();
		statusMgr.setMessage(null);
		// shell.update();
	}

}
