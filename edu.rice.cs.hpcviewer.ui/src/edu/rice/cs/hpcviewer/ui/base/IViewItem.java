package edu.rice.cs.hpcviewer.ui.base;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpcviewer.ui.ProfilePart;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;

public interface IViewItem 
{
	public void setService(EPartService partService, 
			IEventBroker broker,
			DatabaseCollection database,
			ProfilePart   profilePart,
			EMenuService  menuService);
	
	public void createContent(Composite parent);
	
	public void setInput(Object input);
	
	public Object getInput();
}
