package edu.rice.cs.hpcviewer.ui.parts;

import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpc.data.experiment.scope.RootScope;

public interface IContentViewer 
{
	public void createContent(Composite parent, EMenuService menuService);
	public void setData(RootScope root);
	public void dispose();
}
