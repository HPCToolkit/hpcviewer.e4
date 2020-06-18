package edu.rice.cs.hpcviewer.ui.experiment;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpcviewer.ui.internal.ViewerDataEvent;
import edu.rice.cs.hpcviewer.ui.parts.Datacentric;
import edu.rice.cs.hpcviewer.ui.parts.IViewPart;
import edu.rice.cs.hpcviewer.ui.parts.bottomup.BottomUpPart;
import edu.rice.cs.hpcviewer.ui.parts.flat.FlatPart;
import edu.rice.cs.hpcviewer.ui.parts.topdown.TopDownPart;
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
	static private final String STACK_ID_BASE 	  = "edu.rice.cs.hpcviewer.ui.partstack.lower.";
	
	static private final int MAX_STACKS_AVAIL = 3;
	
	final private ConcurrentLinkedQueue<BaseExperiment>    queueExperiment;
	final private HashMap<BaseExperiment, ViewerDataEvent> mapColumnStatus;
	final private HashMap<RootScopeType, String> 		   mapRoottypeToPartId;
	
	private EPartService partService;
	
	public DatabaseCollection() {
		queueExperiment = new ConcurrentLinkedQueue<>();
		mapColumnStatus = new HashMap<BaseExperiment, ViewerDataEvent>();

		mapRoottypeToPartId = new HashMap<RootScopeType, String>();

		mapRoottypeToPartId.put(RootScopeType.CallingContextTree, TopDownPart.IDdesc);
		mapRoottypeToPartId.put(RootScopeType.CallerTree, 	 	  BottomUpPart.IDdesc);
		mapRoottypeToPartId.put(RootScopeType.Flat, 		   	  FlatPart.IDdesc);
		mapRoottypeToPartId.put(RootScopeType.DatacentricTree, 	  Datacentric.IDdesc);
	}
	
	@Inject
	@Optional
	private void subscribeApplicationCompleted(
			@UIEventTopic(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE) final MApplication application,
			final EPartService partService,
			final IEventBroker broker,
			final EModelService modelService,
			final IWorkbench workbench) {
		
		this.partService = partService;
		
		// handling the command line arguments:
		// if one of the arguments specify a file or a directory,
		// try to find the experiment.xml and load it.
		
		String args[] = Platform.getApplicationArgs();
		
		Display display = Display.getCurrent();
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
			experiment = openDatabase(myShell, expManager, path);
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
		
		if (service == null) {
			System.out.println("Error: service is not available");
			return;
		}
		IEclipseContext activeWindowContext = application.getContext().getActiveChild();
		if (activeWindowContext == null) {
			Display display = Display.getCurrent();
			MessageDialog.openError(display.getActiveShell(), "Error", 
					"Cannot find an active window with this platform.\n" +
					"Please open a database from the File-Open menu.");
			return;
		}

		//----------------------------------------------------------------
		// find an empty slot in the part stack
		// If no slot is available, we will try to create a new one.
		// However,creating a new part stack is tricky, and it's up to the
		// system where to locate the part stack.
		//----------------------------------------------------------------
		
		MPartStack stack = null;
		List<MStackElement> list = null;
		
		for(int i=1; i<=MAX_STACKS_AVAIL; i++) {
			final String stackId = STACK_ID_BASE + String.valueOf(i) ;
			stack  = (MPartStack)modelService.find(stackId , application);
			
			if (stack == null)
				System.err.println("list of parts is null");
			else
				list = stack.getChildren();
			if (list != null && list.size()==0)
				// we found empty an stack
				break; 
		}
		
		//----------------------------------------------------------------
		// create a new part stack if necessary
		// We don't want this, since it makes the layout weird.
		//----------------------------------------------------------------
		if (stack == null) {
			System.out.println("create a new part stack");
			
			stack = modelService.createModelElement(MPartStack.class);
			stack.setElementId(STACK_ID_BASE  + "1");
			stack.setToBeRendered(true);
		}
		
		//----------------------------------------------------------------
		// part stack is ready, now we create all view parts and add it to the part stack
		// TODO: We assume adding to the part stack is always successful
		//----------------------------------------------------------------
		stack.setVisible(true);
		stack.setOnTop(true);
		
		Object []children = experiment.getRootScopeChildren();
		
		for (int i=0; i<children.length; i++) {

			RootScope root = (RootScope) children[i];
			
			String partId = mapRoottypeToPartId.get(root.getType());
			if (partId == null)
				continue; 	// TODO: should display error message
			
			final MPart part = service.createPart(partId);
			
			list.add(part);

			part.setLabel(root.getRootName());

			//----------------------------------------------------------------
			// We only make the top-down (the first part) to be visible
			// the other parts will be created, but not activated.
			// Let users to activate the other parts by themselves.
			//----------------------------------------------------------------
			if (i==0) {
				
				service.showPart(part, PartState.VISIBLE);
			} else {
				
				service.showPart(part, PartState.CREATE);
			}			
			IViewPart view = null;
			int maxAttempt = 10;
			
			while(maxAttempt>0) {
				view = (IViewPart) part.getObject();
				if (view != null)
					break;
				
				try {
					Thread.sleep(300);					
				} catch (Exception e) {
					System.out.println("thread is interrupted");
				}
				maxAttempt--;
			}
			// has to set the element Id before populating the view
			String elementID = ElementIdManager.getElementId(root);
			part.setElementId(elementID);

			view.setInput(part, experiment);
		}
		
		queueExperiment.add(experiment);
	}
	
	/***
	 * Retrieve the iterator of the database collection
	 * 
	 * @return Iterator for the list
	 */
	public Iterator<BaseExperiment> getIterator() {
		return queueExperiment.iterator();
	}
	
	/***
	 * Retrieve the current registered databases
	 * @return
	 */
	public int getNumDatabase() {
		return queueExperiment.size();
	}
	
	/***
	 * Check if the database is empty or not
	 * @return true if the database is empty
	 */
	public boolean isEmpty() {
		return queueExperiment.isEmpty();
	}
	
	/***
	 * Remove the last registered database
	 * @return
	 */
	public BaseExperiment getLast() {
		return queueExperiment.element();
	}
	
	/****
	 * Remove all databases
	 * @return
	 */
	public int removeAll() {
		int size = queueExperiment.size();
		
		Iterator<BaseExperiment> iterator = queueExperiment.iterator();
		while(iterator.hasNext()) {
			BaseExperiment exp = iterator.next();
			
			
			removeDatabase(exp);
		}
		
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
	
	public void removeDatabase(final BaseExperiment experiment) {
		
		final Collection<MPart> listParts = partService.getParts();
		if (listParts == null)
			return;
		
		String elementID = ElementIdManager.getElementId(experiment);
		
		for(MPart part: listParts) {
			
			if (part.getElementId().startsWith(elementID)) {
				partService.hidePart(part, true);
			}
		}
		queueExperiment.remove(experiment);
		mapColumnStatus.remove(experiment);
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
