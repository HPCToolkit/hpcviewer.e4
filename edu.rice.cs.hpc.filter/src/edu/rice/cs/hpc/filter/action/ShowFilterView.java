package edu.rice.cs.hpc.filter.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;

import edu.rice.cs.hpc.filter.view.FilterPropertyDialog;

public class ShowFilterView extends AbstractHandler 
{
	final public static String ID = "edu.rice.cs.hpc.filter.action.ShowFilterView";

	@Override 
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		FilterPropertyDialog dialog = new FilterPropertyDialog(null);
		
		if (dialog.open() == IDialogConstants.OK_ID) {
			
		}

		return null;
	}
}
