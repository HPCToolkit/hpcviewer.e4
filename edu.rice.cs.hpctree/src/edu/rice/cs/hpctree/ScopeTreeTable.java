package edu.rice.cs.hpctree;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeHelper;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.selection.config.RowOnlySelectionBindings;
import org.eclipse.nebula.widgets.nattable.selection.event.RowSelectionEvent;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeExpandToLevelCommand;
import org.eclipse.nebula.widgets.nattable.ui.menu.AbstractHeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.TreeNode;
import edu.rice.cs.hpcdata.util.OSValidator;
import edu.rice.cs.hpcsetting.fonts.FontManager;
import edu.rice.cs.hpctree.action.IActionListener;
import edu.rice.cs.hpctree.internal.ColumnHeaderDataProvider;
import edu.rice.cs.hpctree.internal.ScopeTreeLabelAccumulator;
import edu.rice.cs.hpctree.resources.IconManager;



public class ScopeTreeTable extends Composite implements IScopeTreeAction, DisposeListener, ILayerListener
{
	private final static float  FACTOR_BOLD_FONT   = 1.2f;
	private final static String TEXT_METRIC_COLUMN = "|8x88+88xx888x8%--";

	private final NatTable natTable ;
	private final ScopeTreeBodyLayerStack bodyLayerStack ;
	private final IDataProvider columnHeaderDataProvider ;
	private final ScopeTreeDataProvider bodyDataProvider;
	private final Collection<IActionListener> listeners = new FastList<IActionListener>();

	public ScopeTreeTable(Composite parent, int style, RootScope root, IMetricManager metricManager) {
		this(parent, style, new ScopeTreeData(root, metricManager));
	}
	
	public ScopeTreeTable(Composite parent, int style, IScopeTreeData treeData) {
		super(parent, style);

		setLayout(new GridLayout());
        
        this.bodyDataProvider = new ScopeTreeDataProvider(treeData); 

        // create a new ConfigRegistry which will be needed for GlazedLists
        // handling
        ConfigRegistry configRegistry = new ConfigRegistry();
        
        bodyLayerStack = new ScopeTreeBodyLayerStack(treeData, bodyDataProvider, this);
        bodyLayerStack.setConfigLabelAccumulator(new ScopeTreeLabelAccumulator(treeData));

        bodyLayerStack.getSelectionLayer().addLayerListener(this);

        setConfigRegistry(configRegistry);
        
        // --------------------------------
        // build the column header layer
        // --------------------------------
        columnHeaderDataProvider = new ColumnHeaderDataProvider(bodyDataProvider);
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
		natTable.addConfiguration(new RowOnlySelectionBindings());
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
        // I don't know why we have to refresh the table here
        // However, without refreshing, the content will be weird
        natTable.refresh();
        natTable.configure();

        // need to freeze the first column once the table is fully materialized 
    	// The position for top-left and the bottom-right are both (0, -1) so
    	// we just need one variable to specify the position coordinate
    	
    	PositionCoordinate pc = new PositionCoordinate(bodyLayerStack, 0, -1);
    	FreezeHelper.freeze(bodyLayerStack.getFreezeLayer(), bodyLayerStack.getViewportLayer(), pc, pc);

    	// Need to set the grid data and layout
    	// if not set here, the table will be weird. I don't know why.
    	
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(natTable);

		addDisposeListener(this);
	}

	private void setConfigRegistry(ConfigRegistry configRegistry) {

		addIconLabel(configRegistry, IconManager.Image_CallTo, ScopeTreeLabelAccumulator.LABEL_CALLSITE);
		addIconLabel(configRegistry, IconManager.Image_CallToDisabled, ScopeTreeLabelAccumulator.LABEL_CALLSITE_DISABLED);

		addIconLabel(configRegistry, IconManager.Image_CallFrom, ScopeTreeLabelAccumulator.LABEL_CALLER);
		addIconLabel(configRegistry, IconManager.Image_CallFromDisabled, ScopeTreeLabelAccumulator.LABEL_CALLER_DISABLED);
		
		// configuration for metric column
		//
		final Font fontMetric  = getMetricFont();
		final Style styleMetric = new Style();
		styleMetric.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
		styleMetric.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);
		styleMetric.setAttributeValue(CellStyleAttributes.FONT, fontMetric);
		
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
											   styleMetric, 
											   DisplayMode.NORMAL, 
											   ScopeTreeLabelAccumulator.LABEL_METRICOLUMN);
		
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
											   styleMetric, 
											   DisplayMode.SELECT, 
											   ScopeTreeLabelAccumulator.LABEL_METRICOLUMN);

		// configuration for tree column
		//
		final Font fontGeneric = getGenericFont();
		final Style styleTree  = new Style();
		styleTree.setAttributeValue(CellStyleAttributes.FONT, fontGeneric);

		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
											   styleTree, 
											   DisplayMode.NORMAL, 
											   ScopeTreeLabelAccumulator.LABEL_TREECOLUMN);
		
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
				   							   styleTree, 
											   DisplayMode.SELECT, 
											   ScopeTreeLabelAccumulator.LABEL_TREECOLUMN);
	}
	
	
	private void addIconLabel(ConfigRegistry configRegistry, String imageName, String label) {
		IconManager iconManager = IconManager.getInstance();
		
		ImagePainter imagePainter = new ImagePainter(iconManager.getImage(imageName));
		CellPainterDecorator cellPainter = new CellPainterDecorator(new TextPainter(), CellEdgeEnum.LEFT, imagePainter);
		
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, 
											   cellPainter, 
											   DisplayMode.NORMAL, 
											   label);
		
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, 
											   cellPainter, 
											   DisplayMode.SELECT, 
											   label);
	}
	
	@Override
	public Scope getSelection() {
		Set<Range> ranges = bodyLayerStack.getSelectionLayer().getSelectedRowPositions();
		for(Range r: ranges) {
			Scope s = bodyDataProvider.getRowObject(r.start);
			if (s != null) {
				return s;
			}
		}
		return null;
	}
	
	public void setSelection(int row) {
		bodyLayerStack.getSelectionLayer().selectRow(0, row, false, false);
		
	}
	
	public int indexOf(Scope scope) {
		return bodyDataProvider.indexOfRowObject(scope);
	}
	
	public void addSelectionListener(IActionListener listener) {
		listeners.add(listener);
	}
	
	public void removeSelectionListener(IActionListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void handleLayerEvent(ILayerEvent event) {
		if (event instanceof RowSelectionEvent) {
			RowSelectionEvent rowEvent = (RowSelectionEvent) event;
			int row = rowEvent.getRowPositionToMoveIntoViewport();
			final Scope scope = bodyDataProvider.getRowObject(row);
			
			listeners.forEach(l -> {
				l.select(scope);
			});
		}
	}

	
	@Override
	public void pack() {		
		final int TREE_COLUMN_WIDTH  = 350;
		
		super.pack();

		// ---------------------------------------------------------------
		// pack the columns based on the title or the content of the cell
		// ---------------------------------------------------------------
		DataLayer bodyDataLayer = bodyLayerStack.getBodyDataLayer();
		
    	// tree column: the width is hard coded at the moment 
        bodyDataLayer.setColumnWidthByPosition(0, TREE_COLUMN_WIDTH);

        // metric columns (if any)
    	Point columnSize = getMetricColumnSize();
    	int numColumns   = columnHeaderDataProvider.getColumnCount();
    	
    	GC gc = new GC(getDisplay());
    	
    	for(int i=1; i<numColumns; i++) {
    		String title = (String) columnHeaderDataProvider.getDataValue(i, 0);
    		Point titleSize = gc.textExtent(title + "XXX");
    		int colWidth = (int) Math.max(titleSize.x * FACTOR_BOLD_FONT, columnSize.x);
    		
        	bodyDataLayer.setColumnWidthByPosition(i, colWidth);
    	}
    	gc.dispose();
	}
	
	
	private static Font getMetricFont() {
		Font font ;
		try {
			font = FontManager.getMetricFont();
		} catch (Exception e) {
			font = JFaceResources.getTextFont();
		}
		return font;
	}

	private static Font getGenericFont() {
		Font font;
		try {
			font = FontManager.getFontGeneric();
		} catch (Exception e) {
			font = JFaceResources.getDefaultFont();
		}
		return font;
	}

	private Point getMetricColumnSize() {
		final GC gc = new GC(natTable.getDisplay());		
		
		gc.setFont(getMetricFont());
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
		
		gc.setFont(getGenericFont());
		extent = gc.stringExtent(text);
		size.y = Math.max(size.y, extent.y);
		
		gc.dispose();
		
		return size;
	}

	
	@Override
	public void refresh() {
		if (natTable != null)
			natTable.refresh();;
	}
	
	
	/***
	 * Redraw the painting of the table.
	 * This doesn't refresh the structure of the table.
	 */
	public void redraw() {
		if (natTable != null) {
			natTable.redraw();
		}
	}
	
	@Override
	public void expand(int index) {
		bodyLayerStack.expand(index);
		if (natTable != null) {
			natTable.doCommand(new TreeExpandToLevelCommand(index, 1));
		}
	}

	@Override
	public void setRoot(Scope root) {
		ScopeTreeRowModel treeRowModel = bodyLayerStack.getTreeRowModel();
		treeRowModel.setRoot(root);
		
		this.refresh();
	}

	@Override
	public Scope getRoot() {
		ScopeTreeRowModel treeRowModel = bodyLayerStack.getTreeRowModel();
		return treeRowModel.getRoot();
	}

	@Override
	public void widgetDisposed(DisposeEvent e) {
        bodyLayerStack.getSelectionLayer().removeLayerListener(this);
	}
	
	
	@Override
	public List<? extends TreeNode> expand(Scope scope) {
		ScopeTreeRowModel treeRowModel = bodyLayerStack.getTreeRowModel();
		int index = treeRowModel.getTreeData().indexOf(scope);
		if (treeRowModel.isCollapsed(scope)) {
			TreeLayer treeLayer = bodyLayerStack.getTreeLayer();
			treeLayer.expandOrCollapseIndex(index);
		}
		return treeRowModel.getDirectChildren(index);
	}
	
	
	@Override
	public int getSortedColumn() {
		ScopeTreeRowModel treeRowModel = bodyLayerStack.getTreeRowModel();
		return treeRowModel.getSortedColumnIndexes().get(0);
	}
	
	public BaseMetric getMetric(int columnIndex) {
		return bodyDataProvider.getMetric(columnIndex);
	}
}
