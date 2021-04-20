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

	
	protected void renameExpression(OpNode node, Map<Integer, Integer> mapOldIndex) {
		Expression left = node.getLeftChild();
		Expression right = node.getRightChild();
		
		renameExpression(left,  mapOldIndex);
		renameExpression(right, mapOldIndex);
	}
	
	
	protected void renameExpression(VarNode node, Map<Integer, Integer> mapOldIndex) {
		String name = node.getName();
		char prefix = name.charAt(0);
		if (prefix == '$' || prefix == '@') {
			String varIndex = name.substring(1);
			Integer intIndex = Integer.valueOf(varIndex);
			Integer newIndex = mapOldIndex.get(intIndex);
			if (newIndex != null) {
				String newStrIndex = prefix + String.valueOf(newIndex);
				node.setName(newStrIndex);
			}
		}
	}
	
	
	protected void renameExpression(FuncNode node, Map<Integer, Integer> mapOldIndex) {
		int n = node.numChildren();
		for(int i=0; i<n; i++) {
			renameExpression(node.child(i), mapOldIndex);
		}
	}

	
	protected void renameExpression(Expression node, Map<Integer, Integer> mapOldIndex) {
		if (node instanceof OpNode) {
			renameExpression((OpNode)node, mapOldIndex);
		} else if (node instanceof VarNode) {
			renameExpression((VarNode)node, mapOldIndex);
		} else if (node instanceof FuncNode) {
			renameExpression((FuncNode) node, mapOldIndex);
		}
	}
}
