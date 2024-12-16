// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctest.local;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.db.IFileDB;
import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpclocal.FilteredBaseData;
import edu.rice.cs.hpctest.util.TestDatabase;


public class FilteredBaseDataTest 
{
	private static List<IFileDB> listFileDB;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		listFileDB = TestDatabase.getFileDbs();
	}

	@AfterClass
	public static void tearDownAfterClass() {
		listFileDB.stream().forEach(IFileDB::dispose);
	}

	@Test
	public void testBaseData() {
		for(var fileDb : listFileDB) {
			var idt = fileDb.getIdTuple(IdTupleOption.BRIEF);
			var idtComplete = fileDb.getIdTuple(IdTupleOption.COMPLETE);
			assertEquals(idt, idtComplete);
			
			assertFalse(idt.isEmpty());
			
			var minLoc = fileDb.getMinLoc(idt.get(0));
			assertTrue(minLoc > 0);
			
			var maxLoc = fileDb.getMaxLoc(idt.get(0));
			assertTrue(maxLoc >= minLoc);
			
			var ranks = fileDb.getNumberOfRanks();
			assertTrue(ranks > 0);
			
			var levels =  fileDb.getParallelismLevel();
			assertTrue(levels > 0 && levels < 10);
			
			var labels = fileDb.getRankLabels();
			assertNotNull(labels);
			
			assertTrue(labels.length == idt.size());
			
			var offsets = fileDb.getOffsets();
			assertNotNull(offsets);
		}
	}

	@Test
	public void testFilteredData() throws IOException {
		IdTupleType idtypes = IdTupleType.createTypeWithOldFormat();
		
		for(var fileDb : listFileDB) {
			FilteredBaseData fbd = new FilteredBaseData(fileDb);
			var idt = fbd.getDenseListIdTuple(IdTupleOption.BRIEF);
			
			assertNotNull(idt);
			assertTrue(idt.size() == fileDb.getNumberOfRanks());

			var idt2 = fbd.getListOfIdTuples(IdTupleOption.BRIEF);			
			assertNotNull(idt2);
			assertTrue(idt.size() == idt2.size());
			
			// check original unfiltered traces
			checkIndex(fbd, idt, idtypes, true);
			
			// filter out even indexes
			List<Integer> indexes = new ArrayList<>();
			for(int i=0; i<idt.size(); i+=2) {
				indexes.add(i);
			}
			fbd.setIncludeIndex(indexes);
			
			checkIndex(fbd, idt, idtypes, false);			
		}
	}
	
	
	@Test
	public void testMapFromExecutionContextToNumberOfTraces() throws IOException {
		for(var fileDb : listFileDB) {
			FilteredBaseData fbd = new FilteredBaseData(fileDb);
			var mapProfileToSamples = fbd.getMapFromExecutionContextToNumberOfTraces();
			assertNotNull(mapProfileToSamples);
			
			var listProfiles = fileDb.getIdTuple(IdTupleOption.BRIEF);
			
			var negativeSamples = listProfiles.stream().filter(idt -> mapProfileToSamples.getNumberOfSamples(idt) < 0).findAny();
			assertTrue(negativeSamples.isEmpty());
		}
	}
	
	
	private void checkIndex(FilteredBaseData fbd, List<IdTuple> idt, IdTupleType idtypes, boolean isDensed) {
		var first = fbd.getFirstIncluded();
		assertEquals(0, first);
		
		var last = fbd.getLastIncluded();
		assertTrue(last >= first);
		
		if (isDensed) 
			assertEquals(idt.size()-1, last);
		else
			assertTrue(last <= idt.size()-1);
		
		assertTrue(fbd.isDenseBetweenFirstAndLast() == isDensed);
		
		assertTrue(fbd.isGPU(0) == idt.get(0).isGPU(idtypes));
		
		var listFilteredIdTuples = fbd.getListOfIdTuples(IdTupleOption.COMPLETE);
		assertTrue(idt.size() >= listFilteredIdTuples.size());

	}
}
