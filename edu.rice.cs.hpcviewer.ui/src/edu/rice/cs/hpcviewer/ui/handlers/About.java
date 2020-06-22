 
package edu.rice.cs.hpcviewer.ui.handlers;

import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

public class About 
{
	@Execute
	public void execute(@Active Shell shell) {
		
		MessageDialog.openInformation(shell, "About hpcviewer", 
				"hpcviewer is a user interface for analyzing a database of performance metrics in conjunction with an application's source code.\n" + 
				"\n" + 
				"hpcviewer is part of Rice University's HPCToolkit project. Development of HPCToolkit is principally funded by the Department of Energy's Office of Science, Lawrence Livermore National Laboratory and National Science Foundation.\n" + 
				"\n" + 
				"Release 2020.06  (C) Copyright 2020, Rice University.");
	}		
}