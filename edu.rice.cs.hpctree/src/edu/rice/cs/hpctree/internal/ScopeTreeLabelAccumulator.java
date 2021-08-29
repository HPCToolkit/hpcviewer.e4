package edu.rice.cs.hpctree.internal;

import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;

import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.CallSiteScopeCallerView;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpctree.IScopeTreeData;
import edu.rice.cs.hpctree.ScopeTreeData;

public class ScopeTreeLabelAccumulator implements IConfigLabelAccumulator 
{
	public final static String LABEL_CALLSITE = "scope.callsite";
	public final static String LABEL_CALLER   = "scope.caller";
	public final static String LABEL_TREECOLUMN  = "column.tree";
	public final static String LABEL_METRICOLUMN = "column.metric_";
	
	private final ScopeTreeData treeData;
	
	public ScopeTreeLabelAccumulator(IScopeTreeData treeData2) {
		this.treeData = (ScopeTreeData) treeData2;
	}

	@Override
	public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
		if (columnPosition > 0) {
			configLabels.add(LABEL_METRICOLUMN);
			return;
		}
		configLabels.add(LABEL_TREECOLUMN);
		
		Scope scope = treeData.getDataAtIndex(rowPosition);
		if (scope instanceof CallSiteScope) {
			configLabels.add(LABEL_CALLSITE);
		} else if (scope instanceof CallSiteScopeCallerView) {
			configLabels.add(LABEL_CALLER);
		}
	}

}
