package edu.rice.cs.hpcremote.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hpctoolkit.client_server_common.calling_context.CallingContextId;
import org.hpctoolkit.client_server_common.profile.ProfileId;
import org.hpctoolkit.hpcclient.v1_0.HpcClient;
import org.hpctoolkit.hpcclient.v1_0.UnknownCallingContextException;
import org.hpctoolkit.hpcclient.v1_0.UnknownProfileIdException;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.db.version4.AbstractDataProfile;
import edu.rice.cs.hpcdata.db.version4.IDataProfile;
import edu.rice.cs.hpcdata.experiment.metric.MetricValueSparse;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;


/*************************************************************
 * 
 * The implementation of {@code IDataProfile} for remote database
 * <p>
 * This class has exactly the same functionality as {@code DataSummary}, 
 * the implementation for local database but can remotely access remote
 * {@code profile.db} file and grab the metrics on the fly.
 * </p>
 * @see edu.rice.cs.hpcdata.db.version4.DataSummary
 * 
 *************************************************************/
public class RemoteDataProfile extends AbstractDataProfile implements IDataProfile
{
	private final HpcClient client;

	
	/*****
	 * Create an instance of remote access to {@code profile.db} file.
	 * 
	 * @param client
	 * 			An object of {@code HpcClient} to access remote database
	 * @param idTupleTypes
	 * 			DataMeta's types of id tuples
	 */
	public RemoteDataProfile(HpcClient client, IdTupleType idTupleTypes) {
		super(idTupleTypes);
		this.client = client;
		
		initIdTuples(client);
	}
	
	
	private void initIdTuples(HpcClient client) {
		
		Set<IdTuple> idtuples;
		try {
			idtuples = client.getHierarchicalIdentifierTuples();
			
			List<IdTuple> list = new ArrayList<>(idtuples.size());
			idtuples.forEach(list::add);
			
			setIdTuple(list);
		} catch (IOException | InterruptedException e) {
			LoggerFactory.getLogger(getClass()).error("Fail to get remote id tuples", e);
		    // Restore interrupted state...
		    Thread.currentThread().interrupt();
		}
	}
	
	
	@Override
	public void open(final String file) {
		throw new IllegalAccessError("The method is cannot be used for opening remote database");
	}
	

	@Override
	public double getMetric(IdTuple idtuple, int cctId, int metricId) throws IOException {
		var listMetrics = getMetrics(idtuple, cctId);
		var value = listMetrics.stream().filter(mvs -> mvs.getIndex() == metricId).findAny();
		if (value.isPresent())
			return value.get().getValue();

		return 0;
	}
	
	private long totalTime = 0;
	private int numRequest = 0;
	private long  maxTime  = 0;
	
	public void printTime() {
		var meanTime = totalTime / numRequest;
		System.out.printf("Total time: %d%nAverage: %d%nNum request: %d%nMax time: %d%n", totalTime, meanTime, numRequest, maxTime);
	}
	
	public void resetTime() {
		totalTime = 0;
		numRequest = 0;
		maxTime  = 0;
	}

	@Override
	public List<MetricValueSparse> getMetrics(IdTuple idtuple, int cctId) throws IOException {
		var profId = ProfileId.make(idtuple.getProfileIndex());
		var nodeId = CallingContextId.make(cctId);
		
		Set<CallingContextId> setNodeId = HashSet.of(nodeId);
		
		try {
			var t0 = System.nanoTime();
			var mapCCTIndexToMetrics = client.getMetrics(profId, setNodeId);
			if (mapCCTIndexToMetrics.isEmpty())
				return Collections.emptyList();
			
			var t1 = System.nanoTime();
			long dt = t1-t0;
			totalTime += dt;
			numRequest++;
			maxTime = Math.max(maxTime, dt);
			if (numRequest % 200 == 0)
				printTime();
			
			List<MetricValueSparse> listMetrics = new ArrayList<>(mapCCTIndexToMetrics.size());

			var iterator = mapCCTIndexToMetrics.iterator();
			while(iterator.hasNext()) {
				var item = iterator.next();
				var cct = item._1.toInt();
				if (cct == cctId) {
					var metricMeasurements = item._2;
					var setMetrics = metricMeasurements.getMetrics();
					
					setMetrics.toStream().forEach(m -> {
						var metId = m.toShort();
						var value = metricMeasurements.getMeasurement(m);
						if (value.isPresent()) {							
							MetricValueSparse mvs = new MetricValueSparse(metId, value.get().getValue());
							listMetrics.add(mvs);
						}
					});
				}
			}
			return listMetrics;
			
		} catch (UnknownProfileIdException e) {
			throw new IllegalArgumentException("Unknown profile " + idtuple.toString());
		} catch (InterruptedException e2) {
		    // Restore interrupted state...
		    Thread.currentThread().interrupt();
		} catch (UnknownCallingContextException e) {
			throw new IllegalArgumentException("Unknown CCT " + cctId);
		}
		return Collections.emptyList();
	}
}
