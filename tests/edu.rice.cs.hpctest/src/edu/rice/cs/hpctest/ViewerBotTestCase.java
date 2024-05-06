package edu.rice.cs.hpctest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.SWTBotTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import edu.rice.cs.hpcbase.IDatabase;
import edu.rice.cs.hpcbase.IDatabaseIdentification;
import edu.rice.cs.hpcbase.ITraceManager;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.InvalExperimentException;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.source.SourceFile;
import edu.rice.cs.hpcdata.util.IProgressReport;
import edu.rice.cs.hpcmerge.DatabaseMergeWizard;
import edu.rice.cs.hpctest.util.TestDatabase;

@RunWith(BlockJUnit4ClassRunner.class)
class ViewerBotTestCase extends SWTBotTestCase {
	
	protected SWTBot bot;
	protected static Thread uiThread;
	protected static Shell shell;
	private static final CyclicBarrier swtBarrier = new CyclicBarrier(2);


	@BeforeClass
	static void setUpBeforeClass() throws Exception {
		if (uiThread == null) {
			uiThread = new Thread(new Runnable() {

				@Override
				public void run() {
					try {	
						while (true) {
							// open and layout the shell
							var list = getDatabases();
							DatabaseMergeWizard wz = new DatabaseMergeWizard(list);
							WizardDialog window = new WizardDialog(shell, wz);
							window.open();
							shell = window.getShell();

							// wait for the test setup
							//swtBarrier.await();
							if (shell == null)
								break;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			uiThread.setDaemon(true);
			uiThread.start();
		}
	}

	@Before
	protected
	void setUp() throws Exception {
		// synchronize with the thread opening the shell
		swtBarrier.await();
		bot = new SWTBot(shell);
	}

	@After
	protected
	void tearDown() throws Exception {
		// close the shell
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				shell.close();
			}
		});
	}

	@Test
	void test() {
		bot.table().getTableItem(0).check();
		bot.table().getTableItem(1).check();
		bot.buttonWithLabel("Next >").click();
	}

	
	
	public static List<IDatabase> getDatabases() throws Exception {
		var listExp = TestDatabase.getExperiments();
		List<IDatabase> databases = new ArrayList<>();
		
		listExp.forEach(e -> {
			databases.add(new DatabaseWrapper(e));
		});
		return databases;
	}


	
	static class DatabaseWrapper implements IDatabase
	{
		Experiment experiment;
		
		DatabaseWrapper(Experiment experiment) {
			this.experiment = experiment;
		}
		@Override
		public IDatabaseIdentification getId() {
			return () -> experiment.getID();
		}

		@Override
		public DatabaseStatus open(Shell shell) {
			return DatabaseStatus.OK;
		}

		@Override
		public DatabaseStatus reset(Shell shell, IDatabaseIdentification id) {
			return DatabaseStatus.OK;
		}

		@Override
		public DatabaseStatus getStatus() {
			return DatabaseStatus.OK;
		}

		@Override
		public void close() {
			
		}

		@Override
		public IMetricManager getExperimentObject() {
			return experiment;
		}

		@Override
		public boolean hasTraceData() {
			return false;
		}

		@Override
		public ITraceManager getORCreateTraceManager() throws IOException, InvalExperimentException {
			return null;
		}

		@Override
		public String getSourceFileContent(SourceFile fileId) throws IOException {
			return null;
		}

		@Override
		public boolean isSourceFileAvailable(Scope scope) {
			return false;
		}

		@Override
		public RootScope createFlatTree(Scope rootCCT, RootScope rootFlat, IProgressReport progressMonitor) {
			return null;
		}

		@Override
		public String getErrorMessage() {
			return null;
		}
		
	}

}
