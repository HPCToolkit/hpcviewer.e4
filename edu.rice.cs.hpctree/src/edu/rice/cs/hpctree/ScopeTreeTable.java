package edu.rice.cs.hpctree;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.VisualRefreshCommand;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.export.command.ExportCommand;
import org.eclipse.nebula.widgets.nattable.export.command.ExportCommandHandler;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeHelper;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiColumnShowCommand;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.painter.layer.NatGridLayerPainter;
import org.eclipse.nebula.widgets.nattable.selection.event.RowSelectionEvent;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.sort.event.SortColumnEvent;
import org.eclipse.nebula.widgets.nattable.style.theme.ThemeConfiguration;
import org.eclipse.nebula.widgets.nattable.tooltip.NatTableContentTooltip;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.command.ShowRowInViewportCommand;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.TreeNode;
import edu.rice.cs.hpcdata.util.string.StringUtil;
import edu.rice.cs.hpcsetting.fonts.FontManager;
import edu.rice.cs.hpctree.action.IActionListener;
import edu.rice.cs.hpctree.internal.ColumnHeaderDataProvider;
import edu.rice.cs.hpctree.internal.ResizeListener;
import edu.rice.cs.hpctree.internal.ScopeTreeBodyLayerStack;
import edu.rice.cs.hpctree.internal.ScopeTreeDataProvider;
import edu.rice.cs.hpctree.internal.ScopeTreeLabelAccumulator;
import edu.rice.cs.hpctree.internal.config.DarkScopeTableStyleConfiguration;
import edu.rice.cs.hpctree.internal.config.ScopeTableStyleConfiguration;
import edu.rice.cs.hpctree.internal.config.ScopeTreeExportConfiguration;
import edu.rice.cs.hpctree.internal.config.TableConfiguration;
import edu.rice.cs.hpctree.internal.config.TableFontConfiguration;


/********************************************************************
 * 
 * Main hpcviewer dynamic table composite widget based on NatTable containing:
 * <ul>
 * <li>A tree column</li>
 * <li>Zero or more metric columns</li>
 * </ul>
 * This class is inherited from Composite class, hence can be treated 
 * like a composite for the layout.
 *
 ********************************************************************/
public class ScopeTreeTable implements IScopeTreeAction, DisposeListener, ILayerListener, ListEventListener<BaseMetric>
{
	private final static String TEXT_METRIC_COLUMN = "|8x88+88xx888x8%";
	private final static String STRING_PADDING  = "XX"; 

	private final NatTable       natTable ;
	private final ResizeListener resizeListener;

	private final DataLayer 			  columnHeaderDataLayer ;
	private final ScopeTreeBodyLayerStack bodyLayerStack ;
	private final ScopeTreeDataProvider   bodyDataProvider;
	private final TableConfiguration      tableConfiguration;
	private final SortHeaderLayer<Scope>  headerLayer;
		
	private final Collection<IActionListener> listeners = new FastList<IActionListener>();
	
	
	/**** 
	 * Default constructor by specifying the custom {@code IScopeTreeData}
	 * @param parent
	 * 			the parent widget, it has to be a composite
	 * @param style
	 * 			The style of the table (not used)
	 * @param treeData
	 * 			Instance of {@link IScopeTreeData}}
	 */
	public ScopeTreeTable(Composite parent, int style, IScopeTreeData treeData) {
        
        this.bodyDataProvider = new ScopeTreeDataProvider(treeData); 

        // create a new ConfigRegistry which will be needed for GlazedLists
        // handling
        ConfigRegistry configRegistry = new ConfigRegistry();
        
        // --------------------------------
        // setup the layers and their configurations
        // --------------------------------
        
        bodyLayerStack = new ScopeTreeBodyLayerStack(treeData, bodyDataProvider);
        bodyLayerStack.getBodyDataLayer().setConfigLabelAccumulator(new ScopeTreeLabelAccumulator(treeData));
        bodyLayerStack.getSelectionLayer().addLayerListener(this);

        tableConfiguration =  new TableConfiguration(parent, bodyDataProvider);
        bodyLayerStack.addConfiguration(tableConfiguration);

        // --------------------------------
        // build the column header layer
        // --------------------------------

        columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(new ColumnHeaderDataProvider(bodyDataProvider));
        ILayer columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer, bodyLayerStack, bodyLayerStack.getSelectionLayer());
        headerLayer = new SortHeaderLayer<>(columnHeaderLayer, bodyLayerStack.getTreeRowModel());
        headerLayer.addLayerListener(this);
        
        // --------------------------------
        // build the composite
        // --------------------------------
        
        CompositeLayer compositeLayer = new CompositeLayer(1, 2);
        compositeLayer.setChildLayer(GridRegion.COLUMN_HEADER, headerLayer, 0, 0);
        compositeLayer.setChildLayer(GridRegion.BODY, bodyLayerStack, 0, 1);
        
        // turn the auto configuration off as we want to add our header menu
        // configuration
        natTable = new NatTable(parent, NatTable.DEFAULT_STYLE_OPTIONS , compositeLayer, false);
        
        // as the autoconfiguration of the NatTable is turned off, we have to
        // add the DefaultNatTableStyleConfiguration and the ConfigRegistry
        // manually
        natTable.setConfigRegistry(configRegistry);
        
        // special event handler to export the content of the table
        natTable.registerCommandHandler(new ExportCommandHandler(natTable));

        // --------------------------------
        // setup the configuration for natTable
        // --------------------------------

        natTable.addConfiguration(new ScopeTreeExportConfiguration(bodyLayerStack.getTreeRowModel()));
        natTable.addConfiguration(new TableFontConfiguration(this));
		natTable.addConfiguration(new SingleClickSortConfiguration());

        // --------------------------------
        // finalization
        // --------------------------------
        
        // I don't know why we have to refresh the table here
        // However, without refreshing, the content will be weird
        visualRefresh();
        natTable.configure();

        // --------------------------------
        // misc config
        // --------------------------------

        // add tooltip
        new ScopeToolTip(natTable, bodyDataProvider);

        // add theme configuration. automatically detect if we are in dark mode or not
        // this config happens only at the start of the table. It doesn't change automatically
        // in the middle of the system switch mode
        
        ThemeConfiguration defaultConfiguration = Display.isSystemDarkTheme() ? 
        							new DarkScopeTableStyleConfiguration(this.natTable, bodyDataProvider) :  new ScopeTableStyleConfiguration(bodyDataProvider);
        natTable.setTheme(defaultConfiguration);

        // --------------------------------
        // table settings and layouts
        // --------------------------------

        freezeTreeColumn();
        
    	// Need to set the grid data and layout
    	// if not set here, the table will be weird. I don't know why.
    	
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.minimumHeight = 1;
        gridData.minimumWidth  = 1;
        natTable.setLayoutData(gridData);
		natTable.setLayerPainter(new NatGridLayerPainter(natTable, DataLayer.DEFAULT_ROW_HEIGHT));

        // --------------------------------
		// table listeners
        // --------------------------------

		natTable.addDisposeListener(this);
		
		resizeListener = new ResizeListener(this);
		parent.addControlListener(resizeListener);
		treeData.getMetricManager().addMetricListener(this);
	}
	
	
	public NatTable getTable() {
		return this.natTable;
	}
	
	/**
	 * Causes the receiver to have the keyboard focus, 
	 * such that all keyboard events will be delivered to it. 
	 * Focus reassignment will respect applicable platform constraints.
	 */
	public void setFocus() {
		natTable.setFocus();
	}
		
	
	/****
	 * Freeze the tree column. 
	 * This should be called after the table initialization and after the hide/show action. 
	 */
	public void freezeTreeColumn() {
        // need to freeze the first column once the table is fully materialized 
    	// The position for top-left and the bottom-right are both (0, -1) so
    	// we just need one variable to specify the position coordinate
    	
    	PositionCoordinate pc = new PositionCoordinate(bodyLayerStack, 0, -1);
    	FreezeHelper.freeze(bodyLayerStack.getFreezeLayer(), bodyLayerStack.getViewportLayer(), pc, pc);
	}
	
	
	/****
	 * Hide one or more columns
	 * @param columnIndexes int or int[] of column indexes
	 */
	public void hideColumn(int... columnIndexes) {
		// the MultiColumnHideCommand requires the column position, not the
		// index one. So we have to convert from index to position :-(
		ColumnHideShowLayer layer = bodyLayerStack.getColumnHideShowLayer();
		int []positions = layer.getColumnPositionsByIndexes(columnIndexes);
		
		natTable.doCommand(new MultiColumnHideCommand(layer, positions));
	}
	
	
	/****
	 * Show one or more columns.
	 * @param columnIndexes int or int[] of column indexes
	 */
	public void showColumn(int... columnIndexes) {
		natTable.doCommand(new MultiColumnShowCommand(columnIndexes));
	}
		
	
	public int[] getHiddenColumnIndexes() {
		ColumnHideShowLayer colLayer = bodyLayerStack.getColumnHideShowLayer();
		return colLayer.getHiddenColumnIndexesArray();
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
	
	public void addActionListener(IActionListener action) {
		tableConfiguration.addListener(action);
	}
	
	public void removeActionListener(IActionListener action) {
		tableConfiguration.removeListener(action);
	}

	@Override
	public void handleLayerEvent(ILayerEvent event) {

		if (event instanceof RowSelectionEvent) {
			RowSelectionEvent rowEvent = (RowSelectionEvent) event;
			int []indexes = rowEvent.getRowIndexes();
			if (indexes == null || indexes.length == 0)
				return;
			
			int row = rowEvent.getRowPositionToMoveIntoViewport();
			final Scope scope = bodyDataProvider.getRowObject(row);
			
			listeners.forEach(l -> {
				l.select(scope);
			});
		} else if (event instanceof SortColumnEvent) {
			Set<Range> ranges = bodyLayerStack.getSelectionLayer().getSelectedRowPositions();
			if (!ranges.isEmpty()) {
				Range range = ranges.iterator().next();
				if (range.start >= 0) {
					natTable.doCommand(new ShowRowInViewportCommand(range.start));
				}
			}
		}
	}

	
	/****
	 * Resize the columns based on the number of visible columns and the 
	 * size of the table (or area of the parent composite).
	 */
	public void pack() {		
		final int TREE_COLUMN_WIDTH  = 350;
		
		// ---------------------------------------------------------------
		// pack the columns based on the title or the content of the cell
		// ---------------------------------------------------------------
		DataLayer bodyDataLayer = bodyLayerStack.getBodyDataLayer();

        // metric columns (if any)
		// the width is the max between the title of the column and the cell value 
    	Point columnSize = getMetricColumnSize();
    	final ColumnHideShowLayer hideShowLayer = bodyLayerStack.getColumnHideShowLayer();
    	int visibleColumns  = hideShowLayer.getColumnCount();
    	GC gc = new GC(natTable.getDisplay());
    	Font genericFont = FontManager.getFontGeneric();
    	gc.setFont(genericFont);
    	
    	int totSize = 0;
    	//
    	// compute and resize the metric columns first.
    	// the tree column is resized the last one since it has the highest priority and
    	// will get the width whatever remains (if exist)
    	//
    	for(int i=1; i<visibleColumns; i++) {
    		// List of metrics is based on column position, while the current display is based on index.
    		// We need to convert from an index to a position.
    		int position = hideShowLayer.getColumnIndexByPosition(i);
    		String title = bodyDataProvider.getMetric(position).getDisplayName() + STRING_PADDING;
    		Point titleSize = gc.textExtent(title);
    		
    		int colWidth = (int) Math.max(titleSize.x , columnSize.x);
    		int pixelWidth = GUIHelper.convertHorizontalDpiToPixel(colWidth);
        	bodyDataLayer.setColumnWidthByPosition(i, pixelWidth);
        	totSize += pixelWidth;
    	}
    	
    	// compute the size of the tree column
    	// if the total size is less than the display, we can use the percentage for the tree column
    	// otherwise we should specify explicitly the width
    	Rectangle area = natTable.getClientArea();
    	if (area.width < 10) {
    		area = natTable.getShell().getClientArea();
    		area.width -= 20;
    	}

    	int areaWidth = GUIHelper.convertHorizontalDpiToPixel(area.width);
    	
    	// tree column: the width is the max between 
    	//  - TREE_COLUMN_WIDTH, 
    	//  - the current width 
    	//  - the calculated recommended width
		int w = Math.max(TREE_COLUMN_WIDTH, areaWidth-totSize);
		bodyDataLayer.setColumnWidthByPosition(0, w);
		
		// Now, compute the ideal size of the row's height
		// 1. size for generic font
		Point genericSize = gc.stringExtent(TEXT_METRIC_COLUMN);
		
		// 2. size for metric font 
		Font metricFont = FontManager.getMetricFont();
		gc.setFont(metricFont);
		Point metricSize = gc.stringExtent(TEXT_METRIC_COLUMN);
		
		int height = Math.max(metricSize.y, genericSize.y);
		int pixelV = GUIHelper.convertVerticalDpiToPixel(height);

		bodyDataLayer.setDefaultRowHeight(pixelV + 4);
		columnHeaderDataLayer.setDefaultRowHeight(pixelV + 4);
		
    	gc.dispose();
	}
	

	private Point getMetricColumnSize() {
		final GC gc = new GC(natTable.getDisplay());		
		
		gc.setFont(FontManager.getMetricFont());
		String text = TEXT_METRIC_COLUMN + STRING_PADDING;
		Point extent = gc.stringExtent(text);
		Point size   = new Point((int) (extent.x), extent.y + 2);
		
		// check the height if we use generic font (tree column)
		// if this font is higher, we should use this height as the standard.
		
		gc.setFont(FontManager.getFontGeneric());
		extent = gc.stringExtent(text);
		size.y = Math.max(size.y, extent.y);
		
		gc.dispose();
		return size;
	}

	
	@Override
	public void refresh() {
		if (natTable != null)
			natTable.refresh();
	}

	
	/*****
	 * The same as refresh, but this time it will reset the root of the tree
	 * and reconstruct completely the tree.
	 */
	public void reset(RootScope root) {
		if (natTable == null)
			return;
		
		// 1. Before updating the root: if the original tree has a selection,
		// we need to preserve it first so that we can restore the selection
		// after updating the root
		//
		Scope currentNode = getSelection();
		FastList<Scope> selectedPath = new FastList<>();
		IScopeTreeData treeData = (IScopeTreeData) this.bodyLayerStack.getTreeRowModel().getTreeData();		
		if (currentNode != null) {
			// preserve the selection
			selectedPath = (FastList<Scope>) treeData.getPath(currentNode);
		}
		
		// 2. reset the root node
		// this will disruptively change the table structure and the metric as well
		// the method setRoot should keep listen to the list of metrics
		setRoot(root);
		
		// 3. expand and restore the selection
		if (selectedPath.size() == 0)
			return;
		
		final ScopeTreeLayer treeLayer = bodyLayerStack.getTreeLayer();
		FastList<Scope> reversePath = selectedPath.reverseThis();
		int lastIndex = 0;
		int lastRow   = 0;
		
		for(lastIndex=0; lastIndex<reversePath.size(); lastIndex++) {
			Scope scope = reversePath.get(lastIndex);
			int row = bodyDataProvider.indexOfRowBasedOnCCT(scope.getCCTIndex());
			if (row < 0) {
				break;
			}
			lastRow = row;
			treeLayer.expandTreeRow(lastRow);
		}
		// select the latest common path
		this.setSelection(lastRow);
		
		// 4. new data: need to add listener to the change in the metrics
		treeData.getMetricManager().addMetricListener(this);
	}
	
	
	/***
	 * The same as {@code refresh} but more lightweight.
	 * Use this method if there is no structural changes
	 */
	public void visualRefresh() {
		if (natTable != null) {
			natTable.doCommand(new VisualRefreshCommand());
		}		
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


	/*****
	 * {@inheritDoc}
	 * <p>
	 * Called when there is an update in the list of metrics (like a new metric).
	 * We want the listener for metric changes to be here so that we can be sure
	 * to make some adaptation AFTER refreshing the tree data. 
	 */
	@Override
	public void listChanged(ListEvent<BaseMetric> listChanges) {
		/**
		 * How far the index to be shifted.
		 * Since in our case we only add ONE metric on the left,
		 * we only shift the existing columns ONE to the right.
		 */
		final int SHIFTED_INDEX = 1;
		
		// there is a change in the list of metrics
		IScopeTreeData treeData = (IScopeTreeData) bodyLayerStack.getTreeRowModel().getTreeData();
		treeData.refreshAndShift(SHIFTED_INDEX);		

		int []hiddenIndexes = getHiddenColumnIndexes();
		int []shiftedIndexes = new int[hiddenIndexes.length];
		for(int i=0; i<hiddenIndexes.length; i++) {
			shiftedIndexes[i] = hiddenIndexes[i];
		}
		
		refresh();
		
		while(listChanges.next()) {
			switch(listChanges.getType()) {
			case ListEvent.INSERT:
				
				int index = listChanges.getIndex();	
				for(int i=0; i<hiddenIndexes.length; i++)
					shiftedIndexes[i] = shiftedIndexes[i] + index + SHIFTED_INDEX;
				
				bodyLayerStack.getColumnHideShowLayer().showAllColumns();
				bodyLayerStack.getColumnHideShowLayer().hideColumnIndexes(shiftedIndexes);
			}
		}
		pack();
	}

	
	@Override
	public void setRoot(Scope root) {
		ScopeTreeRowModel treeRowModel = bodyLayerStack.getTreeRowModel();
		treeRowModel.setRoot(root);
		
		refresh();
		
		// expand the root and select the first child if exist        
        if (root.hasChildren()) {
        	ScopeTreeLayer treeLayer = bodyLayerStack.getTreeLayer();
        	treeLayer.expandTreeRow(0);
        	setSelection(1);
        	
        	Scope node = getSelection();
        	if (node != null) {
    			listeners.forEach(l -> {
    				l.select(node);
    			});
        	}
        }
		pack();	
	}

	
	@Override
	public Scope getRoot() {
		ScopeTreeRowModel treeRowModel = bodyLayerStack.getTreeRowModel();
		return treeRowModel.getRoot();
	}

	
	@Override
	public void widgetDisposed(DisposeEvent e) {
		((IScopeTreeData)bodyLayerStack
							.getTreeRowModel()
							.getTreeData())
								.getMetricManager()
								.removeMetricListener(this);
        bodyLayerStack.getSelectionLayer().removeLayerListener(this);
        natTable.getParent().removeControlListener(resizeListener);
	}
	
	
	@Override
	public void traverseOrExpand(int index) {
		ScopeTreeRowModel treeRowModel = bodyLayerStack.getTreeRowModel();
		Scope scope = treeRowModel.getTreeData().getDataAtIndex(index);
		traverseOrExpand(scope);
	}
	
	
	@Override
	public List<? extends TreeNode> traverseOrExpand(Scope scope) {
		ScopeTreeRowModel treeRowModel = bodyLayerStack.getTreeRowModel();
		if (!treeRowModel.isChildrenVisible(scope)) {
			int index = treeRowModel.getTreeData().indexOf(scope);
			bodyLayerStack.expand(index);
		}
		List<? extends TreeNode> children = treeRowModel.getTreeData().getChildren(scope);
		return children;
	}
	
	
	@Override
	public int getSortedColumn() {
		ScopeTreeRowModel treeRowModel = bodyLayerStack.getTreeRowModel();
		List<Integer> sortedIndexes = treeRowModel.getSortedColumnIndexes();
		Integer sortedIndex = sortedIndexes.get(0);

		Collection<Integer> hiddenIndexes = bodyLayerStack.getColumnHideShowLayer().getHiddenColumnIndexes();
		if (hiddenIndexes != null && hiddenIndexes.size()>0) {
			if (hiddenIndexes.contains(sortedIndex))
				return -1;
		}		
		return sortedIndex;
	}
	
	
	@Override
	public void export() {
		ExportCommand export = new ExportCommand(natTable.getConfigRegistry(), natTable.getShell());
		natTable.doCommand(export);
	}
	
	
	public BaseMetric getMetric(int columnIndex) {
		return bodyDataProvider.getMetric(columnIndex);
	}
	
	
	/************************************************************
	 * 
	 * Class to display tooltips only for column header and the tree column
	 *
	 ************************************************************/
	private static class ScopeToolTip extends NatTableContentTooltip
	{
		private final static int MAX_TOOLTIP_CHAR = 80;
		private final ScopeTreeDataProvider bodyDataProvider;

		public ScopeToolTip(NatTable natTable, ScopeTreeDataProvider bodyDataProvider) {
			super(natTable, GridRegion.BODY, GridRegion.COLUMN_HEADER);
			this.bodyDataProvider = bodyDataProvider;
		}
		
		@Override
	    protected String getText(Event event) {

	        int col = this.natTable.getColumnPositionByX(event.x);
	        int row = this.natTable.getRowPositionByY(event.y);
	        int colIndex = this.natTable.getColumnIndexByPosition(col);
	        int rowIndex = this.natTable.getRowIndexByPosition(row);
	        
	        // We only show the tooltip for column header and the tree column (col index = 0)
        	if (rowIndex == 0) {
        		// header of the table
        		if (colIndex > 0) {
	        		BaseMetric metric = bodyDataProvider.getMetric(colIndex);
	        		String name = metric.getDisplayName();
	        		String desc = StringUtil.wrapScopeName(metric.getDescription(), MAX_TOOLTIP_CHAR);
	        		if (desc == null)
	        			return name;
	        		return name + "\n" + desc;
        		}
        	}
        	if (colIndex == 0) {
        		String text = super.getText(event);
        		if (text != null && text.length() > 0) {
        			text = StringUtil.wrapScopeName(text, MAX_TOOLTIP_CHAR);
        		}
        		return text;
        	}
        	return null;
		}
	}
}
