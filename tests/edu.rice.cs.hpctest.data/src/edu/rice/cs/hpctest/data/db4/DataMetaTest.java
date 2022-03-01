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

public class DataMetaTest 
{
	static DataMeta data;
	
	@BeforeClass
	public static void setUpBeforeClass() throws IOException {
		Path resource = Paths.get("..", "resources", "prof2", "loop-inline");
		File dbPath = resource.toFile();
		
		assertNotNull(dbPath);
		assertTrue(dbPath.isDirectory());
		
		IdTupleType tupleType = new IdTupleType();
		tupleType.initDefaultTypes();
		
		data = new DataMeta();
		data.open(dbPath.getAbsolutePath() + File.separatorChar + "meta.db");
	}
	
	
	@Test
	public void testSetup() {
		assertNotNull(data);
	}
	
	@Test
	public void testGetTitle() {
		assertTrue(data.getTitle().equals("loop"));
	}

}
