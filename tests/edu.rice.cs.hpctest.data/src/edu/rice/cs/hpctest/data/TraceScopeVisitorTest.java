package edu.rice.cs.hpctest.data;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.visitors.TraceScopeVisitor;
import edu.rice.cs.hpcdata.util.ICallPath;
import edu.rice.cs.hpctest.util.TestDatabase;

public class TraceScopeVisitorTest 
{
	private static List<Experiment> listExperiments;
	private static List<TraceScopeVisitor> listTraceVisitors;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		File []files = TestDatabase.getDatabases();
		
		listExperiments = new ArrayList<>(files.length);
		listTraceVisitors = new ArrayList<>(files.length);
		
		for(var file: files) {
			Experiment exp = new Experiment();
			exp.open(file, null, false);
			if (exp.getTraceAttribute() != null) {
				listExperiments.add(exp);
				
				RootScope root = exp.getRootScope(RootScopeType.CallingContextTree);
				TraceScopeVisitor tsv = new TraceScopeVisitor();
				root.dfsVisitScopeTree(tsv);
				
				listTraceVisitors.add(tsv);
			}
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testGetCallPath() {
		int i=0;
		for(var traceVisitor: listTraceVisitors) {
			final var iCallPath = traceVisitor.getCallPath();
			assertNotNull(iCallPath);
			
			final var exp = listExperiments.get(i);
			RootScope root = exp.getRootScope(RootScopeType.CallingContextTree);
			root.getChildren().stream().forEach(c -> traverseTree(c, iCallPath));
			
			i++;
		}
	}
	
	private void traverseTree(Scope scope, ICallPath callpath) {
		if (scope.getSubscopeCount()==0)
			return;
		
		// test: the first depth must be the main program, it can't be a root
		var s = callpath.getScopeAt(scope.getCCTIndex(), 0);
		
		assertNotNull(s);
		assertTrue(!(s instanceof RootScope));
		
		for(var child: scope.getChildren()) {
			traverseTree(child, callpath);
		}
	}

	@Test
	public void testGetMaxDepth() {
		for(var traceVisitor: listTraceVisitors) {
			var iCallPath = traceVisitor.getCallPath();
			assertNotNull(iCallPath);
			int depth = traceVisitor.getMaxDepth();
			
			assertTrue(depth > 0);
		}
	}

}
