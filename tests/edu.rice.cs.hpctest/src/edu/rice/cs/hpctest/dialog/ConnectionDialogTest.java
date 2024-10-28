package edu.rice.cs.hpctest.dialog;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcremote.ui.*;

public class ConnectionDialogTest {

	

	/***
	 * Unit test
	 * 
	 * @param argv
	 */
	@SuppressWarnings("restriction")
	public static void main(String []argv) {
		var display = Display.getDefault();
		Shell shell = new Shell(display);
		
		var dlg = new ConnectionDialog(shell);
		int ret = dlg.open();
		if (ret == Window.OK) {
			System.out.println("host: " + dlg.getHost());
			System.out.println("user: " + dlg.getUsername());
			System.out.println("dir:  " + dlg.getInstallationDirectory());
		}
	}

}
