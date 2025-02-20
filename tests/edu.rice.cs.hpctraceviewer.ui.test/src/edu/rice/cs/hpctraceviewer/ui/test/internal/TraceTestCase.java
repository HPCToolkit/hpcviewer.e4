// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.test.internal;

import static org.mockito.Mockito.when;

import java.io.IOException;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;

import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;
import edu.rice.cs.hpcdata.db.version4.FileDB4;
import edu.rice.cs.hpcdata.db.version4.MetricValueCollection4;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpclocal.SpaceTimeDataControllerLocal;
import edu.rice.cs.hpctest.util.ViewerTestCase;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.timeline.ProcessTimeline;
import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;
import edu.rice.cs.hpctraceviewer.ui.context.BaseTraceContext;

public class TraceTestCase extends ViewerTestCase 
{
	private ITracePart tracePart;
	private IOperationHistory opHistory; 
	
	@Before
	@Override
	public void setUp() {
		super.setUp();
		
		tracePart = Mockito.mock(ITracePart.class);
		opHistory = Mockito.mock(IOperationHistory.class);
		
		when(tracePart.getOperationHistory()).thenReturn(opHistory);
		
		for(var context: BaseTraceContext.CONTEXTS) {
			when(tracePart.getContext(context))
			  .thenReturn(new BaseTraceContext(context));
		}
	}
	
	
	@After
	public void tearDown() {
		tracePart.dispose();
		
		super.tearDown();
	}

	
	protected ITracePart getTracePart() {
		return tracePart;
	}
	
	
	protected IOperationHistory getOperationHistory() {
		return opHistory;
	}
	
	
	/***
	 * Ugly simulation to create a space time data controller
	 * 
	 * @param exp
	 * @param numPixesY
	 * @return
	 * @throws IOException
	 */
	protected SpaceTimeDataController createSTDC(Experiment exp, int numPixesY, int numPixelsX) throws IOException {
		// simulating opening trace view
		var root = exp.getRootScope(RootScopeType.CallingContextTree);
		MetricValueCollection4 mvc = (MetricValueCollection4) root.getMetricValueCollection();
		var fileDB = new FileDB4(exp, mvc.getDataSummary());
				
		SpaceTimeDataController stdc = new SpaceTimeDataControllerLocal(exp, fileDB);

		var rankData = stdc.getBaseData();
		var idtuples = rankData.getListOfIdTuples(IdTupleOption.BRIEF);

		var numTraces = idtuples.size();
		stdc.resetTracelines(numTraces);
		
		for(int i=0; i<numTraces; i++) {
			stdc.setTraceline(i, new ProcessTimeline(i, stdc, idtuples.get(i)));
		}
		// hack to initialize home action
		var attributes = stdc.getTraceDisplayAttribute();
		
		attributes.setPixelVertical(numPixesY);
		attributes.setPixelHorizontal(numPixelsX);
		
		attributes.setTime(0, stdc.getMaxEndTime()-stdc.getMinBegTime());
		
		return stdc;
	}

}
