package edu.rice.cs.hpcviewer.ui.experiment;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpcviewer.ui.internal.ViewerDataEvent;

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
		
		if (broker.post(ViewerDataEvent.TOPIC_HPC_NEW_DATABASE, experiment)) {
			if (application != null && modelService != null) {
				MWindow window = (MWindow) modelService.find(MAIN_WINDOW, application);
				window.setLabel("hpcviewer - " + experiment.getDefaultDirectory().getPath());
			}
		}
		
		if (service == null)
			return;		
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
