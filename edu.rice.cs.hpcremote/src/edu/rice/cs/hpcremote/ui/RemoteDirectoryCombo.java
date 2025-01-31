// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcremote.ui;

import java.util.Comparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import edu.rice.cs.hpcbase.map.UserInputHistory;

public class RemoteDirectoryCombo  
{
	private static final String KEY_HISTORY_REMOTE_DIRECTORY = "hpremote.dir";
	
	private final UserInputHistory history;
	private final Combo directoryText;
	
	private String oldDirectory;

	public RemoteDirectoryCombo(Composite parent, String remoteHost, IUpdateDirectoryCallback callback) {
		
		Composite directoryArea = new Composite(parent, SWT.NONE);
		GridLayout glDirectoryArea = new GridLayout(2, false);
		glDirectoryArea.marginHeight = 1;
		glDirectoryArea.marginWidth = 0;
		directoryArea.setLayout(glDirectoryArea);
		GridData gdDirectoryArea = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gdDirectoryArea.widthHint = 441;
		directoryArea.setLayoutData(gdDirectoryArea);

		
		Label label = new Label(directoryArea, SWT.NONE);
		label.setText("Directory:");
		
		directoryText = new Combo(directoryArea, SWT.DROP_DOWN);
		GridData gdTextDirectory = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gdTextDirectory.widthHint = 378;
		directoryText.setLayoutData(gdTextDirectory);
		directoryText.setBounds(0, 0, 64, 19);


		// populate the combo if needed
		history = new UserInputHistory(KEY_HISTORY_REMOTE_DIRECTORY + "." + remoteHost);
		var listHistory = history.getHistory();
		
		if (listHistory != null && !listHistory.isEmpty()) {
			listHistory.sort(Comparator.naturalOrder());
			listHistory.forEach( directoryText::add);
		}
		
		directoryText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.character == '\r' || e.character == '\t') {
					var directory = directoryText.getText();
					if (!directory.equals(oldDirectory)) {
						updateDirectory(directory, callback);
					}
				}
			}
		});
		
		directoryText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				var directory = directoryText.getText();
				if (!directory.equals(oldDirectory)) {
					updateDirectory(directory, callback);
				}
			}
		});
		oldDirectory = "";
	}

	private void updateDirectory(String newDirectory, IUpdateDirectoryCallback callback) {
		if (newDirectory != null && !newDirectory.isEmpty() &&  (callback.updateDirectory(newDirectory))) {
				history.addLine(newDirectory);
				oldDirectory = newDirectory;
			
		}
	}
	
	public void setText (String string) {
		directoryText.setText(string);
		oldDirectory = string;
		history.addLine(string);
	}
	
	
	public String getText() {
		if (!directoryText.isDisposed())
			return directoryText.getText();
		
		return "";
	}
	
	interface IUpdateDirectoryCallback
	{
		boolean updateDirectory(String newDirectory);
	}
}
