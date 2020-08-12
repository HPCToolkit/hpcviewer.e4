package edu.rice.cs.hpcviewer.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.workbench.IWorkbench;

public class FileExit 
{
	@Execute
	public void execute(IWorkbench workbench) {
		workbench.close();
	}
}
