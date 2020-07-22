 
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
import edu.rice.cs.hpctraceviewer.ui.main.HPCTraceView;

import javax.annotation.PreDestroy;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.IPartListener;

public class TracePart  implements IMainPart, IPartListener
{
	public static final String ID = "edu.rice.cs.hpctraceviewer.ui.partdescriptor.trace";

	private MPart part;
	
	private BaseExperiment experiment;
	private HPCTraceView tbtmTraceView;
	
	
	@Inject
	public TracePart() {
	}
	
	@PostConstruct
	public void postConstruct(Composite parent, 
							  MWindow window, 
							  EPartService partService, 
							  @Named(IServiceConstants.ACTIVE_PART) MPart part) {
		
		this.part = part;
		
		SashForm sashForm_1 = new SashForm(parent, SWT.NONE);
		SashForm sashForm_2 = new SashForm(sashForm_1, SWT.VERTICAL);
		
		CTabFolder tabFolderTopLeft = new CTabFolder(sashForm_2, SWT.BORDER);
		tabFolderTopLeft.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		
		IEclipseContext context = window.getContext();
		
		tbtmTraceView = new HPCTraceView(tabFolderTopLeft, SWT.NONE);
		tbtmTraceView.setText("Trace view");
		
		Composite composite = new Composite(tabFolderTopLeft, SWT.NONE);
		FillLayout fillLayout = new FillLayout();
		fillLayout.type = SWT.VERTICAL;
		composite.setLayout(fillLayout);
		
		tbtmTraceView.createContent(this, context, composite);
		tbtmTraceView.setControl(composite);
		
		CTabFolder tabFolderBottomLeft = new CTabFolder(sashForm_2, SWT.BORDER | SWT.CLOSE);
		tabFolderBottomLeft.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		
		CTabItem tbtmDepthView = new CTabItem(tabFolderBottomLeft, SWT.NONE);
		tbtmDepthView.setText("Depth view");
		sashForm_2.setWeights(new int[] {1, 1});
		
		CTabFolder tabFolderRight = new CTabFolder(sashForm_1, SWT.BORDER);
		tabFolderRight.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		
		CTabItem tbtmCallStack = new CTabItem(tabFolderRight, SWT.NONE);
		tbtmCallStack.setText("Call stack");
		sashForm_1.setWeights(new int[] {1, 1});
		
		tabFolderBottomLeft.setSelection(tbtmDepthView);
		tabFolderRight.setSelection(tbtmCallStack);
		tabFolderTopLeft.setSelection(tbtmTraceView);
		tabFolderTopLeft.setFocus();
		
		partService.addPartListener(this);
	}
	
	
	@PreDestroy
	public void preDestroy() {
		
	}
	
	
	@Focus
	public void onFocus() {}

	@Override
	public BaseExperiment getExperiment() {

		return experiment;
	}

	@Override
	public void setInput(MPart part, Object input) {
		this.experiment = (BaseExperiment) input;
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
		try {
			tbtmTraceView.setInput(experiment);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}