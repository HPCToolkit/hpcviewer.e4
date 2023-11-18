package edu.rice.cs.hpctest.local;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcbase.IFilteredData;
import edu.rice.cs.hpcbase.ITraceDataCollector.TraceOption;
import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.LocalDatabaseRepresentation;
import edu.rice.cs.hpclocal.ILocalBaseData;
import edu.rice.cs.hpclocal.LocalDBOpener;
import edu.rice.cs.hpclocal.LocalTraceDataCollector;
import edu.rice.cs.hpclocal.SpaceTimeDataControllerLocal;
import edu.rice.cs.hpctest.util.TestDatabase;

public class LocalTraceDataCollectorTest 
{
	private static List<SpaceTimeDataControllerLocal> list;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		list = new ArrayList<>();
		
		var directories = TestDatabase.getDatabases();
		
		for(var dir: directories) {
			Experiment e = new Experiment();
			
			var localDb = new LocalDatabaseRepresentation(dir, null, true);
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
			stdc.closeDB();
		}
	}

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
