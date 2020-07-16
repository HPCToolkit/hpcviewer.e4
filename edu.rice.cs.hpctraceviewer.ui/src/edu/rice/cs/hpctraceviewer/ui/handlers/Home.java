 
package edu.rice.cs.hpctraceviewer.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import edu.rice.cs.hpctraceviewer.ui.main.HPCTraceView;
import edu.rice.cs.hpctraceviewer.ui.main.ITraceViewAction;

public class Home 
{
	@Execute
	public void execute(MWindow window, EPartService partService, EModelService modelService, MPart part) {

		if (part == null) {
			return;
		}
		HPCTraceView traceView   = (HPCTraceView) part.getObject();
		ITraceViewAction actions = traceView.getActions();
		actions.home();
	}
		
}