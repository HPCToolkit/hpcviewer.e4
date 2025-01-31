// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.test;

import static org.junit.Assert.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Label;
import org.junit.Before;
import org.junit.Test;

import edu.rice.cs.hpctest.util.ViewerTestCase;
import edu.rice.cs.hpctraceviewer.ui.util.MessageLabelManager;

public class MessageLabelManagerTest extends ViewerTestCase 
{
	@Before
	public void setup() {
		super.setUp();
		
		shell.setLayout(new FillLayout());
	}

	@Test
	public void testMessageAndClear() throws InterruptedException {
		Label label = new Label(shell, SWT.NONE);
		label.setLayoutData(new FillLayout());
		
		shell.layout();
		shell.open();
		
		showWindow();
		
		MessageLabelManager labelManager = new MessageLabelManager(shell.getDisplay(), label);

		String message = "Error message";
		
		labelManager.showError(message);		
		checkLabelMessage(label, message);
		
		message = "Info message";
		labelManager.showInfo(message);
		checkLabelMessage(label, message);

		message = "Warning message";
		labelManager.showWarning(message);
		checkLabelMessage(label, message);
		
		labelManager.clear();
		checkLabelMessage(label, "");

		message = "Processing message";
		labelManager.showProcessingMessage(message);
		checkLabelMessage(label, message);
		
		// wait long enough until the message disappears automatically
		showWindow(5500);

		assertTrue(label.getText().isEmpty());
	}
	
	private void checkLabelMessage(Label label, String message) {
		// wait a bit
		showWindow();
		
		// check if the label shows the exact message
		assertTrue(label.getText().equals(message));
	}
}
