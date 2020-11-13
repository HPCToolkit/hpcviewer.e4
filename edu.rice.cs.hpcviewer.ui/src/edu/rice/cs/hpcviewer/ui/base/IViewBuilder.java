package edu.rice.cs.hpcviewer.ui.base;

import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpcviewer.ui.ProfilePart;
import edu.rice.cs.hpcviewer.ui.internal.ScopeTreeViewer;

public interface IViewBuilder 
{
	public void createContent(ProfilePart profilePart, Composite parent, EMenuService menuService);
	public void setData(RootScope root);
	public void dispose();
	
	public ScopeTreeViewer getTreeViewer();
}
