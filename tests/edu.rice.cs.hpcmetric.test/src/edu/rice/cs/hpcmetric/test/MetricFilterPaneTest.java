// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcmetric.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import edu.rice.cs.hpcbase.BaseConstants.ViewType;
import org.hpctoolkit.db.local.experiment.Experiment;
import org.hpctoolkit.db.local.experiment.metric.BaseMetric;
import org.hpctoolkit.db.local.experiment.metric.IMetricManager;
import org.hpctoolkit.db.local.experiment.scope.RootScope;
import org.hpctoolkit.db.local.experiment.scope.RootScopeType;
import edu.rice.cs.hpcfilter.FilterDataItem;
import edu.rice.cs.hpcmetric.IFilterable;
import edu.rice.cs.hpcmetric.MetricFilterInput;
import edu.rice.cs.hpcmetric.MetricFilterPane;
import edu.rice.cs.hpcmetric.internal.MetricFilterDataItem;
import edu.rice.cs.hpctest.util.TestDatabase;
import edu.rice.cs.hpctest.util.ViewerTestCase;

public class MetricFilterPaneTest extends ViewerTestCase 
{
	private SWTBot bot;
	
	
	@Override
	@Before
	public void setUp() {
		super.setUp();
		
        bot = new SWTBot();
        shell.setText("test");
	}
	
	
	@Test
	public void testChangeInput() throws Exception {
		
		IEventBroker eventBroker = Mockito.mock(IEventBroker.class);
		var experiments = TestDatabase.getExperiments();
		
		shell.open();
		MetricFilterPane pane = null;
		
		for(var exp: experiments) {
			var input = createFilterInput(exp, eventBroker);
			assertNotNull(input);
			
			if (pane == null)
				pane = new MetricFilterPane(shell, 0, eventBroker, input);
			else
				pane.setInput(input);

			testPane(pane, exp);
			
			var numMetric = exp.getMetricCount();
			
			var input2 = createFilterInput(exp, eventBroker);
			
			BaseMetric metric = Mockito.mock(BaseMetric.class);
			when(metric.getDisplayName()).thenReturn("Just-a-name");
			
			input2.getListItems().add(new MetricFilterDataItem(metric, false, false));
			
			pane.setInput(input2);
			
			var eventList = pane.getEventList();
			int currentNumMetrics = numMetric + 1;
			assertEquals(currentNumMetrics, eventList.size());
		}
	}
	
	
	private MetricFilterPane testPane(MetricFilterPane pane, Experiment exp) {
		
		var eventList = pane.getEventList();
		assertNotNull(eventList);
		assertEquals(exp.getMetricCount(), eventList.size());
		
		var filterList = pane.getFilterList();
		assertNotNull(filterList);
		assertEquals(exp.getMetricCount(), filterList.size());
		
		var dialogShell = bot.shell("test");
		assertNotNull(dialogShell);

		var name = bot.text();
		name.setText("lj3*-/=980jl"); // filtering non-sense
		
		filterList = pane.getFilterList();
		assertEquals(0, filterList.size());
		
		return pane;
	}
	
	
	private MetricFilterInput createFilterInput(Experiment exp, IEventBroker eventBroker) {
		var listMetrics = exp.getMetrics();
		
		List<FilterDataItem<BaseMetric>> listData = new ArrayList<>(listMetrics.size());
		for(var metric: listMetrics) {
			listData.add(new MetricFilterDataItem(metric, false, true));
		}
		
		return new MetricFilterInput(new IFilterable() {

			@Override
			public List<FilterDataItem<BaseMetric>> getFilterDataItems() {
				return listData;
			}

			@Override
			public ViewType getViewType() {
				return ViewType.INDIVIDUAL;
			}

			@Override
			public IMetricManager getMetricManager() {
				return exp;
			}

			@Override
			public RootScope getRoot() {
				return exp.getRootScope(RootScopeType.CallingContextTree);
			}
			
		}, eventBroker);
	}
}
