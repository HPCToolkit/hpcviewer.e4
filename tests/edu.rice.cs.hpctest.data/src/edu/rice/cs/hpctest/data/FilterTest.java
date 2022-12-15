package edu.rice.cs.hpctest.data;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.filter.FilterAttribute;
import edu.rice.cs.hpcdata.filter.IFilterData;
import edu.rice.cs.hpctest.data.util.TestMetricValue;
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
								
				RootScope rootCCT  = experiments[i].getRootScope(RootScopeType.CallingContextTree);
				RootScope rootCall = experiments[i].getRootScope(RootScopeType.CallerTree);
				RootScope rootFlat = experiments[i].getRootScope(RootScopeType.Flat);

				rootCall = experiments[i].createCallersView(rootCCT, rootCall);			
				rootFlat = experiments[i].createFlatView(rootCCT, rootFlat);
				
			} catch (Exception e) {
				assertFalse(e.getMessage(), true);
			}
			
			assertNotNull(experiments[i].getRootScope());
			i++;
		}
	}

	@Test
	public void testFilter() throws Exception {
		final String []filterText = {"testBandwidth", "loop at *", "*: *"};
		
		FilterAttribute.Type []attributes = new FilterAttribute.Type[] {
				FilterAttribute.Type.Descendants_Only,
				FilterAttribute.Type.Self_And_Descendants,
				FilterAttribute.Type.Self_Only};
		
		for (var exp: experiments) {
			for(var attr: attributes) {
				for(var text: filterText) {
					IFilterData filterData = new FilterData(text, attr) ;
					
					int numScopes = exp.filter(filterData, true);
					assertTrue(numScopes >= 0);
					
					var roots = exp.getRootScopeChildren();
					for(var root: roots) {
						if (root.getSubscopeCount() == 0)
							continue;
						for(var child: root.getChildren()) {					
							boolean result = TestMetricValue.testMetricValueCorrectness(exp, root, child);
							assertTrue( "Tree test fails for: " + exp.getName() + " scope: " + child.getName(), result);
						}
					}
				}
			}
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
