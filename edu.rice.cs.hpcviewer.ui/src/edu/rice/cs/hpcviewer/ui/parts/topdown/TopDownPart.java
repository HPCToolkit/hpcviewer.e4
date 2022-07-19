package edu.rice.cs.hpcviewer.ui.parts.topdown;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.tld.ThreadDataCollectionFactory;
import edu.rice.cs.hpcfilter.FilterDataItem;
import edu.rice.cs.hpcfilter.dialog.ThreadFilterDialog;
import edu.rice.cs.hpctree.IScopeTreeData;
import edu.rice.cs.hpctree.ScopeTreeData;
import edu.rice.cs.hpctree.ScopeTreeTable;
import edu.rice.cs.hpcviewer.ui.ProfilePart;
import edu.rice.cs.hpcviewer.ui.graph.GraphMenu;
import edu.rice.cs.hpcviewer.ui.internal.AbstractTableView;
import edu.rice.cs.hpcviewer.ui.parts.thread.ThreadViewInput;
import edu.rice.cs.hpcviewer.ui.resources.IconManager;

public class TopDownPart extends AbstractTableView 
{	
	private static final String TITLE = "Top-down view";
	
	final static private int ITEM_GRAPH = 0;
	final static private int ITEM_THREAD = 1;

	/* thread data collection is used to display graph or 
	 * to display a thread view. We need to instantiate this variable
	 * once we got the database experiment. */
	private IThreadDataCollection threadData;

	private ToolItem []items;

	public TopDownPart(CTabFolder parent, int style) {
		super(parent, style, TITLE);
		setToolTipText("A view to display the calling context tree (CCT) of the profile data");
	}
	

	@Override
    protected void beginToolbar(CoolBar coolbar, ToolBar toolbar) {}
	
	@Override
    protected void endToolbar  (CoolBar coolbar, ToolBar toolbar) {
		
		items = new ToolItem[2];
		
		createToolItem(toolbar, SWT.SEPARATOR, "", "");
		
		items[ITEM_GRAPH] = createToolItem(toolbar, SWT.DROP_DOWN, IconManager.Image_Graph, 
				"Show the graph of metric values of the selected CCT node for all processes/threads");
		items[ITEM_THREAD] = createToolItem(toolbar, IconManager.Image_ThreadView, 
				"Show the metric(s) of a group of threads");

		items[ITEM_GRAPH].addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.detail == SWT.ARROW || e.detail == 0 || e.detail == SWT.PUSH) {
					
					Rectangle rect = items[ITEM_GRAPH].getBounds();
					Point pt = new Point(rect.x, rect.y + rect.height);
					pt = toolbar.toDisplay(pt);

					final MenuManager mgr = new MenuManager("graph");
					
					mgr.removeAll();
					mgr.createContextMenu(toolbar);
					
					ScopeTreeTable table = getTable();
					Scope scope = table.getSelection();
					IMetricManager metricManager = getMetricManager();
					
					// create the context menu of graphs
					IThreadDataCollection threadData = getThreadDataCollection();
					GraphMenu.createAdditionalContextMenu(getProfilePart(),  mgr, metricManager, threadData, scope);
					
					// make the context menu appears next to tool item
					final Menu menu = mgr.getMenu();
					menu.setLocation(pt);
					menu.setVisible(true);
				}
			}
		});
		
		items[ITEM_THREAD].addSelectionListener(new SelectionAdapter() {			
			@Override
			public void widgetSelected(SelectionEvent e) {
				showThreadView(e.widget.getDisplay().getActiveShell());
			}
		});
	}

	
	private void showThreadView(Shell shell) {
		String[] labels = null;
		IThreadDataCollection threadData = getThreadDataCollection();
		try {
			labels = threadData.getRankStringLabels();
		} catch (IOException e) {
			String msg = "Error opening thread data";
			Logger logger = LoggerFactory.getLogger(getClass());
			logger.error(msg, e);
			
			MessageDialog.openError(shell, msg, e.getClass().getName() + ": " + e.getLocalizedMessage());
			return;
		}
		
		List<FilterDataItem<String>> items = ThreadFilterDialog.filter(shell, "Select rank/thread to view", labels, null);
		
		if (items != null) {
			List<Integer> threads = new ArrayList<Integer>();
			for(int i=0; i<items.size(); i++) {
				if (items.get(i).checked) {
					threads.add(i);
				}
			}
			if (threads.size()>0) {
				RootScope root = getRoot();
				ThreadViewInput input = new ThreadViewInput(root, threadData, threads);
				ProfilePart profilePart = getProfilePart();
				profilePart.addThreadView(input);
			}
		}
	}


	@Override
	protected void updateStatus() {
		IThreadDataCollection threadData = getThreadDataCollection();

		// do not enable the buttons if we have only 1 rank or thread
		// there is no point to show a graph or a thread view for only 
		// 1 thread or rank.
		try {
			if (threadData.getRankLabels().length < 2) {
				items[ITEM_THREAD].setEnabled(false);
				items[ITEM_GRAPH] .setEnabled(false);
				
				return;
			}
		} catch (IOException e) {
			// file error. Should we display a message?
			// usually it's been displayed before, and we don't want
			// to show a message every time there's a I/O problem 
		}
		boolean enableItems = (threadData != null);
		items[ITEM_THREAD].setEnabled(enableItems);
		
		Scope selectedScope = super.getTable().getSelection();
		boolean enableGraph = enableItems && (selectedScope != null);
		items[ITEM_GRAPH] .setEnabled(enableGraph);
	}


	@Override
	protected RootScope buildTree(boolean reset) {
		// the tree is already built by the hpcprof
		// just return the existing root
		return getRoot();		
	}


	@Override
	public RootScopeType getRootType() {
		return RootScopeType.CallingContextTree;
	}


	@Override
	protected RootScope getRoot() {
		IMetricManager mm = getMetricManager();
		Experiment experiment = (Experiment) mm;
		return experiment.getRootScope(RootScopeType.CallingContextTree);
	}


	@Override
	protected IScopeTreeData getTreeData(RootScope root, IMetricManager metricManager) {
		return new ScopeTreeData(root, metricManager);
	}
	
	/***
	 * Retrieve the object of {@link IThreadDataCollection}.
	 * Ideally this object is created only once after opening the database.
	 * However, since it may take time (minutes) to collect thread data with
	 * old databases, we should generate it on demand. 
	 * 
	 * @return object of {@link IThreadDataCollection}
	 */
	protected IThreadDataCollection getThreadDataCollection() {
		if (threadData == null) {
			RootScope root = getRoot();
			try {
				threadData = ThreadDataCollectionFactory.build(root);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return threadData;
	}
}
