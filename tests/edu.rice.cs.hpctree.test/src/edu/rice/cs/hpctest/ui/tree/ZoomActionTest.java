// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

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
import edu.rice.cs.hpcdata.experiment.LocalDatabaseRepresentation;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpctest.util.TestDatabase;
import edu.rice.cs.hpctree.ScopeTreeData;
import edu.rice.cs.hpctree.ScopeTreeTable;
import edu.rice.cs.hpctree.action.UndoableActionManager;
import edu.rice.cs.hpctree.action.ZoomAction;

public class ZoomActionTest 
{
	static List<RecordData> listData;
	static Shell shell;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		var database = TestDatabase.getDatabases();
		
		listData = new ArrayList<>();
		
		shell = new Shell(Display.getDefault());
		shell.setSize(1000, 500);

		for (var path: database) {
			assertNotNull(path);

			var experiment = new Experiment();
			var localDb = new LocalDatabaseRepresentation(path, null, true);
			try {
				experiment.open(localDb);
			} catch (Exception e) {
				assertFalse(e.getMessage(), true);
			}			
			assertNotNull(experiment.getRootScope());
			
			var root = experiment.getRootScope(RootScopeType.CallingContextTree);
			assertNotNull(root);
			
			if (!root.hasChildren())
				continue;

			RecordData data = new RecordData();
			data.treeData = new ScopeTreeData(null, root, experiment);
			
			ScopeTreeTable table = new ScopeTreeTable(shell, 0, data.treeData);
			
			var natTable = table.getTable();
			natTable.setSize(900, 400);

			data.action = new ZoomAction(new UndoableActionManager(), table);			
			listData.add(data);
		}
	}

	@AfterClass
	public static void tearDownAfterClass() {
		for(var data: listData) {
			data.action.actionClear();
			data.treeData.clear();
		}
		shell.dispose();
	}

	@Test
	public void testZoomIn() {
		for(var data: listData) {
			var root = data.treeData.getRoot();
			
			if (root.hasChildren()) {
				var children = new ArrayList<>( root.getChildren() );
				var iterator = children.iterator();
				while(iterator.hasNext()) {
					var child = iterator.next();
					var czi = data.action.canZoomIn(child);
					assertEquals(czi, child.hasChildren());

					if (!czi)
						continue;
					
					data.action.zoomIn(child);
					assertTrue(data.action.canZoomOut());
					
					assertEquals(child, data.treeData.getRoot());
					data.action.zoomOut();
					
					assertFalse(data.action.canZoomOut());
					assertEquals(root, data.treeData.getRoot());
				}
			}
		}
	}

	
	static class RecordData 
	{
		ZoomAction action;
		ScopeTreeData treeData;
	}
}
