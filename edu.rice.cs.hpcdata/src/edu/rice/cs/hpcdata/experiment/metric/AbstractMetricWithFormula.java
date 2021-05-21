package edu.rice.cs.hpcdata.experiment.metric;

import java.util.Map;

import com.graphbuilder.math.Expression;
import com.graphbuilder.math.FuncNode;
import com.graphbuilder.math.OpNode;
import com.graphbuilder.math.VarNode;

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
			renameExpression(expression, mapOldIndex, mapOldOrder);
		}
	}
	
	
	private void renameExpression(OpNode node, Map<Integer, Integer> mapOldIndex, Map<Integer, Integer> mapOldOrder) {
		Expression left = node.getLeftChild();
		Expression right = node.getRightChild();
		
		renameExpression(left,  mapOldIndex, mapOldOrder);
		renameExpression(right, mapOldIndex, mapOldOrder);
	}
	
	private void renameExpression(VarNode node, Map<Integer, Integer> mapOldIndex, Map<Integer, Integer> mapOldOrder) {
		String name = node.getName();
		char prefix = name.charAt(0);
		
		if (prefix == '$' || prefix == '@') {
			// index-based formula
			changeName(node, prefix, mapOldIndex);
		} else if (prefix == '#') {
			// order-based formula
			changeName(node, prefix, mapOldOrder);
		}
		//System.out.println("renameExpression " + name + " -> " + node.getName());
	}

	private void changeName(VarNode node, char prefix, Map<Integer, Integer> map) {
		String name = node.getName();
		String varIndex = name.substring(1);
		Integer intIndex = Integer.valueOf(varIndex);
		Integer newIndex = map.get(intIndex);
		if (newIndex != null) {
			String newStrIndex = prefix + String.valueOf(newIndex);
			node.setName(newStrIndex);
		}
	}
	
	private void renameExpression(FuncNode node, Map<Integer, Integer> mapOldIndex, Map<Integer, Integer> mapOldOrder) {
		int children = node.numChildren();
		for(int i=0; i<children; i++) {
			Expression e = node.child(i);
			renameExpression(e, mapOldIndex, mapOldOrder);
		}
	}
	
	private void renameExpression(Expression node, Map<Integer, Integer> mapOldIndex, Map<Integer, Integer> mapOldOrder) {
		if (node instanceof OpNode) {
			renameExpression((OpNode)node, mapOldIndex, mapOldOrder);
		} else if (node instanceof VarNode) {
			renameExpression((VarNode)node, mapOldIndex, mapOldOrder);
		} else if (node instanceof FuncNode) {
			renameExpression((FuncNode) node, mapOldIndex, mapOldOrder);
		}
	}

	protected abstract Expression[] getExpressions();
}
