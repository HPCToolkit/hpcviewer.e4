package edu.rice.cs.hpcviewer.ui.internal;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpcviewer.ui.resources.IconManager;

public class FlatContentViewer extends BaseContentViewer 
{

	static final private int ITEM_FLAT = 0;
	static final private int ITEM_UNFLAT = 1;
	private ToolItem[] items;

	public FlatContentViewer(EPartService  partService, 
							 EModelService modelService, 
							 MApplication  app,
							 IEventBroker  broker) {
		
		super(partService, modelService, app, broker);
	}

	@Override
	protected void beginToolbar(CoolBar coolbar, ToolBar toolbar) {

		items = new ToolItem[2];
		
		items[ITEM_FLAT] = createToolItem(toolbar, IconManager.Image_Flatten, 
				"Flatten nodes one level");
		items[ITEM_UNFLAT] = createToolItem(toolbar, IconManager.Image_Unflatten, 
				"Unflatten nodes one level");
		createToolItem(toolbar, SWT.SEPARATOR, "", "");
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
		
		Object obj = selection.getFirstElement();
		
		if (obj != null && obj instanceof Scope) {
			
		}
	}

}
