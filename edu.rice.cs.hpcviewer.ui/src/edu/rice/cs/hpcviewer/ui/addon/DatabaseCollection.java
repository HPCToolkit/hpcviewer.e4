package edu.rice.cs.hpcviewer.ui.addon;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;

import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpcbase.ui.IMainPart;
import edu.rice.cs.hpctraceviewer.ui.TracePart;
import edu.rice.cs.hpcviewer.ui.ProfilePart;
import edu.rice.cs.hpcviewer.ui.experiment.ExperimentManager;
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
	static private final String STACK_ID_BASE 	  = "edu.rice.cs.hpcviewer.ui.partstack.integrated";
	
	final private HashMap<MWindow, List<BaseExperiment>>   mapWindowToExperiments;
	final private HashMap<BaseExperiment, ViewerDataEvent> mapColumnStatus;
	
	private EPartService      partService;
	private IEventBroker      eventBroker;
    private ExperimentManager experimentManager;
    private MApplication      application;
    private EModelService     modelService;
	
	private Logger    statusReporter;
	
	@Inject UISynchronize sync;
	
	public DatabaseCollection() {

		mapColumnStatus = new HashMap<BaseExperiment, ViewerDataEvent>();		
		experimentManager = new ExperimentManager();
		
		mapWindowToExperiments = new HashMap<MWindow, List<BaseExperiment>>(1);
	}
	
	@Inject
	@Optional
	private void subscribeApplicationCompleted(
			@UIEventTopic(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE) 
			final MApplication   application,
			final EPartService   partService,
			final IEventBroker   broker,
			final EModelService  modelService, @Named(IServiceConstants.ACTIVE_SHELL) Shell myShell) {
		
		this.partService    = partService;
		this.eventBroker    = broker;
		this.application    = application;
		this.modelService   = modelService;

		this.statusReporter = LoggerFactory.getLogger(getClass());

		// handling the command line arguments:
		// if one of the arguments specify a file or a directory,
		// try to find the experiment.xml and load it.
		
		String args[] = Platform.getApplicationArgs();
		
		if (myShell == null) {
			myShell = new Shell(SWT.TOOL | SWT.NO_TRIM);
		}
		final Shell shell = myShell;
		
		String path = null;
		
		for (String arg: args) {
			if (arg.charAt(0) != '-')
				path = arg;
		}
		if (path == null || path.length() < 1) {
			path = experimentManager.openFileExperiment(shell);
			if (path == null)
				return; 
		}

		openDatabaseAndCreateViews(shell, path, "Open a database");
	}
	

	/****
	 * One-stop API to open and add a database. 
	 * This method shows a dialog box to pick a directory, check if the database already exists or not,
	 * create views and add to the list of the database collection.
	 * 
	 * @param shell the current shell
	 * @param application MApplication
	 * @param service EPartService
	 * @param modelService EModelService
	 * @param parentId the id of the part stack to contain the views. If the value is null, then
	 *   it searches any available slot if possible.
	 */
	public void addDatabase(Shell shell, 
			MApplication 	application, 
			EPartService    service,
			EModelService 	modelService,
			String          parentId) {
		
		String filename = experimentManager.openFileExperiment(shell);
		if (filename == null)
			return;

		if (isExist(shell, filename))
			return;

		openDatabaseAndCreateViews(shell, filename, "Add a database");
	}
	
	
	/****
	 * One-stop API to open a database. 
	 * This method shows a dialog box to pick a directory, check if the database already exists or not,
	 * create views and remove the existing databases before adding it to the list of the database collection.
	 * The removal is important to make sure there is only one database exist.
	 * 
	 * @param shell the current shell
	 * @param application MApplication
	 * @param service EPartService
	 * @param modelService EModelService
	 * @param parentId the id of the part stack to contain the views. If the value is null, then
	 *   it searches any available slot if possible.
	 */
	public void openDatabase(Shell shell, 
			MApplication 	application, 
			EPartService    service,
			EModelService 	modelService,
			String          parentId) {
		
		final String filename = experimentManager.openFileExperiment(shell);
		if (filename == null)
			return;

		if (isExist(shell, filename))
			return;

		removeAll();
		openDatabaseAndCreateViews(shell, filename, "Open a database");
	}

	
	/****
	 * Add a new database into the collection.
	 * This database can be remove later on by calling {@code removeLast}
	 * or {@code removeAll}.
	 * 
	 * @param experiment cannot be null
	 * @param application the main application
	 * @param service EPartService to create parts
	 * @param modelService
	 * @param parentId the parent EPartStack of the parts. If it's null, the new parts will be assigned to the current active
	 */
	public void createViewsAndAddDatabase(BaseExperiment experiment, 
										  MApplication 	 application, 
										  EPartService   service,
										  EModelService  modelService,
										  String         parentId) {
		
		if (experiment == null || service == null) {
			MessageDialog.openError( Display.getDefault().getActiveShell(), 
									 "Error in opening the file", 
									 "Database not found: " + experiment.getDefaultDirectory().getAbsolutePath());
			return;
		}
		
		IEclipseContext activeWindowContext = application.getContext().getActiveChild();;
		
		// Corner case for TWM window manager: sometimes the processing is faster
		// than the UI, and thus Eclipse doesn't provide any context or any child
		// at this stage. Maybe we should wait until it's ready?
		
		if (activeWindowContext == null) {
			// we give up. There's still no active window yet.
			MWindow window = application.getSelectedElement();
			
			((EObject) window).eAdapters().add(new AdapterImpl() {
				
				@SuppressWarnings("restriction")
				@Override
				public void notifyChanged(Notification notification) {
					if (notification.getFeatureID(MWindow.class) != BasicPackageImpl.WINDOW__CONTEXT) {
						return;
					}
					IEclipseContext windowContext = (IEclipseContext) notification.getNewValue();
					if (windowContext != null) {
						showPart(experiment, application, service, parentId);
					}
				}
			});
			return;
		}
		showPart(experiment, application, service, parentId);
	}
	

	/***
	 * Display a profile part 
	 * 
	 * @param experiment
	 * @param application
	 * @param service
	 * @param parentId
	 */
	private void showPart( BaseExperiment experiment, 
						   MApplication application, 
						   EPartService   service,
						   String parentId) {

		//----------------------------------------------------------------
		// find an empty slot in the part stack
		// If no slot is available, we will try to create a new one.
		// However,creating a new part stack is tricky, and it's up to the
		// system where to locate the part stack.
		//----------------------------------------------------------------
		
		MPartStack stack = null;
		List<MStackElement> list = null;
		MWindow  window = application.getSelectedElement();
		String stackId = parentId;
		
		if (parentId == null) {
			stackId = STACK_ID_BASE; 
		}
		stack  = (MPartStack)modelService.find(stackId, window);
		if (stack != null)
			list = stack.getChildren();
		
		//----------------------------------------------------------------
		// create a new part stack if necessary
		// We don't want this, since it makes the layout weird.
		//----------------------------------------------------------------
		if (stack == null) {
			System.out.println("create a new part stack");
			
			stack = modelService.createModelElement(MPartStack.class);
			stack.setElementId(STACK_ID_BASE);
			stack.setToBeRendered(true);
		}
		
		final MPart part = service.createPart(ProfilePart.ID);
		if (list != null)
			list.add(part);
		service.showPart(part, PartState.VISIBLE);
		IMainPart view = null;

		int maxAttempt = 20;		
		while(maxAttempt>0) {
			view = (IMainPart) part.getObject();
			if (view != null)
				break;
			
			try {
				Thread.sleep(300);					
			} catch (Exception e) {
				
			}
			maxAttempt--;
		}
		if (view == null) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Fail to get the view", "hpcviewer is unable to retrieve the view. Please try again");
			return;
		}
		
		// has to set the element Id before populating the view
		//String elementID = ElementIdManager.getElementId(experiment);
		//part.setElementId(elementID);
		view.setInput(part, experiment);

		//----------------------------------------------------------------
		// part stack is ready, now we create all view parts and add it to the part stack
		// TODO: We assume adding to the part stack is always successful
		//----------------------------------------------------------------
		stack.setVisible(true);
		stack.setOnTop(true);
		
		List<BaseExperiment> listExperiments = getActiveListExperiments();
		if (listExperiments != null) {
			listExperiments.add(experiment);
		}
		if (experiment.getTraceAttribute() != null) {
			MPart tracePart = service.createPart(TracePart.ID);
			MPart createPart = service.showPart(tracePart, PartState.CREATE);
			
			if (createPart != null) {
				list.add(createPart);
				
				Object objTracePart = createPart.getObject();
				if (objTracePart != null) {
					((TracePart)objTracePart).setInput(createPart, experiment);
				}
			}
		}

	}
	
	/***
	 * Retrieve the iterator of the database collection
	 * 
	 * @return Iterator for the list
	 */
	public Iterator<BaseExperiment> getIterator() {
		List<BaseExperiment> list = getActiveListExperiments();
		return list.iterator();
	}
	
	
	/***
	 * Retrieve the iterator of the database collection from a given windo
	 * 
	 * @param window 
	 * @return Iterator for the list of the given window
	 */
	public Iterator<BaseExperiment> getIterator(MWindow window) {
		List<BaseExperiment> list = mapWindowToExperiments.get(window);
		return list.iterator();
	}
	
		
	/***
	 * Retrieve the current registered databases
	 * @return
	 */
	public int getNumDatabase() {
		List<BaseExperiment> list = getActiveListExperiments();
		if (list == null)
			return 0;
		return list.size();
	}
	
	
	/***
	 * Check if the database is empty or not
	 * @return true if the database is empty
	 */
	public boolean isEmpty() {
		List<BaseExperiment> list = getActiveListExperiments();
		return list.isEmpty();
	}
	
	
	/***
	 * Remove the last registered database
	 * @return
	 */
	public BaseExperiment getLast() {
		List<BaseExperiment> list = getActiveListExperiments();
		return list.get(list.size()-1);
	}
	
	
	/***
	 * Check if an experiment already exist in the collection
	 * @param experiment
	 * @return true if the experiment already exists. False otherwise.
	 */
	public boolean IsExist(BaseExperiment experiment) {
		List<BaseExperiment> list = getActiveListExperiments();
		return list.contains(experiment);
	}
	
	
	/***
	 * Retrieve the experiment object given a XML file path
	 * 
	 * @param pathXML the absolute path of the experiment XNK file path
	 * @return BaseExperiment object if the database exist, null otherwise.
	 */
	public BaseExperiment getExperiment(String pathXML) {
		List<BaseExperiment> list = getActiveListExperiments();
		
		if (list.isEmpty())
			return null;
		
		for (BaseExperiment exp: list) {
			String directory = exp.getXMLExperimentFile().getAbsolutePath();
			if (directory.equals(pathXML)) {

				return exp;
			}
		}
		return null;
	}
	
	/***
	 * Check if a database path already exist in the collection
	 * @param shell
	 * @param pathXML the absolute path to XML file
	 * @return true of the XML file already exist. False otherwise
	 */
	public boolean isExist(Shell shell, String pathXML) {
		
		BaseExperiment exp = getExperiment(pathXML);
		if (exp != null) {
			// we cannot have two exactly the same database in one window
			MessageDialog.openError(shell, "Error", pathXML +": the database is already opened." );

			return true;
		}
		return false;
	}
	
	
	/***
	 * Remove a database and all parts (views and editors) associated with it
	 * 
	 * @param experiment
	 */
	public void removeDatabase(final BaseExperiment experiment) {
		
		if (experiment == null)
			return;
		
		// remove any database associated with this experiment
		// some parts may need to check the database if the experiment really exits or not.
		// If not, they will consider the experiment will be removed.
		List<BaseExperiment> list = getActiveListExperiments();
		list.remove(experiment);

		mapColumnStatus.remove(experiment);
		
		MWindow window = application.getSelectedElement();
		List<MPart> listParts = modelService.findElements(window, null, MPart.class);
		
		if (listParts == null)
			return;

		// first, notify all the parts that have experiment that they will be destroyed.
		
		ViewerDataEvent data = new ViewerDataEvent((Experiment) experiment, null);
		eventBroker.post(ViewerDataEvent.TOPIC_HPC_REMOVE_DATABASE, data);
		
		// destroy all the views and editors that belong to experiment
		// since Eclipse doesn't have "destroy" method, we hide them.
		
		for(MPart part: listParts) {
			Object obj = part.getObject();
			if (obj instanceof IMainPart) {
				IMainPart mpart = (IMainPart) obj;
				if (mpart.getExperiment() == experiment) {
					partService.hidePart(part, true);
				}
			}
		}
	}	
	
	
	/****
	 * Remove all databases
	 * @return
	 */
	public int removeAll() {
		List<BaseExperiment> list = getActiveListExperiments();
		
		int size = list.size();
		
		// TODO: ugly solution to avoid concurrency (java will not allow to remove a list while iterating).
		// we need to copy the list to an array, and then remove the list
		
		BaseExperiment arrayExp[] = new BaseExperiment[size];
		list.toArray(arrayExp);
		
		for(BaseExperiment exp: arrayExp) {
			// inside this method, we remove the database AND hide the view parts
			removeDatabase(exp);
		}
		
		list.clear();

		mapColumnStatus.clear();
		
		return size;
	}
	
	
	public void addColumnStatus(BaseExperiment experiment, ViewerDataEvent data) {
		mapColumnStatus.put(experiment, data);
	}
	
	
	public ViewerDataEvent getColumnStatus(BaseExperiment experiment) {
		return mapColumnStatus.get(experiment);
	}
	

	
	
	/***
	 * Log status to the Eclipse log service
	 * @param status type of status {@link IStatus}
	 * @param message 
	 * @param e any exception
	 */
	public void statusReport(int status, String message, Exception e) {
		switch(status) {
		case IStatus.INFO:
			if (e == null)
				statusReporter.info(message);
			
			else 
				statusReporter.debug(message, e);
			
			break;
			
		case IStatus.ERROR:
			if (e == null)
				statusReporter.debug(message);
			else
				statusReporter.error(message, e);
			break;
			
		}
		if (e != null)
			e.printStackTrace();
	}
	
	
	/***
	 * Retrieve the list of experiments of the current window.
	 * If Eclipse reports there is no active window, the list is null.
	 * 
	 * @return the list of experiments (if there's an active window). null otherwise.
	 * 
	 */
	private List<BaseExperiment> getActiveListExperiments() {
		MWindow window = application.getSelectedElement();
		if (window == null) {
			statusReport(IStatus.ERROR, "no active window", null);
			return null;
		}
		List<BaseExperiment> list = mapWindowToExperiments.get(window);
		
		if (list == null) {
			list = new ArrayList<BaseExperiment>();
			mapWindowToExperiments.put(window, list);
		}
		return list;
	}
	
	/****
	 * Find a database for a given path
	 * 
	 * @param shell the active shell
	 * @param expManager the experiment manager
	 * @param sPath path to the database
	 * @return
	 * @throws Exception 
	 */
	private BaseExperiment openDatabase(Shell shell, String sPath) throws Exception {
    	IFileStore fileStore;

		try {
			fileStore = EFS.getLocalFileSystem().getStore(new URI(sPath));
			
		} catch (URISyntaxException e) {
			// somehow, URI may throw an exception for certain schemes. 
			// in this case, let's do it traditional way
			fileStore = EFS.getLocalFileSystem().getStore(new Path(sPath));

			statusReport(IStatus.ERROR, "Locating " + sPath, e);
		}
    	IFileInfo objFileInfo = fileStore.fetchInfo();

    	if (!objFileInfo.exists())
    		return null;

    	BaseExperiment experiment = null;
    	
    	if (objFileInfo.isDirectory()) {
    		experiment = experimentManager.openDatabaseFromDirectory(shell, sPath);
    	} else {
			EFS.getLocalFileSystem().fromLocalFile(new File(sPath));
			experiment = experimentManager.loadExperiment(sPath);
    	}
    	return experiment;
	}
	

	/****
	 * Open a database using background job, and then create views using UI thread
	 * 
	 * @param shell
	 * @param database
	 * @param message
	 */
	private void openDatabaseAndCreateViews(final Shell shell, final String database, final String message) {

		try {
			final BaseExperiment experiment = openDatabase(shell, database);
			createViewsAndAddDatabase(experiment, application, partService, modelService, null);
		} catch (Exception e) {
			final String msg = "Error opening the database";
			statusReport(IStatus.ERROR, msg, e);
		}
	}

}
