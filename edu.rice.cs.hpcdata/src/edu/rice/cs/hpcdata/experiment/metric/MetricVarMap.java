/**
 * 
 */
package edu.rice.cs.hpcdata.experiment.metric;

import com.graphbuilder.math.VarMap;
import edu.rice.cs.hpcdata.experiment.scope.IMetricScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;


/**
 * Specialization of {@code VarMap} to map between a variable
 * to a value given the database for a specific metric and scope.
 *
 * @see VarMap
 */
public class MetricVarMap extends VarMap 
{
	private Scope scope;
	private BaseMetric metric = null;
	private IMetricManager metricManager;

	public MetricVarMap() {
		this(null);
	}
	
	public MetricVarMap(IMetricManager metricManager) {
		this(null, metricManager);
	}
	
	public MetricVarMap(IMetricScope s, IMetricManager metricManager) {
		super(false);
		this.scope = (Scope) s;
		this.metricManager = metricManager;
	}
	
	//===========================
	// APIs
	//===========================

	public void setMetricManager(IMetricManager newMetricManager) {
		this.metricManager = newMetricManager;
	}
	
	public IMetricManager getMetricManager() {
		return metricManager;
	}
	
	/****
	 * Set new base metric for this variable mapping
	 * 
	 * @param metric
	 * 			The new metric
	 */
	public void setMetric(BaseMetric metric) {
		this.metric = metric;
	}
	
	
	public BaseMetric getMetric() {
		return metric;
	}
	
	
	/**
	 * set the current scope which contains metric values
	 * @param s
	 * 			the scope of node
	 */
	public void setScope(IMetricScope s) {
		this.scope = (Scope) s;
	}
	
	/****
	 * Return the current scope for this map 
	 * 
	 * @return
	 */
	public IMetricScope getScope() {
		return scope;
	}
	
	/**
	 * Overloaded method: a callback to retrieve the value of a variable (or a metric)
	 * If the variable is a normal variable, it will call the parent method.		
	 */
	public double getValue(String varName) {
		
		char firstLetter = varName.charAt(0);
		if (firstLetter == '$' || firstLetter == '@') 
		{
			//---------------------------------------------------------
			// get the value of the scope for this metric
			//---------------------------------------------------------

			// Metric variable
			RootScope root = scope instanceof RootScope? (RootScope) scope : scope.getRootScope();
			IMetricManager mm = metricManager == null ? (IMetricManager) root.getExperiment() : metricManager;
			BaseMetric metricToQuery = mm.getMetric(getIntMetricIndex(varName));
			if (metricToQuery == null) 
				throw new RuntimeException("metric ID unknown: " + varName);
			
			//---------------------------------------------------------
			// 2011.02.08: new interpretation of the symbol "@x" where x is the metric ID
			// @x returns the aggregate value of metric x 
			//---------------------------------------------------------
			final IMetricScope currentScope = (firstLetter == '@' ? root : scope);
			if (currentScope == null)
				throw new RuntimeException("Invalid scope: " + varName);

			MetricValue value;
			if (this.metric != null && this.metric == metricToQuery) {
				// avoid recursive call: if the metric queries its own value, we returns
				// the "raw" value 
				value = currentScope.getDirectMetricValue(metricToQuery.getIndex());
			} else {
				value = metricToQuery.getValue(currentScope);
			}
			if(MetricValue.isAvailable(value))
				return value.getValue();

		} else if (firstLetter == '#') {
			int index = getIntMetricIndex(varName);
			
			RootScope root = scope instanceof RootScope? (RootScope) scope : scope.getRootScope();
			IMetricManager mm = metricManager == null ? (IMetricManager) root.getExperiment() : metricManager;

			BaseMetric bm = mm.getMetricFromOrder(index);
			
			if (bm != null) {
				
				final IMetricScope currentScope = (firstLetter == '@' ? root : scope);
				MetricValue value = MetricValue.NONE;
				
				if (metric != null && metric.getMetricType() == MetricType.EXCLUSIVE) {
					// for exclusive metric, we have to compute the exclusive metric of the source
					// not the inclusive one

					BaseMetric pm   = mm.getMetric(bm.getPartner());
					
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
	
	private int getIntMetricIndex(String varName) {
		if (varName == null)
			return -1;
		
		if (varName.equals("$$") || varName.equals("##") || varName.equals("@@"))
			return metric.getIndex();
		
		String sIndex = varName.substring(1);
		return Integer.valueOf(sIndex);
	}
}
