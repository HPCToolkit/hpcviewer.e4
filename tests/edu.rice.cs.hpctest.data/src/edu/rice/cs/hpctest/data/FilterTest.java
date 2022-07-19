package edu.rice.cs.hpctest.data;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.filter.FilterAttribute;
import edu.rice.cs.hpcdata.filter.IFilterData;

public class FilterTest 
{
	private static Experiment []experiments;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		File []database;
		final String []dbPaths = new String[] {"bug-no-gpu-trace", "bug-empty", "bug-nometric"};
		
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
	public void testFilter() {
		final int []numFilteredScopes = new int[] {1, 0, 0};
		
		final String filterText = "testBandwidth";
		FilterAttribute.Type []attributes = new FilterAttribute.Type[] {
				FilterAttribute.Type.Descendants_Only,
				FilterAttribute.Type.Self_And_Descendants,
				FilterAttribute.Type.Self_Only};
		
		int i = 0;
		for (var exp: experiments) {
			
			for(var attr: attributes) {
				IFilterData filterData = new IFilterData() {
					
					@Override
					public boolean select(String element) {
						return element.startsWith(filterText);
					}
					
					@Override
					public boolean isFilterEnabled() {
						return true;
					}
					
					@Override
					public FilterAttribute getFilterAttribute(String element) {
						var fa = new FilterAttribute();
						fa.enable = true;
						fa.filterType = attr;
						return fa;
					}
				};
				int numScopes = exp.filter(filterData);
				assertTrue(numScopes >= numFilteredScopes[i]);				
			}
			i++;
		}
	}

}
