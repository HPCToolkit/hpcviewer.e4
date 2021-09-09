package edu.rice.cs.hpctree;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.CallSiteScopeCallerView;
import edu.rice.cs.hpcdata.experiment.scope.ProcedureScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.util.Util;
import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceManager;

/****************************************************************
 * 
 * Class to provide data for each cell (from IDataProvider) and each row
 * (from IRowDataProvider).
 * This class also manage the string output for each cell.
 *
 ****************************************************************/
public class ScopeTreeDataProvider implements IDataProvider, IRowDataProvider<Scope>
{
	private final ScopeTreeData treeData;
	
	public ScopeTreeDataProvider(IScopeTreeData treeData) {
		this.treeData   = (ScopeTreeData) treeData;
	}

	public BaseMetric getMetric(int columnIndex) {
		if (columnIndex == 0)
			return null;
		
		return treeData.getMetric(columnIndex-1);
	}
	
	
	@Override
	public Object getDataValue(int columnIndex, int rowIndex) {
		Scope scope = treeData.getDataAtIndex(rowIndex);

		if (columnIndex > 0) {
			BaseMetric metric = treeData.getMetric(columnIndex-1);
			if (metric == null)
				return null;
			return metric.getMetricTextValue(scope);
		}
		
		// tree column, assuming column 0 is always the tree column
		String text = getDebugText(scope) + scope.getName();		
		if (needToAddLoadModuleSuffix(scope) ) {
			String lm = null;
			ProcedureScope proc = null;
			
			if (scope instanceof CallSiteScope) {
				proc = ((CallSiteScope)scope).getProcedureScope();
			} else {
				proc = (ProcedureScope) scope;
			}
			if (!proc.isFalseProcedure()) {
				lm = proc.getLoadModule().getModuleName();
				int lastDot = 1+lm.lastIndexOf('/');
				
				text += " [" + lm.substring(lastDot) + "]";
			}
		}
		return text;			
	}

	@Override
	public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
		Scope scope = treeData.getDataAtIndex(rowIndex);

		if (columnIndex == 0) {
			return;
		}
		scope.setMetricValue(columnIndex-1, (MetricValue) newValue);
	}

	
	@Override
	public int getColumnCount() {
		
		return 1 + treeData.getMetricCount();
	}

	@Override
	public int getRowCount() {
		return treeData.getElementCount();
	}

	@Override
	public Scope getRowObject(int rowIndex) {
		return treeData.getDataAtIndex(rowIndex);
	}

	@Override
	public int indexOfRowObject(Scope rowObject) {
		return treeData.indexOf(rowObject);
	}
	
	private String getDebugText(Scope node) {
		ViewerPreferenceManager pref = ViewerPreferenceManager.INSTANCE;
		String text = "";
		
		if (pref.getDebugCCT())  
		{
			//---------------------------------------------------------------
			// label for debugging purpose
			//---------------------------------------------------------------
			if (node instanceof CallSiteScope)
			{
				CallSiteScope caller = (CallSiteScope) node;
				Scope cct = caller.getLineScope();
				if (node instanceof CallSiteScopeCallerView) 
				{
					int numMerged = ((CallSiteScopeCallerView)caller).getNumMergedScopes();
					if (numMerged > 0) {
						int mult = numMerged + 1;
						text = mult + "*";
					}
					cct = ((CallSiteScopeCallerView)caller).getScopeCCT();
				}	
				text += " [c:" + caller.getCCTIndex() +"/" + cct.getCCTIndex()  + "] " ;
			} else
				text = "[c:" + node.getCCTIndex() + "] ";
		} 
		if (pref.getDebugFlat()) {
			text += "[f: " + node.getFlatIndex() ;
			if (node instanceof CallSiteScope) {
				ProcedureScope proc = ((CallSiteScope)node).getProcedureScope();
				text += "/" + proc.getFlatIndex();
			}
			text += "] ";
		} 
		return text;
	}
	
	static private boolean needToAddLoadModuleSuffix(Scope node) {
		if (node instanceof CallSiteScope ||
				(node instanceof ProcedureScope && ((ProcedureScope)node).isAlien())) {

			// we only add the load module suffix if:
			// - the file is NOT readable; and
			// - doesn't have load module suffix

			return (!Util.isFileReadable(node) && node.getName().lastIndexOf(']')<0);

		}
		return false;
	}
}
