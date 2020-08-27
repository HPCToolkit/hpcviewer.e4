package edu.rice.cs.hpcviewer.ui.parts.thread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpc.filter.service.FilterStateProvider;
import edu.rice.cs.hpcviewer.ui.ProfilePart;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.base.IViewItem;
import edu.rice.cs.hpcviewer.ui.dialogs.ThreadFilterDialog;
import edu.rice.cs.hpcviewer.ui.internal.ScopeTreeViewer;
import edu.rice.cs.hpcviewer.ui.internal.ViewerDataEvent;
import edu.rice.cs.hpcviewer.ui.tabItems.AbstractBaseViewItem;
import edu.rice.cs.hpcviewer.ui.util.FilterDataItem;

/*************************************************************
 * 
 * View part to display CCT and metrics for a specific set of threads 
 *
 *************************************************************/
public class ThreadView extends AbstractBaseViewItem implements IViewItem, EventHandler
{
	private EPartService  partService;	
	private IEventBroker  eventBroker;
	private EMenuService  menuService;
	
	private DatabaseCollection databaseAddOn;
	private ProfilePart   profilePart;

	private ThreadContentViewer contentViewer; 
	private ThreadViewInput     viewInput; 
	

	public ThreadView(CTabFolder parent, int style) {
		super(parent, style);
		setText("Thread view");
		setToolTipText("A view to display metrics of a certain threads or processes");
		setShowClose(true);
	}

	
	@Override
	public void createContent(Composite parent) {

		contentViewer = new ThreadContentViewer(partService, eventBroker, databaseAddOn, profilePart);
		contentViewer.createContent(parent, menuService);
		
		// subscribe to filter events
		eventBroker.subscribe(FilterStateProvider.FILTER_REFRESH_PROVIDER, this);
	}

	public void setService(EPartService partService, 
			IEventBroker broker,
			DatabaseCollection database,
			ProfilePart   profilePart,
			EMenuService  menuService) {
		
		this.partService = partService;
		this.eventBroker = broker;
		this.databaseAddOn = database;
		this.profilePart = profilePart;
		this.menuService = menuService;
	}


	@Override
	public void setInput(Object input) {
		
		if (input == null || (!(input instanceof ThreadViewInput)))
			return;
		
		// important: needs to store the experiment database for further usage
		// when the view is becoming visible
		viewInput = (ThreadViewInput) input;
		if (viewInput.getThreads() == null) {
			Display display = Display.getDefault();
			try {
				List<Integer> threads = getThreads(display.getActiveShell(), viewInput.getThreadData());
				if (threads == null)
					return;
				
				viewInput.setThread(threads);
				
			} catch (Exception e) {
				e.printStackTrace();
				
				final String label = "Error while opening thread-level data";
				Logger logger = LoggerFactory.getLogger(getClass());
				logger.error(label, e);
				return;
			}
		}
		contentViewer.setData(viewInput);
	}	
	
	
	static public List<Integer> getThreads(Shell shell, IThreadDataCollection threadData) 
			throws NumberFormatException, IOException 
	{
		String []labels = threadData.getRankStringLabels();
		List<FilterDataItem> items =  new ArrayList<FilterDataItem>(labels.length);
		
		for (int i=0; i<labels.length; i++) {
			FilterDataItem obj = new FilterDataItem(labels[i], false, true);
			items.add(obj);
		}

		ThreadFilterDialog dialog = new ThreadFilterDialog(shell, items);
		if (dialog.open() == Window.OK) {
			items = dialog.getResult();
			if (items != null) {
				List<Integer> threads = new ArrayList<Integer>();
				for(int i=0; i<items.size(); i++) {
					if (items.get(i).checked) {
						threads.add(i);
					}
				}
				if (threads.size()>0)
					return threads;
			}
			
		}
		return null;
	}


	@Override
	public void handleEvent(Event event) {
		ScopeTreeViewer treeViewer = contentViewer.getTreeViewer();
		if (treeViewer.getTree().isDisposed())
			return;

		Object obj = event.getProperty(IEventBroker.DATA);
		if (obj == null || profilePart.getExperiment() == null)
			return;
		
		if (!(obj instanceof ViewerDataEvent)) {

			if (event.getTopic().equals(FilterStateProvider.FILTER_REFRESH_PROVIDER)) {
				BaseExperiment experiment = profilePart.getExperiment();
				FilterStateProvider.filterExperiment((Experiment) experiment);
				
				// TODO: this process takes time
				RootScope root = experiment.getRootScope(RootScopeType.CallingContextTree);
				contentViewer.setData(root);
			}
			return;
		}
		
	}


	@Override
	public Object getInput() {
		return viewInput;
	}
}
