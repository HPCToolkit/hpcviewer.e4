package edu.rice.cs.hpcremote.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
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
	
	private String host;
	private int    port;
	private String username;
	private String password;
	
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
		textHost.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				textPort.setEnabled(true);
			}
		});

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
				boolean state = flagEnableTunnel.getSelection();
				enableTunnel(state);
			}			
		});
		flagEnableTunnel.setEnabled(true);
		
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
		host = textHost.getText();
		username = textUsername.getText();
		password = textPassword.getText();
		
		var portString = textPort.getText();
		
		try {
			port = Integer.parseInt(portString);
		} catch (NumberFormatException e) {
			MessageDialog.openError(getShell(), "Invalid port number", portString + ": invalid port number");
			return;
		}
		addIntoHistory(textHost, HISTORY_KEY_HOST);
		addIntoHistory(textPort, HISTORY_KEY_PORT);

		super.okPressed();
	}
	
	
	public String getHost() {
		return host;
	}


	public int getPort() {
		return port;
	}


	public String getUsername() {
		return username;
	}


	public String getPassword() {
		return password;
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
		textUsername.setEnabled(enable);
		textPassword.setEditable(enable);
	}
}