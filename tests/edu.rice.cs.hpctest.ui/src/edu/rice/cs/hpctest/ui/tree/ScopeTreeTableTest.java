package edu.rice.cs.hpctest.ui.tree;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpctest.util.TestDatabase;
import edu.rice.cs.hpctree.ScopeTreeData;
import edu.rice.cs.hpctree.ScopeTreeTable;

public class ScopeTreeTableTest 
{
	private static List<ScopeTreeTable> treeTables;
	private static List<ScopeTreeData>  listData;
	private static Shell shell;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		var database = TestDatabase.getDatabases();
		shell = new Shell(Display.getDefault());
		shell.setSize(1000, 500);
		
		listData = new ArrayList<>();
		treeTables = new ArrayList<>();
		
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
			ScopeTreeTable table = new ScopeTreeTable(shell, 0, treeData);
			
			var natTable = table.getTable();
			natTable.setSize(900, 400);
			var size = natTable.getSize();
			
			assertTrue(size.x >0 && size.y > 0);
			
			table.setRoot(root);
			assertNotNull(table.getRoot());
			
			listData.add(treeData);
			treeTables.add(table);
		}
	}

	@AfterClass
	public static void tearDownAfterClass() {
		treeTables.stream().close();
		treeTables.clear();
		
		listData.stream().close();
		listData.clear();
		
		shell.dispose();
	}


	@Test
	public void testHideColumn() {
		int i=0;
		for(var table: treeTables) {
			var tbl = table.getTable();
			assertNotNull(tbl);

			tbl.pack();
			
			var origHiddenIndexes = table.getHiddenColumnIndexes();

			table.hideColumn(0);
			var hiddenIndexes = table.getHiddenColumnIndexes();
			assertNotNull(hiddenIndexes);
			assertTrue(hiddenIndexes.length == origHiddenIndexes.length + 1);

			table.showColumn(0);
			hiddenIndexes = table.getHiddenColumnIndexes();
			assertNotNull(hiddenIndexes);
			assertTrue(hiddenIndexes.length == origHiddenIndexes.length);
			
			var data = listData.get(i);
			int numColumns = data.getMetricCount();
			for(int j=1; j<=numColumns; j++) {
				// tricky test: 
				//  the table works with column 0 for the tree
				//     and column 1 .. n for the metrics
				//  the data works with column 0 for the first metric
				//     and column n-1 for the last metric
				var metric = table.getMetric(j);
				var metric2 = data.getMetric(j-1);
				assertEquals(metric, metric2);
			}
			
			assertTrue(table.getSortedColumn() >= 0);
			
			i++;
		}
	}

	@Test
	public void testTraverseOrExpandInt() {
		for(var table: treeTables) {
			var root  = table.getRoot();
			assertNotNull(root);
			
			var scope = table.getSelection();
			if (scope == null) {
				table.setSelection(0);
			}
			scope = table.getSelection();
			assertNotNull(scope);
			
			table.clearSelection();
			scope = table.getSelection();
			assertNull(scope);
			
			root.setCounter(1);
			table.reset((RootScope) root);
			assertTrue(table.getRoot().getCounter() == 1);
			
			var children = table.traverseOrExpand(root);
			if (children != null && !children.isEmpty()) {
				int order = 0;
				for(var child: children) {
					var index = table.indexOf(child);
					assertTrue(index > order);
					
					assertEquals(root, child.getParentScope());
					
					order++;
				}
			}
			table.traverseOrExpand(0);
		}
	}

	@Test
	public void testExport() {
		for(var table: treeTables) {
			table.export();
		}
	}

}
