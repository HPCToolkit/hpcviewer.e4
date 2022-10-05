package edu.rice.cs.hpctest.data;

import static org.junit.Assert.*;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.merge.DatabasesToMerge;
import edu.rice.cs.hpcdata.merge.ExperimentMerger;
import edu.rice.cs.hpctest.util.DataFactory;

public class ExperimentMergerTest 
{
	private static List<Experiment> listExperiments;

	@BeforeClass
	public static void setUpBeforeClass() throws NoSuchAlgorithmException  {
		listExperiments = DataFactory.createExperiments(2);
	}

	@Test
	public void testMergeDatabasesToMerge() {
		DatabasesToMerge db = new DatabasesToMerge();
		db.experiment[0] = listExperiments.get(0);
		db.experiment[1] = listExperiments.get(1);
		db.type = RootScopeType.CallingContextTree;
		
		try {
			Experiment merged = ExperimentMerger.merge(db);
			assertNotNull(merged);
			
			int numMetrics1 = db.experiment[0].getMetricCount();
			int numMetrics2 = db.experiment[1].getMetricCount();
			int numMetrics3 = merged.getMetricCount();
			
			assertTrue(numMetrics3 == numMetrics1 + numMetrics2);

			assertNotNull(merged.getRootScope());
			RootScope rootCCT = merged.getRootScope(RootScopeType.CallingContextTree);

			assertNotNull(rootCCT);
			assertNotNull(rootCCT.getChildren());
			
			assertTrue(rootCCT.getSubscopeCount() >= db.experiment[0].getRootScope(RootScopeType.CallingContextTree).getSubscopeCount());
			
			String mergedName = ExperimentMerger.generateMergeName(db.experiment[0], db.experiment[1]);
			assertNotNull(mergedName);
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
}
