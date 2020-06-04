package edu.rice.cs.hpcviewer.ui.internal;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.ToolBar;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpcviewer.ui.experiment.DatabaseCollection;

public class BottomUpContentViewer extends AbstractContentViewer 
{
	private CallerViewContentProvider contentProvider = null;

	public BottomUpContentViewer(EPartService  partService, 
								 EModelService modelService, 
								 MApplication  app,
								 IEventBroker  broker,
								 DatabaseCollection database) {
		
		super(partService, modelService, app, broker, database);
	}

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

	@Override
	protected void selectionChanged(IStructuredSelection selection) {}
}
