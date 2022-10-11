package edu.rice.cs.hpctest.data;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.Experiment.ExperimentOpenFlag;
import edu.rice.cs.hpcdata.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpctest.util.TestDatabase;

public class ThreadDataTest 
{
	private static List<Experiment> listExperiments;
	private static List<IThreadDataCollection> listDataCollector;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		var directories = TestDatabase.getDatabases();
				
		listExperiments   = new ArrayList<>(directories.length);
		listDataCollector = new ArrayList<>(directories.length);
		
		for(var dir: directories) {
			var pathname   = dir.getAbsolutePath();							
			var experiment = new Experiment();
			experiment.open(new File(pathname), null, ExperimentOpenFlag.TREE_CCT_ONLY);
			
			var dataCollector = experiment.getThreadData();
			if (dataCollector != null) {
				listDataCollector.add(dataCollector);
				listExperiments.add(experiment);
			}
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		for(var data: listDataCollector) {
			data.dispose();
		}
		for(var exp: listExperiments) {
			if (exp != null)
				exp.dispose();
		}
	}
	
	@Test
	public void testIsAvailable() {
		for(var data: listDataCollector) {
			boolean available = data.isAvailable();
			assertTrue(available);
		}
	}
	
	
	@Test
	public void testGetRankLabels() throws IOException {
		for(var data: listDataCollector) {
			var labels = data.getRankLabels();
			assertNotNull(labels);
			
			var sparseLabels = data.getEvenlySparseRankLabels();			
			assertNotNull(sparseLabels);
			
			assertEquals(sparseLabels.length, labels.length);
			
			for(int i=1; i<sparseLabels.length; i++) {
				assertTrue(sparseLabels[i-1] <= sparseLabels[i]);
				assertTrue(labels[i-1] <= labels[i]);
			}
		}
	}

	@Test
	public void testBuild() throws Exception {
		int i=0;
		final double delta = 1.0000001f;
		
		for(var data: listDataCollector) {
			var ranks = data.getEvenlySparseRankLabels();
			assertNotNull(ranks);
			
			var idTuples = data.getIdTuples();
			assertNotNull(idTuples);
			assertTrue(idTuples.size()>0);
			
			var exp = listExperiments.get(i);
			var metrics = exp.getVisibleMetrics();
			if (metrics.size() > 0) {
				var root = exp.getRootScope(RootScopeType.CallingContextTree);
				var rawMetrics = exp.getRawMetrics();
				int rawMetricsSize = rawMetrics.size();
				
				for(var m: metrics) {
					var rawMetric = exp.getCorrespondentMetricRaw(m);
					if (rawMetric == null)
						continue;
					
					var value = data.getMetric(root.getCCTIndex(), rawMetric.getIndex(), idTuples.get(0), rawMetricsSize);
					
					final var mv = root.getMetricValue(m);
					final double control = mv != MetricValue.NONE ? mv.getValue() * delta: 0.0; 
					assertTrue(control >= value);
					
					var values = data.getMetrics(root.getCCTIndex(), rawMetric.getIndex(), rawMetricsSize);
					if (values != null && values.length>0) {
						for(int j=0; j<values.length; j++) {
							var idt = idTuples.get(j);
							value = data.getMetric(root.getCCTIndex(), rawMetric.getIndex(), idt, rawMetricsSize);
							var v = values[j];
							assertEquals(value, v, delta);
							assertTrue(control >= value);
						}
					}
				}
			}			
			i++;
		}
	}

}
