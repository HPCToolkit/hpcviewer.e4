 
package edu.rice.cs.hpctraceviewer.ui.handlers;

import java.util.List;

import javax.inject.Named;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;
import edu.rice.cs.hpctraceviewer.ui.internal.TraceEventData;
import edu.rice.cs.hpctraceviewer.ui.util.IConstants;

public class FilterEmptyTraces {
	@Execute
	public void execute( MPart part, 
			 @Named(IServiceConstants.ACTIVE_SHELL) Shell shell, 
			 IEventBroker eventBroker) {
		
		Object obj = part.getObject();		
		ITracePart tracePart = (ITracePart) obj;
		SpaceTimeDataController data = tracePart.getDataController();
		
		var traceData  = data.getBaseData();
		var listProfiles = traceData.getDenseListIdTuple(IdTupleOption.BRIEF);
		var mapSamples = traceData.getMapFromExecutionContextToNumberOfTraces();
		List<Integer> indexesInclude = FastList.newList();
		
		for(int i=0; i<listProfiles.size(); i++) {
			var idt = listProfiles.get(i);
			var samples = mapSamples.get(idt).intValue();
			if (samples >= 3) {
				indexesInclude.add(i);
			}
		}
		traceData.setIncludeIndex(indexesInclude);
		
		// hack: to update the process interval based on the new filtered ranks
		data.setBaseData(traceData);

		TraceEventData eventData = new TraceEventData(data, tracePart, traceData);
		eventBroker.post(IConstants.TOPIC_FILTER_RANKS, eventData);
	}
}