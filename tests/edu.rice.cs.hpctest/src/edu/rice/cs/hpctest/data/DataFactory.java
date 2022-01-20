package edu.rice.cs.hpctest.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.IDatabaseRepresentation;
import edu.rice.cs.hpcdata.experiment.LocalDatabaseRepresentation;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.Metric;
import edu.rice.cs.hpcdata.experiment.metric.MetricType;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric.AnnotationType;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric.VisibilityType;
import edu.rice.cs.hpcdata.experiment.scope.ProcedureScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.Scope;


public class DataFactory 
{
	private static final int DEFAULT_DB = 10;
	
	
	public static List<Experiment> createExperiments() {
		return createExperiments(DEFAULT_DB);
	}
	
	public static List<Experiment> createExperiments(int numItems) {
		List<Experiment> list = new ArrayList<>(numItems);
		for (int i=0; i<numItems; i++) {
			Experiment exp = createExperiment("Database " + i);
			list.add(exp);
		}
		return list;
	}
	
	
	
	private static Experiment createExperiment(String name) {
		Experiment experiment = new Experiment();
		
		List<BaseMetric> listMetrics = new ArrayList<>();
		for (int i=0; i<20; i++) {
			BaseMetric metric = new Metric(String.valueOf(i), name + " Descr metric " + i, "Metric " + i, 
											VisibilityType.SHOW, null, AnnotationType.PERCENT, null, 
											i, 
											i%2 == 0 ? MetricType.INCLUSIVE : MetricType.EXCLUSIVE, 
											i%2 == 0 ? i - 1 : i + 1);
			listMetrics.add(metric);
		}
		experiment.setMetrics(listMetrics);
		IDatabaseRepresentation dbr = new LocalDatabaseRepresentation(new File(name), null, false);
		experiment.setDatabaseRepresentation(dbr);
		
		RootScope root = new RootScope(experiment, "proc 0", RootScopeType.DatacentricTree);
		createMetric(root, experiment);
		createTreeNode(root, root, 1, 4, 1, 10);

		return experiment;
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
