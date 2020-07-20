 
package edu.rice.cs.hpcviewer.ui.parts;

import javax.inject.Inject;
import javax.annotation.PostConstruct;
import org.eclipse.swt.widgets.Composite;
import javax.annotation.PreDestroy;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.custom.CTabItem;
import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpcviewer.ui.experiment.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.parts.editor.PartFactory;
import edu.rice.cs.hpcviewer.ui.parts.topdown.TopDownItem;

import org.eclipse.swt.custom.StyledText;

public class ProfilePart implements IViewPart
{

	@Inject	protected EPartService  partService;
	@Inject protected EModelService modelService;
	@Inject protected MApplication  app;
	@Inject protected IEventBroker  eventBroker;
	
	@Inject protected DatabaseCollection databaseAddOn;

	@Inject protected PartFactory partFactory;

	/** Each view needs to store the experiment database.
	 * In case it needs to populate the table, we know which database 
	 * to be loaded. */
	private BaseExperiment  experiment;
	
	private TopDownItem tbtmTopDown;

	@Inject
	public ProfilePart() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		parent.setLayout(new FillLayout(SWT.HORIZONTAL));

		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		
		CTabFolder tabFolderTop = new CTabFolder(sashForm, SWT.BORDER);
		tabFolderTop.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		
/*		CTabItem tbtmNewItem = new CTabItem(tabFolderTop, SWT.NONE);
		tbtmNewItem.setText("Code viewer");
		
		StyledText styledText = new StyledText(tabFolderTop, SWT.BORDER);
		tbtmNewItem.setControl(styledText);
*/		
		CTabFolder tabFolderBottom = new CTabFolder(sashForm, SWT.BORDER);
		tabFolderBottom.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		
		tbtmTopDown = new TopDownItem(tabFolderBottom, SWT.NONE);
		tbtmTopDown.setText("Top-down");
		tbtmTopDown.setToolTipText("Calling context view of the profile data");

		Composite composite = new Composite(tabFolderBottom, SWT.NONE);
		tbtmTopDown.setControl(composite);
		composite.setLayout(new GridLayout(1, false));

		tbtmTopDown.setService(partService, eventBroker, databaseAddOn, partFactory);
		tbtmTopDown.createContent(composite);
		
		CTabItem tbtmBottomUp = new CTabItem(tabFolderBottom, SWT.NONE);
		tbtmBottomUp.setText("Bottom-up");
		
		CTabItem tbtmFlat = new CTabItem(tabFolderBottom, SWT.NONE);
		tbtmFlat.setText("Flat");
		sashForm.setWeights(new int[] {1, 1});
		
		tabFolderBottom.setSelection(tbtmTopDown);
		tabFolderBottom.setFocus();
	}
	
	
	public void setInput(Experiment experiment) {
		tbtmTopDown.setInput(experiment);
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
		tbtmTopDown.setInput(input);		
	}
}