 
package edu.rice.cs.hpctraceviewer.ui;

import javax.inject.Inject;
import javax.inject.Named;
import javax.annotation.PostConstruct;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpcbase.ui.IMainPart;
import edu.rice.cs.hpctraceviewer.data.AbstractDBOpener;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.local.LocalDBOpener;
import edu.rice.cs.hpctraceviewer.ui.depth.HPCDepthView;
import edu.rice.cs.hpctraceviewer.ui.main.HPCTraceView;
import edu.rice.cs.hpctraceviewer.ui.main.ITraceViewAction;

import javax.annotation.PreDestroy;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.IPartListener;

public class TracePart implements IMainPart, IPartListener
{
	public static final String ID = "edu.rice.cs.hpctraceviewer.ui.partdescriptor.trace";

	private BaseExperiment experiment;
	
	private CTabFolder tabFolderTopLeft;
	private HPCTraceView tbtmTraceView;
	private HPCDepthView tbtmDepthView;
	
	private IEclipseContext context;
	private AbstractDBOpener dbOpener;
	
	@Inject
	public TracePart() {
		dbOpener = null;
	}
	
	@PostConstruct
	public void postConstruct(Composite parent, 
							  MWindow window, 
							  EPartService partService, 
							  IEventBroker eventBroker,
							  @Named(IServiceConstants.ACTIVE_PART) MPart part) {
		
		SashForm sashFormMain = new SashForm(parent, SWT.NONE);
		SashForm sashFormLeft = new SashForm(sashFormMain, SWT.VERTICAL);
		
		context = window.getContext();
		
		// ---------------
		// main view
		// ---------------
		
		tabFolderTopLeft = new CTabFolder(sashFormLeft, SWT.BORDER);
		
		tbtmTraceView = new HPCTraceView(tabFolderTopLeft, SWT.NONE);
		tbtmTraceView.setText("Trace view");
		
		Composite mainArea = new Composite(tabFolderTopLeft, SWT.NONE);
		FillLayout fillLayout = new FillLayout();
		fillLayout.type = SWT.VERTICAL;
		mainArea.setLayout(fillLayout);
		
		tbtmTraceView.createContent(this, context, eventBroker, mainArea);
		tbtmTraceView.setControl(mainArea);
		
		// ---------------
		// depth view
		// ---------------
		
		CTabFolder tabFolderBottomLeft = new CTabFolder(sashFormLeft, SWT.BORDER);
		
		tbtmDepthView = new HPCDepthView(tabFolderBottomLeft, SWT.NONE);
		tbtmDepthView.setText("Depth view");

		Composite depthArea = new Composite(tabFolderBottomLeft, SWT.NONE);
		FillLayout depthfillLayout = new FillLayout();
		depthfillLayout.type = SWT.VERTICAL;
		depthArea.setLayout(depthfillLayout);

		tbtmDepthView.createContent(this, context, eventBroker, depthArea);
		tbtmDepthView.setControl(depthArea);
		
		// ---------------
		// call stack
		// ---------------
		
		CTabFolder tabFolderRight = new CTabFolder(sashFormMain, SWT.BORDER);
		tabFolderRight.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		
		CTabItem tbtmCallStack = new CTabItem(tabFolderRight, SWT.NONE);
		tbtmCallStack.setText("Call stack");
		
		// ---------------
		// sash 
		// ---------------
		
		sashFormLeft.setWeights(new int[] {800, 200});		
		sashFormMain.setWeights(new int[] {800, 200});

		// ---------------
		// finalization
		// ---------------
		
		tabFolderBottomLeft.setSelection(tbtmDepthView);
		tabFolderRight.setSelection(tbtmCallStack);
		tabFolderTopLeft.setSelection(tbtmTraceView);
		tabFolderTopLeft.setFocus();
		
		partService.addPartListener(this);
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
		if (dbOpener != null)
			return;
		
		try {
			dbOpener = new LocalDBOpener(context, experiment);
			SpaceTimeDataController stdc = dbOpener.openDBAndCreateSTDC(null);

			tbtmTraceView.setInput(stdc);
			tbtmDepthView.setInput(stdc);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}