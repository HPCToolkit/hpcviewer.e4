package edu.rice.cs.hpcviewer.ui.parts.thread;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.widgets.TreeColumn;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.IMetricManager;
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.util.ScopeComparator;
import edu.rice.cs.hpcviewer.ui.ProfilePart;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.internal.AbstractContentProvider;
import edu.rice.cs.hpcviewer.ui.internal.ScopeSelectionAdapter;
import edu.rice.cs.hpcviewer.ui.internal.ScopeTreeViewer;

import edu.rice.cs.hpcviewer.ui.metric.MetricRawManager;
import edu.rice.cs.hpcviewer.ui.parts.topdown.TopDownContentViewer;

public class ThreadContentViewer extends TopDownContentViewer 
{
	static final private int MAX_THREAD_INDEX = 2;
	
	/** the metric manager of the view. DO NOT access this variable directly.
	 *  Instead, we need to query to getMetricManager() */
	private IMetricManager metricManager = null;

	public ThreadContentViewer( EPartService partService, 
								IEventBroker eventBroker, 
								DatabaseCollection database, 
								ProfilePart   profilePart) {

		super(partService, eventBroker, database, profilePart);
	}

	
	public void setData(ThreadViewInput input) {

		final IMetricManager metricMgr = getMetricManager();
		List<BaseMetric > metrics = metricMgr.getVisibleMetrics();
		
		// 1. check if the threads already exist in the view
		boolean col_exist = false;
		if (metrics != null && input.getThreads() != null) {
			for (BaseMetric metric : metrics) {
				if (metric instanceof MetricRaw) {
					List<Integer> lt = ((MetricRaw)metric).getThread();
					if (lt.size() == input.getThreads().size()) {
						for(Integer i : input.getThreads()) {
							col_exist = lt.contains(i);
							if (!col_exist) {
								break;
							}
						}
					}
				}
				if (col_exist) 
					break;
			}
		}
		
		// 2. if the column of this thread exist, exit.
		if (col_exist)
			return;

		// 3. add the new metrics into the table
		final Experiment experiment = (Experiment) input.getRootScope().getExperiment();
		initTableColumns(input, experiment.getMetricRaw());

		// 4. update the table content, including the aggregate experiment
		RootScope root = createRoot(experiment);
		ScopeTreeViewer treeViewer = getViewer();
		treeViewer.setInput(root);
		
		// insert the first row (header)
		treeViewer.insertParentNode(root);

		// pack the columns, either to fit the title of the header, or 
		// the item in the column
		
		final TreeColumn []columns = treeViewer.getTree().getColumns();
		
		for (final TreeColumn col : columns) {
			if (col.getData() != null) {
				col.pack();
			}
		}
		updateStatus();
	}
	

	/****
	 * customized table initialization
	 * @param threads : list of threads
	 * @throws IOException 
	 */
	private void initTableColumns(ThreadViewInput input, BaseMetric []mr)  {
		if (mr == null)
		{
			// error
			return;
		}

		IThreadDataCollection threadData = input.getThreadData();
		ScopeTreeViewer treeViewer = getViewer();
		AbstractContentProvider contentProvider = getContentProvider(treeViewer);
		
		String[] labels;
		
		if (treeViewer.getTree().getColumnCount() == 0) {
	        TreeViewerColumn colTree = createScopeColumn(treeViewer);
	        colTree.getColumn().setWidth(ScopeTreeViewer.COLUMN_DEFAULT_WIDTH);
	        
			ScopeSelectionAdapter selectionAdapter = new ScopeSelectionAdapter(treeViewer, colTree);
			colTree.getColumn().addSelectionListener(selectionAdapter);
			
			contentProvider.sort_column(colTree, ScopeComparator.SORT_ASCENDING);
		}

		List<Integer> threads = input.getThreads();
		if (threads == null)
			return;
		
		try {
			labels = threadData.getRankStringLabels();
			
			// duplicate "raw metrics" before setting them into the column. The reason for duplication is: 
			// Although metric A in column X is the same as metric A in column Y (they are both metric A),
			// but column X is for displaying the values for threads P while column Y is for displaying
			// for threads Q. 
			boolean sort = true;
			HashMap<Integer, BaseMetric> listOfDuplicates = new HashMap<Integer, BaseMetric>(mr.length);
			
			for(int j=0; j<mr.length; j++)
			{
				MetricRaw mdup = (MetricRaw) mr[j].duplicate();
				mdup.setThread(threads);
				
				StringBuffer buffer = new StringBuffer();
				buffer.append('[');
				int size = threads.size();
				
				// for the column title: only list the first MAX_THREAD_INDEX of the set of threads
				for(int i=0; i<size && i<=MAX_THREAD_INDEX; i++) {
					final int index;
					if (i<MAX_THREAD_INDEX) {
						index = threads.get(i);
					} else {
						// show the last thread index
						if (size > MAX_THREAD_INDEX+1)
							buffer.append("..");
						index = threads.get(size-1);
					}
					buffer.append(labels[index]);
					if (i < MAX_THREAD_INDEX && i<size-1)
						buffer.append(',');
				}
				buffer.append("]-");
				buffer.append(mdup.getDisplayName());

				mdup.setDisplayName(buffer.toString());
				final String metricID = String.valueOf(treeViewer.getTree().getColumnCount());
				mdup.setShortName(metricID);
				listOfDuplicates.put(mr[j].getIndex(), mdup);
				
				treeViewer.addTreeColumn(mdup, sort);
				
				// sort initially the first column metric
				sort = false;
			}
			
			Iterator<Entry<Integer, BaseMetric>> iterator = listOfDuplicates.entrySet().iterator();
			while(iterator.hasNext()) {
				Entry<Integer, BaseMetric> entry = iterator.next();
				BaseMetric metric = entry.getValue();
				int partner 	  = metric.getPartner();
				if (partner >= 0) {
					BaseMetric metricPartner = listOfDuplicates.get(partner);
					((MetricRaw)metric).setMetricPartner((MetricRaw) metricPartner);
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public IMetricManager getMetricManager() 
	{
		if (metricManager != null)
			return metricManager;
		
		// create a new metric manager for this view
		metricManager = new MetricRawManager(getViewer());
		return metricManager;
	}


	protected RootScope createRoot(BaseExperiment experiment) {

		// create and duplicate the configuration
		RootScope rootCCT    = experiment.getRootScope(RootScopeType.CallingContextTree);
		RootScope rootThread = (RootScope) rootCCT.duplicate();
		rootThread.setRootName("Thread View");
		
		// duplicate the children
		for(int i=0; i<rootCCT.getChildCount(); i++)
		{
			Scope scope = (Scope) rootCCT.getChildAt(i);
			rootThread.addSubscope(scope);
		}
		return rootThread;
	}
	
	@Override
	protected ViewerType getViewerType() {
		return ViewerType.INDIVIDUAL;
	}
}
