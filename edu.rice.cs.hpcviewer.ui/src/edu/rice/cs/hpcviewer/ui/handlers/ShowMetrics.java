 
package edu.rice.cs.hpcviewer.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcviewer.ui.experiment.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.metric.MetricPropertyDialog;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.CanExecute;

public class ShowMetrics 
{
	@Inject DatabaseCollection database;
	
	@Execute
	public void execute(@Active Shell shell) {
					
		MetricPropertyDialog dialog = new MetricPropertyDialog(shell);
		dialog.setDatabase(database);
		dialog.open();
	}
	
	
	@CanExecute
	public boolean canExecute() {
		
		return database.getNumDatabase()>0;
	}
		
}