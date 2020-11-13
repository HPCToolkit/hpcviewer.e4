package edu.rice.cs.hpcviewer.ui.dialogs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.internal.runtime.Activator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import edu.rice.cs.hpclog.LogProperty;

public class InfoLogDialog extends Dialog 
{
	public InfoLogDialog(Shell shell) {
		super(shell);
	}

	
	@Override
	protected boolean isResizable() {
		return true;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite content = (Composite) super.createDialogArea(parent);

		// set the title
		String text = "Log files used by hpcviewer\n";

		List<String> logUser = LogProperty.getLogFile();

		// get the content for each log files
		for (String log: logUser) {
			text += "File: " + log + "\n";
			try {
				text += getFileContent(log);
			} catch (IOException e) {
				// do nothing
			}
		}
		text += "\n\n";
		
		try {
			Activator activator = Activator.getDefault();
			if (activator != null) {
				String locUser = Platform.getLogFileLocation().toOSString(); 
				text += "File: " + locUser + "\n";
				text += getFileContent(locUser);				
			}
		} catch (IOException e) {
			// do nothing
		}
		text += "\n";

		Text wText = new Text(content, SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
		wText.setText(text);
		
		content.setLayout(new FillLayout());

		return content;
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(600, 400);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Log files");
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}
	
	/***
	 * Read the content of the file
	 * @param filename
	 * @return String
	 * @throws IOException
	 */
	private String getFileContent(String filename) throws IOException {
		File file = new File(filename);
		if (!file.canRead())
			return "";
		
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		
		String content = new String(data, "UTF-8");				
		fis.close();
		
		return content;
	}
	
	static public void main(String argv[]) {
		Display d = new Display();
		InfoLogDialog dlg = new InfoLogDialog(new Shell(d));
		dlg.open();
	}
}
