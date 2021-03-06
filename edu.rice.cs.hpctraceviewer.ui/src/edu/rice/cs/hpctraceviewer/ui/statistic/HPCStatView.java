package edu.rice.cs.hpctraceviewer.ui.statistic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.RGB;

import edu.rice.cs.hpctraceviewer.data.color.ColorTable;
import edu.rice.cs.hpctraceviewer.data.color.ProcedureColor;
import edu.rice.cs.hpctraceviewer.ui.base.AbstractItemViewWithTable;
import edu.rice.cs.hpctraceviewer.ui.base.StatisticItem;
import edu.rice.cs.hpctraceviewer.ui.summary.SummaryData;
import edu.rice.cs.hpctraceviewer.ui.util.IConstants;

/*************************************************************************
 * 
 * A view to show the statistics of the current region selection
 *
 *************************************************************************/
public class HPCStatView extends AbstractItemViewWithTable
{
	public HPCStatView(CTabFolder parent, int style) {
		super(parent, style);
	}

	
	@Override
	protected String getTopicEvent() {
		return IConstants.TOPIC_STATISTICS;
	}


	@Override
	protected List<StatisticItem> getListItems(Object input) {

		SummaryData data = (SummaryData) input;
		
		ColorTable colorTable = data.colorTable;

		List<StatisticItem> listItems  = new ArrayList<>();
		
		Set<Integer> set = data.mapPixelToCount.keySet();
		
		for(Iterator<Integer> it = set.iterator(); it.hasNext(); ) {
			final Integer pixel = it.next();
			final Integer count = data.mapPixelToCount.get(pixel);
			final RGB rgb	 	= data.palette.getRGB(pixel);
			
			ProcedureColor procColor = colorTable.getProcedureNameByColorHash(rgb.hashCode());
			
			// TODO: if there is something wrong with the data, we should quit.
			if (procColor != null) {				
				listItems.add(new StatisticItem(procColor, (float)100.0 * count/data.totalPixels));
			}
		}

		return listItems;
	}

}
