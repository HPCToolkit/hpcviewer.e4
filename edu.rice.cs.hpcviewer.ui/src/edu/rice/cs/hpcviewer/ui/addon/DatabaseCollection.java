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
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
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
import edu.rice.cs.hpcbase.IDatabase;
import edu.rice.cs.hpcbase.IDatabase.DatabaseStatus;
import edu.rice.cs.hpcbase.ViewerDataEvent;
import edu.rice.cs.hpcbase.map.UserInputHistory;
import edu.rice.cs.hpcbase.ui.IMainPart;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.InvalExperimentException;
import edu.rice.cs.hpcfilter.service.FilterMap;
import edu.rice.cs.hpclocal.DatabaseLocal;
import edu.rice.cs.hpcremote.data.DatabaseRemote;
import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceManager;
import edu.rice.cs.hpctraceviewer.ui.TracePart;
import edu.rice.cs.hpcviewer.ui.ProfilePart;
import edu.rice.cs.hpcviewer.ui.handlers.RecentDatabase;
import edu.rice.cs.hpcviewer.ui.internal.DatabaseWindowManager;
import edu.rice.cs.hpcviewer.ui.internal.DatabaseWindowManager.DatabaseExistence;


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
	private MApplication application;

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
			@Named(IServiceConstants.ACTIVE_SHELL) Shell myShell) throws InterruptedException, CoreException, URISyntaxException {
		
		this.eventBroker    = broker;
		this.application    = application;
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
		if (convertedPath == null && path != null)
			MessageDialog.openError(
					shell, 
					"Fail top open the database", 
					path + " is not found.\nTry to specify with the absolute full path.");
		
		var window = application.getSelectedElement();
		if (window == null) {
			// Damn Eclipse sometimes gives us null window because the active window is not 
			// created yet.
			// In this case, we just try to delay opening the database for 50 ms to allow
			// Eclipse creating the active window.
			Thread.sleep(50);
			sync.asyncExec(()-> addDatabase(shell, application.getSelectedElement(), partService, modelService, convertedPath));
		} else {
			addDatabase(shell, window, partService, modelService, convertedPath);
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
	public DatabaseStatus addDatabase(
			Shell shell, 
			MWindow         window,
			EPartService    service,
			EModelService 	modelService,
			IDatabase       database) {
		
		var databaseId = database.getId();
		var dbExistence = databaseWindowManager.checkAndConfirmDatabaseExistence(shell, window, databaseId);
		if (dbExistence == DatabaseExistence.EXIST_CANCEL)		
			return DatabaseStatus.CANCEL;

		if ( database.getStatus() == DatabaseStatus.NOT_INITIALIZED &&
		   ( database.open(shell) != IDatabase.DatabaseStatus.OK) ) {
			// cannot open the database
			// should we log it?
			return database.getStatus();
		}

		if (database.getExperimentObject() == null)
			return DatabaseStatus.INEXISTENCE;

		var status = database.getStatus();
		if (status == DatabaseStatus.OK) {
			var currentDatabase   = databaseWindowManager.getDatabase(window, databaseId);

			// On Linux TWM window manager, the window may not be ready yet.
			openDatabaseAndCreateViews(window, modelService, service, shell, database);

			if (!currentDatabase.isEmpty() && dbExistence == DatabaseExistence.EXIST_REPLACE)
				for(var db: currentDatabase)
					removeDatabase(window, modelService, service, db);
		}
		
		return DatabaseStatus.OK;
	}


	/*****
	 * Add a new local database to the list
	 * 
	 * @param shell
	 * @param window
	 * @param service
	 * @param modelService
	 */
	public DatabaseStatus addDatabase(
			Shell 			shell,
			MWindow         window,
			EPartService    service,
			EModelService 	modelService) {
		
		DatabaseLocal localDb = new DatabaseLocal();
		if (localDb.open(shell) == DatabaseStatus.OK) {
			addDatabase(shell, window, service, modelService, localDb);
		}
		if (localDb.getStatus() == DatabaseStatus.INEXISTENCE ||
			localDb.getStatus() == DatabaseStatus.INVALID     ||
			localDb.getStatus() == DatabaseStatus.UNKNOWN_ERROR )
			MessageDialog.openError(shell, "Unable to open the datbaase", localDb.getErrorMessage());
		
		return localDb.getStatus();
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
	public DatabaseStatus addDatabase(
			Shell 			shell,
			MWindow         window,
			EPartService    service,
			EModelService 	modelService,
			String          databaseId) {

		if (databaseId == null) {
			return addDatabase(shell, window, service, modelService);
		}

		IDatabase database;
		
		if (isRemote(databaseId)) {
			database = new DatabaseRemote();
			database.open(shell);
		} else { 
			database = new DatabaseLocal();
			((DatabaseLocal) database).setDirectory(databaseId);
		}
		return addDatabase(shell, window, service, modelService, database);
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
	 * @param databaseId
	 * 			The database unique id, can be remote or local 
	 */
	public void switchDatabase(
			Shell shell, 
			MWindow 	    window, 
			EPartService    service,
			EModelService 	modelService,
			String          databaseId) {

		IDatabase database;
		DatabaseStatus status;
		String dbId = databaseId;

		if (dbId == null) {
			// open a new database file
			// at the moment only support local database
			database = new DatabaseLocal();
			status = ((DatabaseLocal) database).open(shell);
			if (status == DatabaseStatus.CANCEL)
				return;
			dbId = database.getId();
		}
		if (databaseWindowManager.checkAndConfirmDatabaseExistence(shell, window, dbId) == DatabaseExistence.EXIST_CANCEL )
			return;		
		
		if (isRemote(dbId)) {
			database = new DatabaseRemote();
			status = database.open(shell);
		} else {
			database = new DatabaseLocal();
			status = ((DatabaseLocal) database).setDirectory(dbId);
		}
		
		if (status == DatabaseStatus.CANCEL) {
			// should we notify the user?
		} else if (status == DatabaseStatus.OK) {
			removeAllDatabases(window, modelService, service);
			openDatabaseAndCreateViews(window, modelService, service, shell, database);

		} else {
			MessageDialog.openError(shell, "Unable to open the database", dbId + ": not a valid database");
		}
	}
	
	
	/****
	 * Check if the id is for remote or not
	 * 
	 * @param databaseId
	 * 			The database id
	 * 
	 * @return {@code boolean} true if it's a remote database 
	 * 
	 */
	private boolean isRemote(String databaseId) {
		var colon = databaseId.indexOf(':');
		var slash = databaseId.indexOf('/');
		if (colon >= slash)
			return false;
		var port  = databaseId.substring(colon+1, slash);
		
		if (colon <= 0 && slash < 1 || port.isEmpty())
			return false;
		
		for(int i=0; i<port.length(); i++) {
			char c = port.charAt(i);
			if (c < '0' || c > '9')
				return false;
		}
		return true;
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
			sync.asyncExec(()-> showPart(database, application.getSelectedElement(), modelService, message));
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
		
		String elementID = "P." + database.getId();
		part.setElementId(elementID);

		view.setInput(database);
				
		var experiment = database.getExperimentObject();
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
		displayTraceView(database, partService, list);
		
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
	private int displayTraceView(IDatabase database, 
								 EPartService service,
								 List<MStackElement> list) {
		if (!database.hasTraceData()) {
			return 0;
		}

		MPart tracePart  = service.createPart(TracePart.ID);
		MPart createPart = service.showPart(tracePart, PartState.CREATE);
		
		if (createPart != null) {
			// need to set the element id to avoid the same issue with the profile view
			// (see the comment at line 320-323)
			var id = "T." + database.getId();
			createPart.setElementId(id);

			list.add(createPart);
			
			Object objTracePart = createPart.getObject();
			if (objTracePart instanceof TracePart) {

				TracePart part = (TracePart) objTracePart;
				part.setInput(database);
				
				var experiment = database.getExperimentObject();
				
				createPart.setLabel("Trace: " + experiment.getName());
				createPart.setTooltip(database.getId());
			}
		}
		return 1;
	}
	
	
	/***
	 * Remove a database and all parts (views and editors) associated with it
	 * 
	 * @param window
	 * @param modelService
	 * @param partService
	 * @param database 
	 * 			The database to be removed. This can't be null.
	 */
	public void removeDatabase(MWindow window, 
							   EModelService modelService, 
							   EPartService partService, 
							   final IDatabase database) {
		
		if (database == null)
			return;

		closeParts(window, modelService, partService, database);
		
		// remove any database associated with this experiment
		// some parts may need to check the database if the experiment really exits or not.
		// If not, they will consider the experiment will be removed.
		databaseWindowManager.removeDatabase(window, database);
	}
	
	
	/***
	 * Close the views of a given database
	 * 
	 * @param window
	 * @param modelService
	 * @param partService
	 * @param database
	 */
	private void closeParts(MWindow window, 
			   EModelService modelService, 
			   EPartService partService, 
			   final IDatabase database) {
		
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
				if (mpart.getInput() == database ||
					mpart.getInput() == null     || 
					mpart.getExperiment() == null) {
					
					partService.hidePart(part, true);
					mpart.dispose();
				}
			}
		}
	}
	
	
	/****
	 * Remove all of databases of a specified window
	 * 
	 * @param window
	 * @param modelService
	 * @param partService
	 */
	public void removeAllDatabases(
			MWindow window, 
			EModelService modelService, 
			EPartService partService ) {

		var iterator = databaseWindowManager.getIterator(window);
		while (iterator.hasNext()) {
			var database = iterator.next();
			closeParts(window, modelService, partService, database);
			iterator.remove();
		}
	}


	/****
	 * Get the number of databases of a given window
	 * @param window
	 * @return
	 */
	public int getNumDatabase(MWindow window) {
		return databaseWindowManager.getNumDatabase(window);
	}
	
	
	/****
	 * Get the iterator of the set of databases of a given window
	 *  
	 * @param window
	 * @return
	 */
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
	 * @throws CoreException 
	 * @throws URISyntaxException 
	 */
	private String convertPathToLocalFileSystem(String directoryOrXMLFile) {
		if (directoryOrXMLFile == null || directoryOrXMLFile.isEmpty())
			return null;
		
    	var objFileInfo = new File(directoryOrXMLFile);
    	if (!objFileInfo.exists()) {
        	IFileStore fileStore = null;
    		try {
    			// first try with the URI format
				var uri = new URI(directoryOrXMLFile);
				fileStore = EFS.getLocalFileSystem().getStore(uri);
			} catch (URISyntaxException e) {
				// second, try with the Eclipse's path
				var path = new Path(directoryOrXMLFile);
				fileStore = EFS.getLocalFileSystem().getStore(path);
			}
    		if (fileStore == null || !fileStore.fetchInfo().exists())
    			return null;
    		
			objFileInfo = new File(fileStore.fetchInfo().getName());
    	}
    	
    	if (objFileInfo.isDirectory()) {
    		return objFileInfo.getAbsolutePath();
    	} 
    	return objFileInfo.getParent();
	}
	

	/****
	 * Open a database using background job, and then create views using UI thread
	 *
	 * @param window
	 * @param modelService
	 * @param partService
	 * @param shell
	 * @param database
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
		Experiment exp = (Experiment) database.getExperimentObject();
		if (!exp.isMergedDatabase()) {
			UserInputHistory history = new UserInputHistory(RecentDatabase.HISTORY_DATABASE_RECENT);
			history.addLine(database.getId());
		}
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
