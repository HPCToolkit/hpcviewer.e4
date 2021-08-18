package edu.rice.cs.hpctree;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.freeze.command.FreezeColumnCommand;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.AbstractHeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpctree.internal.IScopeTreeAction;


public class ScopeTreeTable extends Composite implements IScopeTreeAction
{
	private final NatTable natTable ;

	public ScopeTreeTable(Composite parent, int style, RootScope root) {
		this(parent, style, root, new ScopeTreeData(root));
	}
	
	public ScopeTreeTable(Composite parent, int style, RootScope root, ScopeTreeData treeData) {
		super(parent, style);

		setLayout(new GridLayout());
        
        // create a new ConfigRegistry which will be needed for GlazedLists
        // handling
        ConfigRegistry configRegistry = new ConfigRegistry();
        
        final ScopeTreeData treedata = new ScopeTreeData(root);
        final ScopeTreeBodyLayerStack bodyLayerStack = new ScopeTreeBodyLayerStack(root, 
        														 treedata, 
        														 (Experiment) root.getExperiment(), this);
        
        // build the column header layer
        IDataProvider columnHeaderDataProvider = new ColumnHeaderDataProvider(root);
        DataLayer columnHeaderDataLayer =
                new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
        ILayer columnHeaderLayer =
                new ColumnHeaderLayer(columnHeaderDataLayer, bodyLayerStack.getFreezeLayer(), bodyLayerStack.getSelectionLayer());
        SortHeaderLayer<Scope> headerLayer = new SortHeaderLayer<>(columnHeaderLayer, bodyLayerStack.getTreeRowModel());

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
                new CornerLayer(cornerDataLayer, rowHeaderLayer, headerLayer);

        // build the grid layer
        GridLayer gridLayer =
                new GridLayer(bodyLayerStack.getFreezeLayer(), headerLayer, rowHeaderLayer, cornerLayer);

        // turn the auto configuration off as we want to add our header menu
        // configuration
        natTable = new NatTable(this, gridLayer, false);

        // as the autoconfiguration of the NatTable is turned off, we have to
        // add the DefaultNatTableStyleConfiguration and the ConfigRegistry
        // manually
        natTable.setConfigRegistry(configRegistry);
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		natTable.addConfiguration(new SingleClickSortConfiguration());
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

        // need to launch in other time once the table is fully materialized 
        natTable.getDisplay().asyncExec(()-> {
            natTable.doCommand(new FreezeColumnCommand(natTable, 1, false, true));
        });
	}
	
	
	@Override
	public void refresh() {
		if (natTable != null)
			natTable.redraw();
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

}
