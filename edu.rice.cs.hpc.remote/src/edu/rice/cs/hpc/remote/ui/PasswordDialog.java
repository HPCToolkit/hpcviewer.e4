package edu.rice.cs.hpc.remote.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/***************************************************************
 * 
 * A simple password prompt dialog
 * @see org.eclipse.jface.dialogs.InputDialog
 *
 ***************************************************************/
public class PasswordDialog extends InputDialog 
{

	public PasswordDialog(Shell parentShell, String dialogTitle,
			String dialogMessage, String initialValue, IInputValidator validator) {

		super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
	}

	
	@Override
    protected int getInputTextStyle() {
        return super.getInputTextStyle() | SWT.PASSWORD;
    }
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Display display = new Display();
		Shell shell = display.getActiveShell();
		
		PasswordDialog dlg = new PasswordDialog(shell, "Password", "Your password", null, null);
		
		if (dlg.open() == Dialog.OK) {
			System.out.println("valud: " + dlg.getValue());
		}
	}

}
