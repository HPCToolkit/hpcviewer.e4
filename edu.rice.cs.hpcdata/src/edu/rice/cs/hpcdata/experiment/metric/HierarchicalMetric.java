package edu.rice.cs.hpcdata.experiment.metric;

import org.apache.commons.math3.util.Precision;

import com.graphbuilder.math.Expression;
import com.graphbuilder.math.ExpressionTree;

import edu.rice.cs.hpcdata.db.MetricValueCollectionWithStorage;
import edu.rice.cs.hpcdata.db.version4.DataSummary;
import edu.rice.cs.hpcdata.experiment.TreeNode;
import edu.rice.cs.hpcdata.experiment.scope.IMetricScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;

/****************************************************************
 * 
 * Metric class with tree-based structure
 * <p>
 * This class is designed to work with meta.db database which allows
 * to create a hierarchy of metrics.<br/>
 * 
 * It has the same structure as the 
 * {@link edu.rice.cs.hpcdata.experiment.scope.Scope} class: <br/>
 * The root metric has no parent (or the parent is null), and the
 * leaves have no children. 
 *
 ****************************************************************/
public class HierarchicalMetric extends AbstractMetricWithFormula 
{	
	private static final byte FMT_METADB_COMBINE_SUM = 0;
	private static final byte FMT_METADB_COMBINE_MIN = 1;
	private static final byte FMT_METADB_COMBINE_MAX = 2;
	
	private static final byte COMBINE_UNKNOWN = -1;
	
	private static final String []COMBINE_LABEL = {"Sum", "Min", "Max", "Mean", "StdDev", "CfVar"};

	private final DataSummary profileDB;
	private final TreeNode<HierarchicalMetric> node;
	private final String originalName;
	
	// map function
	private final ExtFuncMap fctMap;
	// map variable 
	private final MetricVarMap varMap;

	private PropagationScope propagationScope;
	private Expression formula;
	
	/**
	 * The combination function combine is an enumeration with the following possible values (the name after / is the matching name for inputs:combine in METRICS.yaml):
	 * <ul>
	 *   <li>0/sum: Sum of input values
	 *   <li>1/min: Minimum of input values
	 *   <li>2/max: Maximum of input values
	 * </ul>
	 */
	private byte combineType; 

	
	/***
	 * Create a basic metric descriptor without hierarchy information.
	 * The caller has to provide the parent/children information later on.
	 * 
	 * @param profileDB
	 * 			The object to access summary.db file.
	 * @param index
	 * 			The index of this metric
	 * @param name
	 * 			The basic name of the metric. This is not the displayed name.
	 */
	public HierarchicalMetric(DataSummary profileDB, int index, String name) {
		super(String.valueOf(index), name);
		this.profileDB = profileDB;
		setIndex(index);
		
		varMap = new HierarchicalMetricVarMap();
		varMap.setMetric(this);
		
		fctMap = new ExtFuncMap();
		fctMap.loadDefaultFunctions();
		
		node = new TreeNode<>(index);
		originalName = name;
		
		combineType = COMBINE_UNKNOWN;
	}
	
	
	public void setParent(HierarchicalMetric parent) {
		node.setParent(parent);
	}
	
	public HierarchicalMetric getParent() {
		return node.getParent();
	}
	
	public void addChild(HierarchicalMetric child) {
		node.add(child);
	}
	
	public int getChildCount() {
		return node.getChildCount();
	}
	
	public HierarchicalMetric getChildAt(int index) {
		return node.getChildAt(index);
	}
	
	
	/**
	 * Set the type of combine function. 
	 * <ul>
	 *   <li>0/sum: Sum of input values
	 *   <li>1/min: Minimum of input values
	 *   <li>2/max: Maximum of input values
	 * </ul>
	 */
	public void setCombineType(byte type) {
		this.combineType = type;
	}
	
	
	/****
	 * Set the combine type by name
	 * 
	 * @param combineName
	 */
	public void setCombineType(String combineName) {
		if (combineName == null || combineName.isEmpty())
			return;
		
		for(byte i=0; i<COMBINE_LABEL.length; i++) {
			if (combineName.compareToIgnoreCase(COMBINE_LABEL[i]) == 0) {
				combineType = i;
				return;
			}				
		}
		throw new IllegalArgumentException("Unknown combine name: " + combineName);
	}
	
	/***
	 * Get the name of combine function.
	 * 
	 * @return {@code String}
	 */
	public String getCombineTypeLabel() {
		if (combineType == COMBINE_UNKNOWN)
			return "";
		
		return COMBINE_LABEL[combineType];
	}
	
	
	/***
	 * Perform reduction depending on the combine type function.
	 * <ul>
	 * 	<li>If it's a sum, then the reduce operator is minus.
	 *  <li>If it's a min, then the operator is the minimum of the two values
	 *  <li>etc.
	 * </ul>
	 * 
	 * @param target
	 * 			The destination of the result of reduction
	 * @param source
	 * 			The source of reduce operation
	 * @return {@code MetricValue}
	 * 			Equivalent with the {@code target} variable in the argument
	 */
	public MetricValue reduce(MetricValue target, MetricValue source) {
		if (combineType == COMBINE_UNKNOWN)
			return target;
		
		final float INSIGNIFICANT_NUMBER = 0.000001f;
		if (source == MetricValue.NONE)
			return target;

		if (target == MetricValue.NONE) {
			return source.duplicate();
		}

		switch (combineType) {
		case FMT_METADB_COMBINE_MAX:
			var v1 = target.getValue();
			var v2 = source.getValue();
			v1 = Math.max(v1, v2);
			target.setValue(v1);
			break;
			
		case FMT_METADB_COMBINE_MIN:
			v1 = target.getValue();
			v2 = source.getValue();
			v1 = Math.min(v1, v2);
			target.setValue(v1);
			break;
			
		case FMT_METADB_COMBINE_SUM:
			v1 = target.getValue();
			v2 = source.getValue();
			var d = v1-v2;
			if (Math.abs(d/v1) < INSIGNIFICANT_NUMBER)
				return MetricValue.NONE; 
			
			target.setValue(d);
			break;
			
		default:
			// nothing. error?
		}
		return target;
	}
	
	
	/****
	 * Retrieve the original metric name without any changes or suffixes.
	 * 
	 * @see getDisplayName
	 * 
	 * @return {@code String} 
	 * 			the original name
	 */
	public String getOriginalName() {
		return originalName;
	}
	
	
	@Override
	public String getDisplayName() {
		// the display name of hierarchical metric (meta.db) is tricky
		// because the name in meta.db doesn't include the metric type suffix
		// (something like (I) or (E)).
		// Worse, the metric type is only known after we parse the yaml file.
		//
		// So to remedy this issue, we have the "original name" and "display name"
		// the "display name" is the original name with suffix
		if (!displayName.equals(originalName))
			return displayName;
		
		final String SUFFIX_EXCLUSIVE = " (E)";
		final String SUFFIX_INCLUSIVE = " (I)";
		final String SUFFIX_POINT_EXC = " (X)";
		
		// if the display name already have metric-type suffix.
		// return the real display name
		if (displayName.endsWith(SUFFIX_EXCLUSIVE) || 
			displayName.endsWith(SUFFIX_INCLUSIVE) ||
			displayName.endsWith(SUFFIX_POINT_EXC))
			return displayName;
		
		StringBuilder sb = new StringBuilder(originalName);
		final String SUFFIX_COMBINE_TYPE = ": ";
		
		if (combineType >= 0) {
			sb.append(SUFFIX_COMBINE_TYPE);
			sb.append(getCombineTypeLabel());
		}

		// otherwise we need to add suffix for the metric type
		// 
		if (getMetricType() == MetricType.EXCLUSIVE || 
			getMetricType() == MetricType.LEXICAL_AWARE)
			sb.append(SUFFIX_EXCLUSIVE);
		else if (getMetricType() == MetricType.INCLUSIVE)
			sb.append(SUFFIX_INCLUSIVE);
		else if (getMetricType() == MetricType.POINT_EXCL)
			sb.append(SUFFIX_POINT_EXC);

		displayName = sb.toString();
		
		return displayName;
	}
	
	@Override
	protected Expression[] getExpressions() {
		// not supported at the moment
		// all formula-based metrics should use DerivedMetric class
		return new Expression[] {formula};
	}

	/***
	 * Get the math formula of the metric
	 * 
	 * @return
	 */
	public Expression getFormula() {
		return formula;
	}


	/****
	 * Set the math formula of the metric
	 * 
	 * @param formula
	 */
	public void setFormula(String strFormula) {
		if (strFormula.equals("$$"))
			formula = null;
		this.formula = ExpressionTree.parse(strFormula);
	}


	/**
	 * @return the propagationScope
	 */
	public PropagationScope getPropagationScope() {
		return propagationScope;
	}


	/**
	 * @param propagationScope the propagationScope to set
	 */
	public void setPropagationScope(PropagationScope propagationScope) {
		this.propagationScope = propagationScope;
		
		// redundant information
		MetricType type = propagationScope.getMetricType();		
		setMetricType(type);
	}


	@Override
	public MetricValue getValue(IMetricScope s) {
		Scope scope = (Scope)s;
			
		// Fix for issue #248 for meta.db: do not grab the value from profile.db
		// instead, if it's from bottom-up view or flat view, we grab the value 
		// from the computed metrics.
		if (formula == null || 
			scope.getMetricValues() instanceof MetricValueCollectionWithStorage) {
			return scope.getDirectMetricValue(index);
		}
		
		varMap.setScope(scope);
		var value =  formula.eval(varMap, fctMap);
		
		// Usually we don't need to use apache's math to compare zero but in
		// some cases, it's needed. Let's take the precaution using epsilon 
		// next time.
		if (Precision.equals(0.0d, value))
			return MetricValue.NONE;
		
		return new MetricValue(value);
	}

	
	@Override
	public BaseMetric duplicate() {
		var dupl = new HierarchicalMetric(profileDB, index, displayName);
		dupl.annotationType = annotationType;
		dupl.combineType    = combineType;
		dupl.description    = description;
		dupl.displayFormat  = displayFormat;
		dupl.metricType     = metricType;
		dupl.order          = order;
		dupl.partnerIndex   = partnerIndex;
		dupl.sampleperiod   = sampleperiod;
		dupl.formula = formula.duplicate();
		
		return dupl;
	}

	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof HierarchicalMetric))
			return false;
		
		HierarchicalMetric otherMetric = (HierarchicalMetric) other;
		return this.annotationType == otherMetric.annotationType &&
			   this.combineType    == otherMetric.combineType    &&
			   this.displayName.equals(otherMetric.displayName)  &&
			   this.formula.toString().equals(otherMetric.getFormula().toString());  
	}
}
