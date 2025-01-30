// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcremote.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.hpctoolkit.hpcclient.v1_0.BrokerClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import edu.rice.cs.hpcdata.db.IFileDB;
import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcremote.trace.RemoteFilteredData;
import edu.rice.cs.hpctest.util.TestDatabase;
import io.vavr.collection.HashMap;

public class RemoteFilteredDataTest 
{
	private static List<IFileDB> listFileDB;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		listFileDB = TestDatabase.getFileDbs();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		listFileDB.stream().forEach(IFileDB::dispose);
	}

	@Test
	public void testGetMapFromExecutionContextToNumberOfTraces() throws IOException, InterruptedException {
		for(var fileDb : listFileDB) {
			List<IdTuple> idTuples = fileDb.getIdTuple(IdTupleOption.BRIEF);
			
			java.util.Map<Integer, Integer> mapSamples = new java.util.HashMap<>(idTuples.size());
			idTuples.forEach(idt -> mapSamples.put(idt.getProfileIndex(), idt.getProfileIndex()));
			var mapProfileToSamples = HashMap.ofAll(mapSamples);
			
			var hpcClient = Mockito.mock(BrokerClient.class);
			when(hpcClient.getNumberOfSamplesPerTrace()).thenReturn(mapProfileToSamples);
			
			RemoteFilteredData data = new RemoteFilteredData(hpcClient, idTuples, fileDb.getIdTupleTypes());

			// -----------------------------
			// test with dense indexes
			// -----------------------------

			testData(data, idTuples);
			assertTrue(data.isDenseBetweenFirstAndLast());
			
			// -----------------------------
			// test with filtered indexes
			// -----------------------------
			final int size = idTuples.size() / 2;
			List<Integer> includedIndexes = new ArrayList<>(size);
			for(int i=0; i<size; i++) {
				includedIndexes.add(i);
			}
			data.setIncludeIndex(includedIndexes);
			assertTrue(data.isDenseBetweenFirstAndLast());

			testData(data, idTuples);
			
			data.dispose();
		}
	}

	private void testData(RemoteFilteredData data, List<IdTuple> idTuples) {

		data.hasGPU();
		
		var map = data.getMapFromExecutionContextToNumberOfTraces();
		assertNotNull(map);
	
		idTuples.forEach(idt -> {
			int samples = map.getNumberOfSamples(idt);
			assertEquals(idt.getProfileIndex()-1, samples);
		});
		
		assertEquals(0, data.getFirstIncluded());
		
		assertTrue(data.getLastIncluded() >= 0);
		
		var isGPU = data.isGPU(0);
		IdTupleType type = IdTupleType.createTypeWithOldFormat();
		
		assertEquals(idTuples.get(0).isGPU(type), isGPU);
	}
}
