package edu.rice.cs.hpcremote.ui;

import java.io.IOException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcremote.ISecuredConnection;

public class DatabaseBrowserDialog extends Dialog 
{
	private final ISecuredConnection.ISocketSession session;
	private List list;
	private String currentDir;
	
	public DatabaseBrowserDialog(Shell parentShell, ISecuredConnection.ISocketSession session) {
		super(parentShell);
		
		if (session == null)
			throw new IllegalArgumentException("Null argument: session");
		
		this.session = session;
	}

	
	public String getCurrentDirectory() {
		return currentDir;
	}
	
	
	@Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        
        list = new List(parent, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(list);

        list.addSelectionListener(new SelectionAdapter() {
        	
        	@Override
        	public void widgetDefaultSelected(SelectionEvent e) {
        		if (session == null || list.getSelectionCount() == 0)
        			return;
        		
        		String directory = list.getSelection()[0]; 
        		
        		if (sendRequestToRemoteServer(directory)) {
        			list.removeAll();
        			fillListWithRemoteDirectory();
        		}
        	}
		});
                
        sendRequestToRemoteServer("");
        
        fillListWithRemoteDirectory();
        
        return container;
	}
	
	
	private boolean sendRequestToRemoteServer(String directory) {
		try {
			session.writeLocalOutput("@LIST " + directory);
		} catch (IOException e) {
			MessageDialog.openError(
					getShell(), 
					"Fail to communicate with the server", 
					directory);
			return false;
		}
		return true;
	}
	
	private void fillListWithRemoteDirectory() {
        try {
			var listDir = session.getCurrentLocalInput();
			if (listDir == null || listDir.length == 0) {
				MessageDialog.openWarning(getShell(), "Empty directory", "No content in this directory");
				return;
			}
			if (listDir[0].startsWith("@LIST")) {
				currentDir = listDir[0].substring(6);
			} else {
				MessageDialog.openError(getShell(), "Unknown text from the server", listDir[0]);
			}
			for(int i=1; i<listDir.length; i++) {
				list.add(listDir[i]);
			}
			
		} catch (IOException e) {
			MessageDialog.openError(getShell(), "Error to connect to remote host", e.getLocalizedMessage());;
		}
	}
}

