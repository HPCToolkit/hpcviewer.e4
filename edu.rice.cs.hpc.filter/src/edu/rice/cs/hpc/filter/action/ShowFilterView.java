package edu.rice.cs.hpc.filter.action;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpc.filter.service.FilterStateProvider;
import edu.rice.cs.hpc.filter.view.FilterPropertyDialog;

/*******************************************************
 * Class to handle show filter menu
 * The link to this class is hard-coded in fragment.e4xmi
 * Do not change the name
 *******************************************************/
public class ShowFilterView 
{
	final public static String ID = "edu.rice.cs.hpc.filter.action.ShowFilterView";
	
	@Inject FilterStateProvider filterService;

	@Execute
	public Object execute( @Named(IServiceConstants.ACTIVE_SHELL) Shell shell, IEventBroker eventBroker) 
	{
		FilterPropertyDialog.show(shell, filterService);

		return null;
	}
}
