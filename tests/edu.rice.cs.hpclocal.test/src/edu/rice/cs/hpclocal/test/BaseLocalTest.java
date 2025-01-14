// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpclocal.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.LocalDatabaseRepresentation;
import edu.rice.cs.hpcdata.util.IProgressReport;
import edu.rice.cs.hpclocal.LocalDBOpener;
import edu.rice.cs.hpclocal.SpaceTimeDataControllerLocal;
import edu.rice.cs.hpctest.util.TestDatabase;

public abstract class BaseLocalTest 
{
	protected static List<SpaceTimeDataControllerLocal> list;

	protected BaseLocalTest() {
		//nothing
	}
	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		list = new ArrayList<>();
		
		var directories = TestDatabase.getDatabases();
		
		for(var dir: directories) {
			Experiment e = new Experiment();
			
			var localDb = new LocalDatabaseRepresentation(dir, null, IProgressReport.dummy());
			e.open(localDb);
			var opener = new LocalDBOpener(e);
			
			if (e.getTraceDataVersion() < 0)
				continue;

			var stdc = opener.openDBAndCreateSTDC(null);
			
			list.add((SpaceTimeDataControllerLocal) stdc);
		}
	}

	@AfterClass
	public static void tearDownAfterClass() {
		for(var stdc: list) {
			stdc.dispose();
		}
	}

}
