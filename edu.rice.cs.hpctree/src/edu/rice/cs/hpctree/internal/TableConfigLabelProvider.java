package edu.rice.cs.hpctree.internal;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelProvider;

public class TableConfigLabelProvider implements IConfigLabelProvider 
{
	public final static String LABEL_TREECOLUMN  = "column.tree";
	public final static String LABEL_METRICOLUMN = "column.metric";
	
	private final Collection<String> labels = new ArrayList<String>(2);

	
	public TableConfigLabelProvider() {
		labels.add(LABEL_TREECOLUMN);
		labels.add(LABEL_METRICOLUMN);
	}
	
	@Override
	public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
		if (columnPosition == 0) {
			configLabels.add(LABEL_TREECOLUMN);
		} else {
			configLabels.add(LABEL_METRICOLUMN);
		}
	}

	@Override
	public Collection<String> getProvidedLabels() {
		return labels;
	}

}
