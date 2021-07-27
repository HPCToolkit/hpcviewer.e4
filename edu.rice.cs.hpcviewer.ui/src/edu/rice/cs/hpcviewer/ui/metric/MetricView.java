package edu.rice.cs.hpcviewer.ui.metric;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import edu.rice.cs.hpcbase.ViewerDataEvent;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcmetric.MetricFilterInput;
import edu.rice.cs.hpcmetric.MetricFilterPane;
import edu.rice.cs.hpcviewer.ui.internal.AbstractUpperPart;


/***************************************************************
 * 
 * Independent view to display the metric properties:
 * 
 * <ul>
 *  <li> visible or not
 *  <li> name of the metric
 *  <li> its long description
 *  <li> its aggregate value. It's important to show if the metric is empty or not
 * </ul>
 * The caller has to listen the event of {@code ViewerDataEvent.TOPIC_HIDE_SHOW_COLUMN}
 * which contain data which metric (or column) to be shown or hidden
 ***************************************************************/
public class MetricView extends AbstractUpperPart implements EventHandler, DisposeListener
{
	public  static final String TITLE_DEFAULT = "Metric properties";
	private final IEventBroker eventBroker ;
	private final CTabFolder parent;
	
	private MetricFilterInput inputFilter;
	public MetricView(CTabFolder parent, int style, IEventBroker eventBroker ) {
		super(parent, style);
		this.parent = parent;
		this.eventBroker = eventBroker;
		
		setShowClose(true);
		setText(TITLE_DEFAULT);
		
		eventBroker.subscribe(ViewerDataEvent.TOPIC_HPC_ADD_NEW_METRIC, this);
		addDisposeListener(this);
	}
	
	
	@Override
	public String getTitle() {
		return TITLE_DEFAULT;
	}

	@Override
	public void setInput(Object input) {
		if (input == null || !(input instanceof MetricFilterInput))
			return;
		inputFilter = (MetricFilterInput) input;

		Composite container = new Composite(parent, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(container);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(container);
		
		MetricFilterPane pane = new MetricFilterPane(container, getStyle(), eventBroker, inputFilter);
		
		if (pane != null)
			setControl(container);
	}
	
	
	@Override
	public boolean hasEqualInput(Object input) {
		if (input instanceof MetricFilterInput) {
			MetricFilterInput metricInput = (MetricFilterInput) input;
			return metricInput.getMetricManager() == this.inputFilter.getMetricManager();
		}
		return false;
	}

	
	
	@Override
	public void setMarker(int lineNumber) {}


	@Override
	public void handleEvent(Event event) {
		Object obj = event.getProperty(IEventBroker.DATA);
		if (obj == null )
			return;
		
		if (!(obj instanceof ViewerDataEvent)) 
			return;
		
		IMetricManager metricManager = inputFilter.getMetricManager();
		ViewerDataEvent eventInfo = (ViewerDataEvent) obj;
		
		if (metricManager != eventInfo.experiment) 
			return;
		
		setInput(inputFilter);
	}


	@Override
	public void widgetDisposed(DisposeEvent e) {
		eventBroker.unsubscribe(this);
		removeDisposeListener(this);
	}
}
