package edu.rice.cs.hpcviewer.ui.parts.flat;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcviewer.ui.ProfilePart;
import edu.rice.cs.hpcviewer.ui.actions.FlatScopeAction;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.internal.AbstractContentProvider;
import edu.rice.cs.hpcviewer.ui.internal.AbstractViewBuilder;
import edu.rice.cs.hpcviewer.ui.internal.ScopeTreeViewer;
import edu.rice.cs.hpcviewer.ui.resources.IconManager;

public class FlatContentViewer extends AbstractViewBuilder 
{

	static final private int ITEM_FLAT = 0;
	static final private int ITEM_UNFLAT = 1;
	
	private ToolItem[] items;
	private FlatScopeAction action;
	private AbstractContentProvider contentProvider;

	public FlatContentViewer(EPartService  partService, 
							 IEventBroker  broker,
							 DatabaseCollection database,
							 ProfilePart   profilePart) {
		
		super(partService, broker, database, profilePart);
		
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

				if (action.flatten())
					stackActions.push(action);
				
				updateStatus();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		items[ITEM_UNFLAT].addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (action.unflatten()) {
	 				Object obj = stackActions.pop();
					assert (obj == action);
				}
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
		if (contentProvider == null) {
			contentProvider = new AbstractContentProvider(treeViewer) {
				
				@Override
				public Object[] getChildren(Object node) {
					if (node instanceof Scope) {
						/*
						 * debug to display the tree
						 *
						String tab = "";
						Scope scope = (Scope) node;
						while(scope.getParent()!=null) {
							scope = scope.getParentScope();
							tab += "  ";
						}
						System.out.println(tab + ((Scope)node).getCCTIndex() + ": " + node);
						*/
						return ((Scope)node).getChildren();
					}
					return null;
				}
			};
		}
		return contentProvider;
	}

	@Override
	protected void selectionChanged(IStructuredSelection selection) {

		if (action == null)
			action = new FlatScopeAction(getViewer());

		// we can enable unflatten button if:
		// - it has been previously flatten; and
		// - the last action is flatten()
		
		boolean canUnflat = action.canUnflatten() && 
							(!stackActions.isEmpty() && stackActions.peek()==action);
		items[ITEM_UNFLAT].setEnabled(canUnflat);
		
		// we can enable flatten button iff:
		// - the tree has at least one child and 
		// - one of the children has at least a child
		//
		// in other word: the tree has at least a grand-child
		
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

	@Override
	protected IMetricManager getMetricManager() {
		return (IMetricManager) getViewer().getExperiment();
	}

	@Override
	protected ViewerType getViewerType() {
		return ViewerType.COLLECTIVE;
	}
}
