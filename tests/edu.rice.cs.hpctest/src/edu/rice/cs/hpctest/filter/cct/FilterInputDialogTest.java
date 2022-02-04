package edu.rice.cs.hpctest.filter.cct;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcfilter.cct.FilterInputDialog;

public class FilterInputDialogTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		final Display display = new Display();
		final Shell   shell   = new Shell(display);
		shell.setText("Test dialog filter");
		
		FilterInputDialog dialog = new FilterInputDialog(shell, "Toto", "initial", null);
		if (dialog.open() == Dialog.OK){
			System.out.println("pattern: " + dialog.getValue());
			System.out.println("att: " + dialog.getAttribute());
		}
	}
}
