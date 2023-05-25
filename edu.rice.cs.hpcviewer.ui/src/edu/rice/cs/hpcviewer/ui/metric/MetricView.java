package edu.rice.cs.hpcviewer.ui.metric;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpcbase.ui.AbstractUpperPart;
import edu.rice.cs.hpcmetric.MetricFilterInput;
import edu.rice.cs.hpcmetric.MetricFilterPane;



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
public class MetricView extends AbstractUpperPart
{
	public  static final String TITLE_DEFAULT = "Metric properties";
	
	private final IEventBroker eventBroker ;
	
	private MetricFilterPane  pane;	
	private MetricFilterInput inputFilter;
	
	/*****
	 * Constructor of the class without creating any content. <br/> 
	 * Caller needs to call {@link setInput} to create the content.
	 * 
	 * @param parent
	 * @param style
	 * @param eventBroker
	 */
	public MetricView(CTabFolder parent, int style, IEventBroker eventBroker ) {
		super(parent, style);
		this.eventBroker = eventBroker;
		
		setShowClose(true);
		setText(TITLE_DEFAULT);
	}
	

	
	@Override
	public String getTitle() {
		return TITLE_DEFAULT;
	}

	@Override
	public void setInput(Object input) {
		if (!(input instanceof MetricFilterInput))
			return;
		
		inputFilter = (MetricFilterInput) input;
		
		// if the panel is already created, we just need to reset the panel
		// with a new input (list). No need to recreate it again.		
		if (pane != null) {
			pane.setInput(inputFilter);
			return;
		}		
		// the panel is not created yet
		// we will create it from the scratch
		
		Composite container = new Composite(getParent(), SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(container);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(container);
		
		pane = new MetricFilterPane(container, getStyle(), eventBroker, inputFilter);
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
	public void setMarker(int lineNumber) { /* not needed */ }



	@Override
	public void setFocus() {
		pane.setFocus();
	}
}
