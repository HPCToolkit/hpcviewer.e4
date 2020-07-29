package edu.rice.cs.hpctraceviewer.ui;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpcbase.ui.IBaseItem;

public interface ITraceItem extends IBaseItem 
{
	public void createContent(ITracePart parentPart, IEclipseContext context, IEventBroker broker, Composite parentComposite);

}
