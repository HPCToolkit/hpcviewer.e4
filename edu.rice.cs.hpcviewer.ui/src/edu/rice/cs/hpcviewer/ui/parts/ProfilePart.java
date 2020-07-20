 
package edu.rice.cs.hpcviewer.ui.parts;

import javax.inject.Inject;
import javax.annotation.PostConstruct;
import org.eclipse.swt.widgets.Composite;
import javax.annotation.PreDestroy;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpcviewer.ui.experiment.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.parts.editor.PartFactory;
import edu.rice.cs.hpcviewer.ui.parts.topdown.TopDownItem;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.custom.StyledText;

public class ProfilePart 
{

	@Inject	protected EPartService  partService;
	@Inject protected EModelService modelService;
	@Inject protected MApplication  app;
	@Inject protected IEventBroker  eventBroker;
	
	@Inject protected DatabaseCollection databaseAddOn;

	@Inject protected PartFactory partFactory;

	private IViewBuilder  contentViewer;
	
	/** Each view needs to store the experiment database.
	 * In case it needs to populate the table, we know which database 
	 * to be loaded. */
	private BaseExperiment  experiment;
	
	/** This variable is a flag whether a table is already populated or not.
	 * If the root is null, it isn't populated
	 */


	@Inject
	public ProfilePart() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		parent.setLayout(new FillLayout(SWT.HORIZONTAL));

		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		
		CTabFolder tabFolderTop = new CTabFolder(sashForm, SWT.BORDER);
		tabFolderTop.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		
		CTabItem tbtmNewItem = new CTabItem(tabFolderTop, SWT.NONE);
		tbtmNewItem.setText("Code viewer");
		
		StyledText styledText = new StyledText(tabFolderTop, SWT.BORDER);
		tbtmNewItem.setControl(styledText);
		
		CTabFolder tabFolderBottom = new CTabFolder(sashForm, SWT.BORDER);
		tabFolderBottom.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		
		TopDownItem tbtmTopDown = new TopDownItem(tabFolderBottom, SWT.NONE);
		tbtmTopDown.setText("Top-down");
		
		Composite composite = new Composite(tabFolderBottom, SWT.NONE);
		tbtmTopDown.setControl(composite);
		composite.setLayout(new GridLayout(1, false));

		tbtmTopDown.setService(partService, eventBroker, databaseAddOn, partFactory);
		tbtmTopDown.createContent(composite);
		
		/*
		ToolBar toolBar = new ToolBar(composite, SWT.FLAT | SWT.RIGHT);
		toolBar.setBounds(0, 0, 104, 31);
		
		ToolItem tltmNewItem = new ToolItem(toolBar, SWT.NONE);
		tltmNewItem.setText("New Item");
		
		ToolItem tltmNewItem_1 = new ToolItem(toolBar, SWT.NONE);
		tltmNewItem_1.setText("New Item");
		
		TreeViewer treeViewer = new TreeViewer(composite, SWT.BORDER);
		Tree tree = treeViewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
*/		
		CTabItem tbtmBottomUp = new CTabItem(tabFolderBottom, SWT.NONE);
		tbtmBottomUp.setText("Bottom-up");
		
		CTabItem tbtmFlat = new CTabItem(tabFolderBottom, SWT.NONE);
		tbtmFlat.setText("Flat");
		sashForm.setWeights(new int[] {1, 1});
	}
	
	
	@PreDestroy
	public void preDestroy() {
		
	}
	
	
	@Focus
	public void onFocus() {
		
	}
}