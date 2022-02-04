package edu.rice.cs.hpcdata.experiment.metric;

import java.util.Map;

import com.graphbuilder.math.Expression;

/********************************************************************
 * 
 * Base class for metrics with formula.
 * This class allows to rename the formula.
 *
 ********************************************************************/
public abstract class AbstractMetricWithFormula extends BaseMetric
{
	
	public AbstractMetricWithFormula(String sID, String sDisplayName, String sDescription, VisibilityType displayed,
			String format, AnnotationType annotationType, int index, int partner_index, MetricType type) {
		super(sID, sDisplayName, sDescription, displayed, format, annotationType, index, partner_index, type);
	}

	/***
	 * Rename metric index variable using map
	 * 
	 * @param mapOldIndex from old index to a new one
	 * 
	 */
	public void renameExpression(Map<Integer, Integer> mapOldIndex, Map<Integer, Integer> mapOldOrder) {
		Expression []expressions = getExpressions();
		for(Expression expression : expressions) {
			MetricFormulaExpression.rename(expression, mapOldIndex, mapOldOrder);
		}
	}

	protected abstract Expression[] getExpressions();
}
