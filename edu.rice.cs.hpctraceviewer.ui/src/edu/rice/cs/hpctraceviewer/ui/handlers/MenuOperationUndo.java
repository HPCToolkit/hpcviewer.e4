 
package edu.rice.cs.hpctraceviewer.ui.handlers;

import java.util.List;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;
import edu.rice.cs.hpctraceviewer.ui.context.BaseTraceContext;


public class MenuOperationUndo 
{
	static final private String ID_MENU_URI = "bundleclass://edu.rice.cs.hpctraceviewer.ui/" + MenuOperationUndo.class.getName();
	static final private String ID_BASE_ELEMENT = "MenuOperationUndo";
	
	@AboutToShow
	public void aboutToShow(List<MMenuElement> items, EModelService modelService, MPart part) {

		
		ITracePart tracePart = (ITracePart) part.getObject();
		IUndoContext context = tracePart.getContext(BaseTraceContext.CONTEXT_OPERATION_TRACE);
		IUndoableOperation[] histories = tracePart.getOperationHistory().getUndoHistory(context);
		
		for(int i=histories.length-1; i>=0; i--) {
			
			IUndoableOperation history = histories[i];
			MMenuElement menu = modelService.createModelElement(MDirectMenuItem.class);
			menu.setElementId(ID_BASE_ELEMENT + "." + i);
			menu.setLabel(history.getLabel());
			menu.setContributorURI(ID_MENU_URI);
			System.out.println("menu-undo " + i + ": " + ID_MENU_URI);
			items.add(menu);
		}
	}
	

	@Execute
	public void execute(MDirectMenuItem menu) {
		String label = menu.getElementId();
		int index = label.lastIndexOf(',');
		String strIndex = label.substring(index+1);
		System.out.println("undo-op: " + strIndex);
	}
	

	@CanExecute
	public boolean canExecute() {
		return true;
	}
}