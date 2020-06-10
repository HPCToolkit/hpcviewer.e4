package edu.rice.cs.hpcviewer.ui.experiment;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpcviewer.ui.internal.ViewerDataEvent;
import edu.rice.cs.hpcviewer.ui.parts.BottomUpPart;
import edu.rice.cs.hpcviewer.ui.parts.FlatPart;
import edu.rice.cs.hpcviewer.ui.parts.IBaseView;
import edu.rice.cs.hpcviewer.ui.parts.TopDownPart;

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
	private ConcurrentLinkedQueue<BaseExperiment> queueExperiment;
	private HashMap<BaseExperiment, ViewerDataEvent> mapColumnStatus;
	
	
	public DatabaseCollection() {
		queueExperiment = new ConcurrentLinkedQueue<>();
		mapColumnStatus = new HashMap<BaseExperiment, ViewerDataEvent>();
	}
	
	@Inject
	@Optional
	private void subscribeApplicationCompleted(
			@UIEventTopic(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE) final MApplication application,
			final EPartService partService,
			final IEventBroker broker,
			final EModelService modelService,
			final IWorkbench workbench) {
		
		
		// handling the command line arguments:
		// if one of the arguments specify a file or a directory,
		// try to find the experiment.xml and load it.
		
		String args[] = Platform.getApplicationArgs();
		
		Display display = Display.getDefault();
		Shell myShell   = display.getActiveShell();
		
		if (myShell == null) {
			myShell = new Shell(SWT.TOOL | SWT.NO_TRIM);
		}
		BaseExperiment experiment    = null;
		ExperimentManager expManager = new ExperimentManager();
		
		String path = null;
		
		for (String arg: args) {
			if (arg.charAt(0) != '-')
				path = arg;
		}
		if (path == null || path.length() < 1) {
			experiment    = expManager.openFileExperiment(myShell);
		} else {
			experiment = openDatabase(myShell, expManager, args[0]);
		}
		if (experiment == null)
			return;
		
		addDatabase(experiment, application, partService, broker, modelService);
	}
	
	/****
	 * Add a new database into the collection.
	 * This database can be remove later on by calling {@code removeLast}
	 * or {@code removeAll}.
	 * 
	 * @param experiment
	 * @param application
	 * @param service
	 * @param broker
	 * @param modelService
	 */
	public void addDatabase(BaseExperiment experiment, 
			MApplication 	application, 
			EPartService    service,
			IEventBroker 	broker,
			EModelService 	modelService) {
		
		queueExperiment.add(experiment);
		
		if (service == null) {
			System.out.println("Error: service is not available");
			return;
		}
		MPartStack stack = (MPartStack)modelService.find("edu.rice.cs.hpcviewer.ui.partstack.lower", application);
		
		final String []partIds = {
				TopDownPart .IDdesc,
				BottomUpPart.IDdesc,
				FlatPart    .IDdesc
		};
		
		Object []children = experiment.getRootScopeChildren();
		
		for (int i=0; i<children.length; i++) {

			RootScope root = (RootScope) children[i];
			
			final MPart part = service.createPart(partIds[i]);
			
			stack.getChildren().add(part);

			part.setLabel(root.getRootName());
			part.setElementId(experiment.getDefaultDirectory().getAbsolutePath() + ":" + root.getRootName());
			
			if (i==0) {
				service.showPart(part, PartState.VISIBLE);
				
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
	
	
	/****
	 * Find a database for a given path
	 * 
	 * @param shell the active shell
	 * @param expManager the experiment manager
	 * @param sPath path to the database
	 * @return
	 */
	private BaseExperiment openDatabase(Shell shell, ExperimentManager expManager, String sPath) {
    	IFileStore fileStore;

		try {
			fileStore = EFS.getLocalFileSystem().getStore(new URI(sPath));
		} catch (URISyntaxException e) {
			// somehow, URI may throw an exception for certain schemes. 
			// in this case, let's do it traditional way
			fileStore = EFS.getLocalFileSystem().getStore(new Path(sPath));
			e.printStackTrace();
		}
    	IFileInfo objFileInfo = fileStore.fetchInfo();

    	if (!objFileInfo.exists())
    		return null;

    	BaseExperiment experiment = null;
    	
    	if (objFileInfo.isDirectory()) {
    		experiment = expManager.openDatabaseFromDirectory(shell, sPath);
    	} else {
			EFS.getLocalFileSystem().fromLocalFile(new File(sPath));
			experiment = expManager.loadExperiment(shell, sPath);
    	}
    	return experiment;
	}
}
