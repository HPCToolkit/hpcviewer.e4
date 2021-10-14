 
package edu.rice.cs.hpcviewer.ui;

import javax.inject.Inject;

import java.util.List;

import javax.annotation.PostConstruct;
import org.eclipse.swt.widgets.Composite;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

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
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcfilter.service.FilterStateProvider;
import edu.rice.cs.hpcmetric.MetricFilterInput;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.base.IProfilePart;
import edu.rice.cs.hpcviewer.ui.graph.GraphEditorInput;
import edu.rice.cs.hpcviewer.ui.graph.GraphHistoViewer;
import edu.rice.cs.hpcviewer.ui.graph.GraphPlotRegularViewer;
import edu.rice.cs.hpcviewer.ui.graph.GraphPlotSortViewer;
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
	
	private CTabFolder tabFolderTop, tabFolderBottom;

	
	@Inject
	public ProfilePart() {
	}
	
	@PostConstruct
	public void postConstruct(Composite parent, EMenuService menuService) {
		
		this.menuService = menuService;
		
		parent.setLayout(new FillLayout(SWT.HORIZONTAL));

		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);		
		tabFolderTop      = new CTabFolder(sashForm, SWT.BORDER);
		tabFolderBottom   = new CTabFolder(sashForm, SWT.BORDER);
		
		sashForm.setWeights(new int[] {1000, 1700});

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
			RootScope root = experiment.getRootScope(RootScopeType.CallingContextTree);
			Object o = view.getInput();
			IMetricManager metricMgr;
			if (o instanceof IMetricManager) {
				metricMgr = (IMetricManager) o;
			} else if (view instanceof ThreadPart) {
				metricMgr = ((ThreadPart)view).getMetricManager();
			} else {
				throw new RuntimeException("Unknown view: " + view.getText());
			}
			boolean affectAll = view.getViewType() == AbstractView.ViewType.COLLECTIVE;
			MetricFilterInput input  = new MetricFilterInput(root, 
															 metricMgr, 
															 view.getFilterDataItems(), 
															 affectAll);								
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
			
			// if the metric view is created for the traditional views (top-down, bottom-up, flat)
			// we should store the instance. This will be needed because each 3 views only has 1 metric view.
			// However, each thread view has its own metric view. Hence no need to store the instance.
			// to know if a view is thread view or not, we can check from isAffectAll() property.
			
			if ( ((MetricFilterInput)input).isAffectAll() ) {
				// the metric view is generated for the 3 traditional views.
				// we need to store the instance
				
				metricView =  (MetricView) viewer;
				metricView.addDisposeListener((event) -> {
					metricView = null;
				});
			} else {
				// for metric properties from thread view, we need to show as well the title of the thread view
				// this is important to distinguish with other metric properties
				
				final String titleView = tabFolderBottom.getSelection().getText();
				viewer.setText(MetricView.TITLE_DEFAULT + ": " + titleView);
			}
			
		} else {
			viewer = new Editor(tabFolderTop, SWT.NONE);
		}
		viewer.setInput(input);
		
		// need to select the input to refresh the viewer
		// otherwise it will display empty item
		
		tabFolderTop.setSelection(viewer);
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
	public AbstractView getActiveView() {
		return (AbstractView) tabFolderBottom.getSelection();
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
		
		// TODO: make sure this statement is called early.
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
		
		Object []roots = experiment.getRootScopeChildren();
		views = FastList.newList(roots.length);		
		
		for(int numViews=0; numViews<roots.length; numViews++) {
			RootScope root = (RootScope) roots[numViews];
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
				System.err.println("Not supported root: " + root.getType());
				break;
			}
			addView(view, input, numViews==0);
		}
		// subscribe to filter events
		eventBroker.subscribe(FilterStateProvider.FILTER_REFRESH_PROVIDER, this);
	}
	

	@Override
	public void handleEvent(Event event) {
		if (event.getTopic().equals(FilterStateProvider.FILTER_REFRESH_PROVIDER)) {
			// filter the current database
			// warning: the filtering is not scalable. We should do this in the 
			//          background job
			FilterStateProvider.filterExperiment((Experiment) experiment);
			
			// announce to all views to refresh the content.
			// this may take time, and should be done asynchronously
			// with a background task
			Object obj = event.getProperty(IEventBroker.DATA);
			ViewerDataEvent data = new ViewerDataEvent((Experiment) experiment, obj);
			eventBroker.post(ViewerDataEvent.TOPIC_HPC_DATABASE_REFRESH, data);
		}
	}
	
	/**********************************************
	 * 
	 * Thread to render a view using background task
	 *
	 **********************************************/
	static private class RunViewCreation implements Runnable 
	{
		final private AbstractView view;
		final private Composite parent;
		final private Object input;
		
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
}