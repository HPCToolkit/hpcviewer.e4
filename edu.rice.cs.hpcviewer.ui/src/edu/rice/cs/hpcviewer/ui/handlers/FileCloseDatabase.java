package edu.rice.cs.hpcviewer.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;

public class FileCloseDatabase 
{
	@Execute
	public void execute() {
		System.out.println(getClass().getSimpleName() + " called");
	}

}
