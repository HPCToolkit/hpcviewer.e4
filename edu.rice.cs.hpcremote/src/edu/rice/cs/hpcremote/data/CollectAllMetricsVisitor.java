package edu.rice.cs.hpcremote.data;

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
import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.LineScope;
import edu.rice.cs.hpcdata.experiment.scope.LoopScope;
import edu.rice.cs.hpcdata.experiment.scope.ProcedureScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.ScopeVisitType;
import edu.rice.cs.hpcdata.experiment.scope.visitors.ScopeVisitorAdapter;
import edu.rice.cs.hpcdata.util.IProgressReport;
import io.vavr.collection.HashSet;

public class CollectAllMetricsVisitor extends ScopeVisitorAdapter 
{
	private final MutableIntObjectMap<Scope> mapToScope;	
	private final List<CallingContextId> listCCTId;
	private final IProgressReport progress;
	
	
	public CollectAllMetricsVisitor(IProgressReport progress) {
		this.progress = progress;
		
		mapToScope = new IntObjectHashMap<>();
		listCCTId  = FastList.newList();
		
		progress.begin("Collect metrics", 3);
		progress.advance();
	}
	
	
	public void postProcess(HpcClient client) throws UnknownProfileIdException, UnknownCallingContextException, IOException, InterruptedException {
		if (listCCTId.isEmpty())
			return;
		
		var setOfCallingContextId = HashSet.ofAll(listCCTId);
		progress.advance();
		
		// collect the metrics from remote server
		// Warning: this may take some time.
		var mapToMetrics = client.getMetrics(ProfileId.make(IdTuple.PROFILE_SUMMARY.getProfileIndex()), setOfCallingContextId);
		
		mapToMetrics.forEach((cctId, metricMeasurements) -> {
			var scope = mapToScope.remove(cctId.toInt());
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
		mapToScope.stream().forEach((scope) -> {
			
		});
		
		dispose();
		
		progress.end();
	}
	
	
	private void dispose() {
		mapToScope.clear();
		listCCTId.clear();
	}

	
	@Override
	public void visit(LineScope scope, ScopeVisitType vt) {
		if (vt == ScopeVisitType.PreVisit) {
			add(scope);
		}
	}

	@Override
	public void visit(LoopScope scope, ScopeVisitType vt) {
		if (vt == ScopeVisitType.PreVisit) {
			add(scope);
		}
	}

	@Override
	public void visit(CallSiteScope scope, ScopeVisitType vt) {
		if (vt == ScopeVisitType.PreVisit) {
			add(scope);
		}
	}

	@Override
	public void visit(ProcedureScope scope, ScopeVisitType vt) {
		if (vt == ScopeVisitType.PreVisit) {
			add(scope);
		}
	}

	@Override
	public void visit(RootScope scope, ScopeVisitType vt) {
		if (vt == ScopeVisitType.PreVisit) {
			add(scope);
		}
	}

	@Override
	public void visit(Scope scope, ScopeVisitType vt) {
		if (vt == ScopeVisitType.PreVisit) {
			add(scope);
		}
	}

	
	private void add(Scope scope) {
		if (scope.getMetricValues().getValues() == null) {
			// this scope has no metric value or not initialized yet.
			// add it to the list of scope to be sent to the server.
			
			var index = scope.getCCTIndex();
			
			listCCTId.add(CallingContextId.make(index));
			mapToScope.put(index, scope);
		}
	}
}
