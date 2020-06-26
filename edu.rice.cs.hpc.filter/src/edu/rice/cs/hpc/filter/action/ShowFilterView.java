package edu.rice.cs.hpc.filter.action;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpc.filter.view.FilterPropertyDialog;

public class ShowFilterView 
{
	final public static String ID = "edu.rice.cs.hpc.filter.action.ShowFilterView";

	@Execute
	public Object execute( @Named(IServiceConstants.ACTIVE_SHELL) Shell shell, IEventBroker eventBroker) 
	{
		FilterPropertyDialog dialog = new FilterPropertyDialog(shell);
		
		if (dialog.open() == IDialogConstants.OK_ID) {
			
		}

		return null;
	}
}
