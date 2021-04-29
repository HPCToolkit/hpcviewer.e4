package edu.rice.cs.hpcfilter.action;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcfilter.service.FilterStateProvider;
import edu.rice.cs.hpcfilter.view.FilterPropertyDialog;

public class ShowFilterView 
{
	final public static String ID = "edu.rice.cs.hpc.filter.action.ShowFilterView";
	
	@Inject FilterStateProvider filterService;

	@Execute
	public Object execute( @Named(IServiceConstants.ACTIVE_SHELL) Shell shell, 
							IEventBroker eventBroker,
							MApplication application) 
	{
		Map<String, Object> map = application.getTransientData();
		Object obj = map.get(ID);
		
		// if filter panel is already shown, even in other window, we should
		// bring it to the top.
		// we only allow one filter per application (or session)
		
		if (obj != null && (obj instanceof FilterPropertyDialog)) {
			FilterPropertyDialog dlg = (FilterPropertyDialog) obj;
			if (!dlg.getShell().isDisposed()) {
				dlg.getShell().setActive();
				return null;
			}
		}
		FilterPropertyDialog dialog = new FilterPropertyDialog(shell, filterService);
		map.put(ID, dialog);
		
		dialog.open();
		
		return null;
	}
}
