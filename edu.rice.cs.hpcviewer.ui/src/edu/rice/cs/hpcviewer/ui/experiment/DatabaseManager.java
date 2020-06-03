package edu.rice.cs.hpcviewer.ui.experiment;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.inject.Singleton;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;

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
public class DatabaseManager 
{
	/** Event when a new database has arrived. */
	static final public String EVENT_HPC_NEW_DATABASE = "hpcviewer/database_add";

	/** Event when a database has to be removed from the application */
	static final public String EVENT_HPC_REMOVE_DATABASE = "hpcviewer/database_remove";
	
	private ConcurrentLinkedQueue<BaseExperiment> queueExperiment;
	
	public DatabaseManager() {
		queueExperiment = new ConcurrentLinkedQueue<>();
	}
	
	public void addDatabase(BaseExperiment experiment, 
			MApplication application, 
			IEclipseContext context,
			IEventBroker broker,
			EModelService modelService) {
		
		queueExperiment.add(experiment);
		
		if (context == null)
			return;
		
		context.set(DatabaseManager.EVENT_HPC_NEW_DATABASE, experiment);
		
		if (broker.post(DatabaseManager.EVENT_HPC_NEW_DATABASE, experiment)) {
			if (application != null && modelService != null) {
				MWindow window = (MWindow) modelService.find("edu.rice.cs.hpcviewer.window.main", application);
				window.setLabel("hpcviewer - " + experiment.getDefaultDirectory().getPath());
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

		return size;
	}
}
