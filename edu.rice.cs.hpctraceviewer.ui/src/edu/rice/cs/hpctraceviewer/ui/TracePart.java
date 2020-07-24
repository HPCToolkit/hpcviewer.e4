 
package edu.rice.cs.hpctraceviewer.ui;

import javax.inject.Inject;
import javax.inject.Named;
import javax.annotation.PostConstruct;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpcbase.ui.IMainPart;
import edu.rice.cs.hpctraceviewer.data.AbstractDBOpener;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.local.LocalDBOpener;
import edu.rice.cs.hpctraceviewer.ui.callstack.HPCCallStackView;
import edu.rice.cs.hpctraceviewer.ui.depth.HPCDepthView;
import edu.rice.cs.hpctraceviewer.ui.main.HPCTraceView;
import edu.rice.cs.hpctraceviewer.ui.main.ITraceViewAction;
import edu.rice.cs.hpctraceviewer.ui.minimap.MiniMap;

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
	private HPCCallStackView tbtmCallStack;
	
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
		
		CTabFolder tabFolderBottomLeft = new CTabFolder(sashFormLeft, SWT.BORDER);
		
		tbtmDepthView = new HPCDepthView(tabFolderBottomLeft, SWT.NONE);
		createTabItem(tbtmDepthView, "Depth view", tabFolderBottomLeft, eventBroker);
		
		// ---------------
		// call stack
		// ---------------

		CTabFolder tabFolderRight = new CTabFolder(sashFormRight, SWT.BORDER);		
		tbtmCallStack = new HPCCallStackView(tabFolderRight, SWT.NONE);
		createTabItem(tbtmCallStack, "Call stack", tabFolderRight, eventBroker);
		
		// ---------------
		// call stack
		// ---------------

		CTabFolder tabFolderBottomRight = new CTabFolder(sashFormRight, SWT.BORDER);		
		MiniMap tbtmMinimap = new MiniMap(tabFolderBottomRight, SWT.NONE);
		createTabItem(tbtmMinimap, "Mini map", tabFolderBottomRight, eventBroker);

		// ---------------
		// sash settings
		// ---------------
		
		sashFormLeft .setWeights(new int[] {800, 200});		
		sashFormRight.setWeights(new int[] {700, 300});		
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
		if (dbOpener != null)
			return;
		
		try {
			dbOpener = new LocalDBOpener(context, experiment);
			SpaceTimeDataController stdc = dbOpener.openDBAndCreateSTDC(null);

			// TODO: make sure all the tabs other than trace view has the stdc first
			tbtmDepthView.setInput(stdc);
			tbtmCallStack.setInput(stdc);
			tbtmTraceView.setInput(stdc);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}