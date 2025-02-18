// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcviewer.ui.dialogs;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.internal.runtime.Activator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpclog.LogProperty;
import edu.rice.cs.hpcviewer.ui.util.FileUtility;

public class InfoLogDialog extends Dialog 
{
	private Text wText;
	
	public InfoLogDialog(Shell shell) {
		super(shell);
	}

	
	@Override
	protected boolean isResizable() {
		return true;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite content = (Composite) super.createDialogArea(parent);

		wText = new Text(content, SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
		fillText();
		
		content.setLayout(new FillLayout());

		return content;
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(600, 400);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Log files");
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.HELP_ID, "Clear logs", true);
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}
	
	
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.HELP_ID) {
			if (!MessageDialog.openConfirm(getShell(), "Removing log files", "Are you sure to clear log files?")) {
				return;
			}
			List<String> logUser = LogProperty.getLogFile();

			// get the content for each log files
			for (String log: logUser) {
				try {
					FileUtility.clearFileContent(log);
				} catch (IOException e) {
					LoggerFactory.getLogger(getClass()).error("Unable to clear the log file " + log, e);
				}
			}
			Activator activator = Activator.getDefault();
			if (activator != null) {
				String locUser = Platform.getLogFileLocation().toOSString(); 
				try {
					FileUtility.clearFileContent(locUser);
				} catch (IOException e) {
					LoggerFactory.getLogger(getClass()).error("Unable to clear the log file " + locUser, e);
				}
			}
			fillText();
			
		} else {
			super.buttonPressed(buttonId);
		}
	}
	
	private void fillText() {

		// set the title
		String text = "Log files used by hpcviewer\n";

		List<String> logUser = LogProperty.getLogFile();

		// get the content for each log files
		for (String log: logUser) {
			text += "File: " + log + "\n";
			try {
				text += FileUtility.getFileContent(log);
			} catch (IOException e) {
				LoggerFactory.getLogger(getClass()).error("Unable to read " + log, e);
			}
		}
		text += "\n\n";
		
		Activator activator = Activator.getDefault();
		if (activator != null) {
			String locUser = Platform.getLogFileLocation().toOSString(); 
			text += "File: " + locUser + "\n";
			try {
				text += FileUtility.getFileContent(locUser);
			} catch (IOException e) {
				LoggerFactory.getLogger(getClass()).error("Unable to read " + locUser, e);
			}				
		}
		text += "\n";
		wText.setText(text);

	}
}
