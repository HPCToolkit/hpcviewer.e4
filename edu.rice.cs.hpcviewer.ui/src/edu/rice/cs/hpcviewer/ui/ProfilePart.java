 
package edu.rice.cs.hpcviewer.ui;

import javax.inject.Inject;

import java.util.List;

import javax.annotation.PostConstruct;
import org.eclipse.swt.widgets.Composite;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;

import edu.rice.cs.hpcbase.ViewerDataEvent;
import edu.rice.cs.hpcdata.experiment.BaseExperiment;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcfilter.service.FilterMap;
import edu.rice.cs.hpcmetric.MetricFilterInput;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.base.IProfilePart;
import edu.rice.cs.hpcviewer.ui.graph.GraphEditorInput;
import edu.rice.cs.hpcviewer.ui.graph.GraphHistoViewer;
import edu.rice.cs.hpcviewer.ui.graph.GraphPlotRegularViewer;
import edu.rice.cs.hpcviewer.ui.graph.GraphPlotSortViewer;
import edu.rice.cs.hpcviewer.ui.internal.AbstractTableView;
import edu.rice.cs.hpcviewer.ui.internal.AbstractUpperPart;
import edu.rice.cs.hpcviewer.ui.internal.AbstractView;
import edu.rice.cs.hpcviewer.ui.metric.MetricView;
import edu.rice.cs.hpcviewer.ui.parts.bottomup.BottomUpPart;
import edu.rice.cs.hpcviewer.ui.parts.datacentric.Datacentric;
import edu.rice.cs.hpcviewer.ui.parts.editor.Editor;
import edu.rice.cs.hpcviewer.ui.parts.flat.FlatPart;
import edu.rice.cs.hpcviewer.ui.parts.thread.ThreadPart;
import edu.rice.cs.hpcviewer.ui.parts.thread.ThreadViewInput;
import edu.rice.cs.hpcviewer.ui.parts.topdown.TopDownPart;


public class ProfilePart implements IProfilePart, EventHandler
{
	public static final String ID = "edu.rice.cs.hpcviewer.ui.partdescriptor.profile";
	private static final String PREFIX_TITLE = "Profile: ";
	
	@Inject	protected EPartService  partService;
	@Inject protected EModelService modelService;
	@Inject protected MApplication  app;
	@Inject protected IEventBroker  eventBroker;
	@Inject protected UISynchronize sync;

	
	protected EMenuService menuService;
	
	@Inject protected DatabaseCollection databaseAddOn;


	/** Each view needs to store the experiment database.
	 * In case it needs to populate the table, we know which database 
	 * to be loaded. */
	private Experiment  experiment;
	
	private List<AbstractView> views;
	private MetricView metricView;
	
	private CTabFolder tabFolderTop;
	private CTabFolder tabFolderBottom;

	
	
	@PostConstruct
	public void postConstruct(Composite parent, EMenuService menuService) {
		
		this.menuService = menuService;
		
		parent.setLayout(new FillLayout(SWT.HORIZONTAL));

		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);		
		tabFolderTop      = new CTabFolder(sashForm, SWT.BORDER);
		tabFolderBottom   = new CTabFolder(sashForm, SWT.BORDER);
		
		sashForm.setWeights(1000, 1700);

		tabFolderBottom.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (AbstractView view: views) {
					if (e.item == view) {
						// activate the view if necessary
						// this includes creating the tree and populate the metrics
						// (for bottom-up and flat views)
						
						sync.asyncExec(()->{
							view.activate();
							refreshMetricView(view);
						});
					}
				}
			}
		});
		tabFolderBottom.setFocus();
	}
	
	
	private void refreshMetricView(AbstractView view) {
		
		// if the metric part is active, we need to inform it that
		// a new view is activated. The metric part has to refresh
		// its content to synchronize with the table in the active view
		
		if (metricView != null && !metricView.isDisposed()) {			
			final MetricFilterInput input  = new MetricFilterInput(view);								
			metricView.setInput(input);
		}
	}
	
	/***
	 * Display an editor in the top folder
	 * 
	 * @param input cannot be null
	 */
	public CTabItem addEditor(Object input) {
		// search if the input is already displayed 
		// if this is the case, we reuse the existing item
		
		CTabItem []items =  tabFolderTop.getItems();
		for (int i=0; i<items.length; i++) {
			CTabItem item = items[i];
			if (item instanceof AbstractUpperPart) {
				AbstractUpperPart editor = (AbstractUpperPart) item;
				
				if (editor.hasEqualInput(input)) {
					editor.setInput(input);
					tabFolderTop.setSelection(editor);
					
					return editor;
				}
			}
		}
		// the input is not yet displayed
		// create a new item for this input
		AbstractUpperPart viewer = null;
		
		if (input instanceof GraphEditorInput) {
			GraphEditorInput graphInput = (GraphEditorInput) input;
			if (graphInput.getGraphType() == GraphPlotRegularViewer.LABEL) {
				viewer = new GraphPlotRegularViewer(tabFolderTop, SWT.NONE);
				
			} else if (graphInput.getGraphType() == GraphPlotSortViewer.LABEL) {
				viewer = new GraphPlotSortViewer(tabFolderTop, SWT.NONE);
			
			} else if (graphInput.getGraphType() == GraphHistoViewer.LABEL) {
				viewer = new GraphHistoViewer(tabFolderTop, SWT.NONE);
			}			
		
		} else if (input instanceof MetricFilterInput) {
			viewer = new MetricView(tabFolderTop, SWT.NONE, eventBroker);
						
			metricView =  (MetricView) viewer;
			metricView.addDisposeListener(event -> metricView = null);
			
			
		} else {
			viewer = new Editor(tabFolderTop, SWT.NONE);
		}
		
		if (viewer != null) {
			viewer.setInput(input);
			
			// need to select the input to refresh the viewer
			// otherwise it will display empty item
			
			tabFolderTop.setSelection(viewer);
		}
		return viewer;
	}
	
	
	public Editor getActiveEditor() {
		if (tabFolderTop == null) return null;
		
		CTabItem item = tabFolderTop.getSelection();
		if (item instanceof Editor) {
			return (Editor) item;
		}
		return null;
	}
	
	
	/****
	 * Retrieve the current "selected" view.
	 * The definition of "selected" view depends on the {@code CTabFolder}.
	 * It can be the active one or the visible one.
	 * 
	 * @return {@code AbstractBaseViewItem} the current active item, or null
	 */
	public AbstractTableView getActiveView() {
		return (AbstractTableView) tabFolderBottom.getSelection();
	}

	
	/*****
	 * Specifically adding a new thread view
	 * 
	 * @param input
	 */
	public void addThreadView(ThreadViewInput input) {
		CTabItem []items = tabFolderBottom.getItems();
		for(CTabItem item: items) {
			if (item instanceof ThreadPart) {
				ThreadPart tv = (ThreadPart) item;
				ThreadViewInput tvinput = (ThreadViewInput) tv.getInput();
				
				if (input.getThreads().equals(tvinput.getThreads())) {
					tabFolderBottom.setSelection(tv);
					return;
				}
			}
		}
		ThreadPart threadView = new ThreadPart(tabFolderBottom, SWT.NONE);
		addView(threadView, input, true);

		// the metric view has to be updated according to the current active view
		// since we have just created the thread view, the metric view should be updated too
		refreshMetricView(threadView);
		
		// make sure the new view is visible and get the focus
		tabFolderBottom.setSelection(threadView);
	}

	
	/****
	 * Add a view tab item (at the lower folder) to the profile part
	 * 
	 * @param view the view item
	 * @param input the view's input
	 * @param sync boolean whether the display has to be synchronous or not
	 */
	public void addView(AbstractView view, Object input, boolean sync) {
		
		// The content builder will need many services. So we have to make they are initialized
		view.setService(partService, eventBroker, databaseAddOn, this, menuService);

		Composite composite = new Composite(tabFolderBottom, SWT.NONE);
		view.setControl(composite);
		composite.setLayout(new GridLayout(1, false));
		views.add(view);
		
		if (sync) {
			RunViewCreation createView = new RunViewCreation(view, composite, input);
			BusyIndicator.showWhile(composite.getDisplay(), createView);
		} else {
			// background task
			this.sync.asyncExec(()-> {
				view.createContent(composite);
				view.setInput(input);
			});
		}
	}
		
		
	@PreDestroy
	public void preDestroy() {
		eventBroker.unsubscribe(this);
		
		if (experiment != null)
			experiment.dispose();

		if (metricView != null)
			metricView.dispose();

		views.clear();
		
		experiment = null;
	}
	
	
	@Focus
	public void onFocus() {
		// force the focus to the table tab
		// On Mac and Linux, we need to use asyncExec to delay the focus since
		// the UI thread may not be ready when the focus arrives.
		sync.asyncExec(() -> {
			if (!tabFolderBottom.isDisposed()) {
				// setting the focus here will cause flickering when splitting the window
				// if the part activation is done synchronously
				// if it's done asynchronously, we're fine.
				tabFolderBottom.setFocus();
				int index = tabFolderBottom.getSelectionIndex();
				tabFolderBottom.setSelection(index);
			}
		});
	}

	@Override
	public BaseExperiment getExperiment() {
		return experiment;
	}

	@Override
	public void setInput(MPart part, Object input) {
		if (input == null ) return;
		if (!(input instanceof Experiment)) return;
		
		this.experiment = (Experiment) input;
		
		part.setLabel(PREFIX_TITLE + experiment.getName());
		part.setTooltip(experiment.getDefaultDirectory().getAbsolutePath());
		
		var roots = experiment.getRootScopeChildren();
		views = FastList.newList(roots.size());		
		boolean active = true;
		
		for(var rootNode: roots) {
			RootScope root = (RootScope) rootNode;
			AbstractView view;
			
			if (root.getType() == RootScopeType.CallingContextTree) {
				view = new TopDownPart(tabFolderBottom, SWT.NONE);
				
			} else if (root.getType() == RootScopeType.CallerTree) {
				view = new BottomUpPart(tabFolderBottom, SWT.NONE);
				
			} else if (root.getType() == RootScopeType.Flat) {
				view = new FlatPart(tabFolderBottom, SWT.NONE);
			
			} else if (root.getType() == RootScopeType.DatacentricTree) {				
				view = new Datacentric(tabFolderBottom, SWT.NONE);
			} else {
				root.getType();
				LoggerFactory.getLogger(getClass()).error("Not supported root: {}", root.getType());
				break;
			}
			addView(view, input, active);
			active = false; // only the first view will be activated first
		}
		// subscribe to filter events
		eventBroker.subscribe(FilterMap.FILTER_REFRESH_PROVIDER, this);
	}
	

	@Override
	public void handleEvent(Event event) {
		if (event.getTopic().equals(FilterMap.FILTER_REFRESH_PROVIDER)) {
			
			Object obj = event.getProperty(IEventBroker.DATA);
			ViewerDataEvent data = new ViewerDataEvent(experiment, obj);

			// announce to all views that a filtering process is on the way
			// if needed, each view can preserve their current states			
			eventBroker.send(ViewerDataEvent.TOPIC_FILTER_PRE_PROCESSING, data);
			
			// filter the current database
			// warning: the filtering is not scalable. We should do this in the 
			//          background job
			try {
				experiment.filter(FilterMap.getInstance(), true);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
			
			// announce to all views to refresh the content.
			// this may take time, and should be done asynchronously
			// with a background task
			eventBroker.post(ViewerDataEvent.TOPIC_FILTER_POST_PROCESSING, data);
		}
	}
	
	/**********************************************
	 * 
	 * Thread to render a view using background task
	 *
	 **********************************************/
	private static class RunViewCreation implements Runnable 
	{
		private final AbstractView view;
		private final Composite parent;
		private final Object input;
		
		RunViewCreation(AbstractView view, Composite parent, Object input) {
			this.view   = view;
			this.parent = parent;
			this.input  = input;
			
		}
		
		@Override
		public void run() {
			view.createContent(parent);
			view.setInput(input);
			view.activate();
		}
	}

	@Override
	public void showErrorMessage(String str) {
		var view = getActiveView();
		if (view != null)
			view.showErrorMessage(str);
	}

	@Override
	public void showInfo(String message) {
		var view = getActiveView();
		if (view != null)
			view.showInfo(message);
	}

	@Override
	public void showWarning(String message) {
		var view = getActiveView();
		if (view != null)
			view.showWarning(message);
	}
}