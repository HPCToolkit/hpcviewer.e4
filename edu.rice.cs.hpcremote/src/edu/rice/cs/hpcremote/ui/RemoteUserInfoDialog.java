// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcremote.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

public class RemoteUserInfoDialog implements UserInfo, UIKeyboardInteractive 
{
	private static final String TITLE = "Remote connection";
	private static final String PASS_KEY = "remote.pass.key";
	
	private final Shell shell;
	
	private String password;
	private String passphrase;
	
	public RemoteUserInfoDialog(Shell parentShell) {
		this.shell = parentShell;
		var pass = parentShell.getData(PASS_KEY);
		if (pass != null)
			passphrase = (String) pass;
	}

	@Override
	public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt,
			boolean[] echo) {

		var dialog = new PromptDialog(shell, destination, name, instruction, prompt, echo);
		if (dialog.open() == Window.OK)
			return dialog.getPrompt();
		
		return new String[0];
	}

	@Override
	public String getPassphrase() {
		return passphrase;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public boolean promptPassword(String message) {
		var inputDialog = new InputDialog(shell, TITLE, message, "", null) {
			
			@Override
			protected int getInputTextStyle() {
				return super.getInputTextStyle() | SWT.PASSWORD;
			}
		};		
		if (inputDialog.open() == Window.OK) {
			password = inputDialog.getValue();
			return true;
		}
		return false;
	}

	@Override
	public boolean promptPassphrase(String message) {
		if (passphrase != null)
			return true;
		
		var inputDialog = new InputDialog(shell, TITLE, message, "", null) {
			
			@Override
			protected int getInputTextStyle() {
				return super.getInputTextStyle() | SWT.PASSWORD;
			}
		};
		var result = (inputDialog.open() == Window.OK);
		passphrase = inputDialog.getValue();
		
		shell.setData(PASS_KEY, passphrase);

		return result;
	}

	@Override
	public boolean promptYesNo(String message) {
		return MessageDialog.openQuestion(shell, TITLE, message);
	}

	@Override
	public void showMessage(String message) {
		MessageDialog.openInformation(shell, TITLE, message);
	}

	
	private static class PromptDialog extends Dialog
	{
		private final String destination;
		private final String name; 
		private final String instruction; 
		private final String[] prompt;
		private final boolean[] echo;
		
		private Text []textPrompt;
		private String []strPrompt;
		
		protected PromptDialog(
				Shell parentShell,
				String destination, 
				String name, 
				String instruction, 
				String[] prompt,
				boolean[] echo) {
			
			super(parentShell);
			this.destination = destination;
			this.name = name;
			this.instruction = instruction;
			this.prompt = prompt;
			this.echo = echo;
		}
		
		@Override
		protected Control createDialogArea(Composite parent) {
			Composite container = (Composite) super.createDialogArea(parent);			
			GridLayoutFactory.fillDefaults().numColumns(1).applyTo(container);

			Composite header = new Composite(container, SWT.NONE);
			GridLayoutFactory.fillDefaults().numColumns(1).applyTo(header);
			GridDataFactory.fillDefaults().grab(true, false);
			
			Label lblInstruction = new Label(header, SWT.WRAP);
			lblInstruction.setText(instruction);
			
			Label lblName = new Label(header, SWT.NONE);
			lblName.setText(name);
			
			Label lblDestination = new Label(header, SWT.WRAP);
			lblDestination.setText(destination);
			
			Composite promptArea = new Composite(container, SWT.NONE);
			GridLayoutFactory.fillDefaults().numColumns(1).applyTo(promptArea);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(promptArea);
			
			textPrompt = new Text[prompt.length];
			
			for(int i=0; i<prompt.length; i++) {
				Label lbl = new Label(promptArea, SWT.LEFT);
				lbl.setText(prompt[i]);
				
				int style = echo[i] ? SWT.NONE : SWT.PASSWORD;
				textPrompt[i] = new Text(promptArea, style);
				textPrompt[i].setText("");
				GridDataFactory.fillDefaults().grab(true, false).applyTo(textPrompt[i]);
			}
			
			return container;
		}
		
		
		@Override
		protected void okPressed() {
			strPrompt = new String[textPrompt.length];
			
			for(int i=0; i<textPrompt.length; i++) {
				strPrompt[i] = textPrompt[i].getText();
			}
			super.okPressed();
		}
		
		
		public String[] getPrompt() {
			return strPrompt;
		}
	}
}
