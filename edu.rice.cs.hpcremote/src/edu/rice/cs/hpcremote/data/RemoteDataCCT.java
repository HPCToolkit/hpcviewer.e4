package edu.rice.cs.hpcremote.data;

import java.io.IOException;

import org.hpctoolkit.client_server_common.calling_context.CallingContextId;
import org.hpctoolkit.client_server_common.metric.MetricId;
import org.hpctoolkit.client_server_common.profile.ProfileId;
import org.hpctoolkit.hpcclient.v1_0.ContextMeasurementsMap;
import org.hpctoolkit.hpcclient.v1_0.HpcClient;
import org.hpctoolkit.hpcclient.v1_0.UnknownCallingContextException;

import edu.rice.cs.hpcdata.db.version4.DataPlotEntry;
import edu.rice.cs.hpcdata.db.version4.IDataCCT;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;

public class RemoteDataCCT implements IDataCCT 
{
	private final HpcClient client;
	
	public RemoteDataCCT(HpcClient client) {
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
