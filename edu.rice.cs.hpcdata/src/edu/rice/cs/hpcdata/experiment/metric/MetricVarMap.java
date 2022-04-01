/**
 * 
 */
package edu.rice.cs.hpcdata.experiment.metric;

import com.graphbuilder.math.VarMap;
import edu.rice.cs.hpcdata.experiment.scope.IMetricScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;


/**
 * @author la5
 *
 */
public class MetricVarMap extends VarMap 
{
	private Scope 	scope;
	private BaseMetric 		metric = null;
	private final IMetricManager metricManager;

	public MetricVarMap() {
		this(null, null, null);
	}
	
	public MetricVarMap(RootScope root, IMetricManager metricManager) {
		this(root, null, metricManager);
	}
	
	public MetricVarMap(RootScope root, IMetricScope s, IMetricManager metricManager) {
		super(false);
		this.scope = (Scope) s;
		this.metricManager = metricManager;
	}
	

	//===========================
	

	public void setMetricManager(IMetricManager metricManager) {
	}
	
	public void setMetric(BaseMetric metric)
	{
		this.metric = metric;
	}
	
	/**
	 * set the current scope which contains metric values
	 * @param s: the scope of node
	 */
	public void setScope(IMetricScope s) {
		this.scope = (Scope) s;
	}
	
	public void setRootScope(RootScope root)
	{
	}
	
	/**
	 * Overloaded method: a callback to retrieve the value of a variable (or a metric)
	 * If the variable is a normal variable, it will call the parent method.		
	 */
	public double getValue(String varName) {
		assert(varName != null);
		
		char firstLetter = varName.charAt(0);
		if (firstLetter == '$' || firstLetter == '@') 
		{
			//---------------------------------------------------------
			// get the value of the scope for this metric
			//---------------------------------------------------------

			// Metric variable
			String sIndex = varName.substring(1);
			RootScope root = scope.getRootScope();
			IMetricManager mm = metricManager == null ? (IMetricManager) root.getExperiment() : metricManager;
			BaseMetric metricToQuery = mm.getMetric(Integer.valueOf(sIndex));
			if (metricToQuery == null) 
				throw new RuntimeException("metric ID unknown: " + sIndex);
			
			//---------------------------------------------------------
			// 2011.02.08: new interpretation of the symbol "@x" where x is the metric ID
			// @x returns the aggregate value of metric x 
			//---------------------------------------------------------
			final IMetricScope currentScope = (firstLetter == '@' ? root : scope);
			if (currentScope == null)
				throw new RuntimeException("Invalid scope: " + varName);

			MetricValue value = MetricValue.NONE;
			if (this.metric != null && this.metric == metricToQuery) {
				// avoid recursive call: if the metric queries its own value, we returns
				// the "raw" value 
				value = currentScope.getMetricValue(metricToQuery.getIndex());
			} else {
				value = metricToQuery.getValue(currentScope);
			}
			if(MetricValue.isAvailable(value))
				return value.getValue();

		} else if (firstLetter == '#') {
			String sIndex = varName.substring(1);
			Integer index = Integer.valueOf(sIndex);
			
			RootScope root = scope.getRootScope();
			IMetricManager metricManager = (IMetricManager) root.getExperiment();

			BaseMetric bm = metricManager.getMetricFromOrder(index);
			
			if (bm != null) {
				
				final IMetricScope currentScope = (firstLetter == '@' ? root : scope);
				MetricValue value = MetricValue.NONE;
				
				if (metric != null && metric.getMetricType() == MetricType.EXCLUSIVE) {
					// for exclusive metric, we have to compute the exclusive metric of the source
					// not the inclusive one

					BaseMetric pm   = metricManager.getMetric(bm.getPartner());
					
					if (pm != null)
						value = pm.getValue(currentScope);
				} else {
					
					// avoid recursive computation
					// if we are trying to get our own value, we return zero
					if (metric != bm)
						value = bm.getValue(currentScope);
				}
				
				if (value != MetricValue.NONE)
					return bm.getValue(currentScope).value;
			}
		} else
			//---------------------------------------------------------
			// get directly the value of the variable
			//---------------------------------------------------------
			return super.getValue(varName);

		return 0.0d;
	}
}
