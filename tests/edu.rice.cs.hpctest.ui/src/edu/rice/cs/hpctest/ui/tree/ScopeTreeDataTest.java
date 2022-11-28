package edu.rice.cs.hpctest.ui.tree;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpctest.util.TestDatabase;
import edu.rice.cs.hpctree.ScopeTreeData;

class ScopeTreeDataTest 
{
	private static ScopeTreeData []treeData;
	private static final Random random = new Random(); 

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		var database    = TestDatabase.getDatabases();
		var experiments = new Experiment[database.length];
		treeData = new ScopeTreeData[database.length];
		 
		int i=0;

		for (var path: database) {			
			assertNotNull(path);

			experiments[i]= new Experiment();
			try {
				experiments[i].open(path, null, Experiment.ExperimentOpenFlag.TREE_ALL);
			} catch (Exception e) {
				assertFalse(e.getMessage(), true);
			}			
			assertNotNull(experiments[i].getRootScope());
			
			treeData[i] = new ScopeTreeData(experiments[i].getRootScope(RootScopeType.CallingContextTree), experiments[i]);
			
			i++;
		}
	}


	@Test
	void testMain() {		
		for (ScopeTreeData tree: treeData) {
			var root = tree.getRoot();
			assertNotNull(root);
			
			var list = tree.getList();
			assertNotNull(list);

			int numMetrics = tree.getMetricCount();			
			int i = numMetrics == 0 ? 0 : random.nextInt(numMetrics);
			tree.sort(i, SortDirectionEnum.ASC, false);
			
			for (i=0; i<numMetrics; i++) {
				var metric = tree.getMetric(i);
				assertNotNull(metric);
			}
			
			int depth = 0;
			int numScopes = 1;
			
			assertTrue( tree.getDepthOfData(root) == depth);
			Scope scope = root;
			
			while (scope.hasChildren()) {
				list.addAll(scope.getChildren());
				
				int numChildren = scope.getSubscopeCount();
				int index = random.nextInt(numChildren);				
				scope = scope.getSubscope(index);
				
				depth++;
				int d = tree.getDepthOfData(scope);
				assertEquals(depth, d, "Inequal depth " + d + " vs " + depth +" for scope " + scope.getCCTIndex() + ": " + scope.getName());
				
				var child = tree.getDataAtIndex(numScopes + index);
				assertNotNull(child);
				assertTrue(child == scope);
				
				numScopes += numChildren;
			}

		}
	}
}
