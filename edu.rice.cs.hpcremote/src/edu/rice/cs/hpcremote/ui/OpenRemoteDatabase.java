 
package edu.rice.cs.hpcremote.ui;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.hpctoolkit.hpcclient.v1_0.HpcClient;

import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.version4.DataMeta;
import edu.rice.cs.hpcdata.db.version4.IDataProfile;
import edu.rice.cs.hpcdata.experiment.Experiment;

import edu.rice.cs.hpcremote.data.RemoteDataProfile;
import io.vavr.collection.Set;

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
			var dataMeta = collectMetaData(client);
			var dataProfile = collectProfileData(client, dataMeta);
			
			var metrics  = client.getMetricsDefaultYamlContents();
			
			Experiment experiment = (Experiment) dataMeta.getExperiment();
			
			var str = String.format("Connection info: %s%nName: %s%nmeta.db load-modules: %d%nId-tuples size: %d%nmetrics length: %d%n",
					remoteInfo.getId(),
					experiment.getName(),
					dataMeta.getNumLoadModules(),
					dataProfile.getIdTuple().size(),
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
	
	
	private DataMeta collectMetaData(HpcClient client) throws IOException, InterruptedException {
		var metaDBbytes   = client.getMetaDbFileContents();
		ByteBuffer buffer = ByteBuffer.wrap(metaDBbytes);
		
		DataMeta dataMeta = new DataMeta();
		dataMeta.open(buffer);
		
		return dataMeta;
	}
	
	
	private IDataProfile collectProfileData(HpcClient client, DataMeta dataMeta) throws IOException, InterruptedException {
		Set<IdTuple> idtuples = client.getHierarchicalIdentifierTuples();
		List<IdTuple> list = new ArrayList<>(idtuples.size());
		idtuples.forEach(list::add);
		
		var dataProfile = new RemoteDataProfile(client, dataMeta.getExperiment().getIdTupleType());
		dataProfile.setIdTuple(list);
		
		return dataProfile;
	}
}