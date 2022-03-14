package edu.rice.cs.hpctest.data;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.db.DatabaseManager;
import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;
import edu.rice.cs.hpcdata.db.version2.FileDB2;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.Experiment.ExperimentOpenFlag;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.trace.TraceAttribute;
import edu.rice.cs.hpcdata.trace.TraceReader;
import edu.rice.cs.hpcdata.util.CallPath;

public class CallPathTest {
	
	private static Experiment experiment;
	private static CallPath   callpath;
	private static FileDB2    fileDB;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Path resource = Paths.get("..", "resources", "bug-no-gpu-trace");
		var database  = resource.toFile();
		var pathname  = DatabaseManager.getDatabaseFilePath(database.getAbsolutePath());
		
		experiment = new Experiment();
		experiment.open(new File(pathname.orElse("")), null, ExperimentOpenFlag.TREE_CCT_ONLY);

		final TraceAttribute trAttribute = (TraceAttribute) experiment.getTraceAttribute();		

		fileDB = new FileDB2();
		try {
			String filename = resource.toFile().getAbsolutePath() + File.separator + "experiment.mt";
			fileDB.open(filename, trAttribute.dbHeaderSize, TraceReader.RECORD_SIZE);
		} catch (IOException e) {
			fail(e.getMessage());
			return;
		}
		callpath = new CallPath();
	}

	
	@Test
	public void tetFileDB() throws IOException {
		int numRanks = fileDB.getNumberOfRanks();
		assertTrue(numRanks == 6);
		
		var offsets = fileDB.getOffsets();
		assertNotNull(offsets);
		assertTrue(offsets.length == numRanks);

		int level = fileDB.getParallelismLevel();
		assertTrue(level == 1);

		final long []controlOffsets = new long[] {104, 103768, 103812, 103856, 103900, 103944};
		
		for(int i=0; i<numRanks; i++) {
			assertTrue(offsets[i] == controlOffsets[i]);
			
			long maxLoc = fileDB.getMaxLoc(i);
			long minLoc = fileDB.getMinLoc(i);
			
			assertTrue(maxLoc >= minLoc);
			
			long time = fileDB.getLong(minLoc);
			int  cpid = fileDB.getInt(minLoc + 8);
			assertTrue(time >= 1598383463041077000L && cpid > 60);
		}
		var type = fileDB.getIdTupleTypes();
		assertNotNull(type);
		
		var idt = fileDB.getIdTuple(IdTupleOption.BRIEF);
		assertNotNull(idt);
		assertTrue(idt.size() == numRanks);
	}
	

	@Test
	public void testAddCallPath() {
		Scope scope = experiment.getRootScope(RootScopeType.CallingContextTree);
		var children = scope.getChildren();
		for(var child: children) {
			addCallPath(child, 0);
		}
		// test existent cpid
		assertTrue(callpath.getCallPathDepth(91) == 16);
		assertTrue(callpath.getCallPathDepth(111) == 19);
		
		assertNotNull(callpath.getCallPathScope(6169));
		assertNotNull(callpath.getCallPathInfo(61));
		
		// test non existent cpid
		int depth = callpath.getCallPathDepth(1000000);
		assertTrue(depth < 0);
		
		var cp = callpath.getCallPathInfo(1009494);
		assertNull(cp);
	}

	
	private void addCallPath(Scope scope, int depth) {
		if (scope == null) {
			return;
		}
		int cpid = scope.getCpid();
		if (cpid >= 0) {
			callpath.addCallPath(cpid, scope, depth);
			
			assertNotNull(callpath.getCallPathInfo(cpid));
			assertTrue(callpath.getCallPathDepth(cpid) == depth);
			assertTrue(callpath.getCallPathScope(cpid) == scope);
		}
		
		var children = scope.getChildren();
		if (children == null) {
			var names = callpath.getFunctionNames(scope.getCpid());
			if (cpid >= 0)
				assertNotNull(names);
			else 
				assertNull(names);
			return;
		}
		
		for(var child: children) {
			addCallPath(child, depth + 1);
		}
	}
}
