package edu.rice.cs.hpcviewer.ui.addon;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import edu.rice.cs.hpcbase.ViewerDataEvent;
import edu.rice.cs.hpcbase.map.UserInputHistory;
import edu.rice.cs.hpcbase.ui.IMainPart;
import edu.rice.cs.hpcdata.db.DatabaseManager;
import edu.rice.cs.hpcdata.experiment.BaseExperiment;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.experiment.InvalExperimentException;
import edu.rice.cs.hpcfilter.service.FilterMap;
import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceManager;
import edu.rice.cs.hpctraceviewer.data.local.LocalDBOpener;
import edu.rice.cs.hpctraceviewer.ui.TracePart;
import edu.rice.cs.hpcviewer.ui.ProfilePart;
import edu.rice.cs.hpcviewer.ui.experiment.ExperimentManager;
import edu.rice.cs.hpcviewer.ui.handlers.RecentDatabase;
import edu.rice.cs.hpcviewer.ui.util.ElementIdManager;


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
	
	private final HashMap<MWindow, List<IExperiment>>   mapWindowToExperiments;
	
	private @Inject IEventBroker eventBroker;
	private @Inject UISynchronize sync;

	private ExperimentManager    experimentManager;
	private Logger statusReporter;
		
	public DatabaseCollection() {
		experimentManager = new ExperimentManager();
		mapWindowToExperiments = new HashMap<>(1);
	}
	
	@Inject
	@Optional
	private void subscribeApplicationCompleted(
			@UIEventTopic(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE) 
			final MApplication   application,
			final EPartService   partService,
			final IEventBroker   broker,
			final EModelService  modelService, 
			@Named(IServiceConstants.ACTIVE_SHELL) Shell myShell) {
		
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
		
		for (String arg: args) {
			if (arg.charAt(0) != '-')
				path = arg;
		}
		String filename = checkExistance(application.getSelectedElement(), shell, path);
		if (filename == null)
			return; 
		
		// On Linux TWM window manager, the window may not be ready yet.
		sync.asyncExec(()-> {
			openDatabaseAndCreateViews(application, modelService, partService, shell, filename);
		});
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
	 * @param database directory
	 */
	public void addDatabase(Shell shell, 
			MApplication 	application, 
			MWindow         window,
			EPartService    service,
			EModelService 	modelService,
			String          database) {
		
		String filename = checkExistance(window, shell, database);
		if (filename == null)
			return;
		
		var exp = getExperimentObject(window, filename);
		if (exp != null) {
			removeDatabase(application, modelService, service, exp);
		}
		
		openDatabaseAndCreateViews(application, modelService, service, shell, filename);
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
	 * @param database
	 */
	public void switchDatabase(Shell shell, 
			MApplication 	application, 
			EPartService    service,
			EModelService 	modelService,
			String          database) {
		
		String filename = checkExistance(application.getSelectedElement(), shell, database);
		if (filename == null)
			return;

		removeAll(application, modelService, service);
		
		openDatabaseAndCreateViews(application, modelService, service, shell, filename);
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
	public void createViewsAndAddDatabase(IExperiment experiment, 
										  MApplication 	 application, 
										  EPartService   service,
										  EModelService  modelService,
										  String         message) {
		
		if (experiment == null || service == null) {
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
			showPart(experiment, application, modelService,  message);
		});
	}
	

	/***
	 * Display a profile part 
	 * 
	 * @param experiment
	 * @param application
	 * @param service
	 * @param message
	 * 
	 * @return int
	 */
	private int showPart( IExperiment    experiment, 
						  MApplication   application, 
						  EModelService  modelService,
						  String         message) {

		//----------------------------------------------------------------
		// find an empty slot in the part stack
		// If no slot is available, we will try to create a new one.
		// However,creating a new part stack is tricky, and it's up to the
		// system where to locate the part stack.
		//----------------------------------------------------------------
		
		MWindow  window = application.getSelectedElement();
		
		if (window == null) {
			// window is not active yet
			
			// using asyncExec we hope Eclipse will delay the processing until the UI 
			// is ready. This doesn't guarantee anything, but works in most cases :-(
			sync.asyncExec(()-> showPart(experiment, application, modelService, message));
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
			sync.asyncExec(()-> showPart(experiment, application, modelService, message));
			return -1;
		}
		if (list != null)
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
			} catch (Exception e) {
				// sleep is interrupted
				// no op
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
		
		String elementID = "P." + ElementIdManager.getElementId(experiment);
		part.setElementId(elementID);

		view.setInput(part, experiment);

		//----------------------------------------------------------------
		// part stack is ready, now we create all view parts and add it to the part stack
		// We assume adding to the part stack is always successful
		//----------------------------------------------------------------
		stack.setVisible(true);
		stack.setOnTop(true);
		
		List<IExperiment> listExperiments = getActiveListExperiments(application.getSelectedElement());
		listExperiments.add(experiment);

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
	 * 
	 * @param experiment
	 * @param service
	 * @param elementID
	 * @param list
	 * @return
	 */
	private int displayTraceView(IExperiment experiment, 
								 EPartService service,
								 List<MStackElement> list) {
		if (LocalDBOpener.directoryHasTraceData(experiment.getDirectory()) < 0) {
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
			if (objTracePart != null) {
				((TracePart)objTracePart).setInput(createPart, experiment);
			}
		}
		return 1;
	}
		
	/***
	 * Retrieve the iterator of the database collection from a given windo
	 * 
	 * @param window 
	 * @return Iterator for the list of the given window
	 */
	public Iterator<IExperiment> getIterator(MWindow window) {
		 var list = mapWindowToExperiments.get(window);
		if (list == null)
			return null;
		return list.iterator();
	}
	
		
	/***
	 * Retrieve the current registered databases
	 * @return
	 */
	public int getNumDatabase(MWindow window) {
		var list = getActiveListExperiments(window);
		return list.size();
	}
	
	
	/***
	 * Check if the database is empty or not
	 * @return true if the database is empty
	 */
	public boolean isEmpty(MWindow window) {
		var list = getActiveListExperiments(window);		
		return list.isEmpty();
	}
	
	
	/***
	 * Retrieve the experiment object given a XML file path
	 * 
	 * @param pathXML the absolute path of the experiment XNK file path
	 * @return BaseExperiment object if the database exist, null otherwise.
	 */
	public IExperiment getExperimentObject(MWindow window, String pathXML) {
		var list = getActiveListExperiments(window);
		
		if (list.isEmpty())
			return null;
		
		for (var exp: list) {
			String directory = exp.getPath();
			if (directory.equals(pathXML)) {
				return exp;
			}
		}
		return null;
	}
	
	/***
	 * Check if a database is good or already exist in the collection.
	 * <ul>
	 * <li>If it's empty: show a window to pick a database
	 * <li>If it's a directory, add the database filename
	 * <li>If it's a file: check if exist or not.
	 * </ul>

	 * @param shell
	 * @param pathXML the absolute path to XML file
	 * @return {@code String} database file path
	 */
	public String checkExistance(MWindow window, Shell shell, String fileOrDirectory) {
		
		// -------------------------------------------------------
		// 1. convert the database path to experiment.xml file name
		// -------------------------------------------------------
		String filename = null;
		if (fileOrDirectory == null) {
			try {
				filename = experimentManager.openFileExperiment(shell);
			} catch (Exception e) {
				MessageDialog.openError(shell, "File to open the database", e.getMessage());
				return null;
			}
		} else {
			if (Files.isRegularFile(Paths.get(fileOrDirectory))) {
				filename = fileOrDirectory;
			} else {
				var filepath = DatabaseManager.getDatabaseFilePath(fileOrDirectory);
				if (filepath.isEmpty()) {
					String files = DatabaseManager.getDatabaseFilenames(java.util.Optional.empty());
					MessageDialog.openError(shell, 
											"Error opening a database", 
											"Directory has no database: " + fileOrDirectory +
											"\nRecognized database files:\n" + files );
					return null;				
				}
				filename = filepath.get();
			}
		}

		// -------------------------------------------------------
		// 2. check if the file exists
		// -------------------------------------------------------
		if (filename == null)
			return null;
		
		File file = new File(filename);
		if (!file.exists()) {
			MessageDialog.openError(shell, 
					"Fail to open a database", 
					filename + ": file not found");
			return null;
		}

		// -------------------------------------------------------
		// 3. check if the database already opened or not
		// -------------------------------------------------------
		
		var exp = getExperimentObject(window, filename);
		if (exp == null)
			// database is valid, fresh and not already opened
			return filename;
		
		// we cannot have two exactly the same database in one window
		if (MessageDialog.openQuestion(shell, 
								   "Warning: database already exists", 
								   exp.getDirectory() +
								   ": the database is already opened.\nDo you want to replace the existing one?" ) )
		
			// user decides to replace the database
			return filename;

		// we give up
		return null;
	}
	
	
	/***
	 * Remove a database and all parts (views and editors) associated with it
	 * 
	 * @param experiment
	 */
	public void removeDatabase(MApplication application, 
							   EModelService modelService, 
							   EPartService partService, 
							   final IExperiment experiment) {
		
		if (experiment == null)
			return;
		
		// remove any database associated with this experiment
		// some parts may need to check the database if the experiment really exits or not.
		// If not, they will consider the experiment will be removed.
		var list = getActiveListExperiments(application.getSelectedElement());
		list.remove(experiment);
		
		MWindow window = application.getSelectedElement();
		List<MPart> listParts = modelService.findElements(window, null, MPart.class);
		
		if (listParts == null)
			return;

		// first, notify all the parts that have experiment that they will be destroyed.
		
		ViewerDataEvent data = new ViewerDataEvent((Experiment) experiment, null);
		if (eventBroker != null)
			eventBroker.send(BaseConstants.TOPIC_HPC_REMOVE_DATABASE, data);
		
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
		experiment.dispose();
	}	
	
	
	/****
	 * Remove all databases
	 * @return
	 */
	public int removeAll(MApplication application, EModelService modelService, EPartService partService) {
		var list = getActiveListExperiments(application.getSelectedElement());		
		int size = list.size();
		
		// TODO: ugly solution to avoid concurrency (java will not allow to remove a list while iterating).
		// we need to copy the list to an array, and then remove the list
		
		BaseExperiment[] arrayExp = new BaseExperiment[size];
		list.toArray(arrayExp);
		
		for(BaseExperiment exp: arrayExp) {
			// inside this method, we remove the database AND hide the view parts
			removeDatabase(application, modelService, partService, exp);
		}
		list.clear();
		
		return size;
	}
	
	
	
	
	/****
	 * Remove a window 
	 * @param window
	 * @return
	 */
	public List<IExperiment> removeWindowExperiment(MWindow window) {
		return mapWindowToExperiments.remove(window);
	}
	
	
	/***
	 * Retrieve the list of experiments of the current window.
	 * If Eclipse reports there is no active window, the list is null.
	 * 
	 * @return the list of experiments (if there's an active window). 
	 * 		   empty list otherwise.
	 * 
	 */
	private List<IExperiment> getActiveListExperiments(MWindow window) {

		if (window == null) {
			return Collections.emptyList();
		}
		return mapWindowToExperiments.computeIfAbsent(window, key -> new ArrayList<>());
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
	private IExperiment openDatabase(Shell shell, String directoryOrXMLFile) throws Exception {
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

    	IExperiment experiment = null;
    	
    	if (objFileInfo.isDirectory()) {
    		experiment = experimentManager.openDatabaseFromDirectory(shell, directoryOrXMLFile);
    	} else {
			EFS.getLocalFileSystem().fromLocalFile(new File(directoryOrXMLFile));
			experiment = experimentManager.loadExperiment(directoryOrXMLFile);
    	}
    	return experiment;
	}
	

	/****
	 * Open a database using background job, and then create views using UI thread
	 *
	 * @param application
	 * @param modelService
	 * @param partService
	 * @param shell
	 * @param xmlFileOrDirectory
	 */
	public void openDatabaseAndCreateViews(final MApplication application,
											final EModelService modelService,
											final EPartService partService,
											final Shell shell, 
											final String xmlFileOrDirectory) {
		final Experiment experiment;
		if (this.statusReporter == null)
			this.statusReporter = LoggerFactory.getLogger(getClass());
		try {
			experiment = (Experiment) openDatabase(shell, xmlFileOrDirectory);
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
			createViewsAndAddDatabase(experiment, application, partService, modelService, message);

		} catch (InvalExperimentException ei) {
			final String msg = "Invalid database " + xmlFileOrDirectory + "\nError at line " + ei.getLineNumber();
			statusReporter.error(msg, ei);
			MessageDialog.openError(shell, PREFIX_ERROR + ei.getClass(), msg);
			return;

		} catch (NullPointerException enpe) {
			final String msg = xmlFileOrDirectory + ": Empty or corrupt database";
			statusReporter.error(msg, enpe);
			MessageDialog.openError(shell, PREFIX_ERROR + enpe.getClass(), msg);
			return;
			
		} catch (Exception e) {
			final String msg = "Error opening the database " + xmlFileOrDirectory + ":\n  " + e.getMessage();
			statusReporter.error(msg, e);
			MessageDialog.openError(shell, PREFIX_ERROR + e.getClass(), msg);
			return;
		}

		// store the current loaded database to history
		// we need to ensure we only store the directory, not the xml file
		// minor fix: only store the absolute path, not the relative one.
		
		String path = experiment.getDirectory();
		UserInputHistory history = new UserInputHistory(RecentDatabase.HISTORY_DATABASE_RECENT);
		history.addLine(path);
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
}
