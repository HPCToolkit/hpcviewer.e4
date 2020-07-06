 
package edu.rice.cs.hpcviewer.ui.parts;

import javax.inject.Inject;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.swt.widgets.Composite;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpc.filter.service.FilterStateProvider;
import edu.rice.cs.hpcviewer.ui.experiment.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.internal.ScopeTreeViewer;
import edu.rice.cs.hpcviewer.ui.internal.ViewerDataEvent;
import edu.rice.cs.hpcviewer.ui.parts.editor.PartFactory;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.IPartListener;

public abstract class BaseViewPart implements IViewPart, EventHandler, IPartListener
{

	@Inject	protected EPartService  partService;
	@Inject protected EModelService modelService;
	@Inject protected MApplication  app;
	@Inject protected IEventBroker  eventBroker;
	
	@Inject protected DatabaseCollection databaseAddOn;

	@Inject protected PartFactory partFactory;

	private IViewBuilder  contentViewer;
	
	/** Each view needs to store the experiment database.
	 * In case it needs to populate the table, we know which database 
	 * to be loaded. */
	private BaseExperiment  experiment;
	
	/** This variable is a flag whether a table is already populated or not.
	 * If the root is null, it isn't populated
	 */
	private RootScope       root;

	@Inject
	public BaseViewPart() {
	}
	
	@PostConstruct
	public void postConstruct(Composite parent, EMenuService menuService) {

		contentViewer = setContentViewer(parent, menuService);
		
		// listen to part events: visible, activate, hide, ...
		partService.addPartListener(this);		
		
		// subscribe to user action events
		eventBroker.subscribe(ViewerDataEvent.TOPIC_HPC_REMOVE_DATABASE, this);
		eventBroker.subscribe(ViewerDataEvent.TOPIC_HIDE_SHOW_COLUMN,    this);
		eventBroker.subscribe(ViewerDataEvent.TOPIC_HPC_ADD_NEW_METRIC,  this);
		eventBroker.subscribe(ViewerDataEvent.TOPIC_HPC_METRIC_UPDATE,   this);
		
		// subscribe to filter events
		eventBroker.subscribe(FilterStateProvider.FILTER_REFRESH_PROVIDER, this);
	}
	
	@PreDestroy
	public void preDestroy() {
		
		partService.removePartListener(this);
		eventBroker.unsubscribe(this);
		
		if (contentViewer != null)
			contentViewer.dispose();
	}
	

	@Override
	public void setInput(MPart part, Object input) {
		
		if (!(input instanceof BaseExperiment))
			return;
					
		// important: needs to store the experiment database for further usage
		// when the view is becoming visible
		this.experiment = (BaseExperiment) input;
		
		if (partService.isPartVisible(part)) {
			
			// TODO: this process takes time
			root = createRoot(experiment);
			contentViewer.setData(root);
		}
	}

	@Override
	public BaseExperiment getExperiment() {
		return experiment;
	}
	
	@Override
	public void handleEvent(Event event) {
		ScopeTreeViewer treeViewer = contentViewer.getTreeViewer();
		if (treeViewer.getTree().isDisposed())
			return;

		Object obj = event.getProperty(IEventBroker.DATA);
		if (obj == null)
			return;
		
		if (!(obj instanceof ViewerDataEvent)) {

			if (event.getTopic().equals(FilterStateProvider.FILTER_REFRESH_PROVIDER)) {
				FilterStateProvider.filterExperiment((Experiment) experiment);
				
				// TODO: this process takes time
				root = createRoot(experiment);
				contentViewer.setData(root);
			}
			return;
		}
		
		ViewerDataEvent eventInfo = (ViewerDataEvent) obj;
		if (getExperiment() != eventInfo.experiment) 
			return;
		
		String topic = event.getTopic();
		if (topic.equals(ViewerDataEvent.TOPIC_HIDE_SHOW_COLUMN)) {
			treeViewer.setColumnsStatus((boolean[]) eventInfo.data);
			
		} else if (topic.equals(ViewerDataEvent.TOPIC_HPC_ADD_NEW_METRIC)) {
			treeViewer.addUserMetricColumn((BaseMetric) eventInfo.data);

		} else if (topic.equals(ViewerDataEvent.TOPIC_HPC_REMOVE_DATABASE)) {
			// mark that this part will be destroyed
			experiment = null;

		} else if (topic.equals(ViewerDataEvent.TOPIC_HPC_METRIC_UPDATE)) {
			treeViewer.refreshColumnTitle();
		}
	}

	
	@Override
	public void partActivated(MPart part) {}

	@Override
	public void partBroughtToTop(MPart part) {}

	@Override
	public void partDeactivated(MPart part) {}

	@Override
	public void partHidden(MPart part) {}

	@Override
	public void partVisible(MPart part) {
		if (part.getObject() != this)
			return;
		
		if (experiment != null && root == null) {
			
			// if the database doesn't exist anymore, it means we are
			// exiting...
			
			if (!databaseAddOn.IsExist(experiment))
				return;
			
			// TODO: this process takes time
			root = createRoot(experiment);
			contentViewer.setData(root);
		}
	}

	protected IViewBuilder getContentViewer() {
		return contentViewer;
	}

	protected abstract RootScope      createRoot(BaseExperiment experiment);
	protected abstract IViewBuilder   setContentViewer(Composite parent, EMenuService menuService);
	protected abstract RootScopeType  getRootType();
	
}