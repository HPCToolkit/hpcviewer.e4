package edu.rice.cs.hpcremote.ui;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringTokenizer;

import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
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
	private static final String HISTORY_SEPARATOR = "|";

	private static final String HISTORY_KEY_PROFILE = "hpcremote.profile";

	private static final String HISTORY_KEY_PRIV = "hpcremote.priv";
	
	private Combo  textHost;
	private Combo  textUsername;
	private Combo  textDirectory;
	private Combo  textPrivateKey;
	private Combo  textProxyAgent;
	private Combo  textConfig;
	
	private Button labelPrivateKey;
	private Button labelProxyAgent;
	private Button labelUsePassword;
	private Button labelConfiguration;
	
	private String host;
	private String username;
	private String directory;
	private String privateKey;
	private String proxyAgent;
	private String configRepo;
	
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

		GridDataFactory.fillDefaults().grab(true, false).applyTo(textHost);
				
		//------------------------------------------------------
		
		var labelUsername = new Label(container, SWT.RIGHT);
		labelUsername.setText("Username:");
		
		textUsername = new Combo(container, SWT.DROP_DOWN);
		
		GridDataFactory.fillDefaults().grab(true, false).applyTo(textUsername);
		
		//------------------------------------------------------

		var labelDirectory = new Label(container, SWT.RIGHT);
		labelDirectory.setText("Remote installation directory:");
		
		textDirectory = new Combo(container, SWT.DROP_DOWN);
		
		GridDataFactory.fillDefaults().grab(true, false).applyTo(textDirectory);
				
		//------------------------------------------------------
		createOptionArea(container);

		//------------------------------------------------------
		// fill up the form for the default
		//------------------------------------------------------

		initFields();
		
		return area;
	}
	
	
	private void createOptionArea(Composite container) {
		var optionsArea = new Group(container, SWT.BORDER_SOLID);
		
		GridDataFactory.fillDefaults().span(2, 1).grab(true, true).applyTo(optionsArea);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(optionsArea);

		createPrivateKeyOption(optionsArea);
		createProxyAgentOption(optionsArea);
		createPasswordOption(optionsArea);
		
		createConfigRepo(optionsArea);
	}
	
	
	private void createConfigRepo(Composite optionsArea) {		
		labelConfiguration = new Button(optionsArea, SWT.CHECK);
		labelConfiguration.setText("SSH configuration:");
		
		textConfig = new Combo(optionsArea, SWT.DROP_DOWN);
		var configFile = System.getProperty("user.home") + "/.ssh/config";

		if (Files.isReadable(Path.of(configFile))) {
			textConfig.add(configFile);
			textConfig.select(0);
			textConfig.setEnabled(true);
			
			labelConfiguration.setSelection(true);
		} else {
			textConfig.setEnabled(false);
			labelConfiguration.setSelection(false);
		}
		labelConfiguration.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				var selected = labelConfiguration.getSelection();
				textConfig.setEnabled(selected);
			}
		});
	}
	
	
	private void createPasswordOption(Composite optionsArea) {
		labelUsePassword = new Button(optionsArea, SWT.RADIO);
		labelUsePassword.setText("Use password");
		GridDataFactory.fillDefaults().span(2, 1).grab(true, true).applyTo(labelUsePassword);
	}
	
	private void createProxyAgentOption(Composite optionsArea) {
		labelProxyAgent = new Button(optionsArea, SWT.RADIO);
		labelProxyAgent.setText("Use identity");
		
		textProxyAgent = new Combo(optionsArea, SWT.DROP_DOWN | SWT.READ_ONLY);
		textProxyAgent.add("Proxy agent");
		textProxyAgent.select(0);
	}
	
	private void createPrivateKeyOption(Composite optionsArea) {
		labelPrivateKey = new Button(optionsArea, SWT.RADIO);
		labelPrivateKey.setText("Use private key:");
		labelPrivateKey.setSelection(false);
		
		var privateKeyArea  = new Composite(optionsArea, SWT.NONE);
		
		GridDataFactory.fillDefaults().grab(true, false).applyTo(privateKeyArea);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(privateKeyArea);

		textPrivateKey = new Combo(privateKeyArea, SWT.DROP_DOWN);
		
		if (textPrivateKey.getSelectionIndex() < 0) {
			var home = System.getProperty("user.home");
			var key  = home + File.separator + ".ssh" + File.separator + "id_rsa";
			textPrivateKey.setText(key);
		}
		
		var browserButton = new Button(privateKeyArea, SWT.PUSH);
		browserButton.setText("...");
		browserButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				var browseDlg = new FileDialog(getShell(), SWT.OPEN);
				var keyFilename = browseDlg.open();
				if (keyFilename != null)
					textPrivateKey.setText(keyFilename);
			}
		});
		
		GridDataFactory.fillDefaults().grab(true, false).applyTo(textPrivateKey);

		labelPrivateKey.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				var selected = labelPrivateKey.getSelection();
				textPrivateKey.setEnabled(selected);
				browserButton.setEnabled(selected);
			}
		});
		var privateKeyEnabled = textPrivateKey.getText() != null && !textPrivateKey.getText().isEmpty();
		textPrivateKey.setEnabled(privateKeyEnabled);
		browserButton.setEnabled(privateKeyEnabled);
		
		boolean privateKeyChecked = privateKeyEnabled;
		if (privateKeyEnabled) {
			// if rsa file is not accessible, do not check the private key label
			String rsaFile = textPrivateKey.getText();
			privateKeyChecked = Files.exists(Paths.get(rsaFile));
		}
		labelPrivateKey.setSelection(privateKeyChecked);
	}
	
	
	private void initFields() {
		var histories = initHistory();

		if (histories.length > 0) {
			for(var h: histories) {
				if (h == null)
					continue;
				
				textHost.add(h.host);
				textUsername.add(h.username);
				textDirectory.add(h.remoteDirectory);
			}
			textHost.select(0);
			textUsername.select(0);
			textDirectory.select(0);
			
			textHost.addModifyListener(event -> {
				int selection = textHost.getSelectionIndex();
				textUsername.select(selection);
				textDirectory.select(selection);
			});
		}
		fillAndSetComboWithHistory(textPrivateKey, HISTORY_KEY_PRIV);
	}
	
	
	@Override
	protected void okPressed() {
		if (labelPrivateKey.getSelection() && (textPrivateKey.getText() == null || textPrivateKey.getText().isEmpty()) ) {
			MessageDialog.openError(getShell(), "Invalid private key", "Invalid Private key: the option is selected but the field is empty.");
		}
		
		host = textHost.getText();
		username = textUsername.getText();		
		directory = textDirectory.getText();
		privateKey = labelPrivateKey.getSelection() ? textPrivateKey.getText() : null;
		proxyAgent = labelProxyAgent.getSelection() ? textProxyAgent.getText() : null;
		configRepo = labelConfiguration.getSelection() ? textConfig.getText()  : null;
		
		var history = new UserInputHistory(HISTORY_KEY_PROFILE);
		var line = host + HISTORY_SEPARATOR + username + HISTORY_SEPARATOR + directory;
		history.addLine(line);
		
		if (privateKey != null && !privateKey.isEmpty() && !privateKey.isBlank()) {
			history = new UserInputHistory(HISTORY_KEY_PRIV);
			history.addLine(privateKey);
		}

		super.okPressed();
	}


	@Override
	public String getConfig() {
		return checkVariable(configRepo);
	}

	
	@Override
	public String getProxyAgent() {
		return checkVariable(proxyAgent);
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
		return privateKey;
	}


	@Override
	public String getInstallationDirectory() {
		return checkVariable(directory);
	}

	
	private String checkVariable(String varString) {
		return varString;
	}
	

	@Override
	public String getId() {
		return getUsername() + "@" + getHost() + ":" + getInstallationDirectory();
	}

	
	private void checkFields() {
		boolean notEmpty = !textHost.getText().isEmpty() && !textUsername.getText().isEmpty();
		var btnOk = getButton(IDialogConstants.OK_ID);
		if (btnOk != null) 
			btnOk.setEnabled(notEmpty);
	}
	
	private void fillAndSetComboWithHistory(Combo combo, String key) {	
		UserInputHistory history = new UserInputHistory(key);
		var histories = history.getHistory();
		for(var item: histories) {
			combo.add(item);
		}
		if (!histories.isEmpty())
			combo.select(0);
		
		combo.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				checkFields();
			}
		});
	}
	
	
	public static void main(String []argv) {
		var display = Display.getDefault();
		Shell shell = new Shell(display);
		
		var dlg = new ConnectionDialog(shell);
		dlg.open();
	}
	
	
	private History[] initHistory() {
		var history = new UserInputHistory(HISTORY_KEY_PROFILE);
		var listOfHistories = history.getHistory();
		if (listOfHistories == null || listOfHistories.isEmpty()) {
			return new History[] {new History("localhost", username, "")};
		}
		History []histories = new History[listOfHistories.size()];
		int i=0;
		for(var line: listOfHistories) {
			StringTokenizer tokenizer = new StringTokenizer(line, HISTORY_SEPARATOR);
			if (tokenizer.countTokens() == 3) {
				var prof = tokenizer.nextToken();
				var user = tokenizer.nextToken();
				var dir  = tokenizer.nextToken();
				histories[i] = new History(prof, user, dir);
				i++;
			}
		}
		return histories;
	}
	
	private record History (String host, String username, String remoteDirectory)
	{}
}