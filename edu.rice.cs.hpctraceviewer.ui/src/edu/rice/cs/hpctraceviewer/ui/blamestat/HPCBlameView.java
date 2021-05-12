package edu.rice.cs.hpctraceviewer.ui.blamestat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.RGB;

import edu.rice.cs.hpctraceviewer.data.color.ColorTable;
import edu.rice.cs.hpctraceviewer.data.color.ProcedureColor;
import edu.rice.cs.hpctraceviewer.data.util.Constants;
import edu.rice.cs.hpctraceviewer.ui.base.AbstractItemViewWithTable;
import edu.rice.cs.hpctraceviewer.ui.base.StatisticItem;
import edu.rice.cs.hpctraceviewer.ui.summary.SummaryData;
import edu.rice.cs.hpctraceviewer.ui.util.IConstants;

/*************************************************************************
 * 
 * A view to show the statistics of the current region selection
 *
 *************************************************************************/
public class HPCBlameView extends AbstractItemViewWithTable 
{

	private ColorTable colorTable = null;

	public HPCBlameView(CTabFolder parent, int style) {
		super(parent, style);
	}


	@Override
	protected String getTopicEvent() {
		return IConstants.TOPIC_BLAME;
	}


	@Override
	protected ColorTable getColorTable() {
		return colorTable;
	}


	@Override
	protected List<StatisticItem> getListItems(Object input) {
		List<StatisticItem>listItems  = new ArrayList<>();

		SummaryData data = (SummaryData) input;
		colorTable = data.colorTable;
		
		Set<Entry<Integer, Float>> entries = data.cpuBlameMap.entrySet();
		
		for(Map.Entry<Integer, Float> entry: entries ) {
			final Integer pixel = entry.getKey();
			final Float count   = entry.getValue();
			final RGB rgb	 	= data.palette.getRGB(pixel);
			
			String proc;
			ProcedureColor procColor = colorTable.getProcedureNameByColorHash(rgb.hashCode());
			if (procColor == null) {
				proc = Constants.PROC_NO_ACTIVITY;
			} else {
				proc = procColor.getProcedure();
			}
			listItems.add(new StatisticItem(proc, procColor.color, (float) 100.0 * count / data.totalCpuBlame));
		}
		return listItems;
	}
}
