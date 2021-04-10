package edu.rice.cs.hpcviewer.ui.internal;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;

import edu.rice.cs.hpcviewer.ui.base.IViewItem;

public abstract class AbstractBaseViewItem extends CTabItem implements IViewItem {

	public AbstractBaseViewItem(CTabFolder parent, int style) {
		super(parent, style);
	}

}
