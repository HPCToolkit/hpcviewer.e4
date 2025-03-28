// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctree.test.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.hpctoolkit.db.local.experiment.Experiment;
import org.hpctoolkit.db.local.experiment.LocalDatabaseRepresentation;
import org.hpctoolkit.db.local.experiment.scope.RootScopeType;
import edu.rice.cs.hpctest.util.TestDatabase;
import edu.rice.cs.hpctree.IScopeTreeData;
import edu.rice.cs.hpctree.ScopeTreeData;


public class DatabaseWalker 
{
	private DatabaseWalker() {
		// nothing
	}
	
	public static List<IScopeTreeData> getScopeTreeData() {
		var database    = TestDatabase.getDatabases();
		
		List<IScopeTreeData> listTreeData = new ArrayList<>();
		
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
			
			var treeData = new ScopeTreeData(null, root, experiment);			
			var listRows = treeData.getList();
			listRows.addAll(root.getChildren());
			
			listTreeData.add(treeData);
		}
		return listTreeData;
	}
}
