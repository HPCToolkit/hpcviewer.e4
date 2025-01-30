// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcviewer.ui.parts.thread;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.custom.CTabFolder;
import org.osgi.service.event.Event;

import edu.rice.cs.hpcbase.BaseConstants.ViewType;
import edu.rice.cs.hpcbase.IDatabase;
import edu.rice.cs.hpcbase.ThreadViewInput;
import edu.rice.cs.hpcbase.ViewerDataEvent;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpctree.IScopeTreeData;
import edu.rice.cs.hpctree.ScopeTreeData;
import edu.rice.cs.hpctree.ScopeTreeTable;
import edu.rice.cs.hpcviewer.ui.parts.topdown.TopDownPart;


/****************************************************
 * 
 * Part for thread view. <br/>
 * A thread view is a top-down view but the metrics are from
 * a set of threads (or id-tuples) instead of the aggregate
 * of all id-tuples.
 * 
 * <p>Requires to set the input by calling {@link setInput} 
 * containing a {@code ThreadViewInput} object.
 * 
 *  @see ThreadViewInput
 *
 ****************************************************/
public class ThreadPart extends TopDownPart 
{
	private static final int MAX_THREAD_INDEX = 2;
	private static final String TITLE_PREFIX  = "Thread view ";

	private ThreadViewInput viewInput; 

	public ThreadPart(CTabFolder parent, int style) {
		super(parent, style);
	}

	

	@Override
	public void setInput(IDatabase database, Object input) {
		
		if (!(input instanceof ThreadViewInput))
			return;

		// if the input doesn't include the list of threads to be displayed,
		// we'll ask the user to pick the threads
		
		viewInput = (ThreadViewInput) input;
		var threads = viewInput.getThreads();
		if (threads == null)
				return;
		
		// create the table
		RootScope root = viewInput.getRootScope();
		var experiment = (Experiment) root.getExperiment();
		List<BaseMetric> rawMetrics = experiment.getRawMetrics();
		var id = experiment.getDirectory();
		
		ThreadMetricManager metricManager = new ThreadMetricManager(id, rawMetrics, threads);
		
		super.setInput(database, metricManager); 
		
		// create the title and make sure it's closable.
		String title = TITLE_PREFIX + getLabel(viewInput).toString(); 
		setText(title);
		setToolTipText(getTooltipText(viewInput));
		setShowClose(true);
	}

	private int []expandedNodes;
	
	@Override
	public void handleEvent(Event event) {
		super.handleEvent(event);
		Object obj = event.getProperty(IEventBroker.DATA);
		if (!(obj instanceof ViewerDataEvent)) 
			return;
		
		String topic = event.getTopic();
		if (topic.equals(ViewerDataEvent.TOPIC_FILTER_POST_PROCESSING)) {

			ViewerDataEvent data = (ViewerDataEvent) obj;
			var thesameDatabase = getMetricManager().getID().equals(data.metricManager.getID());
			if (thesameDatabase) {
				// grab the new root of the refreshed database
				var newRoot = ((Experiment) data.metricManager).getRootScope(RootScopeType.CallingContextTree);

				try {
					((Experiment) data.metricManager).getThreadData();
				} catch (IOException e) {
					MessageDialog.openError(super.getControl().getShell(), "Error", e.getMessage());
					return;
				}
				
				// duplicate codes from AbstractTableView to refresh the table
				// need to avoid duplication
				viewInput.setRootScope(newRoot);
				getActionManager().clear();
				
				ScopeTreeTable table = (ScopeTreeTable) getTable();
				table.reset(newRoot);
				
				if (expandedNodes != null) {
					table.expandAndSelectNode(expandedNodes);
					expandedNodes = null;
				}
				updateButtonStatus();
			}

		} else if (topic.equals(ViewerDataEvent.TOPIC_FILTER_PRE_PROCESSING)) {
			expandedNodes = ((ScopeTreeTable)getTable()).getPathOfSelectedNode();
		}
	}
	
	@Override
	public Object getInput() {
		return viewInput;
	}
	
	
	@Override
	public RootScope getRoot() {
		return viewInput.getRootScope();
	}
	
	
	@Override
	protected RootScope buildTree(boolean reset) {
		return viewInput.getRootScope();
	}
	
	/***
	 * Static method to create a label based on the list of thread
	 * 
	 * @param input ThreadViewInput 
	 * @return StringBuilder
	 * @throws IOException
	 */
	private static StringBuilder getLabel(ThreadViewInput input) {
		
		var buffer = new StringBuilder();
		buffer.append('[');

		var threads = input.getThreads();
		int size = threads.size();
		
		IdTupleType idTypes = getIdTupleTypes(input);
		
		// for the column title: only list the first MAX_THREAD_INDEX of the set of threads
		for(int i=0; i<size && i<=MAX_THREAD_INDEX; i++) {
			final IdTuple idtuple;
			if (i<MAX_THREAD_INDEX) {
				idtuple = threads.get(i);
			} else {
				// show the last thread index
				if (size > MAX_THREAD_INDEX+1)
					buffer.append("..");
				idtuple = threads.get(size-1);
			}
			buffer.append(idtuple.toString(idTypes));
			if (i < MAX_THREAD_INDEX && i<size-1)
				buffer.append(',');
		}
		buffer.append("]");
		return buffer;
	}
	

	@Override
	protected IScopeTreeData getTreeData(RootScope root, IMetricManager metricManager) {
		return new ScopeTreeData(getDatabase(), root, metricManager);
	}

	
	@Override
	protected IThreadDataCollection getThreadDataCollection() {
		return viewInput.getThreadData();
	}
	
	
	
	@Override
	public ViewType getViewType() {
		return ViewType.INDIVIDUAL;
	}

	@Override
	protected boolean isMetricToSkip(Scope scope, BaseMetric metric) {
		return false;
	}

	
	private String getTooltipText(ThreadViewInput input) {
		final String TOOLTIP_PREFIX = "Top down view for thread(s): ";
		
		IdTupleType idTypes = getIdTupleTypes(input);

		var threads = input.getThreads();
		var labels = threads.stream().map(t -> t.toString(idTypes)).collect(Collectors.toList());

		return TOOLTIP_PREFIX + Arrays.toString(labels.toArray());
	}
	

	private static IdTupleType getIdTupleTypes(ThreadViewInput input) {
		
		var exp = input.getRootScope().getExperiment();
		IdTupleType idTypes;
		if (exp instanceof Experiment) {
			idTypes = ((Experiment)exp).getIdTupleType();
		} else {
			idTypes = IdTupleType.createTypeWithOldFormat();
		}
		return idTypes;
	}
}
