package edu.rice.cs.hpctraceviewer.ui;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import edu.rice.cs.hpcbase.ui.IBaseItem;

public abstract class AbstractBaseItem extends CTabItem implements IBaseItem {

	public AbstractBaseItem(CTabFolder parent, int style) {
		super(parent, style);
	}
}
