package edu.rice.cs.hpctest.remote;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import org.hpctoolkit.hpcclient.v1_0.HpcClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import edu.rice.cs.hpcdata.db.IFileDB;
import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;
import edu.rice.cs.hpcdata.db.IdTuple;
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
			
			var hpcClient = Mockito.mock(HpcClient.class);
			when(hpcClient.getNumberOfSamplesPerTrace()).thenReturn(mapProfileToSamples);
			
			RemoteFilteredData data = new RemoteFilteredData(hpcClient, idTuples, fileDb.getIdTupleTypes());

			var map = data.getMapFromExecutionContextToNumberOfTraces();
			assertNotNull(map);
		
			idTuples.forEach(idt -> {
				int samples = map.getNumberOfSamples(idt);
				assertEquals(idt.getProfileIndex()-1, samples);
			});
		}
	}

}
