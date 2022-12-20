package edu.rice.cs.hpctest.ui.tree;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpctest.util.TestDatabase;
import edu.rice.cs.hpctree.ScopeTreeData;
import edu.rice.cs.hpctree.ScopeTreeRowModel;

public class ScopeTreeRowModelTest 
{
	private static List<ScopeTreeRowModel> treeModels;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		var database    = TestDatabase.getDatabases();
		
		treeModels = new ArrayList<>();
		for (var path: database) {
			assertNotNull(path);

			var experiment = new Experiment();
			try {
				experiment.open(path, null, Experiment.ExperimentOpenFlag.TREE_ALL);
			} catch (Exception e) {
				assertFalse(e.getMessage(), true);
			}			
			assertNotNull(experiment.getRootScope());
			
			var root = experiment.getRootScope(RootScopeType.CallingContextTree);
			assertNotNull(root);
			
			if (!root.hasChildren())
				continue;
			
			var treeData = new ScopeTreeData(root, experiment);			
			var listRows = treeData.getList();
			listRows.addAll(root.getChildren());
			
			treeModels.add(new ScopeTreeRowModel(treeData));
		}
	}

	
	@AfterClass
	public static void tearDownAfterClass() {
		for(var tree: treeModels) {
			tree.clear();
		}
	}


	@Test
	public void testSortColumn() {
		for(var treeModel: treeModels) {
			
			var treeData = treeModel.getTreeData();
			assertNotNull(treeData);
			assertTrue(treeData instanceof ScopeTreeData);
			
			var metrics = ((ScopeTreeData) treeData).getMetricCount();
			int sortCol = metrics == 0 ? 0 : 1;
			var sortedColumn = treeModel.getSortedColumnIndexes();
			
			assertTrue(sortedColumn.get(0) == sortCol);			
			assertTrue( treeModel.isColumnIndexSorted(sortCol) );
			
			var direction = treeModel.getSortDirection(sortCol);
			assertEquals(SortDirectionEnum.DESC, direction);
			
			treeModel.sort(sortCol, SortDirectionEnum.ASC, false);
			direction = treeModel.getSortDirection(sortCol);
			assertEquals(SortDirectionEnum.ASC, direction);
		}
	}
	
	 
	@Test
	public void testExpandInt() {
		for(var treeModel: treeModels) {
			var root = treeModel.getTreeData().getDataAtIndex(0);
			assertEquals(root, treeModel.getRoot());
			
			boolean isCollapsed = treeModel.isCollapsed(0);
			boolean childVisible = !treeModel.isChildrenVisible(root);
			assertEquals(isCollapsed, childVisible);
			
			if (!isCollapsed) {				
				var collapsedNodes = treeModel.collapse(0);
				assertNotNull(collapsedNodes);				
			}

			var list = treeModel.expand(0);
			assertNotNull(list);
			
			if (list.isEmpty())
				continue;

			for(var child: list) {
				var depth = treeModel.depth(child);
				assertEquals(1, depth);				
			}
			var list2 = treeModel.collapse(root);
			assertNotNull(list2);
			
			assertEquals(list.size(), list2.size());
			assertEquals(list, list2);
			
			for(int i=0; i<list.size(); i++) {
				assertEquals(list.get(i), list2.get(i));
			}
		} 
	}
}
