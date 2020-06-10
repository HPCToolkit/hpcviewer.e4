package edu.rice.cs.hpcviewer.ui.experiment;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpcviewer.ui.internal.ViewerDataEvent;
import edu.rice.cs.hpcviewer.ui.parts.BaseViewPart;
import edu.rice.cs.hpcviewer.ui.parts.BottomUpPart;
import edu.rice.cs.hpcviewer.ui.parts.FlatPart;
import edu.rice.cs.hpcviewer.ui.parts.IBaseView;
import edu.rice.cs.hpcviewer.ui.parts.TopDownPart;
import edu.rice.cs.hpcviewer.ui.parts.editor.Editor;

/***
 * <b>
 * Class Database manager
 * </b>
 * <p>It manages multiple experiment databases, including:
 * <ul>
 *  <li>Adding a new database. It sends message DatabaseManager.EVENT_HPC_NEW_DATABASE to
 *      the application components.</li>
 *  <li>Removing an existing database</li>
 * </ul>
 *</p>
 */
@Creatable
@Singleton
public class DatabaseCollection 
{
	static final private String MAIN_WINDOW = "edu.rice.cs.hpcviewer.window.main";
	
	private ConcurrentLinkedQueue<BaseExperiment> queueExperiment;
	private HashMap<BaseExperiment, ViewerDataEvent> mapColumnStatus;
	
	@Inject EPartService partService;
	
	public DatabaseCollection() {
		queueExperiment = new ConcurrentLinkedQueue<>();
		mapColumnStatus = new HashMap<BaseExperiment, ViewerDataEvent>();
	}
	
	public void addDatabase(BaseExperiment experiment, 
			MApplication 	application, 
			EPartService    service,
			IEventBroker 	broker,
			EModelService 	modelService) {
		
		queueExperiment.add(experiment);
		
		/*
		if (broker.post(ViewerDataEvent.TOPIC_HPC_NEW_DATABASE, experiment)) {
			if (application != null && modelService != null) {
				MWindow window = (MWindow) modelService.find(MAIN_WINDOW, application);
				window.setLabel("hpcviewer - " + experiment.getDefaultDirectory().getPath());
			}
		} */
		
		if (service == null) {
			System.out.println("Error: service is not available");
			return;
		}
		MPartStack stack = (MPartStack)modelService.find("edu.rice.cs.hpcviewer.ui.partstack.lower", application);
		
		final String []partIds = {
				TopDownPart.IDdesc,
				BottomUpPart.IDdesc,
				FlatPart.IDdesc
		};
		
		Object []children = experiment.getRootScopeChildren();
		
		for (int i=0; i<children.length; i++) {

			RootScope root = (RootScope) children[i];
			
			final MPart part = partService.createPart(partIds[i]);
			
			stack.getChildren().add(part);

			part.setLabel(root.getRootName());
			part.setElementId(experiment.getDefaultDirectory().getAbsolutePath() + ":" + root.getRootName());
			
			if (i==0) {
				partService.showPart(part, PartState.VISIBLE);
				
				IBaseView view = (IBaseView) part.getObject();			
				view.setExperiment(experiment);
			}
		}
	}
	
	public Iterator<BaseExperiment> getIterator() {
		return queueExperiment.iterator();
	}
	
	public int getNumDatabase() {
		return queueExperiment.size();
	}
	
	public boolean isEmpty() {
		return queueExperiment.isEmpty();
	}
	
	public BaseExperiment getLast() {
		return queueExperiment.element();
	}
	
	public BaseExperiment removeLast() {
		return queueExperiment.remove();
	}
	
	public int removeAll() {
		int size = queueExperiment.size();
		queueExperiment.clear();

		mapColumnStatus.clear();
		
		return size;
	}
	
	public void addColumnStatus(BaseExperiment experiment, ViewerDataEvent data) {
		mapColumnStatus.put(experiment, data);
	}
	
	public ViewerDataEvent getColumnStatus(BaseExperiment experiment) {
		return mapColumnStatus.get(experiment);
	}
}
