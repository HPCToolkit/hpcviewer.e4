package edu.rice.cs.hpcviewer.ui.parts.thread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcfilter.FilterDataItem;
import edu.rice.cs.hpcfilter.StringFilterDataItem;
import edu.rice.cs.hpcfilter.dialog.ThreadFilterDialog;
import edu.rice.cs.hpctree.IScopeTreeData;
import edu.rice.cs.hpctree.ScopeTreeData;
import edu.rice.cs.hpcviewer.ui.internal.AbstractView;
import edu.rice.cs.hpcviewer.ui.parts.topdown.TopDownPart;

public class ThreadPart extends TopDownPart 
{
	static final private int MAX_THREAD_INDEX = 2;
	static final private String TITLE_PREFIX  = "Thread view ";

	private ThreadViewInput viewInput; 
	private List<Integer> threads;

	public ThreadPart(CTabFolder parent, int style) {
		super(parent, style);
	}

	

	@Override
	public void setInput(Object input) {
		
		if (input == null || (!(input instanceof ThreadViewInput)))
			return;

		// if the input doesn't include the list of threads to be displayed,
		// we'll ask the user to pick the threads
		
		viewInput = (ThreadViewInput) input;
		threads   = viewInput.getThreads();
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
				
				throw new RuntimeException(e.getMessage());
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
	 * @return StringBuffer
	 * @throws IOException
	 */
	private static StringBuffer getLabel(ThreadViewInput input) throws IOException {
		
		StringBuffer buffer = new StringBuffer();
		buffer.append('[');
		
		IThreadDataCollection threadData = input.getThreadData();
		String[] labels = threadData.getRankStringLabels();

		List<Integer> threads = input.getThreads();
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

	
	private String getTooltipText(ThreadViewInput input) throws IOException {
		final String TOOLTIP_PREFIX = "Top down view for thread(s): ";
		
		IThreadDataCollection threadData = input.getThreadData();
		String[] labels = threadData.getRankStringLabels();

		List<Integer> threads = input.getThreads();
		int size = threads.size();

		String label = TOOLTIP_PREFIX;
		for(int i=0; i<size; i++) {
			int index = threads.get(i);
			label += labels[index];
			
			if (i+1 < size) {
				label += ", ";
			}
		}
		return label;
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
	 * @throws IOException
	 */
	static private List<Integer> getThreads(Shell shell, IThreadDataCollection threadData) 
			throws NumberFormatException, IOException 
	{
		String []labels = threadData.getRankStringLabels();
		List<FilterDataItem<String>> items =  new ArrayList<>(labels.length);
		
		for (int i=0; i<labels.length; i++) {
			FilterDataItem<String> obj = new StringFilterDataItem(labels[i], false, true);
			items.add(obj);
		}

		ThreadFilterDialog dialog = new ThreadFilterDialog(shell, "Select rank/thread to view", items);
		
		if (dialog.open() == Window.OK) {
			items = dialog.getResult();
			if (items != null) {
				final List<Integer> threads = new ArrayList<Integer>();
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
}
