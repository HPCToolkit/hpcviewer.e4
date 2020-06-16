package edu.rice.cs.hpcviewer.ui.parts.thread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpcviewer.ui.parts.ThreadView;
import edu.rice.cs.hpcviewer.ui.parts.editor.PartFactory;
import edu.rice.cs.hpcviewer.ui.util.FilterDataItem;

/*****************************************************************************
 * 
 * A collection of methods to build a thread view intelligently
 * The function {@link ThreadViewFactory.build} returns the thread view of
 * a given experiment and the list of threads as follows:
 * 
 * build ( experiment x threads ) -> thread_view
 * <ul>
 *  <li>if the input is unique, it will create the view with the column for the threads.
 *  <li>if the experiment is not unique but the threads is unique, it activates the view
 * 	but creates the new column for threads
 *  <li>if the input is not unique, it just activates the view
 *  <li>if the thread is null, it prompts a dialog box to ask the list of threads to display
 * </ul>
 *****************************************************************************/
public class ThreadViewFactory 
{
	/****
	 * Build or activate a thread view. <br>
	 * This method will prompt a dialog box to ask users which threads to be displayed.
	 * 
	 * @param window : the current active window
	 * @param experiment : the current database
	 * 
	 * @return the thread view if successful, null otherwise
	 */
	static public void build(PartFactory partFactory, Shell shell, RootScope rootScope, IThreadDataCollection threadData) 
	{
		build(partFactory, shell, rootScope, threadData, null);
	}
	
	/*****
	 * Build or activate a thread view. <br>
	 * This method will prompt a dialog box to ask users which threads to be displayed.
	 * 
	 * @param window : the current active window
	 * @param experiment : the current database
	 * @param threads : the list of threads to be displayed. If the this parameter is null,
	 * it will prompt users to choose the threads.
	 * 
	 * @return the thread view if successful, null otherwise
	 */
	static public void build(PartFactory partFactory, Shell shell, RootScope rootScope, IThreadDataCollection threadData, List<Integer> threads) 
	{
		try {
			if (threads == null) {
				// ask users to select which threads to be displayed
				threads = getThreads(shell, threadData);
				// if users click cancel, we return immediately
				if (threads == null)
					return;
			}
			ThreadViewInput input  = new ThreadViewInput(rootScope, threadData, threads);
			final String elementId = getThreadViewKey(rootScope);
			
			partFactory.display("edu.rice.cs.hpcviewer.ui.partsashcontainer.0", ThreadView.IDdesc, elementId, input);
			
		} catch (Exception e) {
			e.printStackTrace();
			String msg = e.getMessage() == null ? e.getClass().getCanonicalName() : e.getMessage();
			MessageDialog.openError(shell, "Error", msg);
		}
	}
	
	
	static private String getThreadViewKey(RootScope root) 
	{
		BaseExperiment experiment = root.getExperiment();
		String key = experiment.getDefaultDirectory().getAbsolutePath() + "." + root.getType();
		
		// Bug on Windows : second key cannot contain a colon
		return key.replace(':', '-');
	}

	
	static private List<Integer> getThreads(Shell shell, IThreadDataCollection threadData) 
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

	
}
