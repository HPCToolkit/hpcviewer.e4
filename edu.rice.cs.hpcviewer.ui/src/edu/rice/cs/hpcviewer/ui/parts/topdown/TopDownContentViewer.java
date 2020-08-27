package edu.rice.cs.hpcviewer.ui.parts.topdown;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.IMetricManager;
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpcdata.tld.collection.ThreadDataCollectionFactory;
import edu.rice.cs.hpcviewer.ui.ProfilePart;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.dialogs.ThreadFilterDialog;
import edu.rice.cs.hpcviewer.ui.graph.GraphMenu;
import edu.rice.cs.hpcviewer.ui.internal.AbstractContentProvider;
import edu.rice.cs.hpcviewer.ui.internal.AbstractViewBuilder;
import edu.rice.cs.hpcviewer.ui.internal.ScopeTreeViewer;
import edu.rice.cs.hpcviewer.ui.parts.thread.ThreadViewInput;
import edu.rice.cs.hpcviewer.ui.resources.IconManager;
import edu.rice.cs.hpcviewer.ui.util.FilterDataItem;

/*************************************************************
 * 
 * Top down content builder
 *
 *************************************************************/
public class TopDownContentViewer extends AbstractViewBuilder 
{	
	final static private int ITEM_GRAPH = 0;
	final static private int ITEM_THREAD = 1;
	
	private ToolItem []items;
	final private ProfilePart   profilePart;
	
	/* thread data collection is used to display graph or 
	 * to display a thread view. We need to instantiate this variable
	 * once we got the database experiment. */
	private IThreadDataCollection threadData;
	private boolean threadDataAvailable = false;
	
	private AbstractContentProvider contentProvider = null;
	
	public TopDownContentViewer(
			EPartService partService, 
			IEventBroker broker,
			DatabaseCollection database,
			ProfilePart   profilePart) {
		
		super(partService, broker, database, profilePart);
		this.profilePart = profilePart;
	}

	@Override
	protected void beginToolbar(CoolBar coolbar, ToolBar toolbar) {}

	@Override
	protected void endToolbar(CoolBar coolbar, ToolBar toolbar) {
		
		items = new ToolItem[2];
		
		createToolItem(toolbar, SWT.SEPARATOR, "", "");
		
		items[ITEM_GRAPH] = createToolItem(toolbar, SWT.DROP_DOWN, IconManager.Image_Graph, 
				"Show the graph of metric values of the selected CCT node for all processes/threads");
		items[ITEM_THREAD] = createToolItem(toolbar, IconManager.Image_ThreadView, 
				"Show the metric(s) of a group of threads");
		
		items[ITEM_GRAPH].addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.detail == SWT.ARROW || e.detail == 0 || e.detail == SWT.PUSH) {
					
					Rectangle rect = items[ITEM_GRAPH].getBounds();
					Point pt = new Point(rect.x, rect.y + rect.height);
					pt = toolbar.toDisplay(pt);

					final MenuManager mgr = new MenuManager("graph");
					
					mgr.removeAll();
					mgr.createContextMenu(toolbar);
					
					ScopeTreeViewer treeViewer = getViewer();
					BaseExperiment exp = treeViewer.getExperiment();
					Scope scope = treeViewer.getSelectedNode();
					
					// create the context menu of graphs
					GraphMenu.createAdditionalContextMenu(profilePart,  mgr, (Experiment) exp, threadData, scope);
					
					// make the context menu appears next to tool item
					final Menu menu = mgr.getMenu();
					menu.setLocation(pt);
					menu.setVisible(true);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		items[ITEM_THREAD].addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {

				showThreadView(e.widget.getDisplay().getActiveShell());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
	}

	
	private void showThreadView(Shell shell) {
		String[] labels = null;
		try {
			labels = threadData.getRankStringLabels();
		} catch (IOException e) {

			e.printStackTrace();
			
			Logger logger = LoggerFactory.getLogger(getClass());
			logger.error("Error opening thread data", e);
			return;
		}
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
				if (threads.size()>0) {
					ScopeTreeViewer treeViewer = getViewer();
					
					ThreadViewInput input = new ThreadViewInput(treeViewer.getRootScope(), threadData, threads);

					profilePart.addThreadView(input);
				}
			}
		}
	}
	
	@Override
	protected AbstractContentProvider getContentProvider(ScopeTreeViewer treeViewer) {
		
		if (contentProvider != null)
			return contentProvider;
		
		contentProvider = new AbstractContentProvider(treeViewer) {
			
			@Override
			public Object[] getChildren(Object node) {
				if (node instanceof Scope) {
					return ((Scope)node).getChildren();
				}
				return null;
			}
		};
		return contentProvider;
	}

	@Override
	protected void selectionChanged(IStructuredSelection selection) {
		if (!threadDataAvailable)
			return;
		
		Object obj = selection.getFirstElement();
		if (obj == null || !(obj instanceof Scope))
			return;
		
		if (threadData == null)
			return;
		
		boolean available = threadData.isAvailable();
		
		items[ITEM_GRAPH] .setEnabled(available);
	}

	@Override
	public void setData(RootScope root) {
		Experiment experiment = (Experiment) root.getExperiment();
		try {
			threadData = ThreadDataCollectionFactory.build(experiment);
		} catch (IOException e) {
			e.printStackTrace();
		}

		BaseMetric[]metrics  = experiment.getMetricRaw();
		threadDataAvailable = false;
		
		if (threadData != null && metrics != null) {
			// thread level data exists
			// we need to tell all metric raws of thread data
			for (BaseMetric metric: metrics)
			{
				if (metric instanceof MetricRaw)
					((MetricRaw)metric).setThreadData(threadData);
			}
			threadDataAvailable = threadData.isAvailable();
		}
		super.setData(root);
		
		if (threadDataAvailable) {
			// check the validity
			try {
				threadData.getParallelismLevel();
			} catch (IOException e) {
				threadDataAvailable = false;
				items[ITEM_GRAPH] .setEnabled(threadDataAvailable);
			}
		}

		items[ITEM_THREAD].setEnabled(threadDataAvailable);
	}

	@Override
	protected IMetricManager getMetricManager() {
		return (IMetricManager) getViewer().getExperiment();
	}

	@Override
	protected ViewerType getViewerType() {
		return ViewerType.COLLECTIVE;
	}
}
