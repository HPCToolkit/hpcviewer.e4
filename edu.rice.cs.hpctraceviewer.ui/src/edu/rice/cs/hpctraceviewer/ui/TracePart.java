 
package edu.rice.cs.hpctraceviewer.ui;

import javax.inject.Inject;
import javax.inject.Named;

import java.util.HashMap;

import javax.annotation.PostConstruct;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpctraceviewer.data.AbstractDBOpener;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.local.LocalDBOpener;
import edu.rice.cs.hpctraceviewer.ui.base.AbstractBaseItem;
import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;
import edu.rice.cs.hpctraceviewer.ui.base.ITraceViewAction;
import edu.rice.cs.hpctraceviewer.ui.callstack.HPCCallStackView;
import edu.rice.cs.hpctraceviewer.ui.context.BaseTraceContext;
import edu.rice.cs.hpctraceviewer.ui.depth.HPCDepthView;
import edu.rice.cs.hpctraceviewer.ui.main.HPCTraceView;
import edu.rice.cs.hpctraceviewer.ui.minimap.SpaceTimeMiniCanvas;
import edu.rice.cs.hpctraceviewer.ui.statistic.HPCStatView;
import edu.rice.cs.hpctraceviewer.ui.summary.HPCSummaryView;

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
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;


/***************************************************************
 * 
 * The main class for trace viewer
 *
 ***************************************************************/
public class TracePart implements ITracePart, IPartListener
{
	public static final String ID = "edu.rice.cs.hpctraceviewer.ui.partdescriptor.trace";

	private final HashMap<String, IUndoContext> mapLabelToContext;
	
	private BaseExperiment experiment;
	
	private CTabFolder tabFolderTopLeft;
	private CTabFolder tabFolderBottomLeft;
	
	private HPCTraceView     tbtmTraceView;
	private HPCDepthView     tbtmDepthView;
	private HPCCallStackView tbtmCallStack;
	private HPCSummaryView   tbtmSummaryView;
	
	private HPCStatView tbtmStatView;
	
	private SpaceTimeMiniCanvas miniCanvas;
	
	private IEclipseContext context;
	private SpaceTimeDataController stdc;
	
	@Inject
	public TracePart() {
		mapLabelToContext = new HashMap<String, IUndoContext>(8);
	}
	
	@PostConstruct
	public void postConstruct(Composite parent, 
							  MWindow window, 
							  EPartService partService, 
							  IEventBroker eventBroker,
							  @Named(IServiceConstants.ACTIVE_PART) MPart part) {
		
		SashForm sashFormMain  = new SashForm(parent, SWT.NONE);
		SashForm sashFormLeft  = new SashForm(sashFormMain, SWT.VERTICAL);
		SashForm sashFormRight = new SashForm(sashFormMain, SWT.VERTICAL);

		context = window.getContext();
		
		// ---------------
		// main view
		// ---------------
		
		tabFolderTopLeft = new CTabFolder(sashFormLeft, SWT.BORDER);
		
		tbtmTraceView = new HPCTraceView(tabFolderTopLeft, SWT.NONE);
		createTabItem(tbtmTraceView, "Trace view", tabFolderTopLeft, eventBroker);
		
		// ---------------
		// depth view
		// ---------------
		
		tabFolderBottomLeft = new CTabFolder(sashFormLeft, SWT.BORDER);
		
		tbtmDepthView = new HPCDepthView(tabFolderBottomLeft, SWT.NONE);
		createTabItem(tbtmDepthView, "Depth view", tabFolderBottomLeft, eventBroker);
		
		tbtmSummaryView = new HPCSummaryView(tabFolderBottomLeft, SWT.NONE);
		createTabItem(tbtmSummaryView, "Summary view", tabFolderBottomLeft, eventBroker);
		
		// ---------------
		// call stack
		// ---------------

		CTabFolder tabFolderRight = new CTabFolder(sashFormRight, SWT.BORDER);		

		tbtmCallStack = new HPCCallStackView(tabFolderRight, SWT.NONE);
		createTabItem(tbtmCallStack, "Call stack", tabFolderRight, eventBroker);

		tbtmStatView = new HPCStatView(tabFolderRight, SWT.NONE);
		createTabItem(tbtmStatView, "Statistics", tabFolderRight, eventBroker);
		
		tabFolderRight.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {

				if (e.item == tbtmStatView) {
					int numItems = tbtmStatView.getItemCount();
					if (numItems < 1) {
						activateStatisticItem();
					}
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
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
		
		sashFormLeft .setWeights(new int[] {800, 200});		
		sashFormRight.setWeights(new int[] {800, 200});		
		sashFormMain .setWeights(new int[] {800, 200});

		// ---------------
		// finalization
		// ---------------
		
		tabFolderBottomLeft.setSelection(tbtmDepthView);
		tabFolderRight.setSelection(tbtmCallStack);
		tabFolderTopLeft.setSelection(tbtmTraceView);
		tabFolderTopLeft.setFocus();
		
		partService.addPartListener(this);
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
	
	
	public ITraceViewAction getActions() {
		return tbtmTraceView.getActions();
	}
	
	
	@PreDestroy
	public void preDestroy() {
	}
	
	
	@Focus
	public void onFocus() {
		tabFolderTopLeft.setFocus();
	}

	
	@Override
	public BaseExperiment getExperiment() {
		return experiment;
	}

	@Override
	public void setInput(MPart part, Object input) {
		this.experiment = (BaseExperiment) input;
		part.setLabel("Trace: " + experiment.getName());
		part.setTooltip("Traces from " + experiment.getDefaultDirectory().getAbsolutePath());
	}

	@Override
	public void partActivated(MPart part) {
	}

	@Override
	public void partBroughtToTop(MPart part) {
	}

	@Override
	public void partDeactivated(MPart part) {
	}

	@Override
	public void partHidden(MPart part) {
	}

	@Override
	public void partVisible(MPart part) {
		if (part.getObject() != this)
			return;
		
		// if we already create the database and the views, we don't need to recreate again
		if (stdc != null)
			return;
		
		try {
			AbstractDBOpener dbOpener = new LocalDBOpener(context, experiment);
			stdc = dbOpener.openDBAndCreateSTDC(null);

			// TODO: make sure all the tabs other than trace view has the stdc first
			tbtmDepthView.setInput(stdc);
			tbtmCallStack.setInput(stdc);
			miniCanvas.   updateView(stdc);
			tbtmStatView .setInput(stdc);
			
			// TODO: summary view has to be set AFTER the stat view 
			//       since the stat view requires info from summary view 
			tbtmSummaryView.setInput(stdc);

			// this has to be the last tab item to be set
			// start reading the database and draw it
			tbtmTraceView.setInput(stdc);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void activateStatisticItem() {
		tabFolderBottomLeft.setSelection(tbtmSummaryView);
	}

	@Override
	public IUndoContext getContext(final String label) {
		IUndoContext context = mapLabelToContext.get(label);
		if (context != null)
			return context;

		context = new TraceOperationContext(label);
		
		mapLabelToContext.put(label, context);
		
		return context;
	}
	
	
	@Override
	public IOperationHistory getOperationHistory() {
		return OperationHistoryFactory.getOperationHistory();
	}

	
	static private class TraceOperationContext extends BaseTraceContext 
	{
		public TraceOperationContext(final String label) {
			super(label);
		}
	}
}