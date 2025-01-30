// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctree.internal;

import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;

import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.CallSiteScopeCallerView;
import edu.rice.cs.hpcdata.experiment.scope.LineScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpctree.IScopeTreeData;


public class ScopeTreeLabelAccumulator implements IConfigLabelAccumulator 
{
	public final static String LABEL_TOP_ROW  = "top.row";
	
	public final static String LABEL_CALLSITE = "scope.callsite";
	public final static String LABEL_CALLER   = "scope.caller";
	
	public final static String LABEL_CALLSITE_DISABLED = "scope.dis.callsite";
	public final static String LABEL_CALLER_DISABLED   = "scope.dis.caller";
	
	public final static String LABEL_SOURCE_AVAILABLE  = "source.available";
	
	public final static String LABEL_TREECOLUMN  = "column.tree";
	public final static String LABEL_METRICOLUMN = "column.metric_";
	
	private final IScopeTreeData treeData;
	
	public ScopeTreeLabelAccumulator(IScopeTreeData treeData) {
		this.treeData = treeData;
	}

	@Override
	public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
		if (rowPosition == 0)
			configLabels.add(LABEL_TOP_ROW);
		
		if (columnPosition > 0) {
			configLabels.add(LABEL_METRICOLUMN);
			return;
		}
		configLabels.add(LABEL_TREECOLUMN);
		
		Scope scope = treeData.getDataAtIndex(rowPosition);
		if (scope ==  null)
			return;
		
		if (scope instanceof CallSiteScopeCallerView) {
			LineScope ls = ((CallSiteScopeCallerView)scope).getLineScope();
			if (treeData.isSourceFileAvailable(ls))
				configLabels.add(LABEL_CALLER);
			else
				configLabels.add(LABEL_CALLER_DISABLED);
			
		} else if (scope instanceof CallSiteScope) {
			LineScope ls = ((CallSiteScope)scope).getLineScope();
			if (treeData.isSourceFileAvailable(ls)) {
				configLabels.add(LABEL_CALLSITE);				
			} else {
				configLabels.add(LABEL_CALLSITE_DISABLED);
			}
		}
		if (treeData.isSourceFileAvailable(scope)) {
			configLabels.add(LABEL_SOURCE_AVAILABLE);
		}
	}
}
