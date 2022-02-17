package edu.rice.cs.hpctest.viewer.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class ViewerTestCase 
{
	/** the duration in millisec to show chart */
	private static final long durationToShowWindow = 100;

	protected Shell shell;
	

	@BeforeEach
	public void setUp() throws Exception {
		System.out.println("Create shell");
		shell = createShell("Viewer unit test");
	}

	@AfterEach
	public void tearDown() throws Exception {
		shell.dispose();
		System.out.println("Dispose shell");
	}
	
	protected void showWindow() {
		long time = System.currentTimeMillis();
		while(!shell.isDisposed() && System.currentTimeMillis() - time < durationToShowWindow) {
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
