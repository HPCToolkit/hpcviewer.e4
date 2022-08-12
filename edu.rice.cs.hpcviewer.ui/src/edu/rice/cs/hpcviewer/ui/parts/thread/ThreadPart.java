package edu.rice.cs.hpcviewer.ui.parts.thread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcfilter.FilterDataItem;
import edu.rice.cs.hpcfilter.StringFilterDataItem;
import edu.rice.cs.hpcfilter.dialog.ThreadFilterDialog;
import edu.rice.cs.hpctree.IScopeTreeData;
import edu.rice.cs.hpctree.ScopeTreeData;
import edu.rice.cs.hpcviewer.ui.internal.AbstractView;
import edu.rice.cs.hpcviewer.ui.parts.topdown.TopDownPart;

public class ThreadPart extends TopDownPart 
{
	private static final int MAX_THREAD_INDEX = 2;
	private static final String TITLE_PREFIX  = "Thread view ";

	private ThreadViewInput viewInput; 

	public ThreadPart(CTabFolder parent, int style) {
		super(parent, style);
	}

	

	@Override
	public void setInput(Object input) {
		
		if (!(input instanceof ThreadViewInput))
			return;

		// if the input doesn't include the list of threads to be displayed,
		// we'll ask the user to pick the threads
		
		viewInput = (ThreadViewInput) input;
		var threads   = viewInput.getThreads();
		if (threads == null) {
			final Shell shell = getDisplay().getActiveShell();
			try {
				threads = getThreads(shell, viewInput.getThreadData());
				if (threads == null)
					return;
				
				viewInput.setThread(threads);
				
			} catch (Exception e) {
				final String label = "Error while opening thread-level data";
				Logger logger = LoggerFactory.getLogger(getClass());
				logger.error(label, e);
				MessageDialog.openError(shell, label, e.getClass().getName() +": " + e.getLocalizedMessage());
				return;
			}
		}
		// set the table
		RootScope root  = viewInput.getRootScope();
		Experiment experiment = (Experiment) root.getExperiment();
		List<BaseMetric> rawMetrics = experiment.getRawMetrics();
		ThreadMetricManager metricManager = new ThreadMetricManager(rawMetrics, threads);
		
		createTable(metricManager);
		
		try {
			String title = TITLE_PREFIX + getLabel(viewInput).toString(); 
			setText(title);
			setToolTipText(getTooltipText(viewInput));
		} catch (IOException e) {
			e.printStackTrace();
			setText(TITLE_PREFIX + " (Empty)");
		}
		setShowClose(true);
	}

	
	
	@Override
	public Object getInput() {
		return viewInput;
	}
	
	
	@Override
	protected RootScope getRoot() {
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
		
		IdTupleType idTypes;
		
		var exp = input.getRootScope().getExperiment();
		if (exp instanceof Experiment) {
			idTypes = ((Experiment)exp).getIdTupleType();
		} else {
			idTypes = IdTupleType.createTypeWithOldFormat();
		}
		
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
		return new ScopeTreeData(root, metricManager, false);
	}

	
	@Override
	protected IThreadDataCollection getThreadDataCollection() {
		return viewInput.getThreadData();
	}
	
	
	
	@Override
	public ViewType getViewType() {
		return AbstractView.ViewType.INDIVIDUAL;
	}

	@Override
	protected boolean isMetricToSkip(Scope scope, BaseMetric metric) {
		return false;
	}

	
	private String getTooltipText(ThreadViewInput input) throws IOException {
		final String TOOLTIP_PREFIX = "Top down view for thread(s): ";

		var threads = input.getThreads();
		int size = threads.size();

		StringBuilder label = new StringBuilder(TOOLTIP_PREFIX);
		
		for(int i=0; i<size; i++) {
			label.append(threads.get(i).toLabel());
			
			if (i+1 < size) {
				label.append(", ");
			}
		}
		return label.toString();
	}
	
	
	/***
	 * Get the list of threads to view in a thread view.
	 * This method will display a dialog box asking users to choose threads to view.
	 * If the users cancel, then it returns null.
	 * 
	 * @param shell Shell
	 * @param threadData IThreadDataCollection
	 * @return List<Integer>
	 * 
	 * @throws NumberFormatException
	 */
	private static List<IdTuple> getThreads(Shell shell, IThreadDataCollection threadData) 
			throws NumberFormatException 
	{
		var idTuples = threadData.getIdTuples();
		
		final List<FilterDataItem<String>> items =  new ArrayList<>(idTuples.size());
		
		idTuples.stream().forEach(idt -> items.add(new StringFilterDataItem(idt.toLabel(), false, true) ));

		ThreadFilterDialog dialog = new ThreadFilterDialog(shell, "Select rank/thread to view", items);
		
		if (dialog.open() == Window.OK) {
			var result = dialog.getResult();
			if (result == null)
				return Collections.emptyList();
			
			final List<IdTuple> threads = new ArrayList<>();
			for(int i=0; i<items.size(); i++) {
				if (items.get(i).checked) {
					threads.add(idTuples.get(i));
				}
			}
			if (!threads.isEmpty())
				return threads;
		}
		return null;
	}
}
