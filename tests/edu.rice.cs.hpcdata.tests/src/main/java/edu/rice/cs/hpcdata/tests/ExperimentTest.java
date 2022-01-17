package edu.rice.cs.hpcdata.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric.AnnotationType;
import edu.rice.cs.hpcdata.experiment.metric.DerivedMetric;
import edu.rice.cs.hpcdata.experiment.metric.MetricType;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;

class ExperimentTest {

	private static Experiment experiment;
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		Path resource = Paths.get("src", "main", "resources", "bug-no-gpu-trace");
		File database = resource.toFile();
		
		assertNotNull(database);
		
		System.out.println(database.getAbsolutePath());
		
		experiment = new Experiment();
		experiment.open(database, null, Experiment.ExperimentOpenFlag.TREE_ALL);
		
		assertNotNull(experiment.getRootScope());
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testIsMergedDatabase() {
		assertFalse(experiment.isMergedDatabase());
	}

	@Test
	void testCreateFlatView() {
		RootScope root = experiment.getRootScope(RootScopeType.Flat);
		assertNotNull(root);
		assertNotNull(root.getChildren());
		assertTrue(root.getChildCount() > 0);
	}

	@Test
	void testGetRawMetrics() {
		List<BaseMetric> metrics = experiment.getRawMetrics();
		assertNotNull(metrics);
		assertTrue(metrics.size() == 0);
	}

	@Test
	void testGetVisibleMetrics() {
		List<BaseMetric> metrics = experiment.getVisibleMetrics();
		assertNotNull(metrics);
		assertTrue(metrics.size() > 1);
	}

	@Test
	void testGetNonEmptyMetricIDs() {
		RootScope root = experiment.getRootScope(RootScopeType.CallingContextTree);
		List<Integer> metrics = experiment.getNonEmptyMetricIDs(root);
		assertNotNull(metrics);
		assertTrue(metrics.size() > 1);
	}

	@Test
	void testGetMetricCount() {
		assertTrue(experiment.getMetricCount() > 1);
	}

	@Test
	void testGetMetricInt() {
		List<BaseMetric> list = experiment.getVisibleMetrics();
		BaseMetric metric = experiment.getMetric(list.get(0).getIndex());
		assertNotNull(metric);
		assertEquals(list.get(0), metric);
	}

	@Test
	void testGetMetricString() {
		BaseMetric metric = experiment.getMetric("732");
		assertNotNull(metric);
	}

	@Test
	void testGetMetricFromOrder() {
		BaseMetric metric = experiment.getMetricFromOrder(0);
		assertNotNull(metric);
	}

	@Test
	void testAddDerivedMetric() {
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
	void testGetRootScope() {
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
	void testGetDataSummary() {
		Executable exec = new Executable() {
			
			@Override
			public void execute() throws Throwable {
				experiment.getDataSummary();
			}
		};
		assertThrows(IOException.class, exec);
	}

	@Test
	void testGetThreadData() {
		assertNull(experiment.getThreadData());
	}

	@Test
	void testGetMajorVersion() {
		assertTrue(experiment.getMajorVersion() > 0);
	}

	@Test
	void testGetMinorVersion() {
		assertTrue(experiment.getMinorVersion() == 0);
	}

	@Test
	void testGetMaxDepth() {
		assertTrue(experiment.getMaxDepth() > 2);
	}

	@Test
	void testGetScopeMap() {
		assertNotNull(experiment.getScopeMap());
	}

	@Test
	void testGetRootScopeChildren() {
		fail("Not yet implemented");
	}

	@Test
	void testGetRootScopeRootScopeType() {
		fail("Not yet implemented");
	}

	@Test
	void testOpenFileIUserDataOfStringStringBoolean() {
		Experiment exp = new Experiment();
		Executable exec = new Executable() {
			
			@Override
			public void execute() throws Throwable {
				exp.open(experiment.getDefaultDirectory(), null, true);
			}
		};
		assertDoesNotThrow(exec);
		assertTrue(exp.getName().equals(experiment.getName()));
	}

	@Test
	void testGetName() {
		assertNotNull(experiment.getName());
	}

	@Test
	void testGetConfiguration() {
		assertNotNull(experiment.getConfiguration());
	}

	@Test
	void testGetDefaultDirectory() {
		assertNotNull(experiment.getDefaultDirectory());
	}

	@Test
	void testGetXMLExperimentFile() {
		assertNotNull(experiment.getXMLExperimentFile());
	}

}
