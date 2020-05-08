package edu.rice.cs.hpcbase.ui;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.RegistryToggleState;

public class CommandUtil 
{
	
	/****
	 * utility to get a window's command object of a given ID
	 * @param window : window ID
	 * @param commandID : command ID
	 * 
	 * @return the command (usually a menu command)
	 */
	static public Command getCommand( IWorkbenchWindow window, String commandID ) {
		Command command = null;
		ICommandService commandService = (ICommandService) window.getService(ICommandService.class);

		if (commandService != null)
			command = commandService.getCommand( commandID );
		
		return command;
	}
	
	/***
	 * verify if the menu "Show trace records" is checked
	 * 
	 * @return true of the menu is checked. false otherwise
	 */
	static public boolean isOptionEnabled(Command command)
	{
		boolean isEnabled = false;

		final State state = command.getState(RegistryToggleState.STATE_ID);
		if (state != null)
		{
			final Boolean b = (Boolean) state.getValue();
			isEnabled = b.booleanValue();
		}
		return isEnabled;
	}

}
