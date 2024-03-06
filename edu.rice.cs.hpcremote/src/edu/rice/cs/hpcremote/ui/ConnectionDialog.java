package edu.rice.cs.hpcremote.ui;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import edu.rice.cs.hpcbase.map.UserInputHistory;
import edu.rice.cs.hpcremote.IConnection;
import edu.rice.cs.hpcremote.RemoteDatabaseIdentification;


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
public class ConnectionDialog extends TitleAreaDialog implements IConnection
{
	private static final String EMPTY = "";
	
	private static final String HISTORY_KEY_USER = "hpcremote.user";
	private static final String HISTORY_KEY_HOST = "hpcremote.host";
	private static final String HISTORY_KEY_RDIR = "hpcremote.rdir";
	private static final String HISTORY_KEY_PRIV = "hpcremote.priv";
	
	private Combo  textHost;
	private Combo  textUsername;
	private Combo  textDirectory;
	private Combo  textPrivateKey;
	
	private String host;
	private String username;
	private String directory;
	private String privateKey;

	
	/*****
	 * Instantiate a connection window. User needs to call {@code open} 
	 * method and then get the connection object with 
	 * {@code getClientConnection} method. 
	 * 
	 * @param parentShell
	 */
	public ConnectionDialog(Shell parentShell) {
		super(parentShell);
		
		host = null; // default: local host
		username = EMPTY;
	}

	
	public ConnectionDialog(Shell parentShell, RemoteDatabaseIdentification databaseId) {
		super(parentShell);
		
		if (databaseId == null)
			databaseId = new RemoteDatabaseIdentification();
		
		host = databaseId.getHost();
		username = databaseId.getUsername();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Remote connection");
		setTitle("Remote connection setup");
		setMessage("Enter the information needed to connect to the remote server");
		
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(container);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(container);
		
		//------------------------------------------------------

		var labelHost = new Label(container, SWT.RIGHT);
		labelHost.setText("Hostname/IP address:");
		
		textHost = new Combo(container, SWT.NONE);
		textHost.setToolTipText("Please enter the remote host name or its IP address.");
		
		fillAndSetComboWithHistory(textHost, HISTORY_KEY_HOST);

		GridDataFactory.fillDefaults().grab(true, false).applyTo(textHost);
				
		//------------------------------------------------------
		
		var labelUsername = new Label(container, SWT.RIGHT);
		labelUsername.setText("Username:");
		
		textUsername = new Combo(container, SWT.DROP_DOWN);
		fillAndSetComboWithHistory(textUsername, HISTORY_KEY_USER);
		
		GridDataFactory.fillDefaults().grab(true, false).applyTo(textUsername);
		
		//------------------------------------------------------

		var labelDirectory = new Label(container, SWT.RIGHT);
		labelDirectory.setText("Remote installation directory:");
		
		textDirectory = new Combo(container, SWT.DROP_DOWN);
		fillAndSetComboWithHistory(textDirectory, HISTORY_KEY_RDIR);
		
		GridDataFactory.fillDefaults().grab(true, false).applyTo(textDirectory);
				
		//------------------------------------------------------

		var labelPrivateKey = new Label(container, SWT.RIGHT);
		labelPrivateKey.setText("Private key (optional):");
		
		textPrivateKey = new Combo(container, SWT.DROP_DOWN);
		fillAndSetComboWithHistory(textPrivateKey, HISTORY_KEY_PRIV);
		
		GridDataFactory.fillDefaults().grab(true, false).applyTo(textPrivateKey);
		
		return area;
	}
	
	
	@Override
	protected void okPressed() {
		host = textHost.getText();
		username = textUsername.getText();		
		directory = textDirectory.getText();
		privateKey = textPrivateKey.getText();
		
		addIntoHistory(textHost, HISTORY_KEY_HOST);
		addIntoHistory(textUsername, HISTORY_KEY_USER);
		addIntoHistory(textDirectory, HISTORY_KEY_RDIR);
		addIntoHistory(textPrivateKey, HISTORY_KEY_PRIV);

		super.okPressed();
	}


	@Override
	public String getHost() {
		return checkVariable(host);
	}


	@Override
	public String getUsername() {
		return checkVariable(username);
	}


	@Override
	public String getPrivateKey() {
		return checkVariable(privateKey);
	}


	@Override
	public String getInstallationDirectory() {
		return checkVariable(directory);
	}

	
	private String checkVariable(String varString) {
		if (varString == null)
			throw new IllegalAccessError("Not connected");

		return varString;
	}
	

	@Override
	public String getId() {
		return getUsername() + "@" + getHost() + ":" + getInstallationDirectory();
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
	
	
	public static void main(String []argv) {
		var display = Display.getDefault();
		Shell shell = new Shell(display);
		
		var dlg = new ConnectionDialog(shell);
		dlg.open();
	}
}