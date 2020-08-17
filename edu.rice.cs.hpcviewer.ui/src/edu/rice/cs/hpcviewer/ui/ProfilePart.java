 
package edu.rice.cs.hpcviewer.ui;

import javax.inject.Inject;
import javax.inject.Named;

import javax.annotation.PostConstruct;
import org.eclipse.swt.widgets.Composite;
import javax.annotation.PreDestroy;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.services.IServiceConstants;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.base.IProfilePart;
import edu.rice.cs.hpcviewer.ui.base.IUpperPart;
import edu.rice.cs.hpcviewer.ui.graph.AbstractGraphViewer;
import edu.rice.cs.hpcviewer.ui.graph.GraphEditorInput;
import edu.rice.cs.hpcviewer.ui.graph.GraphHistoViewer;
import edu.rice.cs.hpcviewer.ui.graph.GraphPlotRegularViewer;
import edu.rice.cs.hpcviewer.ui.graph.GraphPlotSortViewer;
import edu.rice.cs.hpcviewer.ui.parts.bottomup.BottomUpView;
import edu.rice.cs.hpcviewer.ui.parts.datacentric.Datacentric;
import edu.rice.cs.hpcviewer.ui.parts.editor.Editor;
import edu.rice.cs.hpcviewer.ui.parts.flat.FlatView;
import edu.rice.cs.hpcviewer.ui.parts.thread.ThreadView;
import edu.rice.cs.hpcviewer.ui.parts.topdown.TopDownView;
import edu.rice.cs.hpcviewer.ui.tabItems.AbstractBaseViewItem;
import edu.rice.cs.hpcviewer.ui.tabItems.AbstractViewItem;



public class ProfilePart implements IProfilePart
{
	public static final String ID = "edu.rice.cs.hpcviewer.ui.partdescriptor.profile";
	
	@Inject	protected EPartService  partService;
	@Inject protected EModelService modelService;
	@Inject protected MApplication  app;
	@Inject protected IEventBroker  eventBroker;
	protected EMenuService menuService;
	
	@Inject protected DatabaseCollection databaseAddOn;


	/** Each view needs to store the experiment database.
	 * In case it needs to populate the table, we know which database 
	 * to be loaded. */
	private BaseExperiment  experiment;
	
	private AbstractViewItem []views;

	private CTabFolder tabFolderTop, tabFolderBottom;
	private Shell shell;
	
	@Inject
	public ProfilePart() {
	}
	
	@PostConstruct
	public void postConstruct(Composite parent, EMenuService menuService, @Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		
		this.menuService = menuService;
		this.shell = shell;
		
		parent.setLayout(new FillLayout(SWT.HORIZONTAL));

		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		
		tabFolderTop = new CTabFolder(sashForm, SWT.BORDER);
		
		tabFolderBottom = new CTabFolder(sashForm, SWT.BORDER);
		
		sashForm.setWeights(new int[] {1000, 1000});

		tabFolderBottom.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (AbstractViewItem view: views) {
					if (e.item == view) {
						if (view.getInput() == null)
							view.setInput(experiment);
					}
				}
			}
		});
		tabFolderBottom.setFocus();
	}
	
	
	/***
	 * Display an editor in the top folder
	 * 
	 * @param input cannot be null
	 */
	public void addEditor(Object input) {
		// search if the input is already displayed 
		// if this is the case, we reuse the existing item
		
		CTabItem []items =  tabFolderTop.getItems();
		for (int i=0; i<items.length; i++) {
			CTabItem item = items[i];
			if (item instanceof IUpperPart) {
				IUpperPart editor = (IUpperPart) item;
				if (editor.hasEqualInput(input)) {
					editor.setInput(input);
					
					tabFolderTop.setSelection((CTabItem) editor);
					
					return;
				}
			}
		}
		// the input is not displayed
		// create a new item for this input
		CTabItem viewer = null;
		if (input instanceof GraphEditorInput) {
			GraphEditorInput graphInput = (GraphEditorInput) input;
			if (graphInput.getGraphType() == GraphPlotRegularViewer.LABEL) {
				viewer = new GraphPlotRegularViewer(tabFolderTop, SWT.NONE);
				
			} else if (graphInput.getGraphType() == GraphPlotSortViewer.LABEL) {
				viewer = new GraphPlotSortViewer(tabFolderTop, SWT.NONE);
			
			} else if (graphInput.getGraphType() == GraphHistoViewer.LABEL) {
				viewer = new GraphHistoViewer(tabFolderTop, SWT.NONE);
			}
			
			Composite parent = new Composite(tabFolderTop, SWT.NONE);
			((AbstractGraphViewer)viewer).postConstruct(parent);
			viewer.setControl(parent);
			((AbstractGraphViewer)viewer).setInput(graphInput);
		
		} else {
			
			viewer = new Editor(tabFolderTop, SWT.NONE);
			viewer.setText("code");
			((Editor) viewer).setService(eventBroker, partService.getActivePart());
			
			Composite parent = new Composite(tabFolderTop, SWT.NONE);
			viewer.setControl(parent);
			((Editor) viewer).postConstruct(parent);
			((Editor) viewer).setInput(input);
		}
		
		// need to select the input to refresh the viewer
		// otherwise it will display empty item
		
		tabFolderTop.setSelection(viewer);
	}
	
	
	public Editor getActiveEditor() {
		if (tabFolderTop == null) return null;
		
		CTabItem item = tabFolderTop.getSelection();
		if (item instanceof Editor) {
			return (Editor) item;
		}
		return null;
	}
	
	
	/*****
	 * Specifically adding a new thread view
	 * 
	 * @param input
	 */
	public void addThreadView(Object input) {
		ThreadView threadView = new ThreadView(tabFolderBottom, SWT.NONE);
		addView(threadView, input, true);
	}
	
	/****
	 * Add a view tab item (at the lower folder) to the profile part
	 * 
	 * @param view the view item
	 * @param input the view's input
	 * @param sync boolean whether the display has to be synchronous or not
	 */
	public void addView(AbstractBaseViewItem view, Object input, boolean sync) {
		
		// TODO: make sure this statement is called early.
		// The content builder will need many services. So we have to make they are initialized
		view.setService(partService, eventBroker, databaseAddOn, this, menuService);

		Composite composite = new Composite(tabFolderBottom, SWT.NONE);
		view.setControl(composite);
		composite.setLayout(new GridLayout(1, false));

		if (sync) {

			RunViewCreation createView = new RunViewCreation(view, composite, input);
			Display display = shell.getDisplay();
			BusyIndicator.showWhile(display, createView);
			
			tabFolderBottom.setSelection(view);
			tabFolderBottom.setFocus();
			
		} else {
			// background renderer
			view.createContent(composite);
		}
	}
	
	
		
	@PreDestroy
	public void preDestroy() {
	}
	
	
	@Focus
	public void onFocus() {
	}

	@Override
	public BaseExperiment getExperiment() {
		return experiment;
	}

	@Override
	public void setInput(MPart part, Object input) {
		if (input == null ) return;
		if (!(input instanceof Experiment)) return;
		
		Experiment experiment = (Experiment) input;
		this.experiment = experiment;
		
		part.setLabel("Profile: " + experiment.getName());
		//part.setElementId(ElementIdManager.getElementId(experiment));
		part.setTooltip(experiment.getDefaultDirectory().getAbsolutePath());
		
		Object []roots = experiment.getRootScopeChildren();
		views = new AbstractViewItem[roots.length];		
		
		for(int numViews=0; numViews<roots.length; numViews++) {
			RootScope root = (RootScope) roots[numViews];
			
			if (root.getType() == RootScopeType.CallingContextTree) {
				views[numViews] = new TopDownView(tabFolderBottom, SWT.NONE);
				
			} else if (root.getType() == RootScopeType.CallerTree) {
				views[numViews] = new BottomUpView(tabFolderBottom, SWT.NONE);
				
			} else if (root.getType() == RootScopeType.Flat) {
				
				views[numViews] = new FlatView(tabFolderBottom, SWT.NONE);
			
			} else if (root.getType() == RootScopeType.DatacentricTree) {
				
				views[numViews] = new Datacentric(tabFolderBottom, SWT.NONE);
			} else {
				System.err.println("Not supported root: " + root.getType());
				break;
			}
			addView(views[numViews], input, numViews==0);
		}
	}
	
	
	
	/**********
	 * 
	 * Thread to render a view using background task
	 *
	 */
	static private class RunViewCreation implements Runnable 
	{
		final private AbstractBaseViewItem view;
		final private Composite parent;
		final private Object input;
		
		RunViewCreation(AbstractBaseViewItem view, Composite parent, Object input) {
			this.view   = view;
			this.parent = parent;
			this.input  = input;
			
		}
		
		@Override
		public void run() {
			view.createContent(parent);
			view.setInput(input);
		}
	}
}