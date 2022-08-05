package edu.rice.cs.hpctest.data;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.filter.FilterAttribute;
import edu.rice.cs.hpcdata.filter.IFilterData;
import edu.rice.cs.hpctest.util.TestDatabase;

public class FilterTest 
{
	private static Experiment []experiments;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		var database = TestDatabase.getDatabases();
		experiments  = new Experiment[database.length];

		int i=0;
		for (var dbp: database) {
			experiments[i]= new Experiment();
			try {
				experiments[i].open(dbp, null, Experiment.ExperimentOpenFlag.TREE_ALL);
			} catch (Exception e) {
				assertFalse(e.getMessage(), true);
			}
			
			assertNotNull(experiments[i].getRootScope());
			i++;
		}
	}

	@Test
	public void testFilter() {
		final int []numFilteredScopes = new int[] {1, 0, 0, 0, 0};
		
		final String filterText = "testBandwidth";
		FilterAttribute.Type []attributes = new FilterAttribute.Type[] {
				FilterAttribute.Type.Descendants_Only,
				FilterAttribute.Type.Self_And_Descendants,
				FilterAttribute.Type.Self_Only};
		
		int i = 0;
		for (var exp: experiments) {
			int j = 0;
			for(var attr: attributes) {
				IFilterData filterData = new FilterData(filterText, attr) ;
				
				// the first filter attribute Descendants_Only will remove 
				// 		the descendants but not the node (if match)
				// the second filter attribute will remove the node itself
				// the third filter should match nothing since all the matched nodes
				// 		were already filtered
				
				int numScopes = exp.filter(filterData);
				if (j < 2)
					assertTrue(numScopes >= numFilteredScopes[i]);
				else
					assertTrue(numScopes == 0);

				j++;
			}
			i++;
		}
	}

	private static class FilterData implements IFilterData
	{
		private final String filterText;
		private final FilterAttribute.Type attr;
		
		public FilterData(String textToFilter, FilterAttribute.Type filterType) {
			this.filterText = textToFilter;
			this.attr = filterType;
		}
		
		@Override
		public boolean select(String element) {
			return getFilterAttribute(element) != null;
		}
		
		@Override
		public boolean isFilterEnabled() {
			return true;
		}
		
		@Override
		public FilterAttribute getFilterAttribute(String element) {
			if (element != null) {
				if (element.startsWith(filterText)) {
					var fa = new FilterAttribute();
					fa.enable = true;
					fa.filterType = attr;
					return fa;
				}
			}
			return null;
		}
	}
}
