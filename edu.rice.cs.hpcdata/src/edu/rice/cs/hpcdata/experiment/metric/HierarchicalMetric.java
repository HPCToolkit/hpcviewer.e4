package edu.rice.cs.hpcdata.experiment.metric;

import java.io.IOException;

import com.graphbuilder.math.Expression;
import com.graphbuilder.math.ExpressionTree;

import edu.rice.cs.hpcdata.db.IdTuple;
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
	
	private static final String []COMBINE_LABEL = {"Sum", "Min", "Max"};

	private final DataSummary profileDB;
	private final TreeNode<HierarchicalMetric> node;
	
	private Expression expression;
	
	/**
	 * The combination function combine is an enumeration with the following possible values (the name after / is the matching name for inputs:combine in METRICS.yaml):
	 * <ul>
	 *   <li>0/sum: Sum of input values
	 *   <li>1/min: Minimum of input values
	 *   <li>2/max: Maximum of input values
	 * </ul>
	 */
	private byte combineType; 
	
	private byte []psType;
	private byte []psIndex;

	
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
		node = new TreeNode<>(index);
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

	public void setPropagationScope(byte []psType, byte []psIndex) {
		this.psType  = new byte[psType.length];
		this.psIndex = new byte[psIndex.length];
		for(int i=0; i<psType.length; i++) {
			this.psType[i] = psType[i];
			this.psIndex[i] = psIndex[i];
		}
	}

	public void setFormula(String formula) {
		expression = ExpressionTree.parse(formula);
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
	
	
	/***
	 * Get the name of combine function.
	 * 
	 * @return {@code String}
	 */
	public String getCombineTypeLabel() {
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
	public String getName() {
		return displayName;
	}
	
	@Override
	public String getDisplayName() {
		String name = super.getDisplayName();
		if (getMetricType() == MetricType.EXCLUSIVE)
			return name + " (E)";
		else if (getMetricType() == MetricType.INCLUSIVE)
			return name + " (I)";
		else
			return name + " (X)";
	}
	
	@Override
	protected Expression[] getExpressions() {
		return new Expression[] {expression};
	}

	@Override
	public MetricValue getValue(IMetricScope s) {
		Scope scope = (Scope)s;
		double value = 0;
		try {
			value = profileDB.getMetric(IdTuple.PROFILE_SUMMARY, 
											   scope.getCCTIndex(), 
											   index);
		} catch (IOException e) {
			throw new IllegalArgumentException("Cannot find metric value for " + s.toString());
		}
		if (value == 0.0d)
			return MetricValue.NONE;
		
		return new MetricValue(value);
	}


	@Override
	public BaseMetric duplicate() {
		var dupl = new HierarchicalMetric(profileDB, index, displayName);
		dupl.annotationType = annotationType;
		dupl.displayFormat  = displayFormat;
		dupl.expression     = expression.duplicate();
		dupl.metricType     = metricType;
		dupl.order          = order;
		dupl.partnerIndex  = partnerIndex;
		dupl.sampleperiod   = sampleperiod;
		
		return dupl;
	}

}
