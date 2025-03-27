// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcremote.data;

import java.io.IOException;

import org.hpctoolkit.db.protocol.calling_context.CallingContextId;
import org.hpctoolkit.db.protocol.metric.MetricId;
import org.hpctoolkit.db.protocol.profile.ProfileId;
import org.hpctoolkit.db.client.BrokerClient;
import org.hpctoolkit.db.client.ContextMeasurementsMap;
import org.hpctoolkit.db.client.UnknownCallingContextException;

import org.hpctoolkit.db.local.db.version4.DataPlotEntry;
import org.hpctoolkit.db.local.db.version4.IDataCCT;
import org.hpctoolkit.db.local.experiment.metric.MetricValue;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;

public class RemoteDataCCT implements IDataCCT 
{
	private final BrokerClient client;
	
	public RemoteDataCCT(BrokerClient client) {
		this.client = client;
	}
	
	@Override
	public DataPlotEntry[] getPlotEntry(int cct, int metric) throws IOException {
		var cctId = CallingContextId.make(cct);
		var setCCT = HashSet.of(cctId);
		
		var metricId = MetricId.make((short) metric);
		var setMetrics = HashSet.of(metricId);
		try {
			var mapValues =  client.getMetrics(setCCT, setMetrics);
			if (!mapValues.isEmpty()) {
				return generatePlotEntry(mapValues, cctId, metricId);
			}
			
		} catch (UnknownCallingContextException e) {
			throw new IllegalArgumentException("Unknown CCT+ " + cct);
		} catch (InterruptedException e) {
		    // Restore interrupted state...
		    Thread.currentThread().interrupt();				
		}
		return new DataPlotEntry[0];
	}
	
	private DataPlotEntry[] generatePlotEntry(Map<ProfileId, ContextMeasurementsMap> mapValues, CallingContextId cctId, MetricId metricId) {
		var size = mapValues.size();
		var entries = new DataPlotEntry[size];
		
		int i=0;
		var iterator = mapValues.iterator();
		while(iterator.hasNext()) {
			var item = iterator.next();
			var profileId = item._1;
			var mapMetric = item._2;
			var metrics = mapMetric.get(cctId);
			if (metrics.isEmpty())
				return new DataPlotEntry[0];
			
			var metric = metrics.get().getMeasurement(metricId);
			var entry = new DataPlotEntry(profileId.toInt(), metric.orElse(new MetricValue(0)).getValue());
			entries[i] = entry;
			i++;
		}
		return entries;
	}
	
	@Override
	public void close() throws IOException {
		// nothing
	}

	@Override
	public java.util.Set<Integer> getCallingContexts() {
		throw new IllegalAccessError("Not supported yet");
	}

}
