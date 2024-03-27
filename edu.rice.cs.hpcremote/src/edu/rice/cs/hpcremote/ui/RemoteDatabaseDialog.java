package edu.rice.cs.hpcremote.ui;

import java.io.IOException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
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

		var descFolderDb = ImageDescriptor.createFromFile(getClass(), "folder-16-blue.png");
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
				if (element instanceof String) {
					String text = (String) element;
					if (isDirectory(text)) {
						return imgFolderReg;
					}
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
				if (element instanceof String)
					return (String) element;
				return null;
			}
		});
		
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		
		GridData gdListDirectory = new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1);
		gdListDirectory.heightHint = 362;
		gdListDirectory.widthHint = 440;
		
		var tableDir = viewer.getTable();
		tableDir.setLayoutData(gdListDirectory);
		tableDir.setHeaderVisible(true);
		
		tableDir.addSelectionListener(new SelectionAdapter() {
        	
        	@Override
        	public void widgetDefaultSelected(SelectionEvent e) {
        		StructuredSelection dirSelect = (StructuredSelection) viewer.getSelection();
        		if (dirSelect == null || dirSelect.isEmpty())
        			return;
        		
        		var elem = dirSelect.toList().get(0);
        		if (elem == null)
        			return;
        		if (!isDirectory((String) elem))
        			return;
        		
    			var absoluteDir = textDirectory.getText() + "/" + elem;
    			fillDirectory(absoluteDir);
        	}
		});
		return viewer;
	}

	/***
	 * Check if it's a directory name
	 * 
	 * @param name
	 * @return
	 */
	private boolean isDirectory(String name) {
		return name.endsWith("/");
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
		
		StructuredSelection dirSelect = (StructuredSelection) directoryViewer.getSelection();
		// case for selection:
		// - if no selection, we use the directory at the textDirectory text
		// - if one is selected:
		//   - check if it's a directory. 
		//     - if it isn't, do not use it
		//     - if it's a directory, append it with the base directory
		if (dirSelect != null && !dirSelect.isEmpty()) {
			String selectedElem = (String) dirSelect.getFirstElement();
			if (isDirectory(selectedElem))
				selectedDirectory += "/" + selectedElem;
		}		
		super.okPressed();
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
