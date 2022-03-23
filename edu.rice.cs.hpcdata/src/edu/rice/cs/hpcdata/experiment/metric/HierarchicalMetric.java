package edu.rice.cs.hpcdata.experiment.metric;

import java.io.IOException;

import com.graphbuilder.math.Expression;
import com.graphbuilder.math.ExpressionTree;

import edu.rice.cs.hpcdata.db.version4.DataSummary;
import edu.rice.cs.hpcdata.experiment.scope.IMetricScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;

public class HierarchicalMetric extends AbstractMetricWithFormula 
{	
	private static final byte FMT_METADB_COMBINE_Sum = 0;
	private static final byte FMT_METADB_COMBINE_Min = 1;
	private static final byte FMT_METADB_COMBINE_Max = 2;

	private Expression expression;
	private DataSummary profileDB;
	private byte combineType; 

	public HierarchicalMetric(DataSummary profileDB, int index, String sDisplayName) {
		super(String.valueOf(index), sDisplayName);
		this.profileDB = profileDB;
		setIndex(index);
	}


	public void setFormula(String formula) {
		expression = ExpressionTree.parse(formula);
	}
	
	
	public void setProfileDatabase(DataSummary profileDB) {
		this.profileDB = profileDB;
	}
	
	
	public void setCombineType(byte type) {
		this.combineType = type;
	}
	
	public String getCombineTypeLabel() {
		switch (combineType) {
		case FMT_METADB_COMBINE_Max:
			return "max";
		case FMT_METADB_COMBINE_Min:
			return "min";
		case FMT_METADB_COMBINE_Sum:
			return "sum";
		}
		return null;
	}
	
	@Override
	public String getDisplayName() {
		String name = super.getDisplayName();
		if (getMetricType() == MetricType.EXCLUSIVE)
			return name + " (E)";
		else
			return name + " (I)";
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
			value = profileDB.getMetric(DataSummary.INDEX_SUMMARY_PROFILE, 
											   scope.getCCTIndex(), 
											   index);
		} catch (IOException e) {
			throw new RuntimeException("Cannot find metric value for " + s.toString());
		}
		if (value == 0.0d)
			return MetricValue.NONE;
		
		MetricValue mv = new MetricValue(value);
		return mv;
	}


	@Override
	public BaseMetric duplicate() {
		var dupl = new HierarchicalMetric(profileDB, index, displayName);
		dupl.annotationType = annotationType;
		dupl.displayFormat  = displayFormat;
		dupl.expression     = expression.duplicate();
		dupl.metricType     = metricType;
		dupl.order          = order;
		dupl.partner_index  = partner_index;
		dupl.sampleperiod   = sampleperiod;
		
		return dupl;
	}

}
