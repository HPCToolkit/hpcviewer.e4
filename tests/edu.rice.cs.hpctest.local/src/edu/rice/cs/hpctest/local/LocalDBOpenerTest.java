package edu.rice.cs.hpctest.local;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.IProgressMonitor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcbase.IProcessTimeline;
import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.experiment.LocalDatabaseRepresentation;
import edu.rice.cs.hpclocal.LocalDBOpener;
import edu.rice.cs.hpctest.util.TestDatabase;
import edu.rice.cs.hpctraceviewer.data.Frame;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;

public class LocalDBOpenerTest 
{
	private static LocalDBOpener []localDbOpener;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		var directories = TestDatabase.getDatabases();
		localDbOpener = new LocalDBOpener[directories.length];
		int i = 0;
		
		for(var dir: directories) {
			Experiment exp = new Experiment();
			exp.open(new LocalDatabaseRepresentation(dir, null, true));
			
			// Assume all test database should include traces
			// In the future this assumption may not be correct
			assertTrue(exp.getTraceDataVersion() >= 0);

			localDbOpener[i] = new LocalDBOpener(exp);
			i++;
		}
	}

	@AfterClass
	public static void tearDownAfterClass() {
		for(var dbOpener: localDbOpener) {
			dbOpener.end();
		}
	}

	@Test
	public void testOpenDBAndCreateSTDC() throws Exception {
		for(var dbOpener: localDbOpener) {
			if (dbOpener == null)
				continue;
			
			var stdc = dbOpener.openDBAndCreateSTDC(new IProgressMonitor() {
				
				@Override
				public void worked(int work) {
					System.out.print(".");
				}
				
				@Override
				public void subTask(String name) {
					System.out.println(name);
				}
				
				@Override
				public void setTaskName(String name) {
					System.out.println(name);
				}
				
				@Override
				public void setCanceled(boolean value) { /** nothing or assert false? */
				}
				
				@Override
				public boolean isCanceled() {
					return false;
				}
				
				@Override
				public void internalWorked(double work) {
					System.out.print(".");
				}
				
				@Override
				public void done() {
					System.out.println("done");
				}
				
				@Override
				public void beginTask(String name, int totalWork) {
					System.out.println(name + ": " + totalWork);
				}
			});
			assertNotNull(stdc);
			testInsideSTDC(stdc);
		}
	}
	
	
	private void testInsideSTDC(SpaceTimeDataController stdc) throws Exception {
		assertFalse(stdc.hasTraces());
		assertFalse(stdc.isHomeView());
		
		var baseData = stdc.getBaseData();
		assertNotNull(baseData);
		
		var idTuples = baseData.getDenseListIdTuple(IdTupleOption.BRIEF);
		assertFalse(idTuples.isEmpty());
		
		var attributes = stdc.getTraceDisplayAttribute();
		assertNotNull(attributes);
		
		var frame = new Frame();
		frame.begProcess = 0;
		frame.endProcess = idTuples.size()-1;
		frame.begTime = stdc.getMinBegTime();
		frame.endTime = stdc.getMaxEndTime();
		attributes.setFrame(frame);
		
		attributes.setPixelHorizontal(400);
		attributes.setPixelVertical(100);
		
		int numTraces = Math.min(idTuples.size(), attributes.getPixelVertical());
		stdc.startTrace(numTraces, true);
		var traceLine = stdc.getNextTrace();
		while(traceLine != null) {
			traceLine.readInData();
			var idt = traceLine.getProfileIdTuple();
			assertNotNull(idt);
			assertTrue(idt.getProfileIndex() > 0);
			assertTrue(traceLine.size() >= 0);
			if (traceLine.size() > 0) {
				checkTraceLine(traceLine, frame.begTime);
			}
			traceLine = stdc.getNextTrace();
		}
		assertNotNull(stdc.getExperiment());
		testInsideExperiment(stdc.getExperiment());
	}
	
	
	private void checkTraceLine(IProcessTimeline traceLine, long minTime) {
		
		assertFalse(traceLine.isEmpty());

		var cpi = traceLine.getCallPathInfo(0);
		assertNotNull(cpi);
		
		var maxDepth = cpi.getMaxDepth();
		assertTrue(maxDepth > 0);
		assertNotNull(cpi.getScope());
		assertNotNull(cpi.getScopeAt(0));
		assertTrue(cpi.getScope() == cpi.getScopeAt(maxDepth));
		
		long oldTime = minTime;
		
		for(int i=0; i<traceLine.size(); i++) {
			var cct = traceLine.getContextId(i);
			assertTrue(cct >= 0);
			
			var time = traceLine.getTime(i);
			assertTrue(time >= oldTime);
			
			oldTime = time;
		}
	}
	
	private void testInsideExperiment(IExperiment experiment) {
		assertTrue("Empty database name", !experiment.getName().isEmpty());
	}
}