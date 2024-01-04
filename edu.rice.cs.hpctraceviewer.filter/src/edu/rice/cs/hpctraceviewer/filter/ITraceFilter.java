package edu.rice.cs.hpctraceviewer.filter;

import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcbase.IFilteredData;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;

public interface ITraceFilter 
{
	IFilteredData filterTrace(SpaceTimeDataController stdc);

	static IFilteredData filter(final Shell shell, final SpaceTimeDataController stdc) {
		ITraceFilter traceFilter = data -> {
			var dialog = new TraceFilterDialog(shell, stdc.getBaseData());
			if (dialog.open() == Window.CANCEL)
				return null;
	        
			List<Integer> listChecked = dialog.getCheckedIndexes();
			
			if (listChecked != null && !listChecked.isEmpty()){
				
				// update the data and broadcast to everyone that we probably have new filtered ranks
				var filteredBaseData = stdc.getBaseData();
				filteredBaseData.setIncludeIndex(listChecked);
				
				// hack: to update the process interval based on the new filtered ranks
				stdc.setBaseData(filteredBaseData);

				return filteredBaseData;
			}
			return null;
		};
		return traceFilter.filterTrace(stdc);
	}
}
