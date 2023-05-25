package edu.rice.cs.hpcbase.ui;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;

public abstract class AbstractUpperPart extends CTabItem implements IUpperPart {

	protected AbstractUpperPart(CTabFolder parent, int style) {
		super(parent, style);
	}

}
