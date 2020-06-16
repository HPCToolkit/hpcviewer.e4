package edu.rice.cs.hpcviewer.ui.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpcviewer.ui.experiment.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.parts.editor.PartFactory;
import edu.rice.cs.hpcviewer.ui.parts.thread.ThreadContentViewer;
import edu.rice.cs.hpcviewer.ui.parts.thread.ThreadViewInput;

/*************************************************************
 * 
 * View part to display CCT and metrics for a specific set of threads 
 *
 *************************************************************/
public class ThreadView  implements IViewPart
{
	static public final String IDdesc = "edu.rice.cs.hpcviewer.ui.partdescriptor.thread";

	@Inject EPartService  partService;
	@Inject EModelService modelService;
	@Inject MApplication  app;
	@Inject IEventBroker  broker;
	
	@Inject DatabaseCollection databaseAddOn;

	@Inject PartFactory partFactory;

	
	private ThreadContentViewer contentViewer; 
	private ThreadViewInput     viewInput; 
	

	
	@PostConstruct
	public void postConstruct(Composite parent, EMenuService menuService) {

		contentViewer = new ThreadContentViewer(partService, modelService, app, broker, databaseAddOn, partFactory);
		contentViewer.createContent(parent, menuService);
	}


	@Override
	public BaseExperiment getExperiment() {
		return null;
	}

	@Override
	public void setInput(MPart part, Object input) {
		
		// important: needs to store the experiment database for further usage
		// when the view is becoming visible
		viewInput = (ThreadViewInput) input;
		
		contentViewer.setData(viewInput);
	}	
}
