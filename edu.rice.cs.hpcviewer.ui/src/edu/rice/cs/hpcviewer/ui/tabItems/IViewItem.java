package edu.rice.cs.hpcviewer.ui.tabItems;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpcviewer.ui.experiment.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.parts.ProfilePart;

public interface IViewItem 
{
	public void setService(EPartService partService, 
			IEventBroker broker,
			DatabaseCollection database,
			ProfilePart   profilePart,
			EMenuService  menuService);
	
	public void createContent(Composite parent);
	
	public void setInput(Object input);
}
