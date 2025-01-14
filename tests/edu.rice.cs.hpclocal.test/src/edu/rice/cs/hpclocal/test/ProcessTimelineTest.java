// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpclocal.test;

import static org.junit.Assert.*;

import java.io.IOException;
import org.junit.AfterClass;
import org.junit.Test;

import edu.rice.cs.hpcbase.IProcessTimeline;
import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;
import edu.rice.cs.hpcdata.util.ICallPath.ICallPathInfo;
import edu.rice.cs.hpctest.util.BaseTestAllTraceDatabases;
import edu.rice.cs.hpctraceviewer.data.timeline.ProcessTimeline;

public class ProcessTimelineTest extends BaseTestAllTraceDatabases
{
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		listData.forEach(data -> data.closeDB());
	}

	@Test
	public void testReadInData() throws IOException {
		for(var stdc: listData) {
			
			var ranks = stdc.getBaseData().getNumberOfRanks();
			assertTrue(ranks > 0);			
			
			stdc.resetTracelines(ranks);

			var rankData = stdc.getBaseData();
			var idtuples = rankData.getListOfIdTuples(IdTupleOption.BRIEF);

			var numTraces = stdc.getNumTracelines();
			assertTrue(numTraces <= ranks);
			
			for(int i=0; i<ranks; i++) {
				stdc.setTraceline(i, new ProcessTimeline(i, stdc, idtuples.get(i)));
			}

			// first rank
			var ptl = stdc.getTraceline(0);
			testProcessTimeline(ptl, 0);
			
			// last rank
			ptl = stdc.getTraceline(stdc.getNumTracelines()-1);
			testProcessTimeline(ptl, stdc.getNumTracelines()-1);
		}
	}
	
	
	private void testProcessTimeline(IProcessTimeline ptl, int line) throws IOException {
		assertNotNull(ptl);
		
		assertEquals(line, ptl.line());
		
		// read the data from the disk
		ptl.readInData();
		
		assertTrue(ptl.size() >= 0);
		
		var idt = ptl.getProfileIdTuple();
		assertNotNull(idt);
		assertFalse(idt.getProfileIndex() == 0);
		
		if (ptl.size() > 0) {
			var cpi = ptl.getCallPathInfo(0);
			testCallPathInfo(cpi);
			
			var time1 = ptl.getTime(0);
			assertTrue(time1 > 0);
			
			var time2 = ptl.getTime(ptl.size()-1);
			assertTrue(time1 <= time2);
			
			assertTrue( ptl.getContextId(0) >= 0);
			
			var midTime = (time2 + time1) / 2;
			int index = ptl.findMidpointBefore(midTime, false);
			assertTrue(index >= 0);
			
			ptl.shiftTimeBy(100);
			var time1prime = ptl.getTime(0);
			assertEquals(time1-100, time1prime);
			
			ptl.shiftTimeBy(-100);
			time1prime = ptl.getTime(0);
			assertEquals(time1, time1prime);
			
			assertFalse(ptl.isEmpty());
		}
	}
	
	
	private void testCallPathInfo(ICallPathInfo cpi) {
		// for issue #233 database, cpi can be null
		if (cpi == null)
			return;
		
		assertNotNull(cpi);
		
		assertTrue( cpi.getMaxDepth() > 0);

		assertNotNull(cpi.getScope());
		
		assertNotNull(cpi.getScopeAt(0));
		
		assertEquals(cpi.getScope(), cpi.getScopeAt(cpi.getMaxDepth()));
	}
}
