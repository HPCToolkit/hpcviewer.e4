package edu.rice.cs.hpctest.viewer.util;

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
		shell.dispose();
	}
	
	protected void showWindow() {
		long time = System.currentTimeMillis();
		while(!shell.isDisposed() && System.currentTimeMillis() - time < WINDOW_DURATION) {
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
