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

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.metric.MetricRaw;
import edu.rice.cs.hpcdata.util.ScopeComparator;
import edu.rice.cs.hpcviewer.ui.ProfilePart;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.base.ISortContentProvider;
import edu.rice.cs.hpcviewer.ui.internal.ScopeSelectionAdapter;
import edu.rice.cs.hpcviewer.ui.internal.ScopeTreeViewer;

import edu.rice.cs.hpcviewer.ui.metric.MetricRawManager;
import edu.rice.cs.hpcviewer.ui.parts.topdown.TopDownContentViewer;
import edu.rice.cs.hpcviewer.ui.util.SortColumn;

public class ThreadContentViewer extends TopDownContentViewer 
{
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

		// 3. add the new metrics into the table
		final Experiment experiment = (Experiment) input.getRootScope().getExperiment();
		initTableColumns(input, experiment.getMetricRaw());

		// 4. update the table content, including the aggregate experiment
		ScopeTreeViewer treeViewer = getViewer();
		treeViewer.setInput(input.getRootScope());
				
		treeViewer.expandToLevel(2, true);

		updateStatus();
	}
	

	/****
	 * customized table initialization
	 * @param threads : list of threads
	 * @throws IOException 
	 */
	private void initTableColumns(ThreadViewInput input, List<MetricRaw> list)  {
		if (list == null) {
			// error
			return;
		}
		ScopeTreeViewer treeViewer = getViewer();

		if (treeViewer.getTree().getColumnCount() == 0) {
	        TreeViewerColumn colTree = createScopeColumn(treeViewer);
	        
			ScopeSelectionAdapter selectionAdapter = new ScopeSelectionAdapter(treeViewer, colTree);
			colTree.getColumn().addSelectionListener(selectionAdapter);
		}

		List<Integer> threads = input.getThreads();
		if (threads == null)
			return;
		
		// duplicate "raw metrics" before setting them into the column. The reason for duplication is: 
		// Although metric A in column X is the same as metric A in column Y (they are both metric A),
		// but column X is for displaying the values for threads P while column Y is for displaying
		// for threads Q. 
		boolean sort = true;
		HashMap<Integer, BaseMetric> listOfDuplicates = new HashMap<Integer, BaseMetric>(list.size());
		
		for(MetricRaw metricOrig: list)
		{
			MetricRaw mdup = MetricRaw.create(metricOrig);
			mdup.setThread(threads);
			mdup.setDisplayName(mdup.getDisplayName());
			
			final String metricID = String.valueOf(treeViewer.getTree().getColumnCount());
			mdup.setShortName(metricID);
			listOfDuplicates.put(metricOrig.getIndex(), mdup);
			
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
		
		// sort the first visible column
		TreeColumn []columns = treeViewer.getTree().getColumns();
		for(TreeColumn col: columns) {
			if (col.getData() != null && col.getWidth()>0) {
				// first the visible metric column

				ISortContentProvider sortProvider = (ISortContentProvider) treeViewer.getContentProvider();					
				 // start sorting
				int swtDirection = SortColumn.getSWTSortDirection(ScopeComparator.SORT_DESCENDING);
				sortProvider.sort_column(col, swtDirection);
				break;
			}
		}

		treeViewer.initSelection(0);
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


	@Override
	protected ViewerType getViewerType() {
		return ViewerType.INDIVIDUAL;
	}	
}
