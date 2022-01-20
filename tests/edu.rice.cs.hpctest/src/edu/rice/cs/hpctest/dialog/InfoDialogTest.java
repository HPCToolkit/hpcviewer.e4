package edu.rice.cs.hpctest.dialog;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcviewer.ui.dialogs.InfoDialog;
import edu.rice.cs.hpcviewer.ui.dialogs.InfoLogDialog;

public class InfoDialogTest {
	
	
	/***
	 * Unit test
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		
		InfoDialog dlg = new InfoDialog(shell);
		dlg.open();

		InfoLogDialog dlglog = new InfoLogDialog(new Shell(display));
		dlglog.open();
	}
}
