package edu.rice.cs.hpctraceviewer.ui;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpcbase.ui.IBaseItem;
import edu.rice.cs.hpcbase.ui.IMainPart;

public class AbstractBaseItem extends CTabItem implements IBaseItem {

	public AbstractBaseItem(CTabFolder parent, int style) {
		super(parent, style);
	}


	@Override
	public void createContent(IMainPart parentPart, IEclipseContext context, Composite parentComposite) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setInput(Object input) {
		// TODO Auto-generated method stub

	}

}
