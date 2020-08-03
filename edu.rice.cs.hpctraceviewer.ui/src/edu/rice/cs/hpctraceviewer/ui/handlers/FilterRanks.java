 
package edu.rice.cs.hpctraceviewer.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpc.data.experiment.extdata.IFilteredData;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;
import edu.rice.cs.hpctraceviewer.ui.dialog.FilterDialog;
import edu.rice.cs.hpctraceviewer.ui.internal.TraceEventData;
import edu.rice.cs.hpctraceviewer.ui.util.IConstants;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;

public class FilterRanks 
{
	@Execute
	public void execute( MPart part, 
						 @Named(IServiceConstants.ACTIVE_SHELL) Shell shell, 
						 IEventBroker eventBroker) {
		
		Object obj = part.getObject();		
		ITracePart tracePart = (ITracePart) obj;
		SpaceTimeDataController data = (SpaceTimeDataController) tracePart.getInput();
		
		/*
		 * This isn't the prettiest, but when we are local, we don't want to set
		 * it to filtered unless we have to (i.e. unless the user actually
		 * applies a filter). If the data is already filtered, we don't care and
		 * we just return the filtered data we have been using (which makes the
		 * call to set it redundant). If it's not, we wait to replace the
		 * current filter with the new filter until we know we have to.
		 */
        IFilteredData filteredBaseData = data.createFilteredBaseData();
        if (filteredBaseData == null) {
        	filteredBaseData = data.createFilteredBaseData();
        }
        
        FilterDialog dlgFilter = new FilterDialog(shell, filteredBaseData);
		
		if (dlgFilter.open() == Dialog.OK){
			data.setBaseData(filteredBaseData);
			TraceEventData eventData = new TraceEventData(data, tracePart, filteredBaseData);
			eventBroker.post(IConstants.TOPIC_FILTER_RANKS, eventData);
		}
	}
	
	
	@CanExecute
	public boolean canExecute(MPart part) {
		
		Object obj = part.getObject();
		if (!(obj instanceof ITracePart))
			return false;

		return ((ITracePart)obj).getInput() != null;
	}
		
}