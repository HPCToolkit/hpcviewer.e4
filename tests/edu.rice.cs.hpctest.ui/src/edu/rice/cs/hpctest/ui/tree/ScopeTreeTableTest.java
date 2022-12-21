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
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpctest.util.TestDatabase;
import edu.rice.cs.hpctree.ScopeTreeData;
import edu.rice.cs.hpctree.ScopeTreeTable;

public class ScopeTreeTableTest 
{
	private static List<ScopeTreeTable> treeTables;
	private static Shell shell;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		var database = TestDatabase.getDatabases();
		shell = new Shell(Display.getDefault());
		
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
			table.setRoot(root);
			assertNotNull(table.getRoot());
			
			treeTables.add(table);
		}
	}

	@AfterClass
	public static void tearDownAfterClass() {
		treeTables.stream().close();
		treeTables.clear();
		shell.dispose();
	}


	@Test
	public void testHideColumn() {
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
