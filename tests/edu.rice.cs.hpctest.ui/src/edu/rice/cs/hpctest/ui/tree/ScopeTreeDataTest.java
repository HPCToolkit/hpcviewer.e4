package edu.rice.cs.hpctest.ui.tree;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpctree.ScopeTreeData;

class ScopeTreeDataTest {

	private static Experiment []experiments;
	private static String []dbPaths = new String[] {"bug-no-gpu-trace", "bug-empty", "bug-nometric", 
													"prof2" + File.separator + "loop-inline",
													"prof2" + File.separator + "multithread"};
	private static ScopeTreeData []treeData;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		experiments = new Experiment[dbPaths.length];
		treeData    = new ScopeTreeData[dbPaths.length];
		
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
	void testGetList() {
		for (ScopeTreeData tree: treeData) {
			var list = tree.getList();
			assertNotNull(list);
		}
	}

	@Test
	void testGetRoot() {
		for (ScopeTreeData tree: treeData) {
			var root = tree.getRoot();
			assertNotNull(root);
		}
	}

	@Test
	void testSort() {
		Random r = new Random();
		
		for (ScopeTreeData tree: treeData) {
			var root = tree.getRoot();
			assertNotNull(root);
			int numMetrics = tree.getMetricCount();			
			int i = numMetrics == 0 ? 0 : r.nextInt(numMetrics);
			tree.sort(i, SortDirectionEnum.ASC, false);
		}
	}

	@Test
	void testGetMetric() {
		for (ScopeTreeData tree: treeData) {
			int numMetrics = tree.getMetricCount();
			for (int i=0; i<numMetrics; i++) {
				var metric = tree.getMetric(i);
				assertNotNull(metric);
			}
		}
	}

	@Test
	void testGetDepthOfDataScope() {
		Random r = new Random();
		
		for (ScopeTreeData tree: treeData) {
			var root = tree.getRoot();
			assertNotNull(root);
			int depth = 0;
			int numScopes = 1;
			
			assertTrue( tree.getDepthOfData(root) == depth);
			Scope scope = root;
			var list = tree.getList();
			
			while (scope.hasChildren()) {
				list.addAll(scope.getChildren());
				
				int numChildren = scope.getSubscopeCount();
				int index = r.nextInt(numChildren);				
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
