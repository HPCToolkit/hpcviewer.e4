package edu.rice.cs.hpcviewer.ui.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.ToolBar;

import edu.rice.cs.hpcviewer.ui.resources.IconManager;

public class FlatContentViewer extends BaseContentViewer {

	@Override
	protected void beginToolbar(CoolBar coolbar, ToolBar toolbar) {

		createToolItem(toolbar, IconManager.Image_Flatten, 
				"Flatten nodes one level");
		createToolItem(toolbar, IconManager.Image_Unflatten, 
				"Unflatten nodes one level");
		createToolItem(toolbar, SWT.SEPARATOR, "", "");
	}

	@Override
	protected void endToolbar(CoolBar coolbar, ToolBar toolbar) {}

}
