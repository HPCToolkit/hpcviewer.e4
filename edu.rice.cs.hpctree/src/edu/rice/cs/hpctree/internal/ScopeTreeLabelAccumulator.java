package edu.rice.cs.hpctree.internal;

import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;

import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.CallSiteScopeCallerView;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.util.Util;
import edu.rice.cs.hpctree.IScopeTreeData;
import edu.rice.cs.hpctree.ScopeTreeData;

public class ScopeTreeLabelAccumulator implements IConfigLabelAccumulator 
{
	public final static String LABEL_CALLSITE = "scope.callsite";
	public final static String LABEL_CALLER   = "scope.caller";

	public final static String LABEL_CALLSITE_DISABLED = "scope.dis.callsite";
	public final static String LABEL_CALLER_DISABLED   = "scope.dis.caller";
	
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
			if (Util.isFileReadable(scope)) {
				configLabels.add(LABEL_CALLSITE);				
			} else {
				configLabels.add(LABEL_CALLSITE_DISABLED);
			}
		} else if (scope instanceof CallSiteScopeCallerView) {
			if (Util.isFileReadable(scope))
				configLabels.add(LABEL_CALLER);
			else
				configLabels.add(LABEL_CALLER_DISABLED);
		}	
	}

}
