package edu.rice.cs.hpctest.viewer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.LocalDatabaseRepresentation;
import edu.rice.cs.hpcdata.filter.FilterAttribute;
import edu.rice.cs.hpcfilter.service.FilterMap;
import edu.rice.cs.hpctest.util.TestDatabase;


public class FilterTest {
	private static Experiment []experiments;	
	private static FilterMap fmap;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		var database   = TestDatabase.getDatabases();		
		experiments = new Experiment[database.length];
		int i=0;
		for (var dbp: database) {
			assertNotNull(dbp);

			experiments[i]= new Experiment();
			LocalDatabaseRepresentation localDb = new LocalDatabaseRepresentation(dbp, null, true);
			try {
				experiments[i].open(localDb);
			} catch (Exception e) {
				assertFalse(e.getMessage(), true);
			}
			
			assertNotNull(experiments[i].getRootScope());
			i++;
		}
		fmap = new FilterMap();
		
		var fname = fmap.getFilename();
		assertNotNull(fname);
		
		FilterAttribute fa = new FilterAttribute();
		fa.enable = true;
		fa.filterType = FilterAttribute.Type.Self_Only;
		fmap.put("__GI___*", fa);
		
		assertTrue( fmap.size() == 1);
	}
	
	

	@Test
	public void testFilter() throws Exception {
		for(var exp: experiments) {
			int res = exp.filter(fmap, true);
			
			assertTrue(res >= 0);
		}
	}
}
