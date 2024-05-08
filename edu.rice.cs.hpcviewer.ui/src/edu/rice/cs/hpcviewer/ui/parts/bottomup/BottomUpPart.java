package edu.rice.cs.hpcviewer.ui.parts.bottomup;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.ToolBar;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcbase.ProgressReport;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpctree.BottomUpScopeTreeData;
import edu.rice.cs.hpctree.IScopeTreeData;
import edu.rice.cs.hpcviewer.ui.internal.AbstractTableView;

public class BottomUpPart extends AbstractTableView 
{
	private static final String TITLE = "Bottom-up view";

	public BottomUpPart(CTabFolder parent, int style) {
		super(parent, style, TITLE);
		setToolTipText("A view to display the list of callers");
	}

	@Override
	protected void beginToolbar(CoolBar coolbar, ToolBar toolbar) { /* unused */ }

	@Override
	protected void endToolbar(CoolBar coolbar, ToolBar toolbar) { /* unused */ }

	@Override
	protected void updateStatus() { /* unused */ }
	
	
	private boolean isInitialized = false;
	
	@Override
	protected RootScope buildTree(boolean reset) {
		Experiment experiment = (Experiment) getMetricManager();		
		RootScope rootCCT  = experiment.getRootScope(RootScopeType.CallingContextTree);
		RootScope rootCall = experiment.getRootScope(RootScopeType.CallerTree);
		
		if (rootCCT == null || rootCall == null)
			return null;
		
		if (isInitialized && !reset)
			return rootCall;
		
		//BusyIndicator.showWhile(getDisplay(), () -> {
		createTree(rootCCT, rootCall);
		//});
		isInitialized = true;
		
		return rootCall;
	}

	
	private void createTree(RootScope rootCCT, RootScope rootCall) {
		Job task = new Job("Create the tree") {
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				var progress = new ProgressReport(monitor);				
				final var db = getDatabase();
				
				db.createCallersView(rootCCT, rootCall, progress);

				return Status.OK_STATUS;
			}
		};
		task.schedule();

		try {
			task.join();
		} catch (InterruptedException e) {
			LoggerFactory.getLogger(getClass()).warn("Bottom-up tree is interrupted", e);
		}
	}
	
	@Override
	public RootScopeType getRootType() {
		return RootScopeType.CallerTree;
	}

	@Override
	protected IScopeTreeData getTreeData(RootScope root, IMetricManager metricManager) {
		return new BottomUpScopeTreeData(getDatabase(), root, metricManager);
	}

	@Override
	public RootScope getRoot() {
		IMetricManager mm = getMetricManager();
		Experiment experiment = (Experiment) mm;
		return experiment.getRootScope(RootScopeType.CallerTree);
	}
}
