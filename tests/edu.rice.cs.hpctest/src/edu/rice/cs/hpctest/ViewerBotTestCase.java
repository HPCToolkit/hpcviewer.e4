package edu.rice.cs.hpctest;

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

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcmerge.DatabaseMergeWizard;
import edu.rice.cs.hpctest.util.DataFactory;

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
							List<Experiment> list = DataFactory.createExperiments();
							DatabaseMergeWizard wz = new DatabaseMergeWizard(list);
							WizardDialog window = new WizardDialog(shell, wz);
							window.open();
							shell = window.getShell();

							// wait for the test setup
							//swtBarrier.await();
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

}
