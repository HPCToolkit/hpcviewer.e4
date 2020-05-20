package edu.rice.cs.hpcviewer.ui.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.ToolBar;

import edu.rice.cs.hpcviewer.ui.resources.IconManager;

public class TopDownContentViewer extends BaseContentViewer 
{

	@Override
	protected void beginToolbar(CoolBar coolbar, ToolBar toolbar) {}

	@Override
	protected void endToolbar(CoolBar coolbar, ToolBar toolbar) {
		
		createToolItem(toolbar, SWT.SEPARATOR, "", "");
		createToolItem(toolbar, SWT.DROP_DOWN, IconManager.Image_Graph, 
				"Show the graph of metric values of the selected CCT node for all processes/threads");
		createToolItem(toolbar, IconManager.Image_ThreadView, 
				"Show the metric(s) of a group of threads");
	}

}
