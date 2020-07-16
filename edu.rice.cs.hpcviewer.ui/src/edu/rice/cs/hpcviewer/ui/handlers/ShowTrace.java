 
package edu.rice.cs.hpcviewer.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;

import java.util.List;

import javax.inject.Named;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpctraceviewer.ui.main.HPCTraceView;
import edu.rice.cs.hpcviewer.ui.parts.IBasePart;

public class ShowTrace 
{
	@Execute
	public void execute( 
			MApplication  application, 
			MWindow       window,
			EModelService modelService,
			@Named(IServiceConstants.ACTIVE_PART) MPart part,
			@Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {

		MWindow winTrace = (MWindow) modelService.find(HPCTraceView.ID_WINDOW, application);
		
		MPerspective perspective = (MPerspective) modelService.find(HPCTraceView.ID_PERSPECTIVE, application);
		
		if (perspective == null || winTrace == null) {

			return;
		}
		
		IEclipseContext context = winTrace.getContext();
		if (context == null)
			return;
		
	 	if (!winTrace.isVisible()) {
			int height = window.getHeight();
			int width  = window.getWidth();
			int x = window.getX() + 100;
			int y = window.getY() + 100;
			
			winTrace.setX(x);
			winTrace.setY(y);
			winTrace.setHeight(height);
			winTrace.setWidth(width);

			winTrace.setVisible(true);
		}
		winTrace.setOnTop(true);
		IBasePart objPart = (IBasePart) part.getObject();
		BaseExperiment experiment = objPart.getExperiment();

		context.set(HPCTraceView.ID_DATA, experiment);
		
		List<MPart> list = modelService.findElements(winTrace, HPCTraceView.ID_PART, MPart.class);
		if (list == null || list.size()==0)
			return;
		HPCTraceView traceView = (HPCTraceView) list.get(0).getObject();
		try {
			traceView.setInput(experiment);
		} catch (Exception e) {
			MessageDialog.openError(shell, "Error opening trace data",
					experiment.getDefaultDirectory().getAbsolutePath() + ": " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	@CanExecute
	public boolean canExecute(@Named(IServiceConstants.ACTIVE_PART) MPart part) {
		if (part == null || (part.getObject() == null))
			return false;
		
		IBasePart objPart = (IBasePart) part.getObject();
		BaseExperiment experiment = objPart.getExperiment();
		
		return (experiment.getTraceAttribute() != null);
	}
		
}