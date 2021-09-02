package edu.rice.cs.hpctree.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric.AnnotationType;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric.VisibilityType;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.metric.Metric;
import edu.rice.cs.hpcdata.experiment.metric.MetricType;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.scope.ProcedureScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpctree.ScopeTreeTable;


public class TestMain 
{
	NatTable natTable ;
	

	public static void main(String[] args) {
		System.out.println("Test begin");

		final Display display = new Display();
		final Shell   shell   = new Shell(display);
		shell.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		shell.setLayout(new FillLayout());
		shell.setText("Test Tree");
		
        RootScope root = createTree();

		ScopeTreeTable table = new ScopeTreeTable(shell, SWT.NONE, root, (IMetricManager) root.getExperiment());
		table.pack();
		
		shell.open();
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		 
		display.dispose();

		System.out.println("Test end");
	}


	
	private static RootScope createTree() {
		Experiment experiment = new Experiment();
		List<BaseMetric> listMetrics = new ArrayList<>();
		for (int i=0; i<20; i++) {
			BaseMetric metric = new Metric(String.valueOf(i), "Descr metric " + i, "Metric " + i, 
											VisibilityType.SHOW, null, AnnotationType.PERCENT, null, 
											i, 
											i%2 == 0 ? MetricType.INCLUSIVE : MetricType.EXCLUSIVE, 
											i%2 == 0 ? i - 1 : i + 1);
			listMetrics.add(metric);
		}
		experiment.setMetrics(listMetrics);
		
		RootScope root = new RootScope(experiment, "proc 0", RootScopeType.DatacentricTree);
		createMetric(root, experiment);
		createTreeNode(root, root, 1, 4, 1, 10);
		
		return root;
	}
	
	private static void createTreeNode(RootScope root, Scope parent, int id, int children, int level, int maxLevel) {

		for(int j=0; j<children; j++) {
			String myid = String.valueOf(level) +  String.valueOf(j);
			String parentName = parent instanceof RootScope ? "proc 0" : parent.getName();
			String name = parentName + "." +  myid;
			int intmyid = Integer.valueOf(myid);
			Scope grandChild = new ProcedureScope(root, null, 0, 0, name , false, intmyid, intmyid, null, 0);
			grandChild.setParentScope(parent);
			createMetric(grandChild, (Experiment) root.getExperiment());
			parent.addSubscope(grandChild);
			
			if (level < maxLevel) {
				createTreeNode(root, grandChild, intmyid, 3, level+1, maxLevel);
			}
		}
	}
	
	private static void createMetric(Scope scope, Experiment exp) {
		Random r = new Random();
		
		for(int i=0; i<exp.getMetricCount(); i++) {
			if (r.nextInt(20) == 0)
				continue;
			
			MetricValue mv = new MetricValue(r.nextInt(10));
			scope.setMetricValue(i, mv);
		}
		
	}
}
