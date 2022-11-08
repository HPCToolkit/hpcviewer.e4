package edu.rice.cs.hpcdata.experiment.metric;

import org.apache.commons.math3.util.Precision;

import com.graphbuilder.math.Expression;
import com.graphbuilder.math.ExpressionTree;

import edu.rice.cs.hpcdata.db.version4.DataSummary;
import edu.rice.cs.hpcdata.experiment.scope.IMetricScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;


/************************************************************************
 * 
 * Special class of Hierarchical metric with derived formula to compute
 * the value
 *
 ************************************************************************/
public class HierarchicalMetricDerivedFormula extends HierarchicalMetric 
{
	
	// map function
	private final ExtFuncMap fctMap;
	// map variable 
	private final MetricVarMap varMap;
	
	private Expression derivedFormula;

	public HierarchicalMetricDerivedFormula(DataSummary profileDB, int index, String name, String formula) {
		super(profileDB, index, name, formula);
		
		varMap = new HierarchicalMetricVarMap();
		varMap.setMetric(this);
		
		fctMap = new ExtFuncMap();
		fctMap.loadDefaultFunctions();
		
		setFormula(formula);
	}


	/***
	 * Get the math formula of the metric
	 * 
	 * @return
	 */
	@Override
	public String getFormula() {
		return derivedFormula.toString();
	}


	/****
	 * Set the math formula of the metric
	 * 
	 * @param derivedFormula
	 */
	public void setFormula(String strFormula) {
		if (strFormula.equals("$$"))
			derivedFormula = null;
		this.derivedFormula = ExpressionTree.parse(strFormula);
	}


	@Override
	public MetricValue getValue(IMetricScope s) {
		Scope scope = (Scope)s;
			
		// Fix for issue #248 for meta.db: do not grab the value from profile.db
		// instead, if it's from bottom-up view or flat view, we grab the value 
		// from the computed metrics.
		if (derivedFormula == null ) {
			return scope.getDirectMetricValue(index);
		}
		
		varMap.setScope(scope);
		var value = derivedFormula.eval(varMap, fctMap);
		
		// Usually we don't need to use apache's math to compare zero but in
		// some cases, it's needed. Let's take the precaution using epsilon 
		// next time.
		if (Precision.equals(0.0d, value))
			return MetricValue.NONE;
		
		return new MetricValue(value);
	}

	@Override
	public BaseMetric duplicate() {
		var dupl = new HierarchicalMetricDerivedFormula(getDataSummary(), index, displayName, getFormula());
		copy(dupl);
		dupl.derivedFormula = derivedFormula;
		
		return dupl;
	}
}
