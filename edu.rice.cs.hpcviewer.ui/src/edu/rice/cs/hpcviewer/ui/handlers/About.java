 
package edu.rice.cs.hpcviewer.ui.handlers;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcviewer.ui.resources.IconManager;


/****
 * 
 * Class to show the About window.
 * Called only as a menu handler. Otherwise it doesn' work.
 *
 */
public class About 
{
	
	@Execute
	public void execute(@Active Shell shell) {
		
		AboutDialog dialog = new AboutDialog(shell, "About hpcviewer", 
				"hpcviewer is a user interface for analyzing a database of performance metrics in conjunction with an application's source code.\n" + 
				"\n" + 
				"hpcviewer is part of Rice University's HPCToolkit project. Development of HPCToolkit is principally funded by the Department of Energy's Office of Science, Lawrence Livermore National Laboratory and National Science Foundation.\n" + 
				"\n" + 
				"Release 2020.06  (C) Copyright 2020, Rice University.");
		
		dialog.open();
	}
	
	
	/****
	 * 
	 * Main window to show the about dialog
	 *
	 */
	static class AboutDialog extends IconAndMessageDialog
	{
		private final String title;
		
		public AboutDialog(Shell parentShell, String title, String message) {
			super(parentShell);
			
			this.message = message;
			this.title   = title;
		}

		@Override
		public Image getImage() {
			ImageRegistry registry = JFaceResources.getImageRegistry();
			Image image = registry.get(IconManager.Image_Viewer_64);
			
			if (image != null)
				return image;
			
			try {
				URL url = FileLocator.toFileURL(new URL(IconManager.Image_Viewer_64));
				image = new Image(Display.getDefault(), url.getFile());
				registry.put(IconManager.Image_Viewer_64, image);
				
				return image;
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		}
		
		@Override
		protected Control createDialogArea(Composite parent) {
			// create message area
			createMessageArea(parent);
			// create the top level composite for the dialog area
			Composite composite = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			composite.setLayout(layout);
			GridData data = new GridData(GridData.FILL_BOTH);
			data.horizontalSpan = 2;
			composite.setLayoutData(data);

			return composite;
		}
		

		@Override
		protected void configureShell(Shell shell) {
			super.configureShell(shell);
			if (title != null) {
				shell.setText(title);
			}
		}

	}
}