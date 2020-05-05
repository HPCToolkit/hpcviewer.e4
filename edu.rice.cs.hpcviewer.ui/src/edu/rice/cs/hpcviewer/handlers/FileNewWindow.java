package edu.rice.cs.hpcviewer.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.workbench.IWorkbench;

public class FileNewWindow 
{
	@Execute
	public void execute(IWorkbench workbench) {
		System.out.println( getClass().getSimpleName() + " called");
	}

}
