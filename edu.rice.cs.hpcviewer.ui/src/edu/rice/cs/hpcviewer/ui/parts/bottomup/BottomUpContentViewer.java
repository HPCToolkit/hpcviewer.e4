package edu.rice.cs.hpcviewer.ui.parts.bottomup;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.ToolBar;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.IMetricManager;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpcviewer.ui.experiment.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.internal.AbstractContentProvider;
import edu.rice.cs.hpcviewer.ui.internal.AbstractViewBuilder;
import edu.rice.cs.hpcviewer.ui.internal.ScopeTreeViewer;
import edu.rice.cs.hpcviewer.ui.parts.ProfilePart;

public class BottomUpContentViewer extends AbstractViewBuilder 
{
	private BottomUpContentProvider contentProvider = null;

	public BottomUpContentViewer(EPartService  partService, 
								 IEventBroker  broker,
								 DatabaseCollection database,
								 ProfilePart   profilePart) {
		
		super(partService, broker, database, profilePart);
	}

	@Override
	protected void beginToolbar(CoolBar coolbar, ToolBar toolbar) {}

	@Override
	protected void endToolbar(CoolBar coolbar, ToolBar toolbar) {}

	@Override
	protected AbstractContentProvider getContentProvider(ScopeTreeViewer treeViewer) {

		if (contentProvider == null)
			contentProvider = new BottomUpContentProvider(treeViewer);
		
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

	@Override
	protected void selectionChanged(IStructuredSelection selection) {}

	@Override
	protected IMetricManager getMetricManager() {
		return (IMetricManager) getViewer().getExperiment();
	}

	@Override
	protected ViewerType getViewerType() {
		return ViewerType.COLLECTIVE;
	}
}
