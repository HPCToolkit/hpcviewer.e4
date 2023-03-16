package edu.rice.cs.hpcviewer.ui.internal;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;

import edu.rice.cs.hpcmetric.IFilterable;
import edu.rice.cs.hpcviewer.ui.base.IViewItem;

public abstract class AbstractView extends CTabItem implements IViewItem, IFilterable 
{
	protected AbstractView(CTabFolder parent, int style) {
		super(parent, style);
	}
}
