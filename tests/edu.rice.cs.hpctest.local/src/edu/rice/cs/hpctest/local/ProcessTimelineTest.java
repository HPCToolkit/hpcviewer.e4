package edu.rice.cs.hpctest.local;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcbase.IProcessTimeline;
import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;
import edu.rice.cs.hpcdata.util.ICallPath.ICallPathInfo;
import edu.rice.cs.hpclocal.LocalDBOpener;
import edu.rice.cs.hpctest.util.TestDatabase;
import edu.rice.cs.hpctraceviewer.data.Frame;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.timeline.ProcessTimeline;

public class ProcessTimelineTest 
{
	private static final int PIXELS_H = 1000;
	private static final int PIXELS_V = 500;

	private static List<SpaceTimeDataController> listData;

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

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		listData.forEach(data -> data.closeDB());
	}

	@Test
	public void testReadInData() throws IOException {
		for(var stdc: listData) {
			
			var ranks = stdc.getBaseData().getNumberOfRanks();
			assertTrue(ranks > 0);			
			
			stdc.resetProcessTimeline(ranks);

			var rankData = stdc.getBaseData();
			var idtuples = rankData.getListOfIdTuples(IdTupleOption.BRIEF);

			var service = stdc.getProcessTimelineService();
			assertNotNull(service);
			for(int i=0; i<ranks; i++) {
				service.setProcessTimeline(i, new ProcessTimeline(i, stdc, idtuples.get(i)));
			}

			// first rank
			var ptl = service.getProcessTimeline(0);
			testProcessTimeline(ptl, 0);
			
			// last rank
			ptl = service.getProcessTimeline(service.getNumProcessTimeline()-1);
			testProcessTimeline(ptl, service.getNumProcessTimeline()-1);
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
		assertNotNull(cpi);
		
		assertTrue( cpi.getMaxDepth() > 0);

		assertNotNull(cpi.getScope());
		
		assertNotNull(cpi.getScopeAt(0));
		
		assertEquals(cpi.getScope(), cpi.getScopeAt(cpi.getMaxDepth()));
	}
}
