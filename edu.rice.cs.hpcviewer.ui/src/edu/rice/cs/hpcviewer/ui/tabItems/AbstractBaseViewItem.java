package edu.rice.cs.hpcviewer.ui.tabItems;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;

public abstract class AbstractBaseViewItem extends CTabItem implements IViewItem {

	public AbstractBaseViewItem(CTabFolder parent, int style) {
		super(parent, style);
	}

}
