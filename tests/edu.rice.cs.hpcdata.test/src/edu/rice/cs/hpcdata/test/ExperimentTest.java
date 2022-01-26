package edu.rice.cs.hpcdata.test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric.AnnotationType;
import edu.rice.cs.hpcdata.experiment.metric.DerivedMetric;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.TreeNode;

public class ExperimentTest {

	private static Experiment experiment;
	private static File database;
	
	public ExperimentTest() {
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Path resource = Paths.get("..", "resources", "bug-no-gpu-trace");
		database = resource.toFile();
		
		assertNotNull(database);
		
		System.out.println("Test database: " + database.getAbsolutePath());

		experiment = new Experiment();
		try {
			experiment.open(database, null, Experiment.ExperimentOpenFlag.TREE_ALL);
		} catch (Exception e) {
			assertFalse(e.getMessage(), true);
		}
		
		assertNotNull(experiment.getRootScope());
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testIsMergedDatabase() {
		assertFalse(experiment.isMergedDatabase());
	}


	@Test
	public void testGetRawMetrics() {
		List<BaseMetric> metrics = experiment.getRawMetrics();
		assertNull(metrics);
	}

	@Test
	public void testGetVisibleMetrics() {
		List<BaseMetric> metrics = experiment.getVisibleMetrics();
		assertNotNull(metrics);
		assertTrue(metrics.size() > 1);
	}

	@Test
	public void testGetNonEmptyMetricIDs() {
		RootScope root = experiment.getRootScope(RootScopeType.CallingContextTree);
		List<Integer> metrics = experiment.getNonEmptyMetricIDs(root);
		assertNotNull(metrics);
		assertTrue(metrics.size() > 1);
	}

	@Test
	public void testGetMetricCount() {
		assertTrue(experiment.getMetricCount() > 1);
	}

	@Test
	public void testGetMetricInt() {
		List<BaseMetric> list = experiment.getVisibleMetrics();
		BaseMetric metric = experiment.getMetric(list.get(0).getIndex());
		assertNotNull(metric);
		assertEquals(list.get(0), metric);
	}

	@Test
	public void testGetMetricString() {
		BaseMetric metric = experiment.getMetric("732");
		assertNotNull(metric);
	}

	@Test
	public void testGetMetricFromOrder() {
		BaseMetric metric = experiment.getMetricFromOrder(0);
		assertNotNull(metric);
	}

	@Test
	public void testAddDerivedMetric() {
		RootScope root = experiment.getRootScope(RootScopeType.CallingContextTree);
		BaseMetric metric = experiment.getMetric(762);
		int numMetrics = experiment.getMetricCount();
		DerivedMetric dm = new DerivedMetric(root, 
									experiment, 
									"$762", 
									"DM " + metric.getDisplayName(), 
									String.valueOf(numMetrics), 
									numMetrics, AnnotationType.NONE, metric.getMetricType());
		experiment.addDerivedMetric(dm);
		
		assertTrue(experiment.getMetricCount() == numMetrics + 1);
	}

	@Test
	public void testGetRootScope() {
		RootScope rootCCT = experiment.getRootScope(RootScopeType.CallingContextTree);
		RootScope rootCall = experiment.getRootScope(RootScopeType.CallerTree);
		RootScope rootFlat = experiment.getRootScope(RootScopeType.Flat);
		
		assertNotNull(rootCCT);
		assertNotNull(rootCall);
		assertNotNull(rootFlat);
		
		assertTrue(rootCCT != rootCall);
		assertTrue(rootCall != rootFlat);
	}

	@Test
	public void testGetDataSummary() {

		try {
			experiment.getDataSummary();
			assertFalse(true);
			
		} catch (IOException e) {
			assertTrue("Correct: no summary", true);
		}

	}

	@Test
	public void testGetThreadData() {
		assertNull(experiment.getThreadData());
	}

	@Test
	public void testGetMajorVersion() {
		assertTrue(experiment.getMajorVersion() == 2);
	}

	@Test
	public void testGetMinorVersion() {
		assertTrue(experiment.getMinorVersion() == 2);
	}

	@Test
	public void testGetMaxDepth() {
		assertTrue(experiment.getMaxDepth() == 40);
	}

	@Test
	public void testGetScopeMap() {
		assertNotNull(experiment.getScopeMap());
	}

	@Test
	public void testGetRootScopeChildren() {
		List<TreeNode> children = experiment.getRootScopeChildren();
		assertNotNull(children);
		assertTrue(children.size() == 3);
	}

	@Test
	public void testGetRootScopeRootScopeType() {
		RootScope root = experiment.getRootScope(RootScopeType.CallingContextTree);
		assertNotNull(root);
	}
	

	@Test
	public void testOpenFileIUserDataOfStringStringBoolean() {
		Experiment exp = new Experiment();

		try {
			exp.open(experiment.getDefaultDirectory(), null, true);
		} catch (Exception e) {
			assertFalse(e.getMessage(), true);
		}

		assertTrue(exp.getName().equals(experiment.getName()));
	}

	@Test
	public void testGetName() {
		String name = experiment.getName();
		assertNotNull(name.equals("bandwidthTest"));
	}

	@Test
	public void testGetConfiguration() {
		assertNotNull(experiment.getConfiguration());
	}

	@Test
	public void testGetDefaultDirectory() {
		File dir = experiment.getDefaultDirectory();
		assertNotNull(dir);
	}

	@Test
	public void testGetXMLExperimentFile() {
		File file = experiment.getExperimentFile();
		assertNotNull(file);
		File dir = file.getParentFile();
		File dbPath = database.getAbsoluteFile();
		assertTrue(dir.getAbsolutePath().equals(dbPath.getAbsolutePath()));
	}

}
