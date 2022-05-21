package edu.rice.cs.hpctest.data;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.db.IFileDB;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;
import edu.rice.cs.hpcdata.db.version2.FileDB2;
import edu.rice.cs.hpcdata.db.version4.DataSummary;
import edu.rice.cs.hpcdata.db.version4.FileDB4;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.Experiment.ExperimentOpenFlag;
import edu.rice.cs.hpcdata.experiment.LocalDatabaseRepresentation;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.trace.TraceAttribute;
import edu.rice.cs.hpcdata.util.CallPath;
import edu.rice.cs.hpcdata.util.Constants;
import edu.rice.cs.hpctest.util.TestDatabase;


public class CallPathTest {
	private static final int RECORD_SIZE    = Constants.SIZEOF_LONG + Constants.SIZEOF_INT;

	private static Experiment []experiment;
	private static IFileDB    []fileDB;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		var directories = TestDatabase.getDatabases();
		int i=0;
		
		experiment = new Experiment[directories.length];
		fileDB = new IFileDB[directories.length];
		
		for(var dir: directories) {
			var pathname = dir.getAbsolutePath();
			int version = LocalDatabaseRepresentation.directoryHasTraceData(pathname);
			if (version < 0 ) 
				continue;
							
			experiment[i] = new Experiment();
			experiment[i].open(new File(pathname), null, ExperimentOpenFlag.TREE_CCT_ONLY);
			TraceAttribute traceAttributes = (TraceAttribute) experiment[i].getTraceAttribute();
			if (traceAttributes == null || traceAttributes.mapCpidToCallpath == null)
				continue;

			String filename = pathname + File.separator;
			if (version == Constants.EXPERIMENT_DENSED_VERSION) {
				fileDB[i] = new FileDB2();
				filename += Constants.TRACE_FILE_DENSED_VERSION; 
			} else if (version == Constants.EXPERIMENT_SPARSE_VERSION) {
				IdTupleType tupleType = new IdTupleType();
				tupleType.initDefaultTypes();
				
				DataSummary ds = new DataSummary(IdTupleType.createTypeWithOldFormat());
				ds.open(filename);
				fileDB[i] = new FileDB4(experiment[i], ds);
			} else 
				fail("unknown database");
			try {				
				fileDB[i].open(filename, traceAttributes.dbHeaderSize, RECORD_SIZE);
			} catch (IOException e) {
				fail(e.getMessage());
				return;
			}
			i++;
		}
	}

	@AfterClass
	public static void afterClass() {
		for(var fdb: fileDB) {
			if (fdb != null)
				fdb.dispose();
		}
	}
	
	@Test
	public void tetFileDB() throws IOException {
		for(var fdb: fileDB) {
			if (fdb == null)
				continue;
			
			int numRanks = fdb.getNumberOfRanks();
			assertTrue(numRanks > 0);
			
			if (fdb instanceof FileDB2) {
				var offsets = fdb.getOffsets();
				assertNotNull(offsets);
				assertTrue(offsets.length == numRanks);
			}

			int level = fdb.getParallelismLevel();
			assertTrue(level >= 1);
			
			for(int i=0; i<numRanks; i++) {
				
				long maxLoc = fdb.getMaxLoc(i);
				long minLoc = fdb.getMinLoc(i);
				
				assertTrue(maxLoc >= minLoc);
				
				long time = fdb.getLong(minLoc);
				int  cpid = fdb.getInt(minLoc + 8);
				assertTrue(time >= 100L && cpid >= 0);
			}
			var type = fdb.getIdTupleTypes();
			assertNotNull(type);
			
			var idt = fdb.getIdTuple(IdTupleOption.BRIEF);
			assertNotNull(idt);
			assertTrue(idt.size() == numRanks);
			
			var labels = fdb.getRankLabels();
			assertNotNull(labels);
			assertTrue(labels.length == numRanks);

			for(int i=0; i<labels.length; i++) {
				assertNotNull(labels[i]);
			}
			if (fdb.getParallelismLevel() > 1) {
				assertFalse(fdb.isGPU(0));
			}
			assertFalse(fdb.hasGPU());
			fdb.dispose();		
		}
	}
	

	@Test
	public void testAddCallPath() {
		for(var exp: experiment) {
			if (exp == null)
				continue;
			
			var callpath = new CallPath();
			Scope scope = exp.getRootScope(RootScopeType.CallingContextTree);
			addCallPath(callpath, scope, 0);

			var children = scope.getChildren();
			for(var child: children) {
				if (child instanceof RootScope)
					System.out.println();
				addCallPath(callpath, child, 1);
			}
			// test existent cpid
			assertTrue(callpath.getCallPathDepth(scope.getCpid()) >= 0);
			
			// test non existent cpid
			int depth = callpath.getCallPathDepth(1000000);
			assertTrue(depth < 0);
			
			var cp = callpath.getCallPathInfo(1009494);
			assertNull(cp);
		}
	}

	
	private void addCallPath(CallPath callpath, Scope scope, int depth) {
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
			addCallPath(callpath, child, depth + 1);
		}
	}
}
