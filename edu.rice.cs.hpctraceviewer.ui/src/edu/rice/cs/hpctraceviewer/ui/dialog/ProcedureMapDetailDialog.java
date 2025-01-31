// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.dialog;

import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/****
 * 
 * display procedure and its class
 * can be used for either adding or editing the map
 *
 */
public class ProcedureMapDetailDialog extends Dialog 
{
	private final static String EMPTY = "\u2588\u2588\u2588";

	final private String title;
	private String proc;
	private String description;
	private RGB rgb;
	private Color currentColor;
	
	private Text txtProc;
	private Text txtClass;

	/***
	 * retrieve the new procedure name
	 * @return
	 */
	public String getProcedure() {
		return proc;
	}
	
	/**
	 * retrieve the new class
	 * @return
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Retrieve the selected color (in RGB)
	 * @return RGB
	 */
	public RGB getRGB() {
		return rgb;
	}
	
	/***
	 * constructor
	 * 
	 * @param parentShell : parent shell
	 * @param title : title of the dialog
	 * @param proc : default name of the procedure
	 * @param procClass : default name of the class
	 */
	public ProcedureMapDetailDialog(Shell parentShell, String title, String proc, String procClass, RGB color) {
		super(parentShell);
		
		this.proc = proc;
		this.description = procClass;
		this.title = title;
		this.rgb = color;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
		
		final Label lblIntro = new Label(composite, SWT.LEFT);
		lblIntro.setText("Please type the name of the procedure or the pattern of procedure name.\n" + 
						 "Symbol * matches all characters, while symbol ? matches only one character.");
		GridDataFactory.swtDefaults().span(2, 1).applyTo(lblIntro);
		
		final Label lblProc = new Label(composite, SWT.LEFT);
		lblProc.setText("Procedure pattern: ");
		txtProc = new Text(composite, SWT.LEFT | SWT.SINGLE);
		txtProc.setText(proc);
		
		GridDataFactory.swtDefaults().hint(
				this.convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH), SWT.DEFAULT)
				.grab(true, false).applyTo(txtProc);
		
		final Label lblClass = new Label(composite, SWT.LEFT);
		lblClass.setText("Description: ");
		txtClass = new Text(composite, SWT.LEFT | SWT.SINGLE);
		txtClass.setText(description);
		GridDataFactory.swtDefaults().hint(
				this.convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH), SWT.DEFAULT)
				.grab(true, false).applyTo(txtClass);
		
		final Label lblColor = new Label(composite, SWT.LEFT);
		lblColor.setText("Color: ");
		
		final Button btnColor = new Button(composite, SWT.PUSH | SWT.FLAT);
		btnColor.setText(EMPTY);
		if (rgb == null) {
			rgb = getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK).getRGB();
		}
		setButtonImage(btnColor, rgb);

		Point size = btnColor.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		btnColor.setSize(size.x+10, size.y+10);
		
		btnColor.addSelectionListener( new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				final Shell shell = ProcedureMapDetailDialog.this.getShell();
				ColorDialog colorDlg = new ColorDialog(shell);
				colorDlg.setRGB(rgb);
				colorDlg.setText("Select color for " + ProcedureMapDetailDialog.this.description);
				final RGB newRGB = colorDlg.open();
				if (newRGB != null) {
					rgb = newRGB;
					setButtonImage(btnColor, rgb);
				}
			}
		});
		
		return composite;
	}

	/***
	 * set an image to a button given the color 
	 * @param button
	 * @param color
	 */
	private void setButtonImage(Button button, RGB color) {
		if (currentColor != null && !currentColor.isDisposed()) {
			currentColor.dispose();
		}
		currentColor = new Color(button.getDisplay(), color);
		button.setBackground(currentColor);
		button.setForeground(currentColor);
	}
	
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        if (title != null) {
			shell.setText(title);
		}
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		proc = txtProc.getText();
		if (proc == null || proc.isEmpty()) {
			// we do not allow empty pattern
			
			MessageDialog.openError(getShell(), "Error", "Procedure pattern cannot be empty");
			Display display = getShell().getDisplay();
			txtProc.setBackground(display.getSystemColor(SWT.COLOR_RED));
			return;
		} else {
			// check the validity of the pattern
			
			try {
				"Function".matches(proc.replace("*", ".*").replace("?", ".?"));
				
				// the pattern looks valid, ready to close the dialog
				
				description = txtClass.getText();
				
				if (currentColor != null && !currentColor.isDisposed()) {
					currentColor.dispose();
				}

				super.okPressed();
				
			} catch (PatternSyntaxException e) {
				MessageDialog.openError(getShell(), "Error", "Pattern is not valid:\n" + e.getMessage());
			}
		}
	}
}
