 
package edu.rice.cs.hpctraceviewer.ui.handlers;

import java.util.List;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

import edu.rice.cs.hpctraceviewer.ui.operation.TraceOperation;

public class MenuOperationRedo 
{
	static final private String ID_MENU_URI = "bundleclass://edu.rice.cs.hpctraceviewer.ui/" + MenuOperationRedo.class.getName();
	static final private String ID_BASE_ELEMENT = "MenuOperationRedo";

	@AboutToShow
	public void aboutToShow(List<MMenuElement> items, EModelService modelService) {
		IUndoableOperation[] histories = getHistory();
		
		int i = 0;
		for(IUndoableOperation history: histories) {
			MMenuElement menu = modelService.createModelElement(MDirectMenuItem.class);
			menu.setElementId(ID_BASE_ELEMENT + "." + i);
			menu.setLabel(history.getLabel());
			menu.setContributorURI(ID_MENU_URI);
			items.add(menu);
			i++;
		}
	}


	@Execute
	public void execute(MDirectMenuItem menu) {
		String label = menu.getElementId();
		int index = label.lastIndexOf(',');
		String strIndex = label.substring(index+1);
		System.out.println("redo " + strIndex);
	}

	
	protected IUndoableOperation[] getHistory() {
		return TraceOperation.getRedoHistory();
	}

}