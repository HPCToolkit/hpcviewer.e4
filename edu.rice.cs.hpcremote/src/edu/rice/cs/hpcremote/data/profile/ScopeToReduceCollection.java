package edu.rice.cs.hpcremote.data.profile;

import java.io.IOException;
import java.util.List;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.hpctoolkit.client_server_common.calling_context.CallingContextId;
import org.hpctoolkit.client_server_common.metric.MetricId;
import org.hpctoolkit.hpcclient.v1_0.HpcClient;
import org.hpctoolkit.hpcclient.v1_0.UnknownCallingContextException;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.scope.LineScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.ScopeVisitType;
import edu.rice.cs.hpcdata.experiment.scope.visitors.ScopeVisitorAdapter;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

public class ScopeToReduceCollection extends ScopeVisitorAdapter 
{
	private Set<CallingContextId>  setOfCallingContextId;

	private MutableIntObjectMap<Scope> mapToScope;
	private final List<CallingContextId> listCCTId; 
	
	public ScopeToReduceCollection() {
		mapToScope = new IntObjectHashMap<>();
		listCCTId  = FastList.newList();
	}
		
	
	public void postProcess(HpcClient client, List<BaseMetric> metrics) throws UnknownCallingContextException, IOException, InterruptedException {
		if (listCCTId.isEmpty())
			return;
		
		setOfCallingContextId = HashSet.ofAll(listCCTId);
		
		// collect the metrics from remote server
		Set<MetricId> setMetricId = HashSet.empty();
		metrics.stream().forEach(metric -> {
			setMetricId.add(MetricId.make((short) metric.getIndex()));
		});
		
		var mapToMetrics = client.getMetrics(setOfCallingContextId, setMetricId);
		
		mapToMetrics.forEach((profile, setMetrics) -> {
			var setCCTNodes = setMetrics.getCallingContexts();
			setCCTNodes.forEach(node -> {
				var metricMeasurements = setMetrics.get(node);
				if (metricMeasurements.isPresent()) {
					var metric = metricMeasurements.get();
					var setMetricIndex = metric.getMetrics();
					
					setMetricIndex.forEach(m -> {
						var value = metric.getMeasurement(m);
						if (value.isPresent()) {
							var scope = mapToScope.get(node.toInt());	
							var index = m.toShort();
							scope.setMetricValue(index, value.get());
						}
					});
					
				}				
			});
		});
	}
	
	
	@Override
	public void visit(LineScope scope, ScopeVisitType vt) {
		if (vt == ScopeVisitType.PreVisit) {

			var list = scope.getScopeReduce();
			if (!list.isEmpty()) {
				CallingContextId parentId = CallingContextId.make(scope.getCCTIndex());
				listCCTId.add(parentId);
				
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
