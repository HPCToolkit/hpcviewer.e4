package edu.rice.cs.hpctest.data.db4;

import static org.junit.Assert.*;

import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.db.version4.DataMeta;
import edu.rice.cs.hpcdata.db.version4.MetricValueCollection4;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpctest.util.TestDatabase;

public class MetricTest {
	
	private static DataMeta []data;
	private static MetricValueCollection4 []mvc;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		var dbPaths = TestDatabase.getMetaDatabases();
		data = new DataMeta[dbPaths.length];
		mvc  = new MetricValueCollection4[data.length];
		
		for(int i=0; i<dbPaths.length; i++) {
			data[i] = new DataMeta();
			
			final var dataMeta = data[i];
			final var dbPath   = dbPaths[i];
			
			assertThrows(IOException.class, ()->{
				dataMeta.open(dbPath.getAbsolutePath());
			});
			dataMeta.open(new Experiment(), dbPaths[i].getAbsolutePath());
			
			try {
				mvc[i] = new MetricValueCollection4(dataMeta.getDataSummary());
			} catch (IOException e) {
				System.err.println("Fail to create MetricValueCollection3: " + e.getMessage());
				fail(e.getMessage());
			}
			Scope s = (Scope) dataMeta.getExperiment().getRootScopeChildren().get(0);		
			mvc[i].getValue(s, 0);
		}
	}

	@Test
	public void testGetValue() {
		int i=0;
		for(var dataMeta: data) {
			Scope s = (Scope) dataMeta.getExperiment().getRootScopeChildren().get(0);		
			var mv = mvc[i].getValue(s, 1);
			assertNotNull(mv);
			assertTrue(mv == MetricValue.NONE);
			
			mv = mvc[i].getValue(s, 2);
			assertNotNull(mvc);
			assertTrue(mv.getValue() > 0);
			i++;
		}
	}



	@Test
	public void testSetValue() {
		for(var m: mvc) {
			m.setValue(1, new MetricValue(10));
			assertTrue(m.getValue(null, 1).getValue() == 10f);
		}
	}



	@Test
	public void testSize() {
		for(var m: mvc) {
			assertTrue(m.size() > 0);
		}
	}

	@Test
	public void testGetDataSummary() {
		for(var dataMeta: data) {
			var ds = dataMeta.getDataSummary();
			assertNotNull(ds);
		}
	}

	@Test
	public void testAppendMetrics() throws IOException {
		for(int i=0; i<data.length; i++) {
			int size1 = mvc[i].size();
			var mvcdup = mvc[i].duplicate();
			mvc[i].appendMetrics(mvcdup, 2);
			int size2 = mvc[i].size();
			assertTrue(size2 == size1);
			
			final var dataMeta = data[i];
			Scope s = (Scope) dataMeta.getExperiment().getRootScopeChildren().get(0);		
			mvcdup.getValue(s, 2);
			mvc[i].appendMetrics(mvcdup, 2);
			size2 = mvc[i].size();
			assertTrue(size2 == size1 * 2);
		}
	}

	@Test
	public void testGetValues() {
		for (var m: mvc) {
			var values = m.getValues();
			assertNotNull(values);
		}
	}

}
