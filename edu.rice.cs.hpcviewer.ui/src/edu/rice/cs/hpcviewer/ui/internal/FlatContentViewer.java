package edu.rice.cs.hpcviewer.ui.internal;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpcviewer.ui.actions.FlatScopeAction;
import edu.rice.cs.hpcviewer.ui.experiment.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.resources.IconManager;

public class FlatContentViewer extends AbstractContentViewer 
{

	static final private int ITEM_FLAT = 0;
	static final private int ITEM_UNFLAT = 1;
	
	private ToolItem[] items;
	private FlatScopeAction action;

	public FlatContentViewer(EPartService  partService, 
							 EModelService modelService, 
							 MApplication  app,
							 IEventBroker  broker,
							 DatabaseCollection database) {
		
		super(partService, modelService, app, broker, database);

	}

	@Override
	protected void beginToolbar(CoolBar coolbar, ToolBar toolbar) {

		items = new ToolItem[2];
		
		items[ITEM_FLAT] = createToolItem(toolbar, IconManager.Image_Flatten, 
				"Flatten nodes one level");
		
		items[ITEM_UNFLAT] = createToolItem(toolbar, IconManager.Image_Unflatten, 
				"Unflatten nodes one level");
		
		createToolItem(toolbar, SWT.SEPARATOR, "", "");
		
		items[ITEM_FLAT].addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {

				action.flatten();
				updateStatus();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		items[ITEM_UNFLAT].addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				action.unflatten();
				updateStatus();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
	}
	

	@Override
	protected void endToolbar(CoolBar coolbar, ToolBar toolbar) {}

	@Override
	protected AbstractContentProvider getContentProvider(ScopeTreeViewer treeViewer) {
		return new AbstractContentProvider(treeViewer) {
			
			@Override
			public Object[] getChildren(Object node) {
				if (node instanceof Scope) {
					return ((Scope)node).getChildren();
				}
				return null;
			}
		};
	}

	@Override
	protected void selectionChanged(IStructuredSelection selection) {

		if (action == null)
			action = new FlatScopeAction(getViewer());

		items[ITEM_UNFLAT].setEnabled(action.canUnflatten());
		
		Object obj = getViewer().getInput();
		if (obj != null) {
			if (obj instanceof Scope) {
				Scope objNode = (Scope)obj;
				for( int i=0; i<objNode.getChildCount(); i++ ) {
					if (objNode.getChildAt(i).hasChildren()) {
						items[ITEM_FLAT].setEnabled(true);
						return;
					}
				}
			}
		}

		items[ITEM_FLAT].setEnabled(false);
	}
}
