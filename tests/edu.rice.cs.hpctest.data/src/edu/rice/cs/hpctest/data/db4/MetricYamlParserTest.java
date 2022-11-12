package edu.rice.cs.hpctest.data.db4;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.db.version4.DataMeta;
import edu.rice.cs.hpcdata.db.version4.MetricYamlParser;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpctest.util.TestDatabase;

public class MetricYamlParserTest 
{
	private static File []databasePaths;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		databasePaths = TestDatabase.getMetaDatabases();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		//
		databasePaths = null;
	}

	@Test
	public void test() throws Exception {
		assertNotNull(databasePaths);
		
		for(var db: databasePaths) {
			DataMeta data  = new DataMeta();
			var experiment = new Experiment();
			
			data.open(experiment, db.getAbsolutePath());
			MetricYamlParser parser = new MetricYamlParser(db.getAbsolutePath(), data);
			
			var metrics = parser.getListMetrics();
			assertNotNull(metrics);
			assertTrue(metrics.size() > 0);
			assertTrue(metrics.size() <= data.getMetrics().size());
			
			var roots = parser.getListRootMetrics();
			assertNotNull(roots);
			assertTrue(roots.size() > 0);
			assertTrue(roots.size() <= metrics.size());
			
			var rootMetrics = parser.getListRootMetrics();
			assertNotNull(rootMetrics);
			assertTrue(rootMetrics.size()>0);
		}
	}
}
