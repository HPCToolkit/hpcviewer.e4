package edu.rice.cs.hpcremote.ui;

import java.io.IOException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import edu.rice.cs.hpcremote.data.IRemoteDirectoryBrowser;

public class RemoteDatabaseDialog extends TitleAreaDialog 
{
	private final IRemoteDirectoryBrowser remoteBrowser;
	
	private Text textDirectory;
	private List listDirectory;

	private String selectedDirectory;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public RemoteDatabaseDialog(Shell parentShell, IRemoteDirectoryBrowser remoteBrowser) {
		super(parentShell);
		
		if (remoteBrowser == null)
			throw new IllegalArgumentException("Null argument: remote browser");
		
		this.remoteBrowser = remoteBrowser;
	}


	/******
	 * Retrieve the selected remote directory.
	 * If the user clicks the cancel button or close the window without clicking the {@code OK} button,
	 * it returns null.
	 *  
	 * @return {@code String} 
	 * 			The selected directory or {@code null} if cancels.
	 */
	public String getSelectedDirectory() {
		return selectedDirectory;
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Remote database browser");
		setTitle("Browsing " + remoteBrowser.getRemoteHost());
		setMessage("Select a HPCToolkit database directory");
		
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite directoryArea = new Composite(container, SWT.NONE);
		GridLayout glDirectoryArea = new GridLayout(2, false);
		glDirectoryArea.marginHeight = 1;
		glDirectoryArea.marginWidth = 0;
		directoryArea.setLayout(glDirectoryArea);
		GridData gdDirectoryArea = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gdDirectoryArea.widthHint = 441;
		directoryArea.setLayoutData(gdDirectoryArea);
		
		Label lblDirectory = new Label(directoryArea, SWT.NONE);
		lblDirectory.setText("Directory:");
		
		textDirectory = new Text(directoryArea, SWT.BORDER);
		GridData gdTextDirectory = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gdTextDirectory.widthHint = 378;
		textDirectory.setLayoutData(gdTextDirectory);
		textDirectory.setBounds(0, 0, 64, 19);
		
		listDirectory = new List(container, SWT.BORDER | SWT.V_SCROLL);
		GridData gd_listDirectory = new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1);
		gd_listDirectory.heightHint = 362;
		gd_listDirectory.widthHint = 440;
		listDirectory.setLayoutData(gd_listDirectory);


		listDirectory.addSelectionListener(new SelectionAdapter() {
        	
        	@Override
        	public void widgetDefaultSelected(SelectionEvent e) {
        		if (listDirectory.getSelectionCount() == 0)
        			return;
        		
        		String directory = textDirectory.getText() + "/" + listDirectory.getSelection()[0];
        		fillDirectory(directory);
        	}
		});
		
		// ask the content of the default remote directory.
		// usually it's the home directory, but the server can give anything
		fillDirectory("");

		return area;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 550);
	}
	
	
	@Override
	protected void okPressed() {
		selectedDirectory = textDirectory.getText();
		if (listDirectory.getSelectionCount() > 0)
			selectedDirectory += "/" + listDirectory.getSelection()[0];
		
		super.okPressed();
	}
	
	private void fillDirectory(String directory) {
		try {
			var content = remoteBrowser.getContentRemoteDirectory(directory);
			if (content != null) {
				textDirectory.setText(content.getDirectory());
				var files = content.getContent();
				
				listDirectory.removeAll();
				
				for(int i=0; i<files.length; i++) {
					listDirectory.add(files[i]);
				}
			}
		} catch (IOException e1) {
			MessageDialog.openError(getShell(), "Error accessomg the remote directory", e1.getMessage());
		}
	}
}
