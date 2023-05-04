package edu.rice.cs.hpcviewer.ui.parts.topdown;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcfilter.FilterDataItem;
import edu.rice.cs.hpcfilter.dialog.ThreadFilterDialog;
import edu.rice.cs.hpctree.IScopeTreeData;
import edu.rice.cs.hpctree.ScopeTreeData;
import edu.rice.cs.hpcviewer.ui.ProfilePart;
import edu.rice.cs.hpcviewer.ui.graph.GraphMenu;
import edu.rice.cs.hpcviewer.ui.internal.AbstractTableView;
import edu.rice.cs.hpcviewer.ui.parts.thread.ThreadViewInput;
import edu.rice.cs.hpcviewer.ui.resources.IconManager;

public class TopDownPart extends AbstractTableView 
{	
	private static final String TITLE = "Top-down view";
	
	private static final int ITEM_GRAPH = 0;
	private static final int ITEM_THREAD = 1;

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
    protected void beginToolbar(CoolBar coolbar, ToolBar toolbar) { /* nothing to do */ }
	
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
					
					var table = getTable();
					Scope scope = table.getSelection();
					IMetricManager metricManager = getMetricManager();
					
					// create the context menu of graphs
					var data = getThreadDataCollection();
					try {
						GraphMenu.createAdditionalContextMenu(getProfilePart(),  mgr, metricManager, data, scope);
					} catch (Exception e1) {
						MessageDialog.openError(e.display.getActiveShell(), "Error", e1.getMessage());
					}
					
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

		IThreadDataCollection dataCollector = getThreadDataCollection();

		IdTupleType idtype;
		if (getRoot().getExperiment() instanceof Experiment) {
			idtype = ((Experiment)getRoot().getExperiment()).getIdTupleType();
		} else {
			idtype = IdTupleType.createTypeWithOldFormat();
		}
		var labels   = dataCollector.getIdTupleLabelWithoutGPU(idtype);
		
		List<FilterDataItem<String>> listItems = ThreadFilterDialog.filter(shell, "Select rank/thread to view", labels, null);
		
		if (listItems != null && !listItems.isEmpty()) {
			List<IdTuple> selectedIdtuples = new ArrayList<>();
			var idtuples = dataCollector.getIdTupleListWithoutGPU(idtype);			

			for(int i=0; i<listItems.size(); i++) {
				if (listItems.get(i).checked) {
					selectedIdtuples.add(idtuples.get(i));
				}
			}
			if (!selectedIdtuples.isEmpty()) {
				RootScope root = getRoot();
				ThreadViewInput input = new ThreadViewInput(root, dataCollector, selectedIdtuples);
				ProfilePart profilePart = getProfilePart();
				profilePart.addThreadView(input);
			}
		}
	}


	@Override
	protected void updateStatus() {
		IThreadDataCollection dataCollector = getThreadDataCollection();
		
		// data collector can be null for old database (no metric-db files)
		boolean enableItems = dataCollector != null && dataCollector.isAvailable();

		items[ITEM_THREAD].setEnabled(enableItems);
		
		Scope selectedScope = super.getTable().getSelection();
		boolean enableGraph = enableItems && selectedScope != null;
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
	public RootScope getRoot() {
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
			var experiment = root.getExperiment();
			
			try {
				threadData = experiment.getThreadData();
			} catch (IOException e) {
				threadData = new EmptyThreadDataCollection();
			}
		}
		return threadData;
	}	


	@Override
	public void dispose() {
		super.dispose();
		
		if (threadData != null) {
			try {
				threadData.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		threadData = null;
		items = null;
	}
	
	static class EmptyThreadDataCollection implements IThreadDataCollection
	{

		@Override
		public void close() throws Exception {  /* no-op */ }

		@Override
		public void open(RootScope root, String directory) throws IOException {  /* no-op */ }

		@Override
		public boolean isAvailable() {
			return false;
		}

		@Override
		public List<IdTuple> getIdTuples() {
			return Collections.emptyList();
		}

		@Override
		public double[] getRankLabels() throws IOException {
			return new double[0];
		}

		@Override
		public String[] getRankStringLabels() throws IOException {
			return new String[0];
		}

		@Override
		public double[] getEvenlySparseRankLabels() throws IOException {
			return new double[0];
		}

		@Override
		public int getParallelismLevel() throws IOException {
			return 0;
		}

		@Override
		public String getRankTitle() throws IOException {
			return "";
		}

		@Override
		public double getMetric(Scope scope, BaseMetric metric, IdTuple idtuple, int numMetrics) throws IOException {
			return 0;
		}

		@Override
		public double[] getMetrics(Scope scope, BaseMetric metric, int numMetrics) throws Exception {
			return new double[0];
		}

		@Override
		public double getMetric(long nodeIndex, int metricIndex, IdTuple idtuple, int numMetrics) throws IOException {
			return 0;
		}

		@Override
		public double[] getMetrics(long nodeIndex, int metricIndex, int numMetrics) throws Exception {
			return new double[0];
		}

		@Override
		public double[] getScopeMetrics(int thread_id, int MetricIndex, int numMetrics) throws IOException {
			return new double[0];
		}

		@Override
		public List<IdTuple> getIdTupleListWithoutGPU(IdTupleType idtype) {
			return Collections.emptyList();
		}

		@Override
		public Object[] getIdTupleLabelWithoutGPU(IdTupleType idtype) {
			return new Object[0];
		}		
	}
}
