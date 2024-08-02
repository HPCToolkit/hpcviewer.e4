// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcremote.ui;

import java.io.File;
import java.io.IOException;
import java.nio.channels.NotYetConnectedException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;

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
import org.hpctoolkit.hpcclient.v1_0.DirectoryContentsNotAvailableException;
import org.hpctoolkit.hpcclient.v1_0.RemoteDirectory;
import org.hpctoolkit.hpcclient.v1_0.RemotePath;
import org.slf4j.LoggerFactory;
import org.eclipse.swt.layout.GridLayout;
import edu.rice.cs.hpcremote.data.IRemoteDirectoryBrowser;


/**********************************************************
 * 
 * Dialog box to browse at the remote host.
 * 
 * Once the user click the button {@code OK}, the caller needs
 * to grab the value of the remote directory by calling
 * {@code getSelectedDirectory()} as follows:
 * <pre>
 *  if (dialog.open() == Window.OK)
 *     directory = dialog.getSelectedDirectory();
 * </pre>
 * 
 **********************************************************/
public class RemoteDatabaseDialog extends TitleAreaDialog 
{
	private final IRemoteDirectoryBrowser remoteBrowser;
	private final Image imgFolderReg;
	private final Image imgFolderDb;
	
	private RemoteDirectoryCombo textDirectory;
	private TableViewer directoryViewer;

	private String selectedDirectory;
	private RemoteDirectory currentDirectory;

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

	
	@Override
	protected Control createContents(Composite parent) {
		// make sure we have done all the content before populate the table
		var control = super.createContents(parent);
		
		// ask the content of the default remote directory.
		// usually it's the home directory, but the server can give anything
		fillDirectory("");
		
		return control;
	}
	
	
	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Remote database browser");
		setTitle("Browsing " + remoteBrowser.getRemoteHostname() + " (" + remoteBrowser.getRemoteHost() + ")");
		setMessage("Select a HPCToolkit database directory");
		
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		textDirectory = new RemoteDirectoryCombo(container, remoteBrowser.getRemoteHostname(), this::fillDirectory);

		directoryViewer = createTableDirectory(container);
		
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
				if (element instanceof RemotePath path) { 
					if (path.isHpcToolkitDbDirectory())
						return imgFolderDb;
					else if (path.isDirectory())
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
				if (element instanceof RemotePath file) {
					String name = file.getName();
					if (file.isDirectory())
						name += "/";
					return name;
				}
				return null;
			}
		});
		
		viewer.setContentProvider(ArrayContentProvider.getInstance());
				
		var tableDir = viewer.getTable();
		tableDir.setHeaderVisible(true);
		
		GridDataFactory.fillDefaults().grab(true, true).hint(366, 446).applyTo(tableDir);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(tableDir);
		
		tableDir.addSelectionListener(new SelectionAdapter() {
			private boolean isSelectedItemADatabase() {
        		StructuredSelection dirSelect = (StructuredSelection) viewer.getSelection();
        		if (dirSelect == null || dirSelect.isEmpty()) {
        			return false;
        		}
        		
        		var elem = dirSelect.toList().get(0);
        		return elem instanceof RemotePath dir && dir.isHpcToolkitDbDirectory();
			}
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				var isADatabase = isSelectedItemADatabase();
				getButton(IDialogConstants.OK_ID).setEnabled(isADatabase);
			}
			
        	@Override
        	public void widgetDefaultSelected(SelectionEvent e) {
        		StructuredSelection dirSelect = (StructuredSelection) viewer.getSelection();
        		if (dirSelect == null || dirSelect.isEmpty())
        			return;
        		
        		var elem = dirSelect.toList().get(0);
        		if ( elem instanceof RemotePath dir && dir.isDirectory() ) {
            		String absoluteDir = dir.getAbsolutePathOnRemoteFilesystem();
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
			if (selectedElem instanceof RemotePath dir && dir.isHpcToolkitDbDirectory()) {
				selectedDirectory = dir.getAbsolutePathOnRemoteFilesystem();
				super.okPressed();
				return; // do we need this?
			}
		}
		if (currentDirectory.isHpcToolkitDbDirectory()) {
			super.okPressed();
			return;
		}
		// shouldn't happen here, or we may have a bug
		LoggerFactory.getLogger(getClass()).debug("{}: Not a database", selectedDirectory);
		
		MessageDialog.openError(getShell(), "Not a database", selectedDirectory + ": is not a database directory");
	}
	
	
	@Override
	protected boolean isResizable() {
	    return true;
	}
	
	
	private boolean fillDirectory(String directory) {
		Shell shell = getShell() != null ? getShell() : getParentShell();

		try {
			var content = remoteBrowser.getContentRemoteDirectory(directory);
			if (content == null || content.getChildren() == null || content.getChildren().size() == 0) {
				MessageDialog.openWarning(
						shell, 
						"Empty remote directory", 
						directory + ": the directory is empty.\nPlease type a new directory or choose another remote host.");
				return false;
			}
			var parent = content.getAbsolutePathOnRemoteFilesystem();
			textDirectory.setText(parent);
			
			var files = content.getChildren();
			RemotePath []paths = new RemotePath[files.size() + 1];
			
			int i=1;
			var iterator = files.iterator();
			
			File dotDot = new File(parent, "..");
			String strDotDot = dotDot.getAbsolutePath();			
			var grandParent = new File(parent).getParent();
			
			paths[0] = RemotePath.make(strDotDot, grandParent, "directory");
			
			while(iterator.hasNext()) {
				paths[i] = iterator.next();
				i++;
			}
			Arrays.sort(paths, new Comparator<RemotePath>() {
				Function<RemotePath, String> getName = (RemotePath path) -> {
					var prefix = path.isDirectory() ? "d." : "f.";
					return prefix + path.toString();
				};

				@Override
				public int compare(RemotePath path1, RemotePath path2) {
					var name1 = getName.apply(path1);
					var name2 = getName.apply(path2);
					
					return name1.compareTo(name2);
				}
			});

			directoryViewer.setInput(paths);
			
			var isDatabase = content.isHpcToolkitDbDirectory();
			getButton(IDialogConstants.OK_ID).setEnabled(isDatabase);

			this.currentDirectory = content;
			
			return true;
				
		} catch (IOException | DirectoryContentsNotAvailableException e) {
			LoggerFactory.getLogger(getClass()).error(directory, e);
			MessageDialog.openError(shell, "Error accessing the remote directory " + directory, e.getMessage());
		} catch (InterruptedException e) {
			LoggerFactory.getLogger(getClass()).error(directory + " is interrupted", e);
		    /* Clean up whatever needs to be handled before interrupting  */
		    Thread.currentThread().interrupt();		
		} catch (NotYetConnectedException e) {
			MessageDialog.openError(shell, "Remote host not connected", e.getMessage());
		}
		return false;
	}
}
