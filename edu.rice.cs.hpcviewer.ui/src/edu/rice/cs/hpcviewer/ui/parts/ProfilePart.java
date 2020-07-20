 
package edu.rice.cs.hpcviewer.ui.parts;

import javax.inject.Inject;
import javax.annotation.PostConstruct;
import org.eclipse.swt.widgets.Composite;
import javax.annotation.PreDestroy;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolder2Listener;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.widgets.Display;
import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpcviewer.ui.experiment.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.parts.bottomup.BottomUpView;
import edu.rice.cs.hpcviewer.ui.parts.editor.PartFactory;
import edu.rice.cs.hpcviewer.ui.parts.flat.FlatView;
import edu.rice.cs.hpcviewer.ui.parts.topdown.TopDownView;

public class ProfilePart implements IViewPart
{

	@Inject	protected EPartService  partService;
	@Inject protected EModelService modelService;
	@Inject protected MApplication  app;
	@Inject protected IEventBroker  eventBroker;
	protected EMenuService menuService;
	
	@Inject protected DatabaseCollection databaseAddOn;

	@Inject protected PartFactory partFactory;

	/** Each view needs to store the experiment database.
	 * In case it needs to populate the table, we know which database 
	 * to be loaded. */
	private BaseExperiment  experiment;
	
	private AbstractViewItem []views;

	private CTabFolder tabFolderTop, tabFolderBottom;
	
	@Inject
	public ProfilePart() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent, EMenuService menuService) {
		
		this.menuService = menuService;
		
		parent.setLayout(new FillLayout(SWT.HORIZONTAL));

		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		
		tabFolderTop = new CTabFolder(sashForm, SWT.BORDER);
		tabFolderTop.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		
/*		CTabItem tbtmNewItem = new CTabItem(tabFolderTop, SWT.NONE);
		tbtmNewItem.setText("Code viewer");
		
		StyledText styledText = new StyledText(tabFolderTop, SWT.BORDER);
		tbtmNewItem.setControl(styledText);
*/		
		tabFolderBottom = new CTabFolder(sashForm, SWT.BORDER);
		tabFolderBottom.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		
		sashForm.setWeights(new int[] {1, 1});

		tabFolderBottom.setFocus();
		tabFolderBottom.addCTabFolder2Listener(new CTabFolder2Adapter() {
			
			@Override
			public void showList(CTabFolderEvent event) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void restore(CTabFolderEvent event) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void close(CTabFolderEvent event) {
				// TODO Auto-generated method stub
				
			}
		});
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
			} else {
				System.err.println("Not supported root: " + root.getType());
				break;
			}
			// TODO: make sure this statement is called early.
			// The content builder will need many services. So we have to make they are initialized
			views[numViews].setService(partService, eventBroker, databaseAddOn, partFactory, menuService);

			Composite composite = new Composite(tabFolderBottom, SWT.NONE);
			views[numViews].setControl(composite);
			composite.setLayout(new GridLayout(1, false));

			views[numViews].createContent(composite);
			views[numViews].setInput(experiment);
		}
		
		tabFolderBottom.setSelection(views[0]);
		tabFolderBottom.setFocus();
	}
}