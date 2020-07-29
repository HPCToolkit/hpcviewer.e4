package edu.rice.cs.hpctraceviewer.ui;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;

public abstract class AbstractBaseItem extends CTabItem implements ITraceItem {

	public AbstractBaseItem(CTabFolder parent, int style) {
		super(parent, style);
	}
}
