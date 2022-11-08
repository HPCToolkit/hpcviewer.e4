package edu.rice.cs.hpctest.data;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.MetricRaw;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpctest.util.TestDatabase;

public class MetricRawTest 
{
	static List<Experiment> experiments;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		var database = TestDatabase.getDatabases();
		experiments  = new ArrayList<>(database.length);

		for(var db: database) {
			var experiment = new Experiment();
			experiment.open(db, null, true);
			
			var metricRaws = experiment.getRawMetrics();
			if (metricRaws != null && !metricRaws.isEmpty()) {
				experiments.add(experiment);
			}
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		if (experiments != null)
			experiments.stream().close();
	}

	@Test
	public void testGetMetricTextValueScope() {
		for(var exp: experiments) {
			var root = exp.getRootScope(RootScopeType.CallingContextTree);
			for(var metric: exp.getRawMetrics()) {
				var text = metric.getMetricTextValue(root);
				var mv   = metric.getValue(root);
								
				assertNotNull(mv);
				assertNotNull(text);
				
				if (mv == MetricValue.NONE) 
					assertTrue(text.isEmpty());
				else
					assertTrue(!text.isEmpty());
				
				var dupl = metric.duplicate();
				assertEquals(dupl.getDisplayName(), metric.getDisplayName());
				assertTrue(dupl.equals(metric));
				
				if (metric instanceof MetricRaw) {
					MetricRaw mr = (MetricRaw) metric;
					mr.setThread(Collections.emptyList());
					assertNotNull(mr.getThread());
					assertTrue(mr.getThread().isEmpty());
					
					assertNotNull(mr.getGlob());
					
					assertEquals(mr.getRawID(), mr.getIndex());
					assertEquals(mr.getIndex(), mr.getID());
					
					mr.setMetricPartner((MetricRaw) dupl);
					assertEquals(mr.getMetricPartner(), dupl);
					
					assertTrue(mr.getSize() > 0);
				}
			}
		}
	}


	@Test
	public void testCreate() {
		for(var exp: experiments) {
			for(var metric: exp.getVisibleMetrics()) {
				var raw = MetricRaw.create(metric);
				
				assertEquals(metric.getDisplayName(), raw.getDisplayName());
			}
		}
	}
}
