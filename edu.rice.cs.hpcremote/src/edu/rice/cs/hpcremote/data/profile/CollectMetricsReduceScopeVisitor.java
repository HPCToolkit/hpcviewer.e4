// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcremote.data.profile;

import java.io.IOException;
import java.util.List;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.hpctoolkit.db.protocol.calling_context.CallingContextId;
import org.hpctoolkit.db.protocol.profile.ProfileId;
import org.hpctoolkit.db.client.BrokerClient;
import org.hpctoolkit.db.client.UnknownCallingContextException;
import org.hpctoolkit.db.client.UnknownProfileIdException;
import org.slf4j.LoggerFactory;

import org.hpctoolkit.db.local.db.IdTuple;
import org.hpctoolkit.db.local.experiment.scope.LineScope;
import org.hpctoolkit.db.local.experiment.scope.Scope;
import org.hpctoolkit.db.local.experiment.scope.ScopeVisitType;
import org.hpctoolkit.db.local.experiment.scope.visitors.ScopeVisitorAdapter;
import org.hpctoolkit.db.local.util.IProgressReport;
import edu.rice.cs.hpcremote.data.EmptyMetricValueCollection;
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
	private final MutableIntObjectMap<Scope> mapToScope;
	
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
	 * 
	 * @throws UnknownCallingContextException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws UnknownProfileIdException
	 */
	public void postProcess(BrokerClient client) 
			throws UnknownCallingContextException, IOException, InterruptedException, UnknownProfileIdException {
		if (listCCTId.isEmpty())
			return;
		
		var setOfCallingContextId = HashSet.ofAll(listCCTId);
		progress.advance();
		
		// collect the metrics from remote server
		// Warning: this may take some time.
		var mapToMetrics = client.getMetrics(ProfileId.make(IdTuple.PROFILE_SUMMARY.getProfileIndex()), setOfCallingContextId);
		
		mapToMetrics.forEach((cctId, metricMeasurements) -> {
			var scope = mapToScope.remove(cctId.toInt());
			if (scope == null) {
				LoggerFactory.getLogger(getClass()).warn("Node {} not exist in CCT", cctId.toInt());
			} else {
				var setOfMetricId = metricMeasurements.getMetrics();
				
				setOfMetricId.toStream().forEach(mId -> {
					var mv = metricMeasurements.getMeasurement(mId);
					if (mv.isPresent()) {
						scope.setMetricValue(mId.toShort(), mv.get());
					}
				});
			}
		});
		// set empty metrics if necessary
		var iterator = mapToScope.iterator();
		while(iterator.hasNext()) {
			var scope = iterator.next();
			scope.setMetricValues(EmptyMetricValueCollection.getInstance());
		}
		
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
			var iterator = scope.getScopeReduceIterator();
			if (iterator != null && iterator.hasNext()) {
				CallingContextId parentId = CallingContextId.make(scope.getCCTIndex());
				listCCTId.add(parentId);
				mapToScope.put(scope.getCCTIndex(), scope);
				
				while (iterator.hasNext()) {
					var childScope = iterator.next();
					addToList(childScope);
				}
			}
			// issue #364: make sure all the line scopes are included in the request.
			// 
			addToList(scope);
		}
	}
	
	private void addToList(Scope scope) {
		var index = scope.getCCTIndex();
		var cctId = CallingContextId.make(index);

		listCCTId.add(cctId);
		mapToScope.put(index, scope);
	}
}
