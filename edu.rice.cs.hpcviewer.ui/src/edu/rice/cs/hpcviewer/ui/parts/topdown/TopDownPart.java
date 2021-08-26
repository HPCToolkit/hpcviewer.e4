package edu.rice.cs.hpcviewer.ui.parts.topdown;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpctree.ScopeTreeTable;
import edu.rice.cs.hpcviewer.ui.ProfilePart;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.internal.AbstractBaseViewItem;
import edu.rice.cs.hpcviewer.ui.internal.LabelMessage;
import edu.rice.cs.hpcviewer.ui.internal.ScopeTreeViewer;
import edu.rice.cs.hpcviewer.ui.resources.IconManager;

public class TopDownPart extends AbstractBaseViewItem 
{	
	private Composite    parent ;
	private ToolItem     toolItem[];
	private LabelMessage lblMessage;
	
	private IMetricManager metricManager;
	private RootScope      root;
	private ScopeTreeTable table ;

	public TopDownPart(CTabFolder parent, int style) {
		super(parent, style);
		setText("Top down part");
	}
	

	
	
	/**
	 * finalize tool item and add it to the current toolbar
	 * 
	 * @param toolbar parent toolbar
	 * @param toolbarStyle style of toolitem ex: {@link SWT.PUSH}
	 * @param name the name of the image file
	 * @param tooltip the tooltip text
	 * 
	 * @return {@code ToolItem} created tool item
	 */
	protected ToolItem createToolItem(ToolBar toolbar, int toolbarStyle, String name, String tooltip) {
		
		IconManager iconManager = IconManager.getInstance();
		ToolItem toolitem = new ToolItem(toolbar, toolbarStyle);
		
		toolitem.setImage(iconManager.getImage(name));
		toolitem.setToolTipText(tooltip);
		toolitem.setEnabled(false);
		
		return toolitem;
	}

	/**
	 * finalize tool item and add it to the current toolbar with {@link SWT.PUSH} as the style
	 * 
	 * @param toolbar parent toolbar
	 * @param name the name of the image file
	 * @param tooltip the tooltip text
	 * @return {@code ToolItem}
	 */
	protected ToolItem createToolItem(ToolBar toolbar, String name, String tooltip) {
		if (name == null) {
			return new ToolItem(toolbar, SWT.SEPARATOR);
		}
		return createToolItem(toolbar, SWT.PUSH, name, tooltip);
	}
	
    /***
     * create cool item based on given toolbar
     * 
     * @param coolBar parent cool bar
     * @param toolBar toolbar to be added in the cool item
     */
	protected void createCoolItem(CoolBar coolBar, ToolBar toolBar) {

		CoolItem coolItem = new CoolItem(coolBar, SWT.NONE);
		coolItem.setControl(toolBar);
		Point size = toolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		size.x += 20;
		coolItem.setSize(size);
    	
    }

    protected void beginToolbar(CoolBar coolbar, ToolBar toolbar) {
    	
    }
    protected void endToolbar  (CoolBar coolbar, ToolBar toolbar) {
    	
    }

	@Override
	public void setService(EPartService partService, IEventBroker broker, DatabaseCollection database,
			ProfilePart profilePart, EMenuService menuService) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createContent(Composite parent) {
		this.parent = parent;
		parent.setLayout(new GridLayout(1, false));
		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite composite = new Composite(parent, SWT.BORDER);
		composite.setLayout(new GridLayout(2, false));
		GridData gd_composite = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_composite.widthHint = 506;
		composite.setLayoutData(gd_composite);
				
		CoolBar coolBar = new CoolBar(composite, SWT.FLAT);
		ToolBar toolBar = new ToolBar(coolBar, SWT.FLAT | SWT.RIGHT);

		// -------------------------------------------
		// add the beginning of toolbar
		// -------------------------------------------
		beginToolbar(coolBar, toolBar);
		
		// -------------------------------------------
		// default tool bar
		// -------------------------------------------
		//toolItem = new ToolItem[actionTypes.length];

		
		// -------------------------------------------
		// add the end of toolbar
		// -------------------------------------------
		endToolbar(coolBar, toolBar);

		createCoolItem(coolBar, toolBar);
		
		Point p = coolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		p.x += 20;
		
		coolBar.setSize(p);
		
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(false, false).applyTo(coolBar);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(coolBar);

		// -------------------------------------------
		// message label
		// -------------------------------------------

		lblMessage = new LabelMessage(composite, SWT.NONE);

		
		// -------------------------------------------
		// table creation
		// -------------------------------------------
	}

	@Override
	public void setInput(Object input) {
		if (!(input instanceof IMetricManager))
			return;
		
		metricManager = (IMetricManager) input;
		root = ((Experiment)metricManager).getRootScope(RootScopeType.CallingContextTree);
		table = new ScopeTreeTable(parent, SWT.NONE, root, metricManager);
		
		table.pack();
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(table);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(table);
	}

	@Override
	public Object getInput() {
		return metricManager;
	}

	@Override
	public void activate() {
		table.redraw();
	}

	@Override
	public ScopeTreeViewer getScopeTreeViewer() {
		// TODO Auto-generated method stub
		return null;
	}

}
