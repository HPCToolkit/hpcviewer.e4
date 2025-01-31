// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctree.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.LocalDatabaseRepresentation;
import edu.rice.cs.hpcdata.experiment.scope.LoopScope;
import edu.rice.cs.hpcdata.experiment.scope.ProcedureScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpctest.util.TestDatabase;
import edu.rice.cs.hpctest.util.TestMetricValue;
import edu.rice.cs.hpctree.FlatScopeTreeData;
import edu.rice.cs.hpctree.IScopeTreeAction;
import edu.rice.cs.hpctree.ScopeTreeTable;
import edu.rice.cs.hpctree.action.FlatAction;
import edu.rice.cs.hpctree.action.UndoableActionManager;

public class FlatActionTest 
{
	static List<RecordFlatAction> flatData;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception  {
		var directories = TestDatabase.getDatabases();
		flatData = new ArrayList<>();
		Display display = Display.getDefault();
		Shell shell = new Shell(display);
		
		for(var dir: directories) {
			var exp = new Experiment();
			
			var localDb = new LocalDatabaseRepresentation(dir, null, true);
			exp.open(localDb);

			var cctRoot = exp.getRootScope(RootScopeType.CallingContextTree);
			var flatRoot = exp.getRootScope(RootScopeType.Flat);			
			final var flaTreetRoot = exp.createFlatView(cctRoot, flatRoot);
			
			var td = new FlatScopeTreeData(null, flaTreetRoot, exp);
			FlatAction fa = null;
			IScopeTreeAction treeAction = null;
			try {
				treeAction = new ScopeTreeTable(shell, 0, td);
				treeAction.setRoot(flatRoot);
				fa = new FlatAction(new UndoableActionManager(), treeAction);
				fa.setTreeData(td);
			} catch (Exception e) {
				treeAction = new IScopeTreeAction() {
					Scope root = flaTreetRoot;
					
					@Override
					public List<Scope> traverseOrExpand(Scope scope) {
						return Collections.emptyList();
					}
					
					@Override
					public void traverseOrExpand(int index) { /* not used */ }
					
					@Override
					public void setRoot(Scope scope) {
						this.root = scope;
					}
					
					@Override
					public void refresh() { /* not used */ }
					
					@Override
					public int getSortedColumn() {
						return exp.getVisibleMetrics().size();
					}
					
					@Override
					public Scope getSelection() {
						return null;
					}
					
					@Override
					public Scope getRoot() {
						return root;
					}
					
					@Override
					public void export() { /* not used */ }
				};
				fa = new FlatAction(new UndoableActionManager(), treeAction);
				fa.setTreeData(td);
			} finally {
				flatData.add(new RecordFlatAction(td, fa, treeAction));
			}
		}
	}

	@AfterClass
	public static void tearDownAfterClass() {
		if (flatData != null)
			flatData.clear();
	}

	@Test
	public void testFlatten() {
		for(var data: flatData) {
			var root = data.treeData.getRoot();
			if (root == null)
				continue;
			
			assertEquals(root.hasChildren(), data.action.canFlatten());
			assertEquals(0, data.treeData.getDepthOfData(root));
			
			int attempt = 0;
			while (data.action.canFlatten()) {
				root = data.treeAction.getRoot();
				
				data.action.flatten(root);

				attempt++;
				
				assertEquals(0, data.treeData.getDepthOfData(0));
				assertTrue(attempt < 50);
				
				var flattenedRoot = data.treeAction.getRoot();
				var metricManager = data.treeData.getMetricManager();
				var metrics = metricManager.getNonEmptyMetricIDs(flattenedRoot);
				var nonEmptyMetrics = metrics.stream().map(metricManager::getMetric).collect(Collectors.toList());
				
				TestMetricValue.testTreMetriceCorrectnes(nonEmptyMetrics, flattenedRoot);
				
				// test sorting after flattening
				for(int sortedColumn = data.treeAction.getSortedColumn()+1; 
						sortedColumn <= data.treeData.getMetricCount(); 
						sortedColumn++) {
					data.treeData.sort(sortedColumn, SortDirectionEnum.DESC, false);
					var metric = data.treeData.getMetric(sortedColumn-1);
					var children = data.treeData.getChildren(0);

					TestMetricValue.testSortedMetricCorrectness(metric, flattenedRoot, children.get(0));

					for(int i=0; i<children.size()-1; i+=2) {
						var child1 = children.get(i);
						var child2 = children.get(i+1);
						TestMetricValue.testSortedMetricCorrectness(metric, child1, child2);
					}
				}
			}
			// check for the leaf nodes
			for(var child: data.treeAction.getRoot().getChildren()) {
				assertFalse(child.hasChildren());
				assertFalse(child instanceof ProcedureScope);
				assertFalse(child instanceof LoopScope);
			}
			
			while( data.action.canUnflatten() ) {
				data.action.unflatten();
				attempt--;
				
				assertTrue(attempt >= 0);
			}
			
		}
	}

	
	static class RecordFlatAction {
		FlatScopeTreeData treeData;
		FlatAction action;
		IScopeTreeAction treeAction;
		
		RecordFlatAction(FlatScopeTreeData treeData, FlatAction action, IScopeTreeAction treeAction) {
			this.treeData = treeData;
			this.action = action;
			this.treeAction = treeAction;
		}
	}
}
