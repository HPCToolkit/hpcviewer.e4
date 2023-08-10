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

import edu.rice.cs.hpcbase.map.UserInputHistory;


/*******************************************************
 * 
 * A window to ask the connection setup to the server/
 * A typical usage of this class:
 * <pre>
 * 		var dialog = new ConnectionDialog(shell);
 *		
 *		if (dialog.open() == Window.OK) {
 *			var client = dialog.getClientConnection();
 * </pre>
 * There is no guarantee if {@code client} is completely valid.
 * Caller needs to check its validity.
 * 
 *******************************************************/
public class ConnectionDialog extends TitleAreaDialog 
{
	private static final String EMPTY = "";
	
	private static final String HISTORY_KEY_HOST = "hpcremote.host";
	private static final String HISTORY_KEY_PORT = "hpcremote.port";
	
	private Button flagEnableTunnel;
	
	private Combo  textHost;
	private Combo  textPort;
	private Combo  textUsername;
	private Text   textPassword;
	
	private HpcClient client;
	
	/*****
	 * Instantiate a connection window. User needs to call {@code open} 
	 * method and then get the connection object with 
	 * {@code getClientConnection} method. 
	 * 
	 * @param parentShell
	 */
	public ConnectionDialog(Shell parentShell) {
		super(parentShell);
	}

	
	/*****
	 * Retrieve the {@code HpcClient} object if the user
	 * confirm the connection. This doesn't mean the connection
	 * is successful. The caller needs to check if the connection
	 * is established and can communicate with hpcserver. 
	 * 
	 * @return {@code HpcClient} can be empty if user clicks cancel or
	 * the instantiation is not successful.
	 */
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
		
		fillAndSetComboWithHistory(textHost, HISTORY_KEY_HOST);
		
		textHost.addModifyListener(e -> textPort.setEnabled(true));

		GridDataFactory.fillDefaults().grab(true, true).applyTo(textHost);
		
		var labelPort = new Label(container, SWT.LEFT);
		labelPort.setText("Port:");
		
		textPort = new Combo(container, SWT.DROP_DOWN);
		textPort.setText(EMPTY);
		textPort.setToolTipText("Please enter the hpcserver's port number as provided by hpcserver's output");

		fillAndSetComboWithHistory(textPort, HISTORY_KEY_PORT);

		textPort.setEnabled(!textPort.getText().isEmpty());

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
				enableTunnel(false /*flagEnableTunnel.getSelection()*/);
			}			
		});
		flagEnableTunnel.setEnabled(false);
		
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
		addIntoHistory(textHost, HISTORY_KEY_HOST);
		addIntoHistory(textPort, HISTORY_KEY_PORT);
		
		super.okPressed();
	}
	
	
	private void fillAndSetComboWithHistory(Combo combo, String key) {		
		UserInputHistory history = new UserInputHistory(key);
		var histories = history.getHistory();
		for(var item: histories) {
			combo.add(item);
		}
		if (!histories.isEmpty())
			combo.select(0);
	}
	
	
	private void addIntoHistory(Combo combo, String key) {
		UserInputHistory history = new UserInputHistory(key);
		history.addLine(combo.getText());
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