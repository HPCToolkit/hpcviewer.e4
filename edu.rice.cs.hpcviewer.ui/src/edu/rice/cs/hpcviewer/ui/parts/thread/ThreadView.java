package edu.rice.cs.hpcviewer.ui.parts.thread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpcviewer.ui.experiment.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.parts.IViewPart;
import edu.rice.cs.hpcviewer.ui.parts.editor.PartFactory;
import edu.rice.cs.hpcviewer.ui.util.FilterDataItem;

/*************************************************************
 * 
 * View part to display CCT and metrics for a specific set of threads 
 *
 *************************************************************/
public class ThreadView  implements IViewPart
{
	@Inject EPartService  partService;
	@Inject IEventBroker  broker;
	
	@Inject DatabaseCollection databaseAddOn;

	@Inject PartFactory partFactory;

	
	private ThreadContentViewer contentViewer; 
	private ThreadViewInput     viewInput; 
	

	
	@PostConstruct
	public void postConstruct(Composite parent, EMenuService menuService) {

		contentViewer = new ThreadContentViewer(partService, broker, databaseAddOn, partFactory);
		contentViewer.createContent(parent, menuService);
	}


	@Override
	public BaseExperiment getExperiment() {
		if (viewInput == null)
			return null;
		
		return viewInput.getRootScope().getExperiment();
	}

	@Override
	public void setInput(MPart part, Object input) {
		
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
				
			} catch (NumberFormatException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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

}
