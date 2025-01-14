// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcmerge.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.junit.Before;
import org.junit.Test;

import edu.rice.cs.hpcbase.IDatabase;
import edu.rice.cs.hpcbase.IDatabaseIdentification;
import edu.rice.cs.hpcbase.ITraceManager;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.InvalExperimentException;
import edu.rice.cs.hpcdata.experiment.LocalDatabaseRepresentation;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.source.SourceFile;
import edu.rice.cs.hpcdata.merge.DatabasesToMerge;
import edu.rice.cs.hpcdata.util.IProgressReport;
import edu.rice.cs.hpcmerge.DatabaseMergeWizard;
import edu.rice.cs.hpctest.util.TestDatabase;
import edu.rice.cs.hpctest.util.ViewerTestCase;

public class DatabaseMergeTest extends ViewerTestCase
{
    private SWTBot bot;

    @Override
    @Before
    public void setUp() {
    	super.setUp();
        bot = new SWTBot();
    }


    @Test
	public void testPopulateDialog() throws Exception {
		var dirs = TestDatabase.getDatabases();
		List<IDatabase> list = new ArrayList<>(dirs.length);
		for(var d: dirs) {
			var exp1 = new Experiment();
			
			var localDb = new LocalDatabaseRepresentation(d, null, IProgressReport.dummy());
			exp1.open(localDb);
			list.add(new DatabaseWrapper(exp1));
		}

		DatabaseMergeWizard wz = new DatabaseMergeWizard(list);
		WizardDialog wd = new WizardDialog(shell, wz);
		
		wd.create();
		wd.getShell().open();
		
		// no selection: the finish button should be disabled
		assertFalse( bot.button("Finish").isEnabled() );

		bot.list(0).select(0);
		bot.list(1).select(0);
		
		// no selection: the finish button should be disabled
		assertTrue( bot.button("Finish").isEnabled() );
		
		bot.button("Finish").click();
		
		DatabasesToMerge dm = wz.getDatabaseToMerge();
		assertNotNull(dm);
		
		assertNotNull(dm.metric[0]);
		assertNotNull(dm.metric[1]);
	}



    @Test
	public void testChooseDatabasePage() throws Exception {
		var experiments = TestDatabase.getExperiments();
		List<IDatabase> list = new ArrayList<>(experiments.size() + 1);
		for(var exp: experiments) {
			list.add(new DatabaseWrapper(exp));
		}

		// add additional database
		list.add(new DatabaseWrapper(experiments.get(0)));
		
		DatabaseMergeWizard wz = new DatabaseMergeWizard(list);
		WizardDialog wd = new WizardDialog(shell, wz);
		
		wd.create();
		wd.getShell().open();
		
		// no selection: the finish and next button should be disabled
		assertFalse( bot.button("Next >").isEnabled() );
		assertFalse( bot.button("Finish").isEnabled() );

		bot.table(0).getTableItem(0).check();
		bot.table(0).getTableItem(1).check();
		
		assertTrue( bot.button("Next >").isEnabled() );		
		assertFalse( bot.button("Finish").isEnabled() );

		bot.button("Next >").click();

		bot.list(0).select(0);
		bot.list(1).select(0);
		
		// no selection: the finish button should be disabled
		assertTrue( bot.button("Finish").isEnabled() );

		bot.button("Finish").click();
		
		DatabasesToMerge dm = wz.getDatabaseToMerge();
		assertNotNull(dm);
		
		assertNotNull(dm.metric[0]);
		assertNotNull(dm.metric[1]);
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
			return rootFlat;
		}

		@Override
		public String getErrorMessage() {
			return null;
		}
		@Override
		public RootScope createCallersView(Scope rootCCT, RootScope rootBottomUp, IProgressReport progress) {
			return rootBottomUp;
		}
		
	}
}
