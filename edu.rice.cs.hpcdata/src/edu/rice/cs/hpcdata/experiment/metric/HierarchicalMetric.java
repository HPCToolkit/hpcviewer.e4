package edu.rice.cs.hpcdata.experiment.metric;

import java.io.IOException;

import com.graphbuilder.math.Expression;
import com.graphbuilder.math.ExpressionTree;

import edu.rice.cs.hpcdata.db.version4.DataSummary;
import edu.rice.cs.hpcdata.experiment.scope.IMetricScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;

public class HierarchicalMetric extends AbstractMetricWithFormula 
{	
	private static final byte FMT_METADB_COMBINE_SUM = 0;
	private static final byte FMT_METADB_COMBINE_MIN = 1;
	private static final byte FMT_METADB_COMBINE_MAX = 2;

	private Expression expression;
	private DataSummary profileDB;
	private byte combineType; 
	
	private byte []psType;
	private byte []psIndex;

	public HierarchicalMetric(DataSummary profileDB, int index, String sDisplayName) {
		super(String.valueOf(index), sDisplayName);
		this.profileDB = profileDB;
		setIndex(index);
	}

	public void setPropagationScope(byte []psType, byte []psIndex) {
		this.psType = new byte[psType.length];
		this.psIndex = new byte[psIndex.length];
		for(int i=0; i<psType.length; i++) {
			this.psType[i] = psType[i];
			this.psIndex[i] = psIndex[i];
		}
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
		case FMT_METADB_COMBINE_MAX:
			return "max";
		case FMT_METADB_COMBINE_MIN:
			return "min";
		case FMT_METADB_COMBINE_SUM:
			return "sum";
		default:
			return null;
		}
	}
	
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
			throw new RuntimeException("Unknown metric combine type: " + combineType);
		}
		return target;
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
			value = profileDB.getMetric(DataSummary.INDEX_SUMMARY_PROFILE, 
											   scope.getCCTIndex(), 
											   index);
		} catch (IOException e) {
			throw new RuntimeException("Cannot find metric value for " + s.toString());
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
