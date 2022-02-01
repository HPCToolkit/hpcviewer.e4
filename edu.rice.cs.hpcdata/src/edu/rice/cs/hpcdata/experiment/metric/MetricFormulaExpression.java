package edu.rice.cs.hpcdata.experiment.metric;

import java.util.Map;

import com.graphbuilder.math.Expression;
import com.graphbuilder.math.FuncNode;
import com.graphbuilder.math.OpNode;
import com.graphbuilder.math.VarNode;


/*************************************************************************
 * 
 * Class to operate and modify the metric formula expression.
 * The goal is to enable to rename the formula expression 
 *
 *************************************************************************/
public class MetricFormulaExpression 
{
	/****
	 * Rename the math expression given the mapping of metric index.
	 *  
	 * @param expression the math expression to modify
	 * @param mapOldIndex the mapping from old index to the new index
 	 * @param mapOldOrder optional, the mapping from old order to the new order
	 */
	public static void rename(Expression expression, Map<Integer, Integer> mapOldIndex, Map<Integer, Integer> mapOldOrder) {
		renameExpression(expression, mapOldIndex, mapOldOrder);
	}
	
	
	private static void renameExpression(OpNode node, Map<Integer, Integer> mapOldIndex, Map<Integer, Integer> mapOldOrder) {
		Expression left = node.getLeftChild();
		Expression right = node.getRightChild();
		
		renameExpression(left,  mapOldIndex, mapOldOrder);
		renameExpression(right, mapOldIndex, mapOldOrder);
	}
	
	private static void renameExpression(VarNode node, Map<Integer, Integer> mapOldIndex, Map<Integer, Integer> mapOldOrder) {
		String name = node.getName();
		char prefix = name.charAt(0);
		
		if (prefix == '$' || prefix == '@') {
			// index-based formula
			changeName(node, prefix, mapOldIndex);
		} else if (prefix == '#') {
			// order-based formula
			changeName(node, prefix, mapOldOrder);
		}
	}

	private static void changeName(VarNode node, char prefix, Map<Integer, Integer> map) {
		if (map == null)
			return;
		
		String name = node.getName();
		String varIndex = name.substring(1);
		Integer intIndex = Integer.valueOf(varIndex);
		Integer newIndex = map.get(intIndex);
		if (newIndex != null) {
			String newStrIndex = prefix + String.valueOf(newIndex);
			node.setName(newStrIndex);
		}
	}
	
	private static void renameExpression(FuncNode node, Map<Integer, Integer> mapOldIndex, Map<Integer, Integer> mapOldOrder) {
		int children = node.numChildren();
		for(int i=0; i<children; i++) {
			Expression e = node.child(i);
			renameExpression(e, mapOldIndex, mapOldOrder);
		}
	}
	
	private static void renameExpression(Expression node, Map<Integer, Integer> mapOldIndex, Map<Integer, Integer> mapOldOrder) {
		if (node instanceof OpNode) {
			renameExpression((OpNode)node, mapOldIndex, mapOldOrder);
		} else if (node instanceof VarNode) {
			renameExpression((VarNode)node, mapOldIndex, mapOldOrder);
		} else if (node instanceof FuncNode) {
			renameExpression((FuncNode) node, mapOldIndex, mapOldOrder);
		}
	}
}
