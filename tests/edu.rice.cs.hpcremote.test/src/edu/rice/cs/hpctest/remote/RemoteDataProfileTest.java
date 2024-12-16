// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctest.remote;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.hpctoolkit.client_server_common.calling_context.CallingContextId;
import org.hpctoolkit.client_server_common.metric.MetricId;
import org.hpctoolkit.client_server_common.profile.ProfileId;
import org.hpctoolkit.hpcclient.v1_0.BrokerClient;
import org.hpctoolkit.hpcclient.v1_0.MetricsMeasurements;
import org.hpctoolkit.hpcclient.v1_0.UnknownCallingContextException;
import org.hpctoolkit.hpcclient.v1_0.UnknownProfileIdException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
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
