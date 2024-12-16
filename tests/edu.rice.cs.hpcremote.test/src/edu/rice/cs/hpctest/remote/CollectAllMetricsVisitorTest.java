// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctest.remote;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.hpctoolkit.client_server_common.calling_context.CallingContextId;
import org.hpctoolkit.client_server_common.metric.MetricId;
import org.hpctoolkit.client_server_common.profile.ProfileId;
import org.hpctoolkit.hpcclient.v1_0.BrokerClient;
import org.hpctoolkit.hpcclient.v1_0.MetricsMeasurements;
import org.junit.Test;
import org.mockito.Mockito;

import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.util.IProgressReport;
import edu.rice.cs.hpcremote.data.CollectAllMetricsVisitor;
import edu.rice.cs.hpctest.util.TestDatabase;
import io.vavr.collection.HashMap;

public class CollectAllMetricsVisitorTest {

	@Test
	public void testCollectAllMetricsVisitor() throws Exception {
		var experiments = TestDatabase.getExperiments();
		
		for (var exp: experiments) {
			var root = exp.getRootScope(RootScopeType.CallingContextTree);
			assertNotNull(root);
			
			MetricValue mv = new MetricValue(1.1);
			
			var hpcClient = Mockito.mock(BrokerClient.class);
			when(hpcClient.getMetrics(any(ProfileId.class), any()))
						  .thenReturn(HashMap.of(CallingContextId.make(1), 
								  				 MetricsMeasurements.make(HashMap.of(MetricId.make((short) 0), mv))));

			var collect = new CollectAllMetricsVisitor(IProgressReport.dummy());
			root.dfsVisitScopeTree(collect);
			collect.postProcess(hpcClient);
			
			var children = root.getChildren();
			for(var child: children) {
				var m = child.getDirectMetricValue(0);
				assertNotNull(m);
			}
		}
	}
}
