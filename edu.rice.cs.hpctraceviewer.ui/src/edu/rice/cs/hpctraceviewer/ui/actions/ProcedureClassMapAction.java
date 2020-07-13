package edu.rice.cs.hpctraceviewer.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpctraceviewer.ui.dialog.ProcedureClassDialog;
import edu.rice.cs.hpctraceviewer.data.util.ProcedureClassMap;


/****
 * 
 * action handler to show procedure-class map dialog
 *
 */
public class ProcedureClassMapAction extends AbstractHandler {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {

		//final Shell shell = HandlerUtil.getActiveShell(event);
		//final IWorkbenchWindow win = HandlerUtil.getActiveWorkbenchWindow(event);
		Shell shell = Display.getCurrent().getActiveShell();
		
		ProcedureClassMap classMap = new ProcedureClassMap(shell.getDisplay());
		ProcedureClassDialog dlg = new ProcedureClassDialog(shell, classMap);
		if ( dlg.open() == Dialog.OK ) {
			classMap.save();
			broadcastChanges();
		}
		
		return null;
	}

	private void broadcastChanges() {
		
		// broadcast to all views
	}
}
