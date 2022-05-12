package edu.rice.cs.hpctest.ui.tree;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpctree.ScopeTreeData;

class ScopeTreeDataTest 
{
	private static String []dbPaths = new String[] {"bug-no-gpu-trace", "bug-empty", "bug-nometric", 
													"prof2" + File.separator + "loop-inline",
													"prof2" + File.separator + "multithread"};
	private static ScopeTreeData []treeData;
	private static SecureRandom random; 

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		var experiments = new Experiment[dbPaths.length];
		treeData    = new ScopeTreeData[dbPaths.length];
		random = SecureRandom.getInstanceStrong();
		 
		File []database = new File[dbPaths.length];
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
				assertTrue(depth == d);
				
				var child = tree.getDataAtIndex(numScopes + index);
				assertNotNull(child);
				assertTrue(child == scope);
				
				numScopes += numChildren;
			}

		}
	}
}
