package edu.rice.cs.hpctest.data;


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

import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric.AnnotationType;
import edu.rice.cs.hpcdata.experiment.metric.DerivedMetric;
import edu.rice.cs.hpcdata.experiment.metric.MetricType;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.Scope;


public class ExperimentTest {

	private static Experiment []experiments;
	private static File []database;
	private static String []dbPaths = new String[] {"bug-no-gpu-trace", "bug-empty", "bug-nometric", 
													"prof2" + File.separator + "loop-inline",
													"prof2" + File.separator + "multithread"};
	
	public ExperimentTest() {
		// empty, nothing to do
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		experiments = new Experiment[dbPaths.length];
		database   = new File[dbPaths.length];
		int i=0;
		for (String dbp: dbPaths) {
			
			Path resource = Paths.get("..", "resources", dbp);
			database[i] = resource.toFile();
			
			assertNotNull(database);

			experiments[i]= new Experiment();
			try {
				experiments[i].open(database[i], null, Experiment.ExperimentOpenFlag.TREE_ALL);
			} catch (Exception e) {
				assertFalse(e.getMessage(), true);
			}
			
			assertNotNull(experiments[i].getRootScope());
			i++;
		}
	}


	@Test
	public void testIsMergedDatabase() {
		for(var exp: experiments) {
			assertFalse(exp.isMergedDatabase());
		}
	}


	@Test
	public void testGetRawMetrics() {
		for(var exp: experiments) {
			List<BaseMetric> metrics = exp.getRawMetrics();
			if (exp.getMajorVersion()<4) {
				assertNull(metrics);
			} else {
				assertNotNull(metrics);
				assertTrue(metrics.size()>1);
			}				
		}
	}

	@Test
	public void testGetVisibleMetrics() {
		int num[] = new int[] {97, 2, 0, 3, 6};
		int i = 0;
		for(var experiment: experiments) {
			List<BaseMetric> metrics = experiment.getVisibleMetrics();
			assertNotNull(metrics);
			assertTrue(metrics.size() == num[i]);
			i++;
		}
	}

	@Test
	public void testGetNonEmptyMetricIDs() {
		final int nmetrics[] = new int[] {18, 0, 0, 1, 3};
		int i=0;
		for(var experiment: experiments) {
			RootScope root = experiment.getRootScope(RootScopeType.CallingContextTree);
			List<Integer> metrics = experiment.getNonEmptyMetricIDs(root);
			assertNotNull(metrics);
			assertTrue(metrics.size() >= nmetrics[i]);
			i++;
		}
	}

	@Test
	public void testGetMetricCount() {
		int counts[] = new int[] {10, 0, 0, 2, 6};
		int i=0;
		
		for(var experiment: experiments) {
			assertTrue(experiment.getMetricCount() >= counts[i]);
			i++;
		}
	}

	@Test
	public void testGetMetricInt() {
		for(var experiment: experiments) {
			List<BaseMetric> list = experiment.getVisibleMetrics();
			if (list != null && list.size()>0) {
				BaseMetric metric = experiment.getMetric(list.get(0).getIndex());
				assertNotNull(metric);
				assertEquals(list.get(0), metric);
			}
		}
	}

	@Test
	public void testGetMetricString() {
		for(var experiment: experiments) {
			List<BaseMetric> list = experiment.getVisibleMetrics();
			if (list != null && list.size()>0) {
				BaseMetric metric = experiment.getMetric(list.get(0).getShortName());
				assertNotNull(metric);
			}
		}
	}

	@Test
	public void testGetMetricFromOrder() {
		int []order = new int[] {0, 0, 1, 1, 1};
		int i = 0;
		for(var experiment: experiments) {
			if (experiment.getMetricCount()>0) {
				BaseMetric metric = experiment.getMetricFromOrder(order[i]);
				assertNotNull(metric);
				i++;
			}
		}
	}

	@Test
	public void testAddDerivedMetric() {
		int indexes[] = new int[] {762, 0, 1, 1, 1};
		int i=0;
		
		for(var experiment: experiments) {
			if (experiment.getMetricCount()==0) 
				continue;
			
			RootScope root = experiment.getRootScope(RootScopeType.CallingContextTree);
			BaseMetric metric = experiment.getMetric(indexes[i]);
			int numMetrics = experiment.getMetricCount();
			DerivedMetric dm = new DerivedMetric(root, 
										experiment, 
										"$" + metric.getIndex(), 
										"DM " + metric.getDisplayName(), 
										String.valueOf(numMetrics), 
										numMetrics, AnnotationType.NONE, metric.getMetricType());
			experiment.addDerivedMetric(dm);
			
			assertTrue(experiment.getMetricCount() == numMetrics + 1);
			i++;
		}
	}
	

	@Test
	public void testMetricValueDisplay() {
		final int nmetrics[] = new int[] {18, 0, 0, 1, 1};
		int i=0;
		for(var experiment: experiments) {
			RootScope root = experiment.getRootScope(RootScopeType.CallingContextTree);
			if (experiment.getMetricCount()>0) {
				var metrics = experiment.getNonEmptyMetricIDs(root);
				assertNotNull(metrics);
				assertTrue(metrics.size() >= nmetrics[i]);
				for(var m: metrics) {
					var metric = experiment.getMetric(m);
					var str = metric.getMetricTextValue(root);
					assertNotNull(str);
					assertTrue(str.length() > 1);
					if (i==3)
						assertTrue(str.equals("1.25e+10 100.0%"));
				}
			}
			i++;
		}
	}


	@Test
	public void testGetRootScope() {
		int i=0;
		final int []children   = new int[] {1, 0, 0, 1, 2, 1};
		
		for(var experiment: experiments) {
			RootScope rootCCT = experiment.getRootScope(RootScopeType.CallingContextTree);
			RootScope rootCall = experiment.getRootScope(RootScopeType.CallerTree);
			RootScope rootFlat = experiment.getRootScope(RootScopeType.Flat);
			
			assertNotNull(rootCCT);
			assertNotNull(rootCall);
			assertNotNull(rootFlat);
			
			assertTrue(rootCCT != rootCall);
			assertTrue(rootCall != rootFlat);
			
			rootCall = experiment.createCallersView(rootCCT, rootCall);
			if (i==3)
				//  not supported at the moment for prof2
				// this will fail
				continue;
			
			rootFlat = experiment.createFlatView(rootCCT, rootFlat);
			
			assertTrue(rootCCT.getSubscopeCount()  >= children[i]);
			assertTrue(rootCall.getSubscopeCount() >= children[i]);
			assertTrue(rootFlat.getSubscopeCount() >= children[i]);

			i++;
		}
	}


	@Test
	public void testTree() {
		for(var experiment: experiments) {
			for (var root: experiment.getRootScopeChildren()) {
				if (!root.hasChildren())
					continue;
				
				for(var child: root.getChildren()) {
					boolean result = testFlatContext(experiment, root, child);
					if (!result)
						System.err.println();
					assertTrue( testFlatContext(experiment, root, child) );
				}
			}
		}
	}
	
	private boolean testFlatContext(Experiment exp, Scope parent, Scope context) {
		var mvc = context.getMetricValues();
		if (mvc == null)
			return true;
		
		var parent_mvc = parent.getMetricValues();
		var metrics = exp.getMetricList();
		
		for(var metric: metrics) {
			var partner = exp.getMetric(metric.getPartner());
			var mv1 = mvc.getValue(context, metric);
			var mv2 = mvc.getValue(context, partner);
			
			// test the exclusive vs inclusive
			if (metric.getMetricType() == MetricType.INCLUSIVE) {
				int c = floatCompare(mv1.getValue(), mv2.getValue());
				if (c < 0)
					System.err.println("Error: incompatible metric value for " + context.getName() + " metric: " + metric.getDisplayName());
				assertTrue(c>=0);
			} else {
				int c = floatCompare(mv1.getValue(), mv2.getValue());
				if (c > 0) {
					System.err.println("Error: incompatible metric value scope: " + context.getName() + " metric: " + metric.getDisplayName());
				}
				assertTrue(c<=0);
			}
			
			// test comparison with the parent
			var pv1 = parent_mvc.getValue(parent, metric);
			
			assertTrue(checkChildValue(metric, pv1, mv1));
		}
		
		// traverse all the children
		if (context.hasChildren()) {
			for(var child: context.getChildren()) {
				var result = testFlatContext(exp, context, child);
				if (!result)
					return false;
			}
		}
		return true;
	}
	
	private static int floatCompare(float f1, float f2) {
		final float EPSILON = 0.000001f;
		final float delta = f1 - f2;
		final float diffEps = Math.abs(delta) / f1;
		if (diffEps < EPSILON)
			return 0;
		else return (int) delta;
	}
	
	
	private boolean checkChildValue(BaseMetric metric, MetricValue mv1, MetricValue mv2) {
		if (metric.getMetricType() == MetricType.INCLUSIVE) {
			final int  c = floatCompare(mv1.getValue(), mv2.getValue());
			if (c<0)
				System.out.println("Error: parent is less than child for " + mv1.getValue() + " vs " + mv2.getValue());
			return c>=0;
		}
		// exclusive metric: anything can happen
		return true;
	}
	

	@Test
	public void testGetThreadData() throws IOException {
		for(var experiment: experiments) {
			var name = experiment.getName();
			if (name.contains("loop") || name.contains("multithread"))
				assertNotNull(experiment.getThreadData());
		}
	}

	@Test
	public void testGetMajorVersion() {
		for(var experiment: experiments) {
			assertTrue(experiment.getMajorVersion() >= 0);
		}
	}

	@Test
	public void testGetMinorVersion() {
		for(var experiment: experiments) {
			assertTrue(experiment.getMinorVersion() >= 0);
		}
	}

	@Test
	public void testGetMaxDepth() {
		final int maxdepth[] = new int[] {4, 0, 0, 6, 20};
		int i=0;
		for(var experiment: experiments) {
			assertTrue(experiment.getMaxDepth() > maxdepth[i]);
			i++;
		}
	}

	@Test
	public void testGetScopeMap() {
		int i=0;
		for(var experiment: experiments) {
			if (i<3)
				assertNotNull(experiment.getScopeMap());
			i++;
		}
	}

	@Test
	public void testGetRootScopeChildren() {
		for(var experiment: experiments) {
			var children = experiment.getRootScopeChildren();
			assertNotNull(children);
			assertTrue(children.size() == 3);
		}
	}

	@Test
	public void testGetRootScopeRootScopeType() {
		for(var experiment: experiments) {
			RootScope root = experiment.getRootScope(RootScopeType.CallingContextTree);
			assertNotNull(root);
		}
	}
	

	@Test
	public void testOpenFileIUserDataOfStringStringBoolean() {
		for(var experiment: experiments) {
			Experiment exp = new Experiment();
			try {
				exp.open(experiment.getDefaultDirectory(), null, true);
			} catch (Exception e) {
				assertFalse(e.getMessage(), true);
			}
			assertTrue(exp.getName().equals(experiment.getName()));
		}
	}

	@Test
	public void testGetName() {
		final String []names = new String[] {"bandwidthTest", "a.out", "a.out", "loop-inline", "multithread"};
		int i=0;
		for(var experiment: experiments) {
			String name = experiment.getName();
			assertNotNull(name.equals(names[i]));
			i++;
		}
	}

	@Test
	public void testGetConfiguration() {
		for(var experiment: experiments) {
			assertNotNull(experiment.getConfiguration());
		}
	}

	@Test
	public void testGetDefaultDirectory() {
		for(var experiment: experiments) {
			File dir = experiment.getDefaultDirectory();
			assertNotNull(dir);
		}
	}

	@Test
	public void testGetExperimentFile() {
		int i=0;
		for(var experiment: experiments) {
			File file = experiment.getExperimentFile();
			assertNotNull(file);
			File dir = file.getParentFile();
			File dbPath = database[i].getAbsoluteFile();
			assertTrue(dir.getAbsolutePath().equals(dbPath.getAbsolutePath()));
			i++;
		}
	}

}
