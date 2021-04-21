/**
 * 
 */
package edu.rice.cs.hpc.data.experiment.metric;

import edu.rice.cs.hpc.data.experiment.BaseExperimentWithMetrics;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.*;

import java.util.Map;

//math expression
import com.graphbuilder.math.*;

/**
 * @author la5
 *
 */
public class DerivedMetric extends AbstractMetricWithFormula implements IMetricMutable 
{
	//===================================================================================
	// DATA
	//===================================================================================

	final static public String DESCRIPTION = "Derived metric";
	
	// formula expression
	private Expression expression;
	// the total aggregate value
	//private double dRootValue = 0.0;
	// map function
	private ExtFuncMap fctMap;
	// map variable 
	private MetricVarMap varMap;

	private IMetricManager experiment;
	
	private MetricValue rootValue;

	
	//===================================================================================
	// CONSTRUCTORS
	//===================================================================================
	

	/*****
	 * Create derived metric based on experiment data. We'll associate this metric with the root scope of CCT
	 * <p/>
	 * A metric should be independent to root scope. The root scope is only used to compute the percentage
	 * 
	 * @param experiment
	 * @param e
	 * @param sName
	 * @param sID
	 * @param index
	 * @param annotationType
	 * @param objType
	 */
	public DerivedMetric(RootScope root, IMetricManager experiment, String expression, 
			String sName, String sID, 
			int index, AnnotationType annotationType, MetricType objType) {
		
		// no root scope information is provided, we'll associate this metric to CCT root scope 
		// the partner of this metric is itself (derived metric has no partner)
		super(sID, sName, DESCRIPTION, VisibilityType.SHOW, null, annotationType, index, index, objType);
		
		this.experiment = experiment;
		
		// set up the functions
		this.fctMap = new ExtFuncMap();
		this.fctMap.init();

		// set up the variables
		this.varMap = new MetricVarMap(root, experiment);
		this.varMap.setMetric(this);
		
		setExpression(expression);
	}
	
	
	/**
	 * Constructor to create a derived metric from hpcrun's formula
	 * Since this metric is created during XML parsing, there is no way we can find
	 * the root and the experiment.<br/> 
	 * We need to allow to delay setting the root and the experiment,
	 *  
	 * @param formulaExpression : math formula 
	 * @param sName : name of the metric
	 * @param sID : ID
	 * @param index : metric index
	 * @param annotationType : type of annotation (percent, ...)
	 * @param objType : metric type (inclusive/exclusive)
	 */
	public DerivedMetric(String sName, String sID, int index, 
			AnnotationType annotationType, MetricType objType) {
		
		super(sID, sName, DESCRIPTION, VisibilityType.SHOW, null, annotationType, index, index, objType);
		
		this.experiment = null; // to be defined later
		this.varMap		= null; // to be defined later
		this.expression = null; // to be defined later
		
		this.fctMap     = new ExtFuncMap();
		
	}
	
	
	/****
	 * Set the new expression
	 * 
	 * @param expr : the new expression
	 */
	public void setExpression( String expr ) {
		expression = ExpressionTree.parse(expr);
		
		if (experiment != null)
			rootValue  = setRootValue(experiment.getRootScope());
	}

	static public boolean evaluateExpression(String expression, 
			MetricVarMap varMap, ExtFuncMap funcMap) {
		Expression exp = ExpressionTree.parse(expression);
		exp.eval(varMap, funcMap);
		return true;
	}
	
	
	//===================================================================================
	// GET VALUE
	//===================================================================================
	/**
	 * Computing the value of the derived metric
	 * @param scope: the current scope
	 * @return the object Double if there is a value, null otherwise
	 */
	public double getDoubleValue(IMetricScope scope) {
		this.varMap.setScope(scope);
		return expression.eval(this.varMap, this.fctMap);
	}
	
	/**
	 * Overloading method to compute the value of the derived metric of a scope
	 * Return a MetricValue
	 */
	@Override
	public MetricValue getValue(IMetricScope scope) {
		// corner case
		// if the scope is a root scope, then we return the aggregate value
		if(scope instanceof RootScope) {
			// if the root value is null, perhaps we haven't computed
			// however, for the sake to fix bug with raw metrics thas has formula,
			// we also need to check if the root value is none or not.
			// For raw metrics with formula, the first time we create a derived metric,
			// the value can be NONE
			if (rootValue == null || rootValue == MetricValue.NONE) {
				rootValue = setRootValue((RootScope)scope);
			}
			return rootValue;
		}
		// otherwise, we need to recompute the value again via the equation
		double dVal = getDoubleValue(scope);
		
		// ugly test to check whether the value exist or not
		if(Double.compare(dVal, 0.0d) == 0)
			return MetricValue.NONE;	// the value is not available !

		if(this.getAnnotationType() == AnnotationType.PERCENT){
			MetricValue myrootValue = getValue(experiment.getRootScope());
			return new MetricValue(dVal, ((float) dVal/myrootValue.getValue()));
		}
		return new MetricValue(dVal);
	}
	
	public MetricValue getRawValue(IMetricScope s)
	{
		return getValue(s);
	}

	/****
	 * return the current expression formula
	 * 
	 * @return
	 */
	public String getFormula() {
		return expression.toString();
	}

	
	/***
	 * Rename metric index variable using map
	 * 
	 * @param mapOldIndex from old index to a new one
	 * 
	 */
	@Override
	public void renameExpression(Map<Integer, Integer> mapOldIndex, Map<Integer, Integer> mapOldOrder) {
		renameExpression(expression, mapOldIndex, mapOldOrder);
	}

	
	@Override
	public BaseMetric duplicate() {
		RootScope root = (experiment != null ? experiment.getRootScope() : null);
		final DerivedMetric copy = new DerivedMetric(root, experiment, 
				expression.toString(), displayName, 
				shortName, index, annotationType, metricType);
		
		// TODO: hack, we need to conserve the format of the metric.
		copy.displayFormat = displayFormat;
		copy.order = order;
		
		return copy;
	}
	
	/****
	 * update the experiment of this derived metric
	 * 
	 * @param experiment
	 */
	public void setExperiment(BaseExperimentWithMetrics experiment)
	{
		this.experiment = experiment;
		// updating as well the variable mapping to metrics
		varMap.setMetricManager(experiment);
	}
	
	public void resetMetric(Experiment experiment, RootScope root)
	{
		this.experiment = experiment;
		
		varMap = new MetricVarMap(root, experiment);
		varMap.setMetric(this);
		
		fctMap.init();
		
		rootValue = null; //setRootValue(root);
	}
	
	private MetricValue setRootValue(RootScope rootScope) 
	{
		if (rootScope == null)
			// we don't have root. 
			// let assume we can compute this later
			return null; 
		
		double rootVal = getDoubleValue(rootScope);
		if (Double.compare(0.0, rootVal) == 0) {
			return MetricValue.NONE;
		}
		if (annotationType != AnnotationType.NONE) {
			double rootAnn = 1.0d;
			return new MetricValue(rootVal, rootAnn);
		}
		return new MetricValue(rootVal);
	}
}
