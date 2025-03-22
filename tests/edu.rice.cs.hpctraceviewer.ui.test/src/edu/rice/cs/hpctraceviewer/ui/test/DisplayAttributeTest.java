// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;
import edu.rice.cs.hpctest.util.BaseTestAllTraceDatabases;
import edu.rice.cs.hpctraceviewer.data.timeline.ProcessTimeline;

public class DisplayAttributeTest extends BaseTestAllTraceDatabases
{
	@Test
	public void testSpaceTimeDataController() {
		for(var stdc: listData) {
			var timeUnit = stdc.getTimeUnit();
			assertNotNull(timeUnit);
			
			// before setting the trace lines
			var proc = stdc.computeScaledProcess();
			assertEquals(0, proc);

			var traceLine = stdc.getCurrentSelectedTraceline();
			assertNull(traceLine);
			
			// setting the trace lines
			var rankData = stdc.getBaseData();
			assertNotNull(rankData);
			
			var idtuples = rankData.getListOfIdTuples(IdTupleOption.BRIEF);
			assertNotNull(idtuples);
			assertFalse(idtuples.isEmpty());
			
			var ranks = stdc.getBaseData().getNumberOfRanks();
			assertTrue(ranks > 0);			
			
			stdc.resetTracelines(ranks);
			
			var numTraces = stdc.getNumTracelines();
			assertTrue(numTraces <= ranks);
			
			for(int i=0; i<ranks; i++) {
				stdc.setTraceline(i, new ProcessTimeline(i, stdc, idtuples.get(i)));
			}
			
			// after setting the traces
			traceLine = stdc.getCurrentSelectedTraceline();
			assertNotNull(traceLine);
			
			var idt = traceLine.getProfileIdTuple();
			assertNotNull(idt);
			assertTrue(idt.getProfileIndex() > 0);
			
			var maxDepth = stdc.getMaxDepth();
			assertTrue(maxDepth > 0);
			
			var depth = stdc.getDefaultDepth();
			assertTrue(depth >= 0 && depth < maxDepth);
		}
	}

	@Test
	public void testTime() {
		for(var stdc: listData) {
			var attribute = stdc.getTraceDisplayAttribute();

			var time = attribute.getDisplayTimeUnit();
			assertNotNull(time);
			
			var timeUnit = attribute.computeDisplayTimeUnit(stdc.getTimeUnit());
			assertNotNull(timeUnit);
			
			var tuIncr =  attribute.increment(timeUnit);
			assertFalse(timeUnit == tuIncr);
			
			var cmp = tuIncr.compareTo(timeUnit);
			assertTrue(cmp > 0);
			
			var tuDecr = attribute.decrement(tuIncr);
			assertEquals(timeUnit, tuDecr);
			
			long t0 = attribute.convertPixelToTime(0);
			assertEquals(t0, attribute.getTimeBegin());
			
			long t1 = attribute.convertPixelToTime(PIXELS_H);
			assertEquals(t1, attribute.getTimeEnd());
			
			assertTrue(t1 >= t0);
			
			var ordNano = attribute.getTimeUnitOrdinal(TimeUnit.NANOSECONDS);
			assertEquals(0, ordNano);
			
			var ordDay = attribute.getTimeUnitOrdinal(TimeUnit.DAYS);
			assertEquals(6, ordDay);
		}
	}
	
	@Test
	public void testRanks() {
		for(var stdc: listData) {
			var attribute = stdc.getTraceDisplayAttribute();
			
			assertEquals(PIXELS_H, attribute.getPixelHorizontal());			
			assertEquals(PIXELS_V, attribute.getPixelVertical());			
			
			var dp = attribute.getProcessInterval();
			assertTrue(dp >= attribute.getProcessBegin() && dp <= attribute.getProcessEnd());
			
			final int depthPixel = 10;
			attribute.setDepthPixelVertical(depthPixel);
			
			assertEquals(depthPixel, attribute.getDepthPixelVertical());
			
			assertEquals(attribute.getProcessBegin(), attribute.convertPixelToRank(0));
			assertEquals(attribute.getProcessEnd(), attribute.convertPixelToRank(PIXELS_V));
			
			// test when too many ranks
			int pixelsV = attribute.getProcessEnd()-1;
			attribute.setPixelVertical(pixelsV);
			assertEquals(attribute.getProcessBegin(), attribute.convertPixelToRank(0));
			assertEquals(attribute.getProcessEnd(), attribute.convertPixelToRank(pixelsV));
			
			// return the value back
			attribute.setPixelVertical(PIXELS_V);
			
			var firstRankPixel = attribute.convertRankToPixel(attribute.getProcessBegin());
			assertTrue(0 <= firstRankPixel);
			assertTrue(PIXELS_V/attribute.getProcessInterval() >= firstRankPixel);
			
			var firstPixelRank = attribute.convertPixelToRank(firstRankPixel);			
			assertEquals(firstPixelRank, firstRankPixel);
		}
	}
	
}
