 
package edu.rice.cs.hpcremote.ui;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcdata.db.version4.DataMeta;
import edu.rice.cs.hpcdata.experiment.Experiment;

public class OpenRemoteDatabase {
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		
		var dialog = new ConnectionDialog(shell);
		
		if (dialog.open() == Window.CANCEL)
			return;
		
		var remoteInfo = dialog.getRemoteInfo();
		var client = remoteInfo.getClient();
		if (client == null)
			return;
		
		try {
			var metaDBbytes   = client.getMetaDbFileContents();
			ByteBuffer buffer = ByteBuffer.wrap(metaDBbytes);
			
			DataMeta dataMeta = new DataMeta();
			dataMeta.open(buffer);
			
			var idtuples = client.getHierarchicalIdentifierTuples();
			var metrics  = client.getMetricsDefaultYamlContents();
			
			Experiment experiment = (Experiment) dataMeta.getExperiment();
			
			var str = String.format("Connection info: %s%nName: %s%nmeta.db length: %d%nId-tuples size: %d%nmetrics length: %d%n",
					remoteInfo.getId(),
					experiment.getName(),
					metaDBbytes.length,
					idtuples.size(),
					metrics.length);
			
			MessageDialog.openInformation(shell, "Connection succeeds", str);

		} catch (IOException | NumberFormatException | InterruptedException e) {
			MessageDialog.openError(shell, 
					"Error connecting", 
					"Error message: " + e.getLocalizedMessage());
		} catch (Exception e) {
			MessageDialog.openError(shell, "Unknown error", e.getMessage());
		}
		
	}
		
}