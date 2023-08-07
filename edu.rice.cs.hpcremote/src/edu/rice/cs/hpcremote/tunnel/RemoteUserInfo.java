package edu.rice.cs.hpcremote.tunnel;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

import edu.rice.cs.hpcremote.ui.PasswordDialog;

/********************************************
 * 
 * Remote user information to store and ask for password
 * This class is needed for SSH tunneling
 *
 ********************************************/
public class RemoteUserInfo 
implements UserInfo, UIKeyboardInteractive
{
	private final Shell shell;

	private String password;
	private String user;
	private String hostname;
	private int port;
	
	public RemoteUserInfo(Shell shell)
	{			
		this.shell = shell;
	}
	
	
	public void setInfo(String user, String hostname, int port)
	{
		this.user 	  = user;
		this.hostname = hostname;
		this.port 	  = port;
	}
	
	// --------------------------------------------------------------------------------------
	// override methods
	// --------------------------------------------------------------------------------------
	
	@Override
	public boolean promptPassword(String message) {
		PasswordDialog dialog = new PasswordDialog(shell, "Input password for " + hostname + ":" + port,
				"password for user " + user, null, null);
		
		boolean ret =  dialog.open() == Window.OK;
		
		if (ret)
			password = dialog.getValue();
		
		return ret;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public boolean promptPassphrase(String message) {
		return promptPassword(message);
	}

	@Override
	public String getPassphrase() {
		return getPassword();
	}

	@Override
	public boolean promptYesNo(String message) {
		return true;
	}

	@Override
	public void showMessage(String message) {
		MessageDialog.openInformation(shell, "Information", message);
	}


	@Override
	public String[] promptKeyboardInteractive(String destination, String name,
			String instruction, String[] prompt, boolean[] echo) 
	{
		KeyboardInteractiveDialog dlg = new KeyboardInteractiveDialog(shell, destination, name, instruction, prompt);
		if (dlg.open() == Window.OK)
		{
			return dlg.inputs;
		}
		return new String[0];
	}			

	
	/********************************************************************
	 * Unit test
	 * 
	 * @param argv
	 ********************************************************************/
	public static void main(String []argv) {
		Shell shell = new Shell(Display.getDefault());
		RemoteUserInfo rui = new RemoteUserInfo(shell);
		rui.setInfo("user", "hostname", 0);
		rui.promptPassword("message: ");
		rui.promptPassphrase("passphrase: ");
		rui.promptKeyboardInteractive("destination", "name", "instruction", new String[] {"user", "password"}, new boolean[] {true, false});
	}
	
	/********************************************************************
	 * 
	 * dialog class for communicating with keyboard interactive style SSH
	 * 
	 * Some SSH servers in national labs require SSH client to support 
	 * keyboard interactive SSH, it doesn't work with traditional SSH client.
	 *
	 ********************************************************************/
	private static class KeyboardInteractiveDialog extends TitleAreaDialog
	{
		private final String destination;
		private final String name;
		private final String instruction;
		private final String []prompts;
		private Text []answers;
		
		/*** output variable based on user's input. 
		 *   The caller needs to retrieve this variable once the user clicks OK button
		 *   **/
		String []inputs;
		
		public KeyboardInteractiveDialog(Shell parentShell, String destination, String name,
				String instruction, String[] prompt) {
			super(parentShell);
			this.destination = destination;
			this.name 		 = name;
			this.instruction = instruction;
			this.prompts 	 = prompt;
		}
		
		@Override
		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.Dialog#create()
		 */
		public void create()
		{
			super.create();
			setTitle(destination + ":" + name);
			setMessage(instruction);
		}
		
		@Override
		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
		 */
		protected Control createDialogArea(Composite parent)
		{
			Composite area = (Composite) super.createDialogArea(parent);
			Composite container = new Composite(area, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(container);
			GridLayoutFactory.fillDefaults().numColumns(1).applyTo(container);
			
			answers = new Text[prompts.length];
			
			for(int i=0; i<prompts.length; i++) 
			{
				Label lbl = new Label(container, SWT.WRAP | SWT.LEFT);
				lbl.setText(prompts[i]);
				
				answers[i] = new Text(container, SWT.PASSWORD);
				answers[i].setText("");
				GridDataFactory.fillDefaults().grab(true, false).applyTo(answers[i]);
			}
			return area;
		}
		
		@Override
		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
		 */
		protected void okPressed()
		{
			inputs = new String[answers.length];
			
			for(int i=0; i<answers.length; i++)
			{
				inputs[i] = answers[i].getText();
			}
			super.okPressed();
		}
	}
}
