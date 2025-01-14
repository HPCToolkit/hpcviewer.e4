// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctest.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;


public class ViewerTestCase 
{
	/** the duration in millisec to show chart */
	private static final long WINDOW_DURATION = 100;

	protected Shell shell;
	

	@Before
	public void setUp() {
		shell = createShell("Viewer unit test");
	}

	@After
	public void tearDown() {
		if (shell != null)
			shell.dispose();
	}
	
	
	/***
	 * Show the window for a short duration
	 */
	protected void showWindow() {
		showWindow(WINDOW_DURATION);
	}
	
	
	/***
	 * Show the window for a specified duration
	 * @param duration window wait time in milisecond
	 */
	protected void showWindow(long duration) {
		long time = System.currentTimeMillis();
		while(!shell.isDisposed() && System.currentTimeMillis() - time < duration) {
			Display.getDefault().readAndDispatch();
		}
	}
	
	
	private static Shell createShell(String title) {
		Display display = Display.getDefault();
		// sufficient window size to show chart with fixed size
		Point windowSize = new Point(500, 450);
		Shell shell = new Shell(display);
		shell.setSize(windowSize);
		shell.setLocation(0, 0);
		shell.setText(title);
		shell.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
		
		return shell;
	}
}
