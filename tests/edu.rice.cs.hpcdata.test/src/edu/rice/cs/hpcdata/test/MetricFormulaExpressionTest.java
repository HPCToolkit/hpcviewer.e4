package edu.rice.cs.hpcdata.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.graphbuilder.math.Expression;
import com.graphbuilder.math.ExpressionTree;

import edu.rice.cs.hpcdata.experiment.metric.MetricFormulaExpression;

public class MetricFormulaExpressionTest {

	@Test
	public void testRename() {
		final Map<Integer, Integer> mapOldIndex = new HashMap<>();
		final Map<Integer, Integer> mapNewIndex = new HashMap<>();
		
		int j=0;
		for(int i=100; i<400; i++, j++) {
			mapOldIndex.put(i, j);
			mapNewIndex.put(j, i);
		}
		final String []formula = new String[] {	"$100+@110/$120", 
												"10+10+20", 
												"$110" , 
												"sum($120, $123, $133)",
												"10+$140"};
		
		// the test consists of converting the original index to the new index,
		// then converting again the new index to the old one.
		// the test is valid if the later is equivalent tot he original formula
		
		for(String f: formula) {
			// convert to the new index
			Expression e = ExpressionTree.parse(f);
			assertNotNull(e);
			
			String fOrig = e.toString();
			MetricFormulaExpression.rename(e, mapOldIndex, null);

			String f2 = e.toString();
			// stupid test: make sure the converted formula has no old index
			mapOldIndex.forEach((k, v)-> assertFalse(f2.contains(String.valueOf(k))) );

			// convert back the new index to the original one
			Expression e2 = ExpressionTree.parse(f2);
			assertNotNull(e2);
			
			MetricFormulaExpression.rename(e2, mapNewIndex, null);
			
			// the test is valid if the last conversion is the same as the original one.
			assertTrue(fOrig.compareTo(e2.toString())==0);
		}
	}

}
