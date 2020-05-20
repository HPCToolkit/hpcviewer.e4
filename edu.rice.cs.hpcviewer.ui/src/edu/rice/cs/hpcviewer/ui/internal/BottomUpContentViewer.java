package edu.rice.cs.hpcviewer.ui.internal;

import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.ToolBar;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;

public class BottomUpContentViewer extends BaseContentViewer 
{
	private CallerViewContentProvider contentProvider = null;
			
	@Override
	protected void beginToolbar(CoolBar coolbar, ToolBar toolbar) {}

	@Override
	protected void endToolbar(CoolBar coolbar, ToolBar toolbar) {}

	@Override
	protected AbstractContentProvider getContentProvider(ScopeTreeViewer treeViewer) {

		if (contentProvider == null)
			contentProvider = new CallerViewContentProvider(treeViewer);
		
		return contentProvider;
	}

	@Override
	public void setData(RootScope root) {
		// TODO: base design !
		// we have to set the database to the content provider to initialize
		//    inclusive and exclusive propagation filters
		//    see {@code CallerViewContentProvider.setDatabase }
		contentProvider.setDatabase((Experiment) root.getExperiment());
		
		super.setData(root);
	}
}
