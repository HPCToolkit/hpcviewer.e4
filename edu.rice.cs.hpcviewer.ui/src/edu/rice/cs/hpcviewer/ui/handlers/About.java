 
package edu.rice.cs.hpcviewer.ui.handlers;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceManager;
import edu.rice.cs.hpcviewer.ui.resources.IconManager;
import edu.rice.cs.hpcviewer.ui.util.ApplicationProperty;


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
		
		AboutDialog dialog = new AboutDialog(shell);
		
		dialog.open();
	}
	
	
	/****
	 * 
	 * Main window to show the about dialog
	 *
	 */
	static class AboutDialog extends IconAndMessageDialog
	{
		// constants copied from org.eclipse.ui.branding.IProductConstants.java
		// we cannot directly import this interface because they are part of 
		// org.eclipse.ui bundle which will force us to use compatibility layer. 
		
		private static final String APP_NAME   = "appName";   //$NON-NLS-1$
		private static final String ABOUT_TEXT = "aboutText"; //$NON-NLS-1$
		
		
		public AboutDialog(Shell parentShell) {
			super(parentShell);

			IProduct product = Platform.getProduct();
			this.message     = product.getProperty(ABOUT_TEXT);
			
			try {
				this.message += "\n\nRelease: " + ApplicationProperty.getVersion();
			} catch (IOException e) {

				e.printStackTrace();
			}
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
			createButton(parent, IDialogConstants.DETAILS_ID, "License", false);
			createButton(parent, IDialogConstants.HELP_ID, "Info", false);
		}
		
		
		@Override
		protected void buttonPressed(int buttonId) {
			if (buttonId == IDialogConstants.DETAILS_ID) {
				showLicense();
			} else if (buttonId == IDialogConstants.HELP_ID) {
				Shell shell = new Shell(getShell());
				shell.setText("Info");
				shell.setLayout(new FillLayout());
				
				Browser text = new Browser(shell, SWT.MULTI);
				text.setText(getText());
				shell.open();
			}
			super.buttonPressed(buttonId);
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

			IProduct product = Platform.getProduct();
			String title     = product.getProperty(APP_NAME);

			if (title != null) {
				shell.setText(title);
			}
		}

		
		private String getText() {
			String message = "<pre>no info</pre>";
			try {
				String location = ViewerPreferenceManager.INSTANCE.getPreferenceStoreLocation();
				String locInstall = Platform.getInstallLocation().getURL().getFile();
				String locInstance = Platform.getInstanceLocation().getURL().getFile();
				String locUser = Platform.getLogFileLocation().toOSString(); //.getUserLocation().getURL().getFile();
				
				message = "<pre>" +
								 "Preference file: "    + location    + "\n" +
								 "Install directory: "  + locInstall  + "\n" +
								 "Instance directory: " + locInstance + "\n" +
								 "Log file location: "  + locUser     + "\n" +
								 "</pre>";
			} catch(Exception e) {
				e.printStackTrace();
			}

			return message;
		}

		private void showLicense() {
			try {				
				String license = ApplicationProperty.getLicense();								
				MessageDialog.openInformation(getShell(), "License", license);
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}
}