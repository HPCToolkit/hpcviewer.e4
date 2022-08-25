package edu.rice.cs.hpctest.data;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.MetricYamlParser;
import edu.rice.cs.hpctest.util.TestDatabase;

public class MetricYamlParserTest 
{
	private static Experiment []experiments;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		var files = TestDatabase.getMetaDatabases();
		experiments = new Experiment[files.length];
		int i=0;
		
		for(var f: files) {
			experiments[i] = new Experiment();
			experiments[i].open(f, null, true);
			i++;					
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		//
		experiments = null;
	}

	@Test
	public void test() throws FileNotFoundException {
		assertNotNull(experiments);
		
		for(var exp: experiments) {
			MetricYamlParser parser = new MetricYamlParser(exp);
			assertNotNull(parser);
			
			int version = parser.getVersion();
			assertTrue(version >= 0);
			
			var metrics = parser.getListMetrics();
			assertNotNull(metrics);
			assertTrue(metrics.size() <= exp.getMetricCount());
		}
	}
}
