 
package edu.rice.cs.hpctraceviewer.ui;

import javax.inject.Inject;
import javax.inject.Named;

import java.util.HashMap;
import javax.annotation.PostConstruct;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import edu.rice.cs.hpcbase.BaseConstants;
import edu.rice.cs.hpcbase.IDatabase;
import edu.rice.cs.hpctraceviewer.config.TracePreferenceConstants;
import edu.rice.cs.hpctraceviewer.config.TracePreferenceManager;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.ui.base.AbstractBaseItem;
import edu.rice.cs.hpctraceviewer.ui.base.IPixelAnalysis;
import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;
import edu.rice.cs.hpctraceviewer.ui.base.ITraceViewAction;
import edu.rice.cs.hpctraceviewer.ui.blamestat.CpuBlameAnalysis;
import edu.rice.cs.hpctraceviewer.ui.blamestat.HPCBlameView;
import edu.rice.cs.hpctraceviewer.ui.callstack.HPCCallStackView;
import edu.rice.cs.hpctraceviewer.ui.context.BaseTraceContext;
import edu.rice.cs.hpctraceviewer.ui.depth.HPCDepthView;
import edu.rice.cs.hpctraceviewer.ui.depthEditor.DepthEditor;
import edu.rice.cs.hpctraceviewer.ui.internal.TraceEventData;
import edu.rice.cs.hpctraceviewer.ui.main.HPCTraceView;
import edu.rice.cs.hpctraceviewer.ui.main.DebugView;

import edu.rice.cs.hpctraceviewer.ui.minimap.SpaceTimeMiniCanvas;
import edu.rice.cs.hpctraceviewer.ui.statistic.HPCStatisticView;
import edu.rice.cs.hpctraceviewer.ui.summary.HPCSummaryView;
import edu.rice.cs.hpctraceviewer.ui.util.IConstants;
import edu.rice.cs.hpctraceviewer.ui.util.Utility;
import edu.rice.cs.hpcbase.ViewerDataEvent;
import edu.rice.cs.hpcdata.experiment.BaseExperiment;
import edu.rice.cs.hpcdata.util.OSValidator;
import edu.rice.cs.hpcsetting.preferences.PreferenceConstants;
import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceManager;

import javax.annotation.PreDestroy;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.IPartListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;


/***************************************************************
 * 
 * The main class for trace viewer
 *
 ***************************************************************/
public class TracePart implements ITracePart, IPartListener, IPropertyChangeListener, EventHandler
{
	public static final String ID = "edu.rice.cs.hpctraceviewer.ui.partdescriptor.trace";
	
	private static final String LABEL_ZOOM_IN_Y  = "zoom.in";
	private static final String ICON_ZOOM_IN_Y   =  "platform:/plugin/edu.rice.cs.hpctraceviewer.ui/resources/zoom-in-process.png";
	private static final String LABEL_ZOOM_OUT_Y = "zoom.out";
	private static final String ICON_ZOOM_OUT_Y  =  "platform:/plugin/edu.rice.cs.hpctraceviewer.ui/resources/zoom-out-process.png";

	private final HashMap<String, IUndoContext> mapLabelToContext;
	
	private IDatabase database;
	
	private CTabFolder tabFolderTopLeft;
	private CTabFolder tabFolderBottomLeft;
	
	private HPCTraceView     tbtmTraceView;
	private HPCDepthView     tbtmDepthView;
	private HPCCallStackView tbtmCallStack;
	private HPCSummaryView   tbtmSummaryView;
	
	private DebugView tbtmDebugView;
	
	private DepthEditor     depthEditor;

	private HPCStatisticView tbtmStatView;
	private HPCBlameView tbtmBlameView;
	
	private ToolItem tiZoomIn;
	private ToolItem tiZoomOut;
	
	private SpaceTimeMiniCanvas miniCanvas;
	
	private IEclipseContext context;
	private SpaceTimeDataController stdc;
	private EPartService partService;
	private IEventBroker eventBroker;
	
	@Inject
	public TracePart() {
		mapLabelToContext = new HashMap<>(8);
	}
	
	@PostConstruct
	public void postConstruct(Composite parent, 
							  MWindow window, 
							  EPartService partService, 
							  IEventBroker eventBroker,
							  @Named(IServiceConstants.ACTIVE_PART) MPart part) {
		
		this.partService = partService;
		this.eventBroker = eventBroker;
		
		SashForm sashFormMain  = new SashForm(parent, SWT.NONE);
		SashForm sashFormLeft  = new SashForm(sashFormMain, SWT.VERTICAL);
		SashForm sashFormRight = new SashForm(sashFormMain, SWT.VERTICAL);

		context = part.getContext();
		
		// ---------------
		// main view
		// ---------------
		
		tabFolderTopLeft = new CTabFolder(sashFormLeft, SWT.BORDER);
		
		tbtmTraceView = new HPCTraceView(tabFolderTopLeft, SWT.NONE);
		createTabItem(tbtmTraceView, "Main view", tabFolderTopLeft, eventBroker);
		
		if (ViewerPreferenceManager.INSTANCE.getDebugMode()) {
			tbtmDebugView = new DebugView(tabFolderTopLeft, 0);
			createTabItem(tbtmDebugView, "Debug view", tabFolderTopLeft, eventBroker);
		}
		
		// ---------------
		// depth view
		// ---------------
		
		tabFolderBottomLeft = new CTabFolder(sashFormLeft, SWT.BORDER);
		
		tbtmDepthView = new HPCDepthView(tabFolderBottomLeft, SWT.NONE);
		createTabItem(tbtmDepthView, "Depth view", tabFolderBottomLeft, eventBroker);
		
		tbtmSummaryView = new HPCSummaryView(tabFolderBottomLeft, SWT.NONE);
		createTabItem(tbtmSummaryView, "Summary view", tabFolderBottomLeft, eventBroker);
		
		ToolBar toolbar = new ToolBar( tabFolderBottomLeft, SWT.FLAT );
		
		tiZoomIn  = createToolItem(toolbar, LABEL_ZOOM_IN_Y, ICON_ZOOM_IN_Y, "Zoom-in the depth");		
		tiZoomOut = createToolItem(toolbar, LABEL_ZOOM_OUT_Y, ICON_ZOOM_OUT_Y, "Zoom-out the depth");
		
		tabFolderBottomLeft.setTopRight(toolbar);
		tabFolderBottomLeft.setTabHeight(Math.max(toolbar.computeSize(SWT.DEFAULT, SWT.DEFAULT).y, tabFolderBottomLeft.getTabHeight()));
		
		tabFolderBottomLeft.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean activateDepth = (e.item == tbtmDepthView);
				tiZoomIn.setEnabled (activateDepth && tbtmDepthView.canZoomIn());
				tiZoomOut.setEnabled(activateDepth && tbtmDepthView.canZoomOut());
			}
		});
		
		tiZoomIn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tbtmDepthView.zoomIn();
				updateToolItem();
			}
		});
		
		tiZoomOut.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tbtmDepthView.zoomOut();
				updateToolItem();
			}
		});
		
		
		// ---------------
		// Depth editor
		// ---------------
		
		Composite stackArea = new Composite(sashFormRight, SWT.NONE);
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(stackArea);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(stackArea);

		depthEditor = new DepthEditor(stackArea, eventBroker);

		// ---------------
		// call stack
		// ---------------

		CTabFolder tabFolderRight = new CTabFolder(stackArea, SWT.BORDER);	
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tabFolderRight);

		tbtmCallStack = new HPCCallStackView(tabFolderRight, SWT.NONE);
		createTabItem(tbtmCallStack, "Call stack", tabFolderRight, eventBroker);

		tbtmStatView = new HPCStatisticView(tabFolderRight, 0); 
		createTabItem(tbtmStatView, "Statistics", tabFolderRight, eventBroker);
		
		tbtmBlameView = new HPCBlameView(tabFolderRight, SWT.NONE);
		createTabItem(tbtmBlameView, "GPU Idleness Blame", tabFolderRight, eventBroker);
		
		tabFolderRight.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {

				if (e.item == tbtmStatView || e.item == tbtmBlameView) {
					int numItems = tbtmStatView.getItemCount();
					if (numItems < 1) {
						activateStatisticItem();
					}
				}
			}
		});
		
		// ---------------
		// mini map
		// ---------------
		
		final Composite miniArea = new Composite(sashFormRight, SWT.BORDER_DASH);
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(miniArea);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(miniArea);

		Label lblMiniMap = new Label(miniArea, SWT.BORDER);
		lblMiniMap.setText("Mini map");
		lblMiniMap.setToolTipText("The view to show the portion of the execution shown by the Trace View," +
				  				  "relative to process/time dimensions");
		
		GridDataFactory.fillDefaults().indent(5, 4).align(SWT.LEFT, SWT.TOP).grab(true, false).applyTo(lblMiniMap);
		
		miniCanvas = new SpaceTimeMiniCanvas(this, miniArea);

		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(miniCanvas);
		GridDataFactory.swtDefaults().grab(true, true).align(SWT.CENTER, SWT.CENTER).hint(160, 100).applyTo(miniCanvas);
		
		// ---------------
		// sash settings
		// ---------------
		
		final int []weights = new int[] {8000, 2000};
		sashFormLeft .setWeights(weights);		
		sashFormRight.setWeights(weights);		
		sashFormMain .setWeights(weights);

		// ---------------
		// finalization
		// ---------------
		
		tabFolderBottomLeft.setSelection(tbtmDepthView);
		tabFolderRight.setSelection(tbtmCallStack);
		tabFolderTopLeft.setSelection(tbtmTraceView);
		tabFolderTopLeft.setFocus();
		
		partService.addPartListener(this);
		TracePreferenceManager.INSTANCE.getPreferenceStore().addPropertyChangeListener(this);
		ViewerPreferenceManager.INSTANCE.getPreferenceStore().addPropertyChangeListener(this);
		
		eventBroker.subscribe(BaseConstants.TOPIC_HPC_REMOVE_DATABASE, this);
	}
	
	
	private void updateToolItem() {
		tiZoomIn.setEnabled(tbtmDepthView.canZoomIn());
		tiZoomOut.setEnabled(tbtmDepthView.canZoomOut());
	}
	
	private ToolItem createToolItem(ToolBar toolbar, String label, String icon, String tooltip) {
		
		ToolItem toolitem = new ToolItem(toolbar, SWT.PUSH);
		Image image = Utility.getImage(icon, label);
		toolitem.setImage(image);
		toolitem.setToolTipText(tooltip);
		
		return toolitem;
	}
	
	private void createTabItem(AbstractBaseItem item, String title, CTabFolder parent, IEventBroker eventBroker) {
		item.setText(title);

		Composite tabArea = new Composite(parent, SWT.NONE);
		FillLayout tabLayout = new FillLayout();
		tabLayout.type = SWT.VERTICAL;
		tabArea.setLayout(tabLayout);
		
		item.createContent(this, context, eventBroker, tabArea);
		item.setControl(tabArea);
	}
	
	
	/***
	 * Return the list of actions of this trace
	 * @return
	 */
	public ITraceViewAction getActions() {
		if (tbtmTraceView != null)
			return tbtmTraceView.getActions();
		
		return null;
	}
	
	
	@PreDestroy
	public void preDestroy() {
		if (partService != null)
			partService.removePartListener(this);
		
		if (eventBroker != null)
			eventBroker.unsubscribe(this);
		
		if (depthEditor != null)
			depthEditor.dispose();
		
		// remove listener to trace config
		PreferenceStore pref = TracePreferenceManager.INSTANCE.getPreferenceStore();
		if (pref != null)
			pref.removePropertyChangeListener(this);
		
		// remove listener to debug config
		pref = ViewerPreferenceManager.INSTANCE.getPreferenceStore();
		if (pref != null)
			pref.removePropertyChangeListener(this);
	}
	
	
	@Focus
	public void onFocus() {
		// bug on Windows: forcing the focus will cause flickering infinitely
		if (OSValidator.isWindows())
			return;
		
		if (tabFolderTopLeft != null && !tabFolderTopLeft.isDisposed())
			tabFolderTopLeft.setFocus();
	}

	
	@Override
	public BaseExperiment getExperiment() {
		if (database != null)
			return (BaseExperiment) database.getExperimentObject();
		
		return null;
	}

	@Override
	public void setInput(IDatabase input) {
		this.database = input;
	}

	@Override
	public void partActivated(MPart part) { /* unused */ }

	@Override
	public void partBroughtToTop(MPart part) { /* unused */ }

	@Override
	public void partDeactivated(MPart part) { /* unused */ }

	@Override
	public void partHidden(MPart part) { /* unused */ }

	@Override
	public void partVisible(MPart part) {
		// if we already create the database and the views, we don't need to recreate again
		if (part.getObject() != this || stdc != null)
			return;

		// if we need to close the part, we shouldn't continue
		if (eventBroker == null || partService == null)
			return;
		
		if (database == null || database.getExperimentObject().getRootScope() == null)
			return;
		
		try {
			//AbstractDBOpener dbOpener = (AbstractDBOpener) database.getORCreateTraceManager(); //new LocalDBOpener(context, database.getExperimentObject());
			stdc = (SpaceTimeDataController) database.getORCreateTraceManager();

			// make sure all the tabs other than trace view has the stdc first
			tbtmDepthView.setInput(stdc);
			tbtmCallStack.setInput(stdc);
			miniCanvas.   updateView(stdc);
			tbtmStatView .setInput(stdc);
			
			//       since the stat view requires info from summary view 
			tbtmSummaryView.setInput(stdc);

			// enable action
			depthEditor.setInput(stdc);

			// this has to be the last tab item to be set
			// start reading the database and draw it
			tbtmTraceView.setInput(stdc);
			
			var data = stdc.getBaseData();

			if (!data.hasGPU()) {
				tbtmBlameView.dispose();
				tbtmSummaryView.setAnalysisTool(IPixelAnalysis.EMPTY);
			} else {
				tbtmSummaryView.setAnalysisTool(new CpuBlameAnalysis(eventBroker));
				tbtmBlameView.setInput(stdc);
			}
			updateToolItem();

			eventBroker.subscribe(ViewerDataEvent.TOPIC_FILTER_POST_PROCESSING, this);

			if (tbtmDebugView != null) {
				tbtmDebugView.setInput(stdc);
			}
		} catch (Exception e) {
			Shell shell = Display.getDefault().getActiveShell();
			MessageDialog.openError(shell, "Error in opening the database", e.getClass().getSimpleName() + ":" + e.getMessage());
			preDestroy();
		}
	}

	@Override
	public void activateStatisticItem() {
		tabFolderBottomLeft.setSelection(tbtmSummaryView);
	}

	@Override
	public IUndoContext getContext(final String label) {
		IUndoContext labelContext = mapLabelToContext.get(label);
		if (labelContext != null)
			return labelContext;

		labelContext = new BaseTraceContext(label);		
		mapLabelToContext.put(label, labelContext);
		
		return labelContext;
	}
	
	
	@Override
	public IOperationHistory getOperationHistory() {
		return OperationHistoryFactory.getOperationHistory();
	}


	@Override
	public IDatabase getInput() {
		return database;
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		TraceEventData data = new TraceEventData(stdc, this, stdc);			
		
		switch (event.getProperty()) {
		
		case TracePreferenceConstants.PREF_RENDER_OPTION:
			// refresh the content
			eventBroker.post(IConstants.TOPIC_COLOR_MAPPING, data);
			break;
		case PreferenceConstants.ID_DEBUG_MODE:
		case TracePreferenceConstants.PREF_GPU_TRACES:
			tbtmTraceView.redraw();
			break;
		}
	}

	@Override
	public void handleEvent(Event event) {
		Object obj = event.getProperty(IEventBroker.DATA);
		if (obj == null || database == null)
			return;
		
		var experiment = database.getExperimentObject();
		if (experiment == null)
			return;
		
		// check if we have a generic event
		ViewerDataEvent eventInfo = (ViewerDataEvent) obj;
		if (experiment != eventInfo.metricManager) 
			return;

		if (event.getTopic().equals(BaseConstants.TOPIC_HPC_REMOVE_DATABASE)) {
			dispose();
		} else if (event.getTopic().equals(ViewerDataEvent.TOPIC_FILTER_POST_PROCESSING)) {
			tbtmTraceView.refresh();
		}
	}

	@Override
	public void showErrorMessage(String message) {
		tbtmTraceView.showMessage(message);
	}

	@Override
	public void showInfo(String message) {
		tbtmTraceView.showMessage(message);
	}

	@Override
	public void showWarning(String message) {
		tbtmTraceView.showMessage(message);
	}

	@Override
	public void dispose() {
		// clean up the history to avoid memory spills
		var history = getOperationHistory();
		for(var strCtx: BaseTraceContext.CONTEXTS) {
			history.dispose(getContext(strCtx), true, true, true);
		}
		database = null;
		
		// mark that this part will be close soon. Do not do any tasks
		partService = null;
		eventBroker = null;
		
		// need to dispose resources
		if (stdc != null)
			stdc.dispose();
		
		stdc = null;
	}

	@Override
	public SpaceTimeDataController getDataController() {
		return stdc;
	}
}