package edu.rice.cs.hpcviewer.ui.addon;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
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
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcbase.BaseConstants;
import edu.rice.cs.hpcbase.ElementIdManager;
import edu.rice.cs.hpcbase.IDatabase;
import edu.rice.cs.hpcbase.IDatabase.DatabaseStatus;
import edu.rice.cs.hpcbase.ViewerDataEvent;
import edu.rice.cs.hpcbase.map.UserInputHistory;
import edu.rice.cs.hpcbase.ui.IMainPart;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.experiment.InvalExperimentException;
import edu.rice.cs.hpcfilter.service.FilterMap;
import edu.rice.cs.hpclocal.DatabaseLocal;
import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceManager;
import edu.rice.cs.hpctraceviewer.ui.TracePart;
import edu.rice.cs.hpcviewer.ui.ProfilePart;
import edu.rice.cs.hpcviewer.ui.handlers.RecentDatabase;
import edu.rice.cs.hpcviewer.ui.internal.DatabaseWindowManager;


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
	private static final String STACK_ID_BASE = "edu.rice.cs.hpcviewer.ui.partstack.integrated";
	private static final String PREFIX_ERROR  = "Database error ";
	
	private @Inject IEventBroker eventBroker;
	private @Inject UISynchronize sync;

	private Logger statusReporter;

	private final DatabaseWindowManager databaseWindowManager;
	
	public DatabaseCollection() {
		databaseWindowManager = new DatabaseWindowManager();
	}
	
	
	@Inject
	@Optional
	/*****
	 * this method is executed once the viewer has completed the start up
	 * 
	 * @param application
	 * @param partService
	 * @param broker
	 * @param modelService
	 * @param myShell
	 */
	private void subscribeApplicationCompleted(
			@UIEventTopic(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE) 
			final MApplication   application,
			final EPartService   partService,
			final IEventBroker   broker,
			final EModelService  modelService, 
			@Named(IServiceConstants.ACTIVE_SHELL) Shell myShell) throws InterruptedException {
		
		this.eventBroker    = broker;
		this.statusReporter = LoggerFactory.getLogger(getClass());

		// handling the command line arguments:
		// if one of the arguments specify a file or a directory,
		// try to find the experiment.xml and load it.
		
		String[] args = Platform.getApplicationArgs();
		
		if (myShell == null) {
			myShell = new Shell(SWT.TOOL | SWT.NO_TRIM);
		}
		final Shell shell = myShell;
		String path = null;
		
		// look for the path to the database in the command argument
		for (String arg: args) {
			if (arg.charAt(0) != '-')
				path = arg;
		}
		var convertedPath = convertPathToLocalFileSystem(path);
		
		var window = application.getSelectedElement();
		if (window == null) {
			// Damn Eclipse sometimes gives us null window because the active window is not 
			// created yet.
			// In this case, we just try to delay opening the database for 50 ms to allow
			// Eclipse creating the active window.
			Thread.sleep(50);
			sync.asyncExec(()-> addDatabase(shell, application.getSelectedElement(), partService, modelService, convertedPath));
		} else {
			addDatabase(shell, application.getSelectedElement(), partService, modelService, convertedPath);
		}
	}

	
	/****
	 * One-stop API to open and add a database. 
	 * This method shows a dialog box to pick a directory, check if the database already exists or not,
	 * create views and add to the list of the database collection.
	 * 
	 * @param shell the current shell
	 * @param window 
	 * 			The handle of the target window (may not be the current active one)
	 * @param service EPartService
	 * @param modelService EModelService
	 * @param database 
	 * 			The database object to be opened. This can be local or remote database.
	 */
	public void addDatabase(
			Shell shell, 
			MWindow         window,
			EPartService    service,
			EModelService 	modelService,
			IDatabase       database) {

		if ( database.getStatus() == DatabaseStatus.NOT_INITIALIZED &&
		   ( database.open(shell) != IDatabase.DatabaseStatus.OK) ) {
			
			return;
		}

		if (database.getExperimentObject() == null)
			return;

		// On Linux TWM window manager, the window may not be ready yet.
		sync.asyncExec(()-> 
			openDatabaseAndCreateViews(window, modelService, service, shell, database)
		);
	}


	/*****
	 * Add a new local database to the list
	 * 
	 * @param shell
	 * @param window
	 * @param service
	 * @param modelService
	 */
	public void addDatabase(
			Shell 			shell,
			MWindow         window,
			EPartService    service,
			EModelService 	modelService) {
		
		DatabaseLocal localDb = new DatabaseLocal();
		if (localDb.open(shell) == DatabaseStatus.OK)
			addDatabase(shell, window, service, modelService, localDb);
		
		if (localDb.getStatus() == DatabaseStatus.INEXISTENCE ||
			localDb.getStatus() == DatabaseStatus.INVALID     ||
			localDb.getStatus() == DatabaseStatus.UNKNOWN_ERROR )
			MessageDialog.openError(shell, "Unable to open the datbaase", localDb.getErrorMessage());
	}
	
	
	/*****
	 * Add a new database based from its Id. 
	 * The Id can be a path or a remote Id (not supported at the moments)
	 * 
	 * @param shell
	 * @param window
	 * @param service
	 * @param modelService
	 * @param databaseId
	 */
	public void addDatabase(
			Shell 			shell,
			MWindow         window,
			EPartService    service,
			EModelService 	modelService,
			String          databaseId) {

		if (databaseId == null) {
			addDatabase(shell, window, service, modelService);
			return;
		}
		if (!checkAndConfirmDatabaseExistence(shell, window, databaseId))			
			return;
		
		DatabaseLocal localDb = new DatabaseLocal();
		DatabaseStatus status = localDb.setDirectory(databaseId);
		
		if (status == DatabaseStatus.OK)
			addDatabase(shell, window, service, modelService, localDb);		
	}
	
	
	/****
	 * One-stop API to open a database. 
	 * This method shows a dialog box to pick a directory, check if the database already exists or not,
	 * create views and remove the existing databases before adding it to the list of the database collection.
	 * The removal is important to make sure there is only one database exist.
	 * 
	 * @param shell 
	 * 			the current shell
	 * @param window
	 * 			the current active hpcviewer window 
	 * @param service EPartService
	 * @param modelService EModelService
	 * @param database
	 */
	public void switchDatabase(
			Shell shell, 
			MWindow 	    window, 
			EPartService    service,
			EModelService 	modelService,
			String          databaseId) {

		if (!checkAndConfirmDatabaseExistence(shell, window, databaseId))
			return;
		
		// hack - hack -hack
		// we should make sure the database id is correct before removing all databases of this window
		removeWindow(window);
		
		addDatabase(shell, window, service, modelService, databaseId);		
	}
	
	
	/****
	 * Verify if a given database is already opened or not for this window.
	 * 
	 * @param shell
	 * @param window
	 * @param databaseId
	 * 
	 * @return {@code boolean} true if the database has already been loaded in this window
	 */
	public boolean checkAndConfirmDatabaseExistence(Shell shell, MWindow window, String databaseId) {

		var database = databaseWindowManager.getDatabase(window, databaseId); 
		if (database == null)
			return true;
		
		String msg = databaseId + ": The database already exists.\nDo you want to replace it?";
		return MessageDialog.openQuestion(shell, "Database already exists", msg);
	}
	
	
	/****
	 * Add a new database into the collection.
	 * This database can be remove later on by calling {@code removeLast}
	 * or {@code removeAll}.
	 * 
	 * @param experiment cannot be null
	 * @param window the main application
	 * @param service EPartService to create parts
	 * @param modelService
	 * @param parentId the parent EPartStack of the parts. If it's null, the new parts will be assigned to the current active
	 */
	private void createViewsAndAddDatabase(IDatabase     database, 
										  MWindow 	     window, 
										  EPartService   service,
										  EModelService  modelService,
										  String         message) {
		
		if (database == null || service == null) {
			MessageDialog.openError( Display.getDefault().getActiveShell(), 
									 "Error in opening the file", 
									 "Database not found. " );
			return;
		}
		
		// Corner case for TWM window manager: sometimes the processing is faster
		// than the UI, and thus Eclipse doesn't provide any context or any child
		// at this stage. Maybe we should wait until it's ready?
		// 
		// using asyncExec we hope Eclipse will delay the processing until the UI 
		// is ready. This doesn't guarantee anything, but works in most cases :-(

		sync.asyncExec(()-> {
			showPart(database, window, modelService,  message);
		});
	}
	
	
	/****
	 * The {@code Experiment} object version of {@link createViewsAndAddDatabase}
	 * 
	 * @param experiment
	 * 			an experiment from a local database.
	 * @param window
	 * @param service
	 * @param modelService
	 * @param message
	 */
	public void createViewsAndAddDatabase(
			IExperiment    experiment, 
			MWindow        window, 
			EPartService   service,
			EModelService  modelService,
			String         message) {
		
		var database = databaseWindowManager.getDatabase(window, experiment.getDirectory());
		
		createViewsAndAddDatabase(database, window, service, modelService, message);
	}
	

	/***
	 * The main method to display a profile part and prepare for the trace part 
	 * 
	 * @param database
	 * 			The database to be displayed (either local or remote)
	 * @param wj dkw[]
	 * @param service
	 * @param message
	 * 			The message to be displayed on the table
	 * 
	 * @return int
	 * 			positive if everything goes find. 
	 */
	private int showPart( IDatabase      database, 
						  MWindow        window, 
						  EModelService  modelService,
						  String         message) {

		//----------------------------------------------------------------
		// find an empty slot in the part stack
		// If no slot is available, we will try to create a new one.
		// However,creating a new part stack is tricky, and it's up to the
		// system where to locate the part stack.
		//----------------------------------------------------------------
		
		if (window == null) {
			// window is not active yet
			
			// using asyncExec we hope Eclipse will delay the processing until the UI 
			// is ready. This doesn't guarantee anything, but works in most cases :-(
			sync.asyncExec(()-> showPart(database, window, modelService, message));
			return -1;
		}

		MPartStack stack  = (MPartStack)modelService.find(STACK_ID_BASE, window);
		if (stack == null) {
			//----------------------------------------------------------------
			// create a new part stack if necessary
			// We don't want this, since it makes the layout weird.
			//----------------------------------------------------------------
			stack = modelService.createModelElement(MPartStack.class);
			stack.setElementId(STACK_ID_BASE);
			stack.setToBeRendered(true);
		}
		var list = stack.getChildren();
		
		// issue #284: use the context from the current window
		EPartService partService = window.getContext().get(EPartService.class);
		MPart part = null;
		
		try {
			part = partService.createPart(ProfilePart.ID);
		} catch (IllegalStateException e) {
			// issue #284: exception due to "no active window"
			// I don't know why, but perhaps it's because the rendering is not ready?
			// Recursively try to create again
			sync.asyncExec(()-> showPart(database, window, modelService, message));
			return -1;
		}
		if (list == null)
			throw new IllegalStateException("No child window detected");
		
		list.add(part);
		
		partService.showPart(part, PartState.VISIBLE);
		IMainPart view = null;

		int maxAttempt = 20;		
		while(maxAttempt>0) {
			view = (IMainPart) part.getObject();
			if (view != null)
				break;
			
			try {
				Thread.sleep(300);					
			} catch ( InterruptedException e) {
				// sleep is interrupted
				LoggerFactory.getLogger(getClass()).warn(e.getMessage(), e);
				Thread.currentThread().interrupt();
			}
			maxAttempt--;
		}
		if (view == null) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), 
									"Fail to get the view", 
									"hpcviewer is unable to render the profile view. Please try again");
			return 0;
		}
		
		// has to set the element Id before populating the view
		// this is to avoid an issue where a stack cannot store parts of the same elementId
		// If there are two parts have the same elementID, when one is moved to the same stack.
		// it will remove the other part.
		
		String elementID = "P." + ElementIdManager.getElementId(database);
		part.setElementId(elementID);

		var experiment = database.getExperimentObject();
		view.setInput(experiment);
				
		part.setLabel(ProfilePart.PREFIX_TITLE + experiment.getName());
		part.setTooltip(database.getId());


		//----------------------------------------------------------------
		// part stack is ready, now we create all view parts and add it to the part stack
		// We assume adding to the part stack is always successful
		//----------------------------------------------------------------
		stack.setVisible(true);
		stack.setOnTop(true);
		
		databaseWindowManager.addDatabase(window, database);

		//----------------------------------------------------------------
		// display the trace view if the information exists
		//----------------------------------------------------------------
		displayTraceView(experiment, partService, list);
		
		if (view instanceof ProfilePart) {
			ProfilePart activeView = (ProfilePart) view;
			if (message != null && !message.isEmpty()) {
				sync.asyncExec(()->{
					activeView.showWarning(message);
				});
			}		
		}
		return 1;
	}

	
	/****
	 * Method to create the trace view if necessary.
	 * 
	 * @param experiment
	 * @param service
	 * @param list
	 * 
	 * @return
	 */
	private int displayTraceView(IExperiment experiment, 
								 EPartService service,
								 List<MStackElement> list) {
		if (experiment.getTraceDataVersion() < 0) {
			return 0;
		}

		MPart tracePart  = service.createPart(TracePart.ID);
		MPart createPart = service.showPart(tracePart, PartState.CREATE);
		
		if (createPart != null) {
			// need to set the element id to avoid the same issue with the profile view
			// (see the comment at line 320-323)
			var id = "T." + ElementIdManager.getElementId(experiment);
			createPart.setElementId(id);

			list.add(createPart);
			
			Object objTracePart = createPart.getObject();
			if (objTracePart instanceof TracePart) {
				TracePart part = (TracePart) objTracePart;
				part.setInput(experiment);
				
				createPart.setLabel("Trace: " + experiment.getName());
				createPart.setTooltip(experiment.getDirectory());
			}
		}
		return 1;
	}
	
	
	/***
	 * Remove a database and all parts (views and editors) associated with it
	 * 
	 * @param application
	 * @param modelService
	 * @param partService
	 * @param database 
	 * 			The database to be removed. This can't be null.
	 */
	public void removeDatabase(MApplication application, 
							   EModelService modelService, 
							   EPartService partService, 
							   final IDatabase database) {
		
		if (database == null)
			return;

		var window = application.getSelectedElement();
		
		List<MPart> listParts = modelService.findElements(window, null, MPart.class);
		
		if (listParts == null)
			return;

		// first, notify all the parts that have experiment that they will be destroyed.
		
		ViewerDataEvent data = new ViewerDataEvent((Experiment) database.getExperimentObject(), null);
		if (eventBroker != null)
			eventBroker.send(BaseConstants.TOPIC_HPC_REMOVE_DATABASE, data);
		
		// destroy all the views and editors that belong to experiment
		// since Eclipse doesn't have "destroy" method, we hide them.
		
		for(MPart part: listParts) {
			Object obj = part.getObject();
			if (obj instanceof IMainPart) {
				IMainPart mpart = (IMainPart) obj;
				if (mpart.getExperiment() == database.getExperimentObject() ||
					mpart.getExperiment() == null) {
					partService.hidePart(part, true);
					mpart.dispose();
				}
			}
		}
		// remove any database associated with this experiment
		// some parts may need to check the database if the experiment really exits or not.
		// If not, they will consider the experiment will be removed.
		databaseWindowManager.removeDatabase(window, database);
	}	

	
	public void removeWindow(MWindow window) {
		databaseWindowManager.removeWindow(window);
	}
	
	
	public int getNumDatabase(MWindow window) {
		return databaseWindowManager.getNumDatabase(window);
	}
	
	
	public Iterator<IDatabase> getIterator(MWindow window) {
		return databaseWindowManager.getIterator(window);
	}
	
	
	/****
	 * Find a database for a given path
	 * 
	 * @param shell the active shell
	 * @param expManager the experiment manager
	 * @param directoryOrXMLFile path to the database
	 * @return
	 * @throws Exception 
	 */
	private String convertPathToLocalFileSystem(String directoryOrXMLFile)  {
		if (directoryOrXMLFile == null || directoryOrXMLFile.isEmpty())
			return null;
		
    	IFileStore fileStore;

		try {
			fileStore = EFS.getLocalFileSystem().getStore(new URI(directoryOrXMLFile));
			
		} catch (URISyntaxException e) {
			// somehow, URI may throw an exception for certain schemes. 
			// in this case, let's do it traditional way
			fileStore = EFS.getLocalFileSystem().getStore(new Path(directoryOrXMLFile));
			statusReporter.warn("Unable to locate " + directoryOrXMLFile, e);
		}
    	IFileInfo objFileInfo = fileStore.fetchInfo();

    	if (!objFileInfo.exists())
    		return null;

    	
    	if (objFileInfo.isDirectory()) {
    		return objFileInfo.getName();
    	} 
		fileStore = EFS.getLocalFileSystem().fromLocalFile(new File(directoryOrXMLFile));
    	return fileStore.getName();
	}
	

	/****
	 * Open a database using background job, and then create views using UI thread
	 *
	 * @param window
	 * @param modelService
	 * @param partService
	 * @param shell
	 * @param xmlFileOrDirectory
	 */
	public void openDatabaseAndCreateViews( final MWindow window,
											final EModelService modelService,
											final EPartService partService,
											final Shell shell, 
											final IDatabase database) {
		final Experiment experiment;
		if (this.statusReporter == null)
			this.statusReporter = LoggerFactory.getLogger(getClass());
		try {
			experiment = (Experiment) database.getExperimentObject();
			if (experiment == null) {
				return;
			}
			String message = null;
			
			// filter the tree if user has defined at least a filter
			// if we elide some nodes, we should notify the user
			FilterMap filterMap = FilterMap.getInstance();
			if (filterMap.isFilterEnabled()) {
				int numFilteredNodes = experiment.filter(filterMap, false);
				if (numFilteredNodes > 0) {
					message = showFilterMessage(shell, numFilteredNodes);
				}
			}
			
			// Everything works just fine: create views
			createViewsAndAddDatabase(database, window, partService, modelService, message);

		} catch (InvalExperimentException ei) {
			final String msg = "Invalid database " + database.getId() + "\nError at line " + ei.getLineNumber();
			statusReporter.error(msg, ei);
			MessageDialog.openError(shell, PREFIX_ERROR + ei.getClass(), msg);
			return;

		} catch (NullPointerException enpe) {
			final String msg = database.getId() + ": Empty or corrupt database";
			statusReporter.error(msg, enpe);
			MessageDialog.openError(shell, PREFIX_ERROR + enpe.getClass(), msg);
			return;
			
		} catch (Exception e) {
			final String msg = "Error opening the database " + database.getId() + ":\n  " + e.getMessage();
			statusReporter.error(msg, e);
			MessageDialog.openError(shell, PREFIX_ERROR + e.getClass(), msg);
			return;
		}

		// store the current loaded database to history
		// we need to ensure we only store the directory, not the xml file
		// minor fix: only store the absolute path, not the relative one.
		
		UserInputHistory history = new UserInputHistory(RecentDatabase.HISTORY_DATABASE_RECENT);
		history.addLine(database.getId());
	}

	
	private String showFilterMessage(Shell shell, int numFilteredNodes) throws IOException {
		final String unit = numFilteredNodes == 1 ? " node has " : " nodes have ";
		final String filterKey = "filterMessage";
		final String message   = "CCT node Filter is enabled and at least " + 
				 				 numFilteredNodes + unit + "been elided.";
		
		var prefStore  = ViewerPreferenceManager.INSTANCE.getPreferenceStore();
		var checkValue = prefStore.getBoolean(filterKey);
		if (checkValue)
			return message;
		
		var dlg = MessageDialogWithToggle.openWarning(shell,  
											"Filter is enabled", 
											"CCT node Filter is enabled and at least " + 
													 numFilteredNodes + unit + "been elided.", 
											"Do not show this message next time", 
											checkValue, 
											prefStore, 
											filterKey);
		checkValue = dlg.getToggleState();
		prefStore.putValue(filterKey, String.valueOf(checkValue));
		prefStore.save();
		
		return null;
	}


	public boolean isEmpty(MWindow window) {
		return databaseWindowManager.isEmpty(window);
	}
}
