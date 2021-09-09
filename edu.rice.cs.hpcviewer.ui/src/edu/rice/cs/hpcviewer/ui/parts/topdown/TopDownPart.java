package edu.rice.cs.hpcviewer.ui.parts.topdown;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.ToolBar;

import edu.rice.cs.hpcviewer.ui.internal.AbstractTableView;

public class TopDownPart extends AbstractTableView 
{	
	private static final String TITLE = "Top-down view";
	
	public TopDownPart(CTabFolder parent, int style) {
		super(parent, style, TITLE);
	}
	

	@Override
    protected void beginToolbar(CoolBar coolbar, ToolBar toolbar) {}
	
	@Override
    protected void endToolbar  (CoolBar coolbar, ToolBar toolbar) {}


	protected void updateStatus() {
		
	}
}
