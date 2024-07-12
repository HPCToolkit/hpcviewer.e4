// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctest.util;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;

import edu.rice.cs.hpclocal.LocalDBOpener;
import edu.rice.cs.hpctraceviewer.data.Frame;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;

public abstract class BaseTestAllTraceDatabases 
{
	protected static final int PIXELS_H = 1000;
	protected static final int PIXELS_V = 500;

	protected static List<SpaceTimeDataController> listData;

	
	protected BaseTestAllTraceDatabases() {
		// nothing, just to avoid warning
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		var experiments = TestDatabase.getExperiments();
		listData = new ArrayList<>();

		for(var exp: experiments) {
			if (exp.getTraceDataVersion() < 0)
				// no trace? skip it
				continue;

			var opener = new LocalDBOpener(exp);
			SpaceTimeDataController stdc = opener.openDBAndCreateSTDC(null);
			assertNotNull(stdc);

			home(stdc, new Frame());

			var attribute = stdc.getTraceDisplayAttribute();
			assertNotNull(attribute);

			attribute.setPixelHorizontal(PIXELS_H);
			attribute.setPixelVertical(PIXELS_V);

			listData.add(stdc);
		}
	}


	private static void home(SpaceTimeDataController stData, Frame frame) {
		frame.begProcess = 0;
		frame.endProcess = stData.getTotalTraceCount();
		
		frame.begTime = 0;
		frame.endTime = stData.getTimeWidth();
		
		stData.getTraceDisplayAttribute().setFrame(frame);
	}
}
