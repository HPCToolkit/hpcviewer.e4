package edu.rice.cs.hpctree;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeHelper;
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
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.util.OSValidator;
import edu.rice.cs.hpctree.internal.IScopeTreeAction;
import edu.rice.cs.hpctree.internal.MetricTableRegistryConfiguration;
import edu.rice.cs.hpctree.internal.TableConfigLabelProvider;


public class ScopeTreeTable extends Composite implements IScopeTreeAction
{
	private final static float  FACTOR_BOLD_FONT   = 1.2f;
	private final static String TEXT_METRIC_COLUMN = "|8x88+88xx888x8%--";

	private final NatTable natTable ;
	private final ScopeTreeBodyLayerStack bodyLayerStack ;
	private final IDataProvider columnHeaderDataProvider ;

	public ScopeTreeTable(Composite parent, int style, RootScope root, IMetricManager metricManager) {
		this(parent, style, new ScopeTreeData(root, metricManager));
	}
	
	public ScopeTreeTable(Composite parent, int style, IScopeTreeData treeData) {
		super(parent, style);

		setLayout(new GridLayout());
        
        // create a new ConfigRegistry which will be needed for GlazedLists
        // handling
        ConfigRegistry configRegistry = new ConfigRegistry();
        
        bodyLayerStack = new ScopeTreeBodyLayerStack(treeData,  this);
        bodyLayerStack.setConfigLabelAccumulator(new TableConfigLabelProvider());

        // --------------------------------
        // build the column header layer
        // --------------------------------
        columnHeaderDataProvider = new ColumnHeaderDataProvider(treeData.getMetricManager());
        DataLayer columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
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
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(natTable);
	}
	
	@Override
	public void pack() {

		DataLayer bodyDataLayer = bodyLayerStack.getBodyDataLayer();
		
    	// tree column
        bodyDataLayer.setColumnWidthByPosition(0, 350);

        // metric columns (if any)
    	Point columnSize = getMetricColumnSize(natTable.getDisplay());
    	int numColumns   = columnHeaderDataProvider.getColumnCount();
    	
    	GC gc = new GC(getDisplay());
    	
    	for(int i=1; i<numColumns; i++) {
    		String title = (String) columnHeaderDataProvider.getDataValue(i, 0);
    		Point titleSize = gc.textExtent(title + "XXX");
    		int colWidth = (int) Math.max(titleSize.x * FACTOR_BOLD_FONT, columnSize.x);
    		
        	bodyDataLayer.setColumnWidthByPosition(i, colWidth);
    	}
    	gc.dispose();

        // need to freeze the first column once the table is fully materialized 
    	// The position for top-left and the bottom-right are both (0, -1) so
    	// we just need one variable to specify the position coordinate
    	
    	PositionCoordinate pc = new PositionCoordinate(bodyLayerStack, 0, -1);
    	FreezeHelper.freeze(bodyLayerStack.getFreezeLayer(), bodyLayerStack.getViewportLayer(), pc, pc);
	}
	
	
	public Point getMetricColumnSize(Display display) {
		final GC gc = new GC(display);		
		
		gc.setFont(MetricTableRegistryConfiguration.getMetricFont());
		String text = TEXT_METRIC_COLUMN;
		if (OSValidator.isWindows()) {
			
			// FIXME: ugly hack to add some spaces for Windows
			// Somehow, Windows 10 doesn't allow to squeeze the text inside the table
			// we have to give them some spaces (2 spaces in my case).
			// A temporary fix for issue #37
			text += "xx";
		}
		Point extent = gc.stringExtent(text);
		Point size   = new Point((int) (extent.x * FACTOR_BOLD_FONT), extent.y + 2);
		
		// check the height if we use generic font (tree column)
		// if this font is higher, we should use this height as the standard.
		
		gc.setFont(MetricTableRegistryConfiguration.getGenericFont());
		extent = gc.stringExtent(text);
		size.y = Math.max(size.y, extent.y);
		
		gc.dispose();
		
		return size;
	}

	
	@Override
	public void refresh() {
		if (natTable != null)
			natTable.redraw();
	}

	
	private static class ColumnHeaderDataProvider implements IDataProvider
	{
		private final List<BaseMetric> listMetrics;
		
		public ColumnHeaderDataProvider(IMetricManager metricManager) {
			listMetrics = metricManager.getVisibleMetrics();			
		}
		@Override
		public Object getDataValue(int columnIndex, int rowIndex) {
			if (columnIndex == 0)
				return "Scope";
			return listMetrics.get(columnIndex-1).getDisplayName();
		}

		@Override
		public void setDataValue(int columnIndex, int rowIndex, Object newValue) {}

		@Override
		public int getColumnCount() {
			return 1 + listMetrics.size();
		}

		@Override
		public int getRowCount() {
			return 1;
		}
		
	}

}
