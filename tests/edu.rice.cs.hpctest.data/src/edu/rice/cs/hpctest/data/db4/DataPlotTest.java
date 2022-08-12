package edu.rice.cs.hpctest.data.db4;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.db.version4.DataMeta;
import edu.rice.cs.hpcdata.db.version4.DataPlot;
import edu.rice.cs.hpcdata.db.version4.DataSummary;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpctest.util.TestDatabase;


public class DataPlotTest {

	private static DataPlot []dataPlot;
	private static DataSummary []dataSummary;
	private static DataMeta []dataMeta;
	private static Experiment []experiment;

	@BeforeClass
	public static void setUpBeforeClass() throws IOException {
		var paths = TestDatabase.getMetaDatabases();
		
		dataPlot = new DataPlot[paths.length];
		dataMeta = new DataMeta[paths.length];
		dataSummary = new DataSummary[paths.length];
		experiment  = new Experiment[paths.length];
		
		int i=0;
		
		for(var dbPath: paths) {			
			assertNotNull(dbPath);
			assertTrue(dbPath.isDirectory());
			
			IdTupleType tupleType = new IdTupleType();
			tupleType.initDefaultTypes();
			
			dataPlot[i] = new DataPlot();
			dataPlot[i].open(dbPath.getAbsolutePath());
			
			dataSummary[i] = new DataSummary(IdTupleType.createTypeWithOldFormat());
			dataSummary[i].open(dbPath.getAbsolutePath());
			
			experiment[i] = new Experiment();
			
			dataMeta[i] = new DataMeta();
			dataMeta[i].open(experiment[i], dbPath.getAbsolutePath());
			i++;
		}
	}



	@Test
	public void testGetPlotEntryIntInt() throws IOException {
		int i=0;
		for(var data: dataPlot) {
			RootScope root = experiment[i].getRootScope(RootScopeType.CallingContextTree);
			var metrics = experiment[i].getRawMetrics();
			if (metrics != null && data != null && root != null) {				
				checkMetric(dataSummary[i], data, root, metrics);
			}
			i++;
		}
	}
	
	private void checkMetric(DataSummary ds, DataPlot dp, Scope scope, List<BaseMetric> metrics) throws IOException {
		if (scope.hasChildren()) {
			for(Scope child: scope.getChildren()) {
				checkMetric(ds, dp, child, metrics);
			}
		}
		for(var m: metrics) {
			var metval = ds.getMetric(IdTuple.PROFILE_SUMMARY, scope.getCCTIndex(), m.getIndex());
			var dpe = dp.getPlotEntry(scope.getCCTIndex(), m.getIndex());
			if (dpe == null)
				continue;
			
			for(var val: dpe) {
				assertTrue(val.metval <= metval);
			}
		}
	}
}
