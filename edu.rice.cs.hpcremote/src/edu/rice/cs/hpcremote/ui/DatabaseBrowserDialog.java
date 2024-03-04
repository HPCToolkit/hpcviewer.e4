package edu.rice.cs.hpcremote.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import edu.rice.cs.hpcremote.ISecuredConnection;

public class DatabaseBrowserDialog extends Dialog 
{
	private final ISecuredConnection.ISessionRemoteSocket session;
	
	private List list;
	private Text txtDirectory;
	
	private String currentDir;
	
	public DatabaseBrowserDialog(Shell parentShell, ISecuredConnection.ISessionRemoteSocket session) {
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
        
        //
        // directory area to show the current remote directory
        //
        Composite directoryArea = new Composite(container, SWT.BORDER);
        
        Label lblDir = new Label(directoryArea, SWT.LEFT);
        lblDir.setText("Directory:");
        GridDataFactory.fillDefaults().grab(false, false).applyTo(lblDir);
        
        txtDirectory = new Text(directoryArea, SWT.SINGLE);
        txtDirectory.setText("");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(txtDirectory);
        
        GridDataFactory.fillDefaults().grab(true, false).applyTo(directoryArea);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(directoryArea);
        
        //
        // list of the content of the current directory
        //
        list = new List(container, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(list);

        list.addSelectionListener(new SelectionAdapter() {
        	
        	@Override
        	public void widgetDefaultSelected(SelectionEvent e) {
        		if (session == null || list.getSelectionCount() == 0)
        			return;
        		
        		String directory = currentDir + "/" + list.getSelection()[0]; 
        		
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
	
	
	@Override
	protected void okPressed() {
		if (list.getSelectionCount() > 0)
			currentDir += "/" + list.getSelection()[0];
		
		super.okPressed();
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
			} else if (listDir[0].startsWith("@ERR")) {
				String errMsg = listDir[0].substring(5);
				MessageDialog.openError(getShell(), "Error retrieving remote directory", errMsg);
				return;
			} else {
				MessageDialog.openError(getShell(), "Unknown text from the server", listDir[0]);
			}
			txtDirectory.setText(currentDir);
			
			for(int i=1; i<listDir.length; i++) {
				list.add(listDir[i]);
			}
			
		} catch (IOException e) {
			MessageDialog.openError(getShell(), "Error to connect to remote host", e.getLocalizedMessage());;
		}
	}
	
	
	public static void main(String []argv) {
		Shell shell = new Shell(Display.getDefault());
		var dialog = new DatabaseBrowserDialog(shell, new ISecuredConnection.ISessionRemoteSocket() {
			
			@Override
			public Session getSession() throws JSchException {
				return null;
			}
			
			@Override
			public void disconnect() {}
			
			@Override
			public OutputStream getLocalOutputStream() throws IOException {
				return System.out;
			}
			
			@Override
			public InputStream getLocalInputStream() throws IOException {
				return System.in;
			}
			
			@Override
			public String[] getCurrentLocalInput() throws IOException {
				return new String[] {"@LIST /home",  "one", "two" };
			}
			
			@Override
			public int getLocalPort() {
				return 0;
			}
		});
		dialog.open();
	}
}

