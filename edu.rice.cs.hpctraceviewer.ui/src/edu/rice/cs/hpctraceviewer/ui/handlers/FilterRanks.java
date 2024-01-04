 
package edu.rice.cs.hpctraceviewer.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.filter.ITraceFilter;
import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;
import edu.rice.cs.hpctraceviewer.ui.internal.TraceEventData;
import edu.rice.cs.hpctraceviewer.ui.util.IConstants;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;

/*****
 * 
 * Filter ranks or threads.
 * This include excluding and including some ranks or filters
 *
 */
public class FilterRanks 
{
	@Execute
	public void execute( MPart part, 
						 @Named(IServiceConstants.ACTIVE_SHELL) Shell shell, 
						 IEventBroker eventBroker) {
		
		Object obj = part.getObject();		
		ITracePart tracePart = (ITracePart) obj;
		SpaceTimeDataController data = tracePart.getDataController();

		var filteredBaseData = ITraceFilter.filter(shell, data);
		
		if (filteredBaseData != null){
			TraceEventData eventData = new TraceEventData(data, tracePart, filteredBaseData);
			eventBroker.post(IConstants.TOPIC_FILTER_RANKS, eventData);
		}
	}
	
	
	@CanExecute
	@Inject
	public boolean canExecute(@Optional @Named(IServiceConstants.ACTIVE_PART)MPart part) {
		
		if (part == null)
			return false;
		
		Object obj = part.getObject();
		if (!(obj instanceof ITracePart))
			return false;

		return ((ITracePart)obj).getInput() != null;
	}
}