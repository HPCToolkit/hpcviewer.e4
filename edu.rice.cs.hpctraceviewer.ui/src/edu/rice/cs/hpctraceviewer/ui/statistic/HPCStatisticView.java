package edu.rice.cs.hpctraceviewer.ui.statistic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.RGB;

import ca.odell.glazedlists.EventList;
import edu.rice.cs.hpctraceviewer.data.color.ColorTable;
import edu.rice.cs.hpctraceviewer.data.color.ProcedureColor;
import edu.rice.cs.hpctraceviewer.ui.base.AbstractBaseItemWithTable;
import edu.rice.cs.hpctraceviewer.ui.base.StatisticItem;
import edu.rice.cs.hpctraceviewer.ui.summary.SummaryData;
import edu.rice.cs.hpctraceviewer.ui.util.IConstants;

public class HPCStatisticView extends AbstractBaseItemWithTable 
{	
	private RowDataProvider dataProvider;

	public HPCStatisticView(CTabFolder parent, int style) {
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
	
	
	@Override
	public IRowDataProvider<StatisticItem> getRowDataProvider(EventList<StatisticItem> eventList) {
		if (dataProvider == null) {
			dataProvider = new RowDataProvider(eventList);
		} else {
			dataProvider.setList(eventList);
		}
		return dataProvider;
	}
}
