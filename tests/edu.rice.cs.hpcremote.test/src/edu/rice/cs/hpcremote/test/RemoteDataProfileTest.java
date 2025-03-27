// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcremote.test;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.hpctoolkit.db.protocol.calling_context.CallingContextId;
import org.hpctoolkit.db.protocol.metric.MetricId;
import org.hpctoolkit.db.protocol.profile.ProfileId;
import org.hpctoolkit.db.client.BrokerClient;
import org.hpctoolkit.db.client.MetricsMeasurements;
import org.hpctoolkit.db.client.UnknownCallingContextException;
import org.hpctoolkit.db.client.UnknownProfileIdException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import org.hpctoolkit.db.local.db.IdTuple;
import org.hpctoolkit.db.local.db.IdTupleType;
import org.hpctoolkit.db.local.experiment.metric.MetricValue;
import edu.rice.cs.hpcremote.data.profile.RemoteDataProfile;
import edu.rice.cs.hpctest.util.TestDatabase;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Set;

public class RemoteDataProfileTest {
	private static File []files;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		files = TestDatabase.getDatabases();
	}


	@Test
	public void testParseHpcClient() throws IOException, InterruptedException, UnknownProfileIdException, UnknownCallingContextException {
		for(var fileDb : files) {
			BrokerClient client = Mockito.mock(BrokerClient.class);
			
			when(client.getHierarchicalIdentifierTuples()).thenReturn(HashSet.of(new IdTuple(0, 0)));
			
			IdTupleType idTypes = new IdTupleType();
			idTypes.initDefaultTypes();

			RemoteDataProfile rdp = new RemoteDataProfile(client, idTypes);
			
			assertThrows(IllegalAccessError.class, ()->{
				rdp.open(fileDb.getAbsolutePath());
			});
			Map<CallingContextId, MetricsMeasurements> []idTuplesToTest = new Map[] 
					{HashMap.empty(), 
					HashMap.of(CallingContextId.make(0), MetricsMeasurements.make(HashMap.of(MetricId.make((short) 0), MetricValue.NONE)))};
			for(var m: idTuplesToTest) {
				when(client.getMetrics(any(ProfileId.class), any(Set.class))).thenReturn(m);

				IdTuple idt = new IdTuple(0, 1);
				
				var metric = rdp.getMetric(idt, 0, 0);
				assertNotNull(metric);
			}
			
			rdp.close();
		}
	}
}
