package edu.rice.cs.hpctree.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.freeze.CompositeFreezeLayer;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeLayer;
import org.eclipse.nebula.widgets.nattable.freeze.command.FreezeColumnCommand;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.tree.ITreeData;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.ui.menu.AbstractHeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric.AnnotationType;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric.VisibilityType;
import edu.rice.cs.hpcdata.experiment.metric.Metric;
import edu.rice.cs.hpcdata.experiment.metric.MetricType;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.scope.ProcedureScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpctree.ScopeTreeRowModel;
import edu.rice.cs.hpctree.internal.IScopeTreeAction;
import edu.rice.cs.hpctree.ScopeTreeData;
import edu.rice.cs.hpctree.ScopeTreeDataProvider;


public class TestMain implements IScopeTreeAction
{
	NatTable natTable ;
	
	@Override
	public void refresh() {
		if (natTable != null)
			natTable.redraw();
	}


	public static void main(String[] args) {
		System.out.println("Test begin");

		final Display display = new Display();
		final Shell   shell   = new Shell(display);
		shell.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		shell.setLayout(new FillLayout());
		shell.setText("Test Tree");
		
		TestMain tm = new TestMain();
		tm.create(shell);
		
		shell.open();
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		 
		display.dispose();

		System.out.println("Test end");
	}

	public void create(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout());

        RootScope root = createTree();
        
        // create a new ConfigRegistry which will be needed for GlazedLists
        // handling
        ConfigRegistry configRegistry = new ConfigRegistry();
        
        final BodyLayerStack bodyLayerStack = new BodyLayerStack(root, 
        														 new ScopeTreeData(root), 
        														 (Experiment) root.getExperiment(), this);
        
        // build the column header layer
        IDataProvider columnHeaderDataProvider = new ColumnHeaderDataProvider(root);
        DataLayer columnHeaderDataLayer =
                new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
        ILayer columnHeaderLayer =
                new ColumnHeaderLayer(columnHeaderDataLayer, bodyLayerStack.getFreezeLayer(), bodyLayerStack.getSelectionLayer());

        // build the row header layer
        IDataProvider rowHeaderDataProvider =
                new DefaultRowHeaderDataProvider(bodyLayerStack.getBodyDataProvider());
        DataLayer rowHeaderDataLayer =
                new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
        ILayer rowHeaderLayer =
                new RowHeaderLayer(rowHeaderDataLayer, bodyLayerStack, bodyLayerStack.getSelectionLayer());

        // build the corner layer
        IDataProvider cornerDataProvider =
                new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
        DataLayer cornerDataLayer =
                new DataLayer(cornerDataProvider);
        ILayer cornerLayer =
                new CornerLayer(cornerDataLayer, rowHeaderLayer, columnHeaderLayer);

        // build the grid layer
        GridLayer gridLayer =
                new GridLayer(bodyLayerStack.getFreezeLayer(), columnHeaderLayer, rowHeaderLayer, cornerLayer);

        // turn the auto configuration off as we want to add our header menu
        // configuration
        natTable = new NatTable(container, gridLayer, false);

        // as the autoconfiguration of the NatTable is turned off, we have to
        // add the DefaultNatTableStyleConfiguration and the ConfigRegistry
        // manually
        natTable.setConfigRegistry(configRegistry);
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.addConfiguration(new AbstractHeaderMenuConfiguration(natTable) {
            @Override
            protected PopupMenuBuilder createColumnHeaderMenu(NatTable natTable) {
            	return super.createColumnHeaderMenu(natTable)
                        .withHideColumnMenuItem()
                        .withShowAllColumnsMenuItem()
                        .withColumnChooserMenuItem()
                        .withFreezeColumnMenuItem();
            }
        });
        natTable.refresh();
        natTable.configure();

        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        natTable.getDisplay().asyncExec(()-> {
            natTable.doCommand(new FreezeColumnCommand(natTable, 1, false, true));
        });

        //natTable.doCommand(new FreezeColumnCommand(bodyLayerStack.getTreeLayer(), 1));
	}
	
	private RootScope createTree() {
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
		
		RootScope root = new RootScope(experiment, "root", RootScopeType.DatacentricTree);
		createMetric(root, experiment);
		
		for (int i=0; i<20000; i++) {
			Scope child = new ProcedureScope(root, null, i, i, "Proc " + i, false, i, i, null, 0);
			child.setParentScope(root);
			createMetric(child, experiment);
 			root.addSubscope(child);
			
			for(int j=0; j<30; j++) {
				Scope grandChild = new ProcedureScope(root, null, i, i, "g-proc " + i +", " + j, false, i, i, null, 0);
				grandChild.setParentScope(child);
				createMetric(grandChild, experiment);
				child.addSubscope(grandChild);
			}
		}		
		return root;
	}
	
	private void createMetric(Scope scope, Experiment exp) {
		Random r = new Random();
		
		for(int i=0; i<exp.getMetricCount(); i++) {
			if (r.nextInt(20) == 0)
				continue;
			
			MetricValue mv = new MetricValue(r.nextInt(10));
			scope.setMetricValue(i, mv);
		}
		
	}
	
	private static class ColumnHeaderDataProvider implements IDataProvider
	{
		private final Experiment exp;
		
		public ColumnHeaderDataProvider(RootScope root) {
			exp = (Experiment) root.getExperiment();			
		}
		@Override
		public Object getDataValue(int columnIndex, int rowIndex) {
			if (columnIndex == 0)
				return "Tree scope";
			
			return exp.getMetric(columnIndex-1).getDisplayName();
		}

		@Override
		public void setDataValue(int columnIndex, int rowIndex, Object newValue) {}

		@Override
		public int getColumnCount() {
			return 1 + exp.getMetricCount();
		}

		@Override
		public int getRowCount() {
			return 1;
		}
		
	}
	
    /**
     * Always encapsulate the body layer stack in an AbstractLayerTransform to
     * ensure that the index transformations are performed in later commands.
     *
     * @param <T>
     */
    class BodyLayerStack extends AbstractLayerTransform  
    {

        private final IDataProvider bodyDataProvider;
        private final SelectionLayer selectionLayer;
        private final TreeLayer treeLayer;
        private final ViewportLayer viewportLayer;
        private final FreezeLayer freezeLayer ;
        private final CompositeFreezeLayer compositeFreezeLayer ;

        public BodyLayerStack(RootScope root,
        					  ITreeData<Scope> treeData,
        					  Experiment experiment,
        					  IScopeTreeAction treeAction) {

            this.bodyDataProvider = new ScopeTreeDataProvider(treeData, experiment); 
            DataLayer bodyDataLayer = new DataLayer(this.bodyDataProvider);

            // simply apply labels for every column by index
            bodyDataLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());

            ScopeTreeRowModel treeRowModel = new ScopeTreeRowModel((ITreeData<Scope>) treeData, treeAction);

            this.selectionLayer = new SelectionLayer(bodyDataLayer);
            this.treeLayer = new TreeLayer(this.selectionLayer, treeRowModel);
            this.viewportLayer = new ViewportLayer(this.treeLayer);
            this.freezeLayer = new FreezeLayer(treeLayer);
            compositeFreezeLayer = new CompositeFreezeLayer(freezeLayer, viewportLayer, selectionLayer);
            
            treeLayer.expandTreeRow(0);
            
            setUnderlyingLayer(compositeFreezeLayer);
        }

        public SelectionLayer getSelectionLayer() {
            return this.selectionLayer;
        }

        public TreeLayer getTreeLayer() {
            return this.treeLayer;
        }
        
        public ViewportLayer getViewportLayer() {
        	return this.viewportLayer;
        }

        public CompositeFreezeLayer getFreezeLayer() {
			return compositeFreezeLayer;
		}

		public IDataProvider getBodyDataProvider() {
            return this.bodyDataProvider;
        }
    }

}
