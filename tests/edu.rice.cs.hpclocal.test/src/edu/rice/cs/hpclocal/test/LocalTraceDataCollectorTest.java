// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpclocal.test;

import static org.junit.Assert.*;

import java.io.IOException;
import org.junit.Test;

import edu.rice.cs.hpcbase.IFilteredData;
import edu.rice.cs.hpcbase.ITraceDataCollector.TraceOption;
import org.hpctoolkit.db.local.db.IFileDB.IdTupleOption;
import org.hpctoolkit.db.local.db.IdTuple;
import edu.rice.cs.hpclocal.ILocalBaseData;
import edu.rice.cs.hpclocal.LocalTraceDataCollector;

public class LocalTraceDataCollectorTest extends BaseLocalTest
{
	@Test
	public void testReadInData() throws IOException {
		
		for(var stdc: list) {
			var baseData = stdc.getBaseData();

			var tbeg = stdc.getMinBegTime();
			var tend = stdc.getMaxEndTime();

			var listIdtuple = baseData.getListOfIdTuples(IdTupleOption.BRIEF);
			
			// first id tuple
			var firstProfile = listIdtuple.get(0);			
			testCollection(tbeg, tend, baseData, firstProfile);
			
			// last id tuple
			var lastProfile = listIdtuple.get(listIdtuple.size()-1);
			testCollection(tbeg, tend, baseData, lastProfile);
		}
	}
	
	
	private void testCollection(long tbeg, long tend, IFilteredData baseData, IdTuple profile) throws IOException {
		final int MAX_SAMPLES = 2;

		LocalTraceDataCollector collector = new LocalTraceDataCollector(baseData, profile, MAX_SAMPLES, TraceOption.REVEAL_GPU_TRACE);
		float pixelLength = (tend - tbeg) / MAX_SAMPLES;
		
		assertEquals(0, collector.size());
		
		collector.readInData(tbeg, tend, pixelLength - 1);
		
		assertTrue(MAX_SAMPLES >= collector.size());
		
		int cpid = collector.getCpid(0);
		assertTrue(cpid >= 0);
		
		var time = collector.getTime(0);
		assertTrue(time >= tbeg && time <= tend);
		
		collector.shiftTimeBy(tbeg);
		time = collector.getTime(0);
		assertTrue(time >= 0);
		
		ILocalBaseData localData = (ILocalBaseData) baseData;

		var minLoc = localData.getMinLoc(profile);
		var maxLoc = localData.getMaxLoc(profile);
		var minTime = localData.getLong(minLoc);
		var maxTime = localData.getLong(maxLoc);
		
		var recordSize = localData.getRecordSize();
		int numRecord  = (int) ((maxLoc - minLoc) / recordSize);
		pixelLength = (float)(maxTime - minTime) / numRecord;
		
		collector = new LocalTraceDataCollector(baseData, profile, numRecord, TraceOption.REVEAL_GPU_TRACE);
		collector.readInData(tbeg, tend, pixelLength);
		
		var size = collector.size();
		
		assertTrue(size <= numRecord);
	}
}
