package edu.rice.cs.hpcremote.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hpctoolkit.client_server_common.calling_context.CallingContextId;
import org.hpctoolkit.client_server_common.profile.ProfileId;
import org.hpctoolkit.hpcclient.v1_0.HpcClient;
import org.hpctoolkit.hpcclient.v1_0.UnknownProfileIdException;

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

	@Override
	public List<MetricValueSparse> getMetrics(IdTuple idtuple, int cctId) throws IOException {
		var profId = ProfileId.make(idtuple.getProfileIndex());
		var nodeId = CallingContextId.make(cctId);
		
		Set<CallingContextId> setNodeId = HashSet.of(nodeId);
		
		try {
			var metrics = client.getMetrics(profId, setNodeId);
			List<MetricValueSparse> listMetrics = new ArrayList<>(metrics.size());

			var iterator = metrics.iterator();
			while(iterator.hasNext()) {
				var item = iterator.next();
				item._2.forEach((k, v) -> {
					MetricValueSparse mvs = new MetricValueSparse(k.toShort(), v.getValue());
					listMetrics.add(mvs);
				});
			}
			return listMetrics;
			
		} catch (UnknownProfileIdException e) {
			throw new IllegalArgumentException("Unknown profile " + idtuple.toString() + " or cct " + cctId);
		} catch (InterruptedException e2) {
		    // Restore interrupted state...
		    Thread.currentThread().interrupt();
		}
		return Collections.emptyList();
	}
}
