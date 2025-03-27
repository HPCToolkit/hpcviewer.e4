// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcremote.test;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.hpctoolkit.db.protocol.calling_context.CallingContextId;
import org.hpctoolkit.db.protocol.metric.MetricId;
import org.hpctoolkit.db.protocol.profile.ProfileId;
import org.hpctoolkit.db.client.BrokerClient;
import org.hpctoolkit.db.client.MetricsMeasurements;
import org.junit.Test;
import org.mockito.Mockito;

import org.hpctoolkit.db.local.experiment.metric.MetricValue;
import org.hpctoolkit.db.local.experiment.scope.RootScopeType;
import org.hpctoolkit.db.local.util.IProgressReport;
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
