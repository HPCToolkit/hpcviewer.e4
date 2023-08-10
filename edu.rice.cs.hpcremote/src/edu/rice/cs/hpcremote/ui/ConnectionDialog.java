package edu.rice.cs.hpcremote.ui;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.hpctoolkit.hpcclient.v1_0.HpcClient;
import org.hpctoolkit.hpcclient.v1_0.HpcClientJavaNetHttp;

public class ConnectionDialog extends TitleAreaDialog 
{
	private static final String EMPTY = "";
	
	private Button flagEnableTunnel;
	
	private Combo  textHost;
	private Combo  textPort;
	private Combo  textUsername;
	private Text   textPassword;
	
	private HpcClient client;
	
	public ConnectionDialog(Shell parentShell) {
		super(parentShell);
	}

	
	public Optional<HpcClient> getClientConnection() {
		return Optional.of(client);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Remote connection");
		setTitle("Remote connection setup");
		setMessage("Please enter the host name and the port number provided by hpcserver");
		
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(container);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(container);

		var labelHost = new Label(container, SWT.LEFT);
		labelHost.setText("Hostname/IP address:");
		
		textHost = new Combo(container, SWT.NONE);
		textHost.setText(EMPTY);
		textHost.setToolTipText("Please enter the remote host name or its IP address as provided by hpcserver output");
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(textHost);
		
		var labelPort = new Label(container, SWT.LEFT);
		labelPort.setText("Port:");
		
		textPort = new Combo(container, SWT.DROP_DOWN);
		textPort.setText(EMPTY);
		textPort.setToolTipText("Please enter the hpcserver's port number as provided by hpcserver's output");
		textPort.addListener(SWT.KeyDown, event -> event.doit = Character.isDigit(event.character));
		textPort.setEnabled(false);
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(textPort);
		
		//------------------------------------------------------
		//------------------------------------------------------
		var groupTunnel = new Group(area, SWT.BORDER_SOLID);
		groupTunnel.setText(EMPTY);
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(groupTunnel);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(groupTunnel);

		flagEnableTunnel = new Button(groupTunnel, SWT.CHECK);
		flagEnableTunnel.setText("Enable SSH tunneling");
		flagEnableTunnel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				enableTunnel(flagEnableTunnel.getSelection());
			}			
		});
		
		GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(flagEnableTunnel);
		
		var labelUsername = new Label(groupTunnel, SWT.LEFT);
		labelUsername.setText("Username:");
		
		textUsername = new Combo(groupTunnel, SWT.DROP_DOWN);
		textUsername.setText(EMPTY);
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(textUsername);
		
		var labelPassword = new Label(groupTunnel, SWT.LEFT);
		labelPassword.setText("Password");
		
		textPassword = new Text(groupTunnel, SWT.PASSWORD);
		textPassword.setText(EMPTY);
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(textPassword);
		
		enableTunnel(false);
		
		return area;
	}
	
	@Override
	protected void okPressed() {
		var host = textHost.getText();
		var portString = textPort.getText();
		int portNumber = 0;
		
		try {
			portNumber = Integer.parseInt(portString);
		} catch (NumberFormatException e) {
			MessageDialog.openError(getShell(), "Invalid port number", portString + ": invalid port number");
			return;
		}
		
		try {
			var address = InetAddress.getByName(host);
			client = new HpcClientJavaNetHttp( address, portNumber);
		} catch (UnknownHostException e) {
			MessageDialog.openError(getShell(), "Fail to connect", "Unable to connect to " + host + ":" + portString);
			return;
		}
		
		super.okPressed();
	}
	
	
	private void enableTunnel(boolean enable) {
		flagEnableTunnel.setEnabled(true);
		flagEnableTunnel.setSelection(enable);
		textUsername.setEnabled(enable);
		textPassword.setEditable(enable);
	}
	
	
	public static void main(String []args) {
		Display display = new Display();
		Shell shell = new Shell(display);

		var dialog = new ConnectionDialog(shell);
		
		if (dialog.open() == Window.OK) {
			var client = dialog.getClientConnection();
			if (client.isPresent()) {
				var connect = client.get();
				try {
					var max = connect.getMaximumTraceSampleTimestamp();
					var min = connect.getMaximumTraceSampleTimestamp();
					
					MessageDialog.openInformation(
							shell, 
							"Connection successful", 
							"max: " + max + "\n" +
							"min: " + min );
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		display.dispose();
	}

}