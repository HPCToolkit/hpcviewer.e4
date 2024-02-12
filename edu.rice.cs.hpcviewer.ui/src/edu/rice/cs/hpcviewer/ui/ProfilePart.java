 
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
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;

import edu.rice.cs.hpcbase.IDatabase;
import edu.rice.cs.hpcbase.IEditorViewerInput;
import edu.rice.cs.hpcbase.ThreadViewInput;
import edu.rice.cs.hpcbase.ViewerDataEvent;
import edu.rice.cs.hpcbase.ui.AbstractUpperPart;
import edu.rice.cs.hpcbase.ui.IProfilePart;
import edu.rice.cs.hpcbase.ui.IUpperPart;
import edu.rice.cs.hpcdata.experiment.BaseExperiment;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcfilter.service.FilterMap;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.internal.AbstractTableView;
import edu.rice.cs.hpcviewer.ui.internal.AbstractView;
import edu.rice.cs.hpcviewer.ui.parts.bottomup.BottomUpPart;
import edu.rice.cs.hpcviewer.ui.parts.datacentric.Datacentric;
import edu.rice.cs.hpcviewer.ui.parts.flat.FlatPart;
import edu.rice.cs.hpcviewer.ui.parts.thread.ThreadPart;
import edu.rice.cs.hpcviewer.ui.parts.topdown.TopDownPart;


/*************
 * 
 * Main class to manage the profile view
 *
 *************/
public class ProfilePart implements IProfilePart, EventHandler
{
	public static final String ID = "edu.rice.cs.hpcviewer.ui.partdescriptor.profile";
	public static final String PREFIX_TITLE = "Profile: ";
	
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
	private IDatabase database;
	
	private List<AbstractView> views;
	private List<AbstractUpperPart> trackedViews;
	
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
							refreshUpperView(view);
						});
					}
				}
			}
		});
		tabFolderBottom.setFocus();
	}
	
	
	/***
	 * Notify the upper view (editors, metric view or graph)
	 * that we have activated another view.
	 * 
	 * @param view
	 * 			The activated view
	 */
	private void refreshUpperView(AbstractView view) {
		
		// if the metric part is active, we need to inform it that
		// a new view is activated. The metric part has to refresh
		// its content to synchronize with the table in the active view
		if (trackedViews == null || trackedViews.isEmpty())
			return;
		
		trackedViews.stream().forEach(v -> {
			if (!v.isDisposed()) {				
				v.refresh(view);
			}
		});
	}
	
	/***
	 * Display an editor in the top folder
	 * 
	 * @param input 
	 * 			The object input
	 * 
	 * @return {@code IUpperPart}
	 * 			The editor object if successful. Null otherwise.
	 */
	@Override
	public IUpperPart addEditor(IEditorViewerInput input) {
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
		AbstractUpperPart viewer = (AbstractUpperPart) input.createViewer(tabFolderTop);
		
		if (viewer != null) {
			try {
				viewer.setInput(input);
			} catch (Exception e) {
				var shell = tabFolderTop.getShell();
				MessageDialog.openError(shell, "Error", "Fail to open " + input.getLongName());
				viewer.dispose();
				return null;
			}
			
			// need to select the input to refresh the viewer
			// otherwise it will display empty item
			
			tabFolderTop.setSelection(viewer);
			
			if (input.needToTrackActivatedView()) {
				trackedViews.add(viewer);
			}
		}
		return viewer;
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
		refreshUpperView(threadView);
		
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
			RunViewCreation createView = new RunViewCreation(database, view, composite, input);
			BusyIndicator.showWhile(composite.getDisplay(), createView);
		} else {
			// background task
			this.sync.asyncExec(()-> {
				view.createContent(composite);
				view.setInput(database, input);
			});
		}
	}
		
		
	@PreDestroy
	public void preDestroy() {
		eventBroker.unsubscribe(this);
		
		dispose();
	}
	
	
	@Focus
	public void onFocus() {
		// force the focus to the table tab
		// On Mac and Linux, we need to use asyncExec to delay the focus since
		// the UI thread may not be ready when the focus arrives.
		sync.asyncExec(() -> {
			// fix issue #329: sometimes tabFolderBottom can be null if the database
			// has already been closed.
			if (tabFolderBottom != null && !tabFolderBottom.isDisposed()) {
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
		return (BaseExperiment) database.getExperimentObject();
	}

	@Override
	public void setInput(IDatabase database) {
		if (database == null ) return;
		
		this.database = database;
		
		var experiment = (Experiment) database.getExperimentObject();
		
		var roots = experiment.getRootScopeChildren();
		views = FastList.newList(roots.size());
		trackedViews = FastList.newList();
		
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
			addView(view, database.getExperimentObject(), active);
			active = false; // only the first view will be activated first
		}
		// subscribe to filter events
		eventBroker.subscribe(FilterMap.FILTER_REFRESH_PROVIDER, this);
	}
	

	@Override
	public void handleEvent(Event event) {
		if (event.getTopic().equals(FilterMap.FILTER_REFRESH_PROVIDER)) {
			
			Object obj = event.getProperty(IEventBroker.DATA);
			Experiment experiment = (Experiment) database.getExperimentObject();
			
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
		private final IDatabase database;
		private final AbstractView view;
		private final Composite parent;
		private final Object input;
		
		RunViewCreation(IDatabase database, AbstractView view, Composite parent, Object input) {
			this.view   = view;
			this.parent = parent;
			this.input  = input;
			this.database = database;
		}
		
		@Override
		public void run() {
			view.createContent(parent);
			view.setInput(database, input);
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


	@Override
	public void dispose() {
		if (tabFolderBottom != null)
			tabFolderBottom.dispose();
		
		if (tabFolderTop != null)
			tabFolderTop.dispose();
		
		if (views != null) {
			views.parallelStream().forEach(AbstractView::dispose);
		}

		if (trackedViews != null && !trackedViews.isEmpty()) {
			trackedViews.stream().forEach(v -> v.dispose());
			trackedViews.clear();
			trackedViews = null;
		}

		views = null;
		tabFolderTop = null;
		tabFolderBottom = null;
		
		database = null;
	}

	

	@Override
	public IDatabase getInput() {
		return database;
	}
}