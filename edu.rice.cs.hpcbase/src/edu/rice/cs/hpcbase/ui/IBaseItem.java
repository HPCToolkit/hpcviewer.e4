package edu.rice.cs.hpcbase.ui;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.swt.widgets.Composite;

public interface IBaseItem 
{
	public void createContent(IMainPart parentPart, IEclipseContext context, IEventBroker broker, Composite parentComposite);
	
	public void setInput(Object input);

}
