 
package edu.rice.cs.hpcremote.ui;

import java.io.IOException;
import java.util.Collections;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

public class OpenRemoteDatabase {
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		
		var dialog = new ConnectionDialog(shell);
		
		if (dialog.open() == Window.CANCEL)
			return;
		
		var connection = dialog.getClientConnection();
		if (connection.isEmpty())
			return;
		
		var client = connection.get();
		try {
			var metaDB = client.getMetaDbFileContents();
			var idtuples = Collections.emptyList(); /// client.getHierarchicalIdentifierTuples();
			var metrics  = client.getMetricsDefaultYamlContents();
			var str = String.format("meta.db length: %d%nId-tuples: %d%nmetrics length: %d%n", 
					metaDB.length,
					idtuples.size(),
					metrics.length);
			MessageDialog.openInformation(shell, "Connection succeeds", str);

		} catch (IOException | NumberFormatException | InterruptedException e) {
			MessageDialog.openError(shell, 
					"Error connecting", 
					"Error message: " + e.getLocalizedMessage());
		}
		
	}
		
}