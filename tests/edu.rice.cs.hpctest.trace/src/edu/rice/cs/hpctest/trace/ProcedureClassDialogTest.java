// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctest.trace;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;

import edu.rice.cs.hpctraceviewer.data.util.ProcedureClassMap;
import edu.rice.cs.hpctraceviewer.ui.dialog.ProcedureClassDialog;
import edu.rice.cs.hpctraceviewer.ui.dialog.ProcedureMapDetailDialog;

public class ProcedureClassDialogTest 
{

	
	/***
	 * unit test
	 * 
	 * @param argv
	 */
	static public void main(String argv[]) {
		Display display = Display.getDefault();
		
		ProcedureClassMap pcMap = new ProcedureClassMap(display);
		ProcedureClassDialog dlg = new ProcedureClassDialog(display.getActiveShell(), pcMap );

		if ( dlg.open() == Dialog.OK ) {
			if (dlg.isModified()) {
				pcMap.save();
			}
		}
		
		ProcedureMapDetailDialog dlgMap = new ProcedureMapDetailDialog(display.getActiveShell(), "edit", "procedure", "procedure-class", null);

		dlg.open();
		
		System.out.println("proc: " + dlgMap.getProcedure() + ", class: " + dlgMap.getDescription() + ", color: " + dlgMap.getRGB());

	}

}
