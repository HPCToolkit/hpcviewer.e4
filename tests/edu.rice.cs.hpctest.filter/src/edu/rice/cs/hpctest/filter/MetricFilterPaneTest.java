package edu.rice.cs.hpctest.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.BeforeClass;
import org.junit.Test;

import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcbase.BaseConstants.ViewType;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcfilter.FilterDataItem;
import edu.rice.cs.hpcmetric.IFilterable;
import edu.rice.cs.hpcmetric.MetricFilterInput;
import edu.rice.cs.hpcmetric.MetricFilterPane;
import edu.rice.cs.hpcmetric.internal.MetricFilterDataItem;
import edu.rice.cs.hpctest.util.TestDatabase;

public class MetricFilterPaneTest 
{
    
    private static Shell shell;

    @BeforeClass
    public static void setup() {
        Display display = Display.getDefault();
        shell = new Shell(display);
        shell.setText("Sample Shell for test");
        
        shell.setSize(200, 200);
        shell.open();
    }
    
	@Test
	public void testGetFilterLabel() throws Exception {
		IEventBroker eventBroker = Mockito.mock(IEventBroker.class);
		var experiments = TestDatabase.getExperiments();
		
		for(var exp: experiments) {
			var listMetrics = exp.getMetricList();
			List<FilterDataItem<BaseMetric>> listData = new ArrayList<>(listMetrics.size());
			for(var metric: listMetrics) {
				listData.add(new MetricFilterDataItem(metric, false, true));
			}
			
			MetricFilterInput input = new MetricFilterInput(new IFilterable() {

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
			MetricFilterPane metricPane = new MetricFilterPane(shell, SWT.BORDER, eventBroker, input);
			var eventList = metricPane.getEventList();
			
			assertNotNull(eventList);
			assertEquals(listMetrics.size(), eventList.size());
		}
	}

}
