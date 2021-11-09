package edu.rice.cs.hpcviewer.ui.internal;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import edu.rice.cs.hpcviewer.ui.base.IUserMessage;


public class LabelMessage implements IUserMessage
{
	static final private int MESSAGE_TIMEOUT = 8000; // time out when showing a message
	
	final private Color colorYellow, colorGreen, colorRed, colorNormal, colorWhite, colorBlack;
	final private Label lblMessage;

	public LabelMessage(Composite parent, int style) {

		Display display = Display.getDefault();
		colorYellow = display.getSystemColor(SWT.COLOR_YELLOW);
		colorRed    = display.getSystemColor(SWT.COLOR_RED);
		colorGreen  = display.getSystemColor(SWT.COLOR_GREEN);
		colorWhite  = display.getSystemColor(SWT.COLOR_WHITE);
		colorBlack  = display.getSystemColor(SWT.COLOR_BLACK);
		
		lblMessage  = new Label(parent, SWT.FLAT | SWT.LEFT);
		colorNormal = lblMessage.getBackground();
		
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(lblMessage);
	}

    
    /***
     * display an error message for a couple of seconds
     * @param str message
     */
    public void showErrorMessage(String str) {
    	lblMessage.setForeground(colorWhite);
    	showMessage(str, colorRed);
    }
    
    public void showInfo(String message) {
    	lblMessage.setForeground(colorBlack);
    	showMessage(message, colorGreen);
    }
    
    public void showWarning(String message) {
    	lblMessage.setForeground(colorBlack);
    	showMessage(message, colorYellow);
    }
	
	/**
	 * Restore the message bar into the original state
	 */
    protected void restoreMessage() {
		if(!lblMessage.isDisposed()) {
			lblMessage.setBackground(colorNormal);
			lblMessage.setText("");
		}
	}
    
    private void showMessage(String message, Color color) {
    	lblMessage.setBackground(color);
    	lblMessage.setText(message);

    	RestoreMessageThread thrRestoreMessage = new RestoreMessageThread();
		thrRestoreMessage.start();
    }

	/**
	 * Class to restoring the background of the message bar by waiting for 5 seconds
	 * TODO: we need to parameterize the timing for the wait
	 *
	 */
	private class RestoreMessageThread extends Thread {	
		RestoreMessageThread() {
			super();
		}
         public void run() {
             try{
            	 sleep(MESSAGE_TIMEOUT);
             } catch(InterruptedException e) {
            	 e.printStackTrace();
             }
             // need to run from UI-thread for restoring the background
             // without UI-thread we will get SWTException !!
        	 Display display = Display.getDefault();
        	 if (display != null && !display.isDisposed()) {
        		 display.asyncExec(new Runnable() {
                	 public void run() {
                    	 restoreMessage();
                	 }
                 });
        	 }
         }
     }

}
