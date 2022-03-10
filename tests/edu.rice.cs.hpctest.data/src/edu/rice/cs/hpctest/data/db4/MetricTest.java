package edu.rice.cs.hpctest.data.db4;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.db.version4.DataMeta;
import edu.rice.cs.hpcdata.db.version4.MetricValueCollection3;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.scope.Scope;

public class MetricTest {
	
	private static DataMeta data;
	private static MetricValueCollection3 mvc;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Path resource = Paths.get("..", "resources", "prof2", "loop-inline");
		File dbPath = resource.toFile();
		
		assertNotNull(dbPath);
		assertTrue(dbPath.isDirectory());
		
		IdTupleType tupleType = new IdTupleType();
		tupleType.initDefaultTypes();
		
		data = new DataMeta();
		
		assertThrows(RuntimeException.class, ()->{
			data.open(dbPath.getAbsolutePath());
		});
		data.open(new Experiment(), dbPath.getAbsolutePath());
		data.finalize(null);
		
		try {
			mvc = new MetricValueCollection3(data.getDataSummary());
		} catch (IOException e) {
			System.err.println("Fail to create MetricValueCollection3: " + e.getMessage());
		}
	}

	@Test
	public void testGetValue() {
		Scope s = (Scope) data.getExperiment().getRootScopeChildren().get(0);		
		var mv = mvc.getValue(s, 1);
		assertNotNull(mv);
	}

	@Test
	public void testGetAnnotation() {
		float f = mvc.getAnnotation(1);
		assertTrue(f>-1);
	}

	@Test
	public void testSetValue() {
		mvc.setValue(1, new MetricValue(10));
		assertTrue(mvc.getValue(null, 1).getValue() == 10f);
	}

	@Test
	public void testSetAnnotation() {
		fail("Not yet implemented");
	}

	@Test
	public void testSize() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetDataSummary() {
		var ds = data.getDataSummary();
		assertNotNull(ds);
	}

	@Test
	public void testAppendMetrics() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetValues() {
		fail("Not yet implemented");
	}

}
