package edu.rice.cs.hpcremote.data.profile;

import java.io.IOException;
import java.util.List;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.hpctoolkit.client_server_common.calling_context.CallingContextId;
import org.hpctoolkit.client_server_common.profile.ProfileId;
import org.hpctoolkit.hpcclient.v1_0.HpcClient;
import org.hpctoolkit.hpcclient.v1_0.UnknownCallingContextException;
import org.hpctoolkit.hpcclient.v1_0.UnknownProfileIdException;

import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.scope.LineScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.ScopeVisitType;
import edu.rice.cs.hpcdata.experiment.scope.visitors.ScopeVisitorAdapter;
import edu.rice.cs.hpcdata.util.IProgressReport;
import io.vavr.collection.HashSet;


/*******************************************************
 * 
 * Collecting metric values to perform metric reduction in 
 * calling context reassignment so that it runs faster.
 * <br/>
 * Caller is required to call {@code postProcess()} method to 
 * retrieve and finalize the metrics from the remote server. 
 *
 *******************************************************/
public class CollectMetricsReduceScopeVisitor extends ScopeVisitorAdapter 
{
	private MutableIntObjectMap<Scope> mapToScope;
	
	private final List<CallingContextId> listCCTId;
	private final IProgressReport progress;
	
	
	/****
	 * Constructor
	 * 
	 * @param progress
	 * 			Non-null progress monitor
	 */
	public CollectMetricsReduceScopeVisitor(IProgressReport progress) {
		this.progress = progress == null ? IProgressReport.dummy() : progress;
		
		mapToScope = new IntObjectHashMap<>();
		listCCTId  = FastList.newList();
		
		this.progress.begin("Collecting metrics", 3);
		this.progress.advance();
	}
		
	
	/****
	 * A mandatory call after tree traversal.
	 * Without calling this, everything is useless.
	 * 
	 * @param client
	 * @param metrics
	 * 
	 * @throws UnknownCallingContextException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws UnknownProfileIdException
	 */
	public void postProcess(HpcClient client, List<BaseMetric> metrics) 
			throws UnknownCallingContextException, IOException, InterruptedException, UnknownProfileIdException {
		if (listCCTId.isEmpty())
			return;
		
		var setOfCallingContextId = HashSet.ofAll(listCCTId);
		progress.advance();
		
		// collect the metrics from remote server
		// Warning: this may take some time.
		var mapToMetrics = client.getMetrics(ProfileId.make(IdTuple.PROFILE_SUMMARY.getProfileIndex()), setOfCallingContextId);
		
		mapToMetrics.forEach((cctId, metricMeasurements) -> {
			var scope = mapToScope.get(cctId.toInt());
			if (scope != null) {
				var setOfMetricId = metricMeasurements.getMetrics();
				setOfMetricId.toStream().forEach(mId -> {
					var mv = metricMeasurements.getMeasurement(mId);
					if (mv.isPresent()) {
						scope.setMetricValue(mId.toShort(), mv.get());
					}
				});
			}
		});
		
		dispose();
		
		progress.end();
	}
	
	
	private void dispose() {
		mapToScope.clear();
		mapToScope = null;
		
		listCCTId.clear();
	}


	@Override
	public void visit(LineScope scope, ScopeVisitType vt) {
		if (vt == ScopeVisitType.PreVisit) {

			var list = scope.getScopeReduce();
			if (!list.isEmpty()) {
				CallingContextId parentId = CallingContextId.make(scope.getCCTIndex());
				listCCTId.add(parentId);
				mapToScope.put(scope.getCCTIndex(), scope);
				
				list.stream().forEach(childScope -> {
					var index = childScope.getCCTIndex();
					var cctId = CallingContextId.make(index);
					
					listCCTId.add(cctId);
					mapToScope.put(index, childScope);
				});
			}
		}
	}
}
