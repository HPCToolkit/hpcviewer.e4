 
package edu.rice.cs.hpctraceviewer.ui.handlers;

import java.util.List;

import javax.inject.Named;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;
import edu.rice.cs.hpctraceviewer.ui.internal.TraceEventData;
import edu.rice.cs.hpctraceviewer.ui.util.IConstants;

public class FilterEmptyTraces 
{
	@Execute
	public void execute( MPart part, 
			 @Named(IServiceConstants.ACTIVE_SHELL) Shell shell, 
			 IEventBroker eventBroker) {
		
		Object obj = part.getObject();
		if (!(obj instanceof ITracePart))
			return;
		
		IInputValidator validator = newText -> {
			try {
				int value = Integer.parseInt(newText);
				if (value < 0)
					return "The value cannot be negative";
				return null;
			} catch (NumberFormatException e) {
				return "Invalid number";
			}
		};
		
		InputDialog inputDlg = new InputDialog(shell, "Exclude trace lines", "Please enter the minimum number of trace samples", "3", validator);
		if (inputDlg.open() == Window.CANCEL)
			return;
		
		var strValue = inputDlg.getValue();
		var intValue = Integer.parseInt(strValue);
		
		ITracePart tracePart = (ITracePart) obj;
		SpaceTimeDataController data = tracePart.getDataController();
		
		var traceData  = data.getBaseData();
		var listProfiles = traceData.getListOfIdTuples(IdTupleOption.BRIEF);
		var listOriginalProfiles = traceData.getDenseListIdTuple(IdTupleOption.BRIEF);
		var mapSamples = traceData.getMapFromExecutionContextToNumberOfTraces();
		
		List<Integer> indexesInclude = FastList.newList();
		int excludeTraces = 0;
		
		for(int i=0; i<listOriginalProfiles.size(); i++) {
			var idt = listOriginalProfiles.get(i);
			var samples = mapSamples.get(idt).intValue();
			if (samples >= intValue) {
				indexesInclude.add(i);
			} else {
				excludeTraces++;
			}
		}
		final var title = "Filtering empty traces";
		
		if (indexesInclude.isEmpty()) {			
			MessageDialog.openWarning(
					shell, 
					title, 
					"Warning: all traces have no sample.");
			return;
		} else if (indexesInclude.size() == listProfiles.size()) {
			var toContinue = MessageDialog.openQuestion(
					shell, 
					title, 
					"All filtered traces have samples, the operation is useless.\nDo you still want to continue?");
			
			if (!toContinue)
				return;
		}
		traceData.setIncludeIndex(indexesInclude);
		
		// hack: to update the process interval based on the new filtered ranks
		data.setBaseData(traceData);

		TraceEventData eventData = new TraceEventData(data, tracePart, traceData);
		eventBroker.post(IConstants.TOPIC_FILTER_RANKS, eventData);
		
		tracePart.showInfo(excludeTraces + " trace(s) have been excluded.");
	}
}