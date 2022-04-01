package edu.rice.cs.hpctest.viewer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.filter.FilterAttribute;
import edu.rice.cs.hpcfilter.service.FilterMap;


@TestMethodOrder(OrderAnnotation.class)
class FilterTest {
	private static Experiment []experiments;
	private static String []dbPaths = new String[] {"bug-no-gpu-trace", "prof2" + File.separator + "loop-inline"};
	
	private static FilterMap fmap;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		experiments = new Experiment[dbPaths.length];
		var database   = new File[dbPaths.length];
		int i=0;
		for (String dbp: dbPaths) {
			
			Path resource = Paths.get("..", "resources", dbp);
			database[i] = resource.toFile();
			
			assertNotNull(database);

			experiments[i]= new Experiment();
			try {
				experiments[i].open(database[i], null, Experiment.ExperimentOpenFlag.TREE_CCT_ONLY);
			} catch (Exception e) {
				assertFalse(e.getMessage(), true);
			}
			
			assertNotNull(experiments[i].getRootScope());
			i++;
		}
		fmap = new FilterMap();
	}
	
	

	@Test
	@Order(2)
	void testFilter() {
		final int []numFilters = new int[] {36, 0};
		int i=0;
		for(var exp: experiments) {
			int res = exp.filter(fmap);
			
			assertTrue(res >= numFilters[i]);
			i++;
		}
	}


	@Test
	@Order(1)
	void testFilterInit() {
		var fname = fmap.getFilename();
		assertNotNull(fname);
		
		FilterAttribute fa = new FilterAttribute();
		fa.enable = true;
		fa.filterType = FilterAttribute.Type.Self_Only;
		fmap.put("__GI___*", fa);
		
		assertTrue( fmap.size() == 1);
	}

}
