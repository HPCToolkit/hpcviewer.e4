package edu.rice.cs.hpcviewer.ui.base;

import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcviewer.ui.ProfilePart;
import edu.rice.cs.hpcviewer.ui.internal.ScopeTreeViewer;

public interface IViewBuilder 
{
	public void createContent(ProfilePart profilePart, Composite parent, EMenuService menuService);
	public void setData(RootScope root);
	public void setData(RootScope root, int sortColumnIndex, int sortDirection);

	public void dispose();
	
	public ScopeTreeViewer getTreeViewer();
}
