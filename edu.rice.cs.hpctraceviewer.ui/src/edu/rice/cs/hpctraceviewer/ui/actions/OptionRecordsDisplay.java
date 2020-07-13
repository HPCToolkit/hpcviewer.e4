package edu.rice.cs.hpctraceviewer.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;



/***************
 * 
 * handler for menu advanced
 *
 */
public class OptionRecordsDisplay extends AbstractHandler {

	final static public String commandId = "edu.rice.cs.hpc.traceviewer.showRecords";
	
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
	     Command command = event.getCommand();
	     //HandlerUtil.toggleCommandState(command);
	     // use the old value and perform the operation 
	     return null;
	}

}
