package edu.rice.cs.hpctree;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.freeze.command.FreezeColumnCommand;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.AbstractHeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpctree.internal.IScopeTreeAction;
import edu.rice.cs.hpctree.internal.MetricTableRegistryConfiguration;
import edu.rice.cs.hpctree.internal.TableConfigLabelProvider;


public class ScopeTreeTable extends Composite implements IScopeTreeAction
{
	private final NatTable natTable ;

	public ScopeTreeTable(Composite parent, int style, RootScope root, IMetricManager metricManager) {
		this(parent, style, new ScopeTreeData(root, metricManager));
	}
	
	public ScopeTreeTable(Composite parent, int style, IScopeTreeData treeData) {
		super(parent, style);

		setLayout(new GridLayout());
        
        // create a new ConfigRegistry which will be needed for GlazedLists
        // handling
        ConfigRegistry configRegistry = new ConfigRegistry();
        
        final ScopeTreeBodyLayerStack bodyLayerStack = new ScopeTreeBodyLayerStack(treeData,  this);
        bodyLayerStack.setConfigLabelAccumulator(new TableConfigLabelProvider());

        // --------------------------------
        // build the column header layer
        // --------------------------------
        IDataProvider columnHeaderDataProvider = new ColumnHeaderDataProvider(treeData.getMetricManager());
        DataLayer columnHeaderDataLayer =
                new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
        ILayer columnHeaderLayer =
                new ColumnHeaderLayer(columnHeaderDataLayer, bodyLayerStack, bodyLayerStack.getSelectionLayer());
        SortHeaderLayer<Scope> headerLayer = new SortHeaderLayer<>(columnHeaderLayer, bodyLayerStack.getTreeRowModel());

        // --------------------------------
        // build the composite
        // --------------------------------
        
        CompositeLayer compositeLayer = new CompositeLayer(1, 2);
        compositeLayer.setChildLayer(GridRegion.COLUMN_HEADER, headerLayer, 0, 0);
        compositeLayer.setChildLayer(GridRegion.BODY, bodyLayerStack, 0, 1);
        

        // turn the auto configuration off as we want to add our header menu
        // configuration
        natTable = new NatTable(this, compositeLayer, false);

        // as the autoconfiguration of the NatTable is turned off, we have to
        // add the DefaultNatTableStyleConfiguration and the ConfigRegistry
        // manually
        natTable.setConfigRegistry(configRegistry);
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		natTable.addConfiguration(new SingleClickSortConfiguration());
		natTable.addConfiguration(new MetricTableRegistryConfiguration());
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
        // I don't know why we have to refresh the table here
        // However, without refreshing, the content will be weird
        natTable.refresh();
        natTable.configure();

        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        // need to launch in other time once the table is fully materialized 
        natTable.getDisplay().asyncExec(()-> {
            natTable.doCommand(new FreezeColumnCommand(natTable, 0, false, true));
        });
	}
	
	
	@Override
	public void refresh() {
		if (natTable != null)
			natTable.redraw();
	}

	
	private static class ColumnHeaderDataProvider implements IDataProvider
	{
		private final IMetricManager metricManager;
		
		public ColumnHeaderDataProvider(IMetricManager metricManager) {
			this.metricManager = metricManager;			
		}
		@Override
		public Object getDataValue(int columnIndex, int rowIndex) {
			if (columnIndex == 0)
				return "Scope";
			
			return metricManager.getMetric(columnIndex-1).getDisplayName();
		}

		@Override
		public void setDataValue(int columnIndex, int rowIndex, Object newValue) {}

		@Override
		public int getColumnCount() {
			return 1 + metricManager.getMetricCount();
		}

		@Override
		public int getRowCount() {
			return 1;
		}
		
	}

}
