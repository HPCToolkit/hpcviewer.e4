package edu.rice.cs.hpctest.local;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.experiment.InvalExperimentException;
import edu.rice.cs.hpcdata.experiment.LocalDatabaseRepresentation;
import edu.rice.cs.hpclocal.LocalDBOpener;
import edu.rice.cs.hpctest.util.TestDatabase;
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
	public void testOpenDBAndCreateSTDC() throws InvalExperimentException, IOException {
		for(var dbOpener: localDbOpener) {
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
	private void testInsideSTDC(SpaceTimeDataController stdc) {
		assertFalse(stdc.hasTraces());
		assertFalse(stdc.isHomeView());
		
		var baseData = stdc.getBaseData();
		assertNotNull(baseData);
		assertFalse( baseData.getDenseListIdTuple(IdTupleOption.BRIEF).isEmpty());
		
		assertNotNull(stdc.getExperiment());
		testInsideExperiment(stdc.getExperiment());
	}
	
	private void testInsideExperiment(IExperiment experiment) {
		assertTrue("Empty database name", !experiment.getName().isEmpty());
	}
}