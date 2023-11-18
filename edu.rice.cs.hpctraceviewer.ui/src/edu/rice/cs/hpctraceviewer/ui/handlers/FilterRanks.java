 
package edu.rice.cs.hpctraceviewer.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcbase.IFilteredData;
import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.filter.TraceFilterDialog;
import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;
import edu.rice.cs.hpctraceviewer.ui.internal.TraceEventData;
import edu.rice.cs.hpctraceviewer.ui.util.IConstants;

import java.util.List;

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

		var dialog = new TraceFilterDialog(shell, data.getBaseData());
		if (dialog.open() == Window.CANCEL)
			return;
        
		List<Integer> listChecked = dialog.getCheckedIndexes();
		
		if (listChecked != null && !listChecked.isEmpty()){
			
			// update the data and broadcast to everyone that we probably have new filtered ranks
			// TODO: we need to check if the new one is the same with the old one or not.

			var filteredBaseData = data.getBaseData();
			filteredBaseData.setIncludeIndex(listChecked);
			
			// hack: to update the process interval based on the new filtered ranks
			data.setBaseData(filteredBaseData);

			TraceEventData eventData = new TraceEventData(data, tracePart, filteredBaseData);
			eventBroker.post(IConstants.TOPIC_FILTER_RANKS, eventData);
		}
	}
	
	
	protected String[] getListOfItems(IFilteredData filteredBaseData) {

        var mapToSamples = filteredBaseData.getMapFromExecutionContextToNumberOfTraces();
        
        List<IdTuple> listDenseIds = filteredBaseData.getDenseListIdTuple(IdTupleOption.BRIEF);
        List<IdTuple> listIds = filteredBaseData.getListOfIdTuples(IdTupleOption.BRIEF);
        
        String []items    = new String[listDenseIds.size()];
        boolean []checked = new boolean[listDenseIds.size()];

        // initialize the ranks to be displayed in the dialog box
        // to know which ranks are already shown, we need to compare with the filtered ranks.
        // if the id tuple in the original list is the same as the one in filtered list, 
        // them the rank is already displayed.
        // This list needs to be optimized.
        
        for(int i=0, j=0; i<listDenseIds.size(); i++) {
        	var idt = listDenseIds.get(i);
        	int numSamples = 0;
        	
        	if(mapToSamples != null) {
            	var samples = mapToSamples.get(idt);
            	if (samples != null)
            		numSamples = samples.intValue();
        	}
        	
        	items[i] = idt.toString(filteredBaseData.getIdTupleTypes()) + (numSamples > 0 ? " (" + numSamples + " samples)" : "");
        	
        	if (j<listIds.size() && listDenseIds.get(i) == listIds.get(j)) {
        		checked[i] = true;
        		j++;
        	} else {
        		checked[i] = false;
        	}
        }
        return items;
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