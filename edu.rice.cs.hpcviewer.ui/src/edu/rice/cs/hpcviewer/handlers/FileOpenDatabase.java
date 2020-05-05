package edu.rice.cs.hpcviewer.handlers;

import org.eclipse.e4.core.di.annotations.Execute;

public class FileOpenDatabase 
{
	@Execute
	public void execute() {
		System.out.println(getClass().getSimpleName() + " called");
	}
}
