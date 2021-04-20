package edu.rice.cs.hpc.data.experiment.metric;

import java.util.Map;

import com.graphbuilder.math.Expression;
import com.graphbuilder.math.FuncNode;
import com.graphbuilder.math.OpNode;
import com.graphbuilder.math.VarNode;


public abstract class AbstractMetricWithFormula extends BaseMetric implements IMetricMutable 
{

	public AbstractMetricWithFormula(String sID, String sDisplayName, String sDescription, VisibilityType displayed,
			String format, AnnotationType annotationType, int index, int partner_index, MetricType type) {

		super(sID, sDisplayName, sDescription, displayed, format, annotationType, index, partner_index, type);
	}

	
	protected void renameExpression(OpNode node, Map<Integer, Integer> mapOldIndex, Map<Integer, Integer> mapOldOrder) {
		Expression left = node.getLeftChild();
		Expression right = node.getRightChild();
		
		renameExpression(left,  mapOldIndex, mapOldOrder);
		renameExpression(right, mapOldIndex, mapOldOrder);
	}
	
	
	protected void renameExpression(VarNode node, Map<Integer, Integer> mapOldIndex, Map<Integer, Integer> mapOldOrder) {
		String name = node.getName();
		char prefix = name.charAt(0);
		if (prefix == '$' || prefix == '@' || prefix == '#') {
			String varIndex = name.substring(1);
			Integer intIndex = Integer.valueOf(varIndex);
			Integer newIndex = null;
			if (prefix == '#') {
				newIndex = mapOldOrder.get(intIndex);
			} else {
				newIndex = mapOldIndex.get(intIndex);
			}
			if (newIndex != null) {
				String newStrIndex = prefix + String.valueOf(newIndex);
				node.setName(newStrIndex);
			}
		}
	}
	
	
	protected void renameExpression(FuncNode node, Map<Integer, Integer> mapOldIndex, Map<Integer, Integer> mapOldOrder) {
		int n = node.numChildren();
		for(int i=0; i<n; i++) {
			renameExpression(node.child(i), mapOldIndex, mapOldOrder);
		}
	}

	
	protected void renameExpression(Expression node, Map<Integer, Integer> mapOldIndex, Map<Integer, Integer> mapOldOrder) {
		if (node instanceof OpNode) {
			renameExpression((OpNode)node, mapOldIndex, mapOldOrder);
		} else if (node instanceof VarNode) {
			renameExpression((VarNode)node, mapOldIndex, mapOldOrder);
		} else if (node instanceof FuncNode) {
			renameExpression((FuncNode) node, mapOldIndex, mapOldOrder);
		}
	}
}
