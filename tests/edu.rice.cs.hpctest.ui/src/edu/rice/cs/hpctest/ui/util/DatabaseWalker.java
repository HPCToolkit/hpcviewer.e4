package edu.rice.cs.hpctest.ui.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
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
			
			listTreeData.add(treeData);
		}
		return listTreeData;
	}
}
