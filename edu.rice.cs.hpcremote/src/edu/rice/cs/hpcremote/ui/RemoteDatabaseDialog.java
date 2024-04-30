package edu.rice.cs.hpcremote.ui;

import java.io.IOException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import edu.rice.cs.hpcremote.data.IRemoteDirectoryBrowser;
import edu.rice.cs.hpcremote.data.IRemoteDirectoryContent;
import edu.rice.cs.hpcremote.data.IRemoteDirectoryContent.IFileContent;

public class RemoteDatabaseDialog extends TitleAreaDialog 
{
	private final IRemoteDirectoryBrowser remoteBrowser;
	private final Image imgFolderReg;
	private final Image imgFolderDb;
	
	private Text textDirectory;
	private TableViewer directoryViewer;

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
		
		ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());
		
		var descFolderReg = ImageDescriptor.createFromFile(getClass(), "folder-16.png");
		imgFolderReg = resourceManager.createImage(descFolderReg);

		var descFolderDb = ImageDescriptor.createFromFile(getClass(), "folder-16-green.png");
		imgFolderDb = resourceManager.createImage(descFolderDb);
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
		setTitle("Browsing " + remoteBrowser.getRemoteHostname());
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
		GridData gdDirectoryArea = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gdDirectoryArea.widthHint = 441;
		directoryArea.setLayoutData(gdDirectoryArea);
		
		Label lblDirectory = new Label(directoryArea, SWT.NONE);
		lblDirectory.setText("Directory:");
		
		textDirectory = new Text(directoryArea, SWT.BORDER);
		GridData gdTextDirectory = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gdTextDirectory.widthHint = 378;
		textDirectory.setLayoutData(gdTextDirectory);
		textDirectory.setBounds(0, 0, 64, 19);
		
		directoryViewer = createTableDirectory(container);
		
		// ask the content of the default remote directory.
		// usually it's the home directory, but the server can give anything
		fillDirectory("");
		
		return area;
	}

	
	private TableViewer createTableDirectory(Composite container) {
		var viewer = new TableViewer(container, SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
		TableViewerColumn iconColViewer = new TableViewerColumn(viewer, SWT.NONE);
		iconColViewer.getColumn().setWidth(20);
		
		iconColViewer.setLabelProvider(new ColumnLabelProvider() {
			
			@Override
		    public String getText(Object element) {
				return null;
			}
			
			@Override
		    public Image getImage(Object element) {
				if (element instanceof IRemoteDirectoryContent.IFileContent file) {
					if (file.isDatabase())
						return imgFolderDb;
					else if (file.isDirectory())
						return imgFolderReg;					
				}
				return null;
			}
		});
		
		TableViewerColumn nameColViewer = new TableViewerColumn(viewer, SWT.NONE);
		nameColViewer.getColumn().setText("Contents");
		nameColViewer.getColumn().setWidth(420);
		
		nameColViewer.setLabelProvider(new ColumnLabelProvider() {
			
			@Override
		    public String getText(Object element) {
				if (element instanceof IRemoteDirectoryContent.IFileContent file)
					return file.getName();
				return null;
			}
		});
		
		viewer.setContentProvider(ArrayContentProvider.getInstance());
				
		var tableDir = viewer.getTable();
		tableDir.setHeaderVisible(true);
		
		GridDataFactory.fillDefaults().grab(true, true).hint(366, 446).applyTo(tableDir);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(tableDir);
		
		tableDir.addSelectionListener(new SelectionAdapter() {
			private boolean isDatabase(SelectionEvent e) {
        		StructuredSelection dirSelect = (StructuredSelection) viewer.getSelection();
        		if (dirSelect == null || dirSelect.isEmpty())
        			return false;
        		
        		var elem = dirSelect.toList().get(0);
        		return (elem instanceof IRemoteDirectoryContent.IFileContent file && 
        				file.isDatabase());
			}
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				var isValid = isDatabase(e);
				getButton(IDialogConstants.OK_ID).setEnabled(isValid);
			}
			
        	@Override
        	public void widgetDefaultSelected(SelectionEvent e) {
        		StructuredSelection dirSelect = (StructuredSelection) viewer.getSelection();
        		if (dirSelect == null || dirSelect.isEmpty())
        			return;
        		
        		var elem = dirSelect.toList().get(0);
        		if (elem instanceof IRemoteDirectoryContent.IFileContent file && 
        				(file.isDirectory() || file.isDatabase())) {
            		var absoluteDir = textDirectory.getText() + "/" + file.getName();
            		fillDirectory(absoluteDir);
        		}
        	}
		});
		return viewer;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
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
		
		StructuredSelection dirSelect = (StructuredSelection) directoryViewer.getSelection();
		// case for selection:
		// - if no selection, we use the directory at the textDirectory text
		// - if one is selected:
		//   - check if it's a directory. 
		//     - if it isn't, do not use it
		//     - if it's a directory, append it with the base directory
		if (dirSelect != null && !dirSelect.isEmpty()) {
			var selectedElem = dirSelect.getFirstElement();
			if (selectedElem instanceof IRemoteDirectoryContent.IFileContent dir && dir.isDatabase()) {
				selectedDirectory += "/" + dir.getName();
				super.okPressed();
				return; // do we need this?
			}
		}
		if (isDatabaseDirectory()) {
			super.okPressed();
			return;
		}
			
		MessageDialog.openError(getShell(), "Not a database", selectedDirectory + ": is not a database directory");
	}
	
	
	private boolean isDatabaseDirectory() {
		var inputs = directoryViewer.getInput();
		if (inputs == null)
			return false;
		
		int numFiledb = 0;
		IFileContent []contents = (IFileContent[]) inputs;
		
		for(var content: contents) {
			var name = content.getName();
			var fileDb = name.equals("meta.db") || name.equals("cct.db") ||  name.equals("profile.db");
			if (fileDb)
				numFiledb++;
			if (numFiledb >= 3)
				return true;
		}
		return false;
	}
	
	@Override
	protected boolean isResizable() {
	    return true;
	}
	
	
	private void fillDirectory(String directory) {
		try {
			var content = remoteBrowser.getContentRemoteDirectory(directory);
			if (content != null) {
				textDirectory.setText(content.getDirectory());
				
				var files = content.getContent();				
				directoryViewer.setInput(files);
			}
		} catch (IOException e1) {
			MessageDialog.openError(getShell(), "Error accessomg the remote directory", e1.getMessage());
		}
	}
}
