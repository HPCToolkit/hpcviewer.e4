// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.blamestat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

/*************************************************************************
 * 
 * A view to show the statistics of the current region selection
 *
 *************************************************************************/
public class HPCBlameView extends AbstractBaseItemWithTable 
{
	private RowDataProvider dataProvider;

	public HPCBlameView(CTabFolder parent, int style) {
		super(parent, style);
	}


	@Override
	protected String getTopicEvent() {
		return IConstants.TOPIC_BLAME;
	}


	@Override
	protected List<StatisticItem> getListItems(Object input) {
		List<StatisticItem>listItems  = new ArrayList<>();

		SummaryData data = (SummaryData) input;
		ColorTable colorTable = data.colorTable;
		
		Set<Entry<Integer, Float>> entries = data.cpuBlameMap.entrySet();
		
		for(Map.Entry<Integer, Float> entry: entries ) {
			final Integer pixel = entry.getKey();
			final Float count   = entry.getValue();
			final RGB rgb	 	= data.palette.getRGB(pixel);
			
			ProcedureColor procColor = colorTable.getProcedureNameByColorHash(rgb.hashCode());
			if (procColor != null) {
				listItems.add(new StatisticItem(procColor, (float) 100.0 * count / data.totalCpuBlame));
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
