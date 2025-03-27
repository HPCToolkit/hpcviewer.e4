// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctree.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.hpctoolkit.db.local.experiment.Experiment;
import org.hpctoolkit.db.local.experiment.LocalDatabaseRepresentation;
import org.hpctoolkit.db.local.experiment.scope.ProcedureScope;
import org.hpctoolkit.db.local.experiment.scope.RootScopeType;
import org.hpctoolkit.db.local.util.IProgressReport;
import edu.rice.cs.hpctest.util.TestDatabase;
import edu.rice.cs.hpctree.BottomUpScopeTreeData;


public class BottomUpScopeTreeDataTest 
{
	static List<BottomUpScopeTreeData> treeData;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		var database    = TestDatabase.getDatabases();
		treeData = new ArrayList<>();
		 
		for (var path: database) {
			assertNotNull(path);

			var experiment = new Experiment();
			var localDb = new LocalDatabaseRepresentation(path, null, IProgressReport.dummy());

			try {
				experiment.open(localDb);
			} catch (Exception e) {
				assertFalse(e.getMessage(), true);
			}			
			assertNotNull(experiment.getRootScope());
			
			var cctRoot = experiment.getRootScope(RootScopeType.CallingContextTree);
			assertNotNull(cctRoot);
			
			var buRoot = experiment.getRootScope(RootScopeType.CallerTree);
			assertNotNull(buRoot);

			buRoot = experiment.createCallersView(cctRoot, buRoot);
			assertNotNull(buRoot);
			
			var data = new BottomUpScopeTreeData(null, buRoot, experiment);
			treeData.add(data);
		}
	}

	@AfterClass
	public static void tearDownAfterClass() {
		for(var data: treeData) {
			data.clear();
			data.getRoot().dispose();
		}
	}


	@Test
	public void testGetChildrenScope() {
		for(var data: treeData) {
			var root = data.getRoot();
			assertNotNull(root);
			
			if (!data.hasChildren(root))
				continue;
			
			var children = data.getChildren(root);
			assertNotNull(children);
			
			if (!children.isEmpty()) {
				
				for(var child: children) {
					assertNotNull(child);
					assertTrue(child instanceof ProcedureScope);
					assertEquals(root, child.getParentScope());
					
					// comment out since the test will fail due to bug issue #271
					// once the bug is fixed, we can enable the test
					// TestMetricValue.testMetricValueCorrectness((Experiment) data.getMetricManager(), root, child);
				}
			}
		}
	}

}
