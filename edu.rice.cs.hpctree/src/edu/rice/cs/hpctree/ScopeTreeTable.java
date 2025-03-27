// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctree;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
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
import org.eclipse.nebula.widgets.nattable.sort.event.SortColumnEvent;
import org.eclipse.nebula.widgets.nattable.style.theme.ThemeConfiguration;
import org.eclipse.nebula.widgets.nattable.tree.config.TreeLayerExpandCollapseKeyBindings;
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

import edu.rice.cs.hpcbase.Theme;
import org.hpctoolkit.db.local.experiment.metric.BaseMetric;
import org.hpctoolkit.db.local.experiment.metric.BaseMetric.AnnotationType;
import org.hpctoolkit.db.local.experiment.metric.BaseMetric.VisibilityType;
import org.hpctoolkit.db.local.experiment.metric.DerivedMetric;
import org.hpctoolkit.db.local.experiment.metric.format.MetricValuePredefinedFormat;
import org.hpctoolkit.db.local.experiment.scope.RootScope;
import org.hpctoolkit.db.local.experiment.scope.Scope;
import edu.rice.cs.hpcsetting.fonts.FontManager;
import edu.rice.cs.hpctree.action.IActionListener;
import edu.rice.cs.hpctree.internal.ColumnHeaderDataProvider;
import edu.rice.cs.hpctree.internal.ResizeListener;
import edu.rice.cs.hpctree.internal.ScopeTooltip;
import edu.rice.cs.hpctree.internal.ScopeTreeBodyLayerStack;
import edu.rice.cs.hpctree.internal.ScopeTreeDataProvider;
import edu.rice.cs.hpctree.internal.ScopeTreeLabelAccumulator;
import edu.rice.cs.hpctree.internal.config.ContextMenuConfiguration;
import edu.rice.cs.hpctree.internal.config.DarkScopeTableStyleConfiguration;
import edu.rice.cs.hpctree.internal.config.ScopeSortConfiguration;
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
public class ScopeTreeTable implements IScopeTreeAction, DisposeListener, ILayerListener, PropertyChangeListener
{
	private static final String TEXT_METRIC_COLUMN = "8x88+88xx888x8%";
	private static final String STRING_PADDING  = "X"; 

	private final NatTable       natTable ;
	private final ResizeListener resizeListener;

	private final DataLayer 			  columnHeaderDataLayer ;
	private final ScopeTreeBodyLayerStack bodyLayerStack ;
	private final ScopeTreeDataProvider   bodyDataProvider;
	private final TableConfiguration      tableConfiguration;
	private final SortHeaderLayer<Scope>  headerLayer;
		
	private final Collection<IActionListener> listeners;
	
	
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
        
		listeners = new FastList<>();
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
        natTable = new NatTable(parent, NatTable.DEFAULT_STYLE_OPTIONS, compositeLayer, false);
        
        // as the autoconfiguration of the NatTable is turned off, we have to
        // add the DefaultNatTableStyleConfiguration and the ConfigRegistry
        // manually
        natTable.setConfigRegistry(configRegistry);
        
        // special event handler to export the content of the table
        natTable.registerCommandHandler(new ExportCommandHandler(natTable));

        // --------------------------------
        // setup the configuration for natTable
        // --------------------------------

        tableConfiguration =  new TableConfiguration(parent, bodyDataProvider);
        var fontConfig = new TableFontConfiguration(this);
        natTable.addConfiguration(tableConfiguration);
        natTable.addConfiguration(fontConfig);
        natTable.addConfiguration(new ScopeTreeExportConfiguration(bodyLayerStack.getTreeRowModel()));
		natTable.addConfiguration(new ScopeSortConfiguration(this));
		natTable.addConfiguration(new ContextMenuConfiguration(this));
		natTable.addConfiguration(new TreeLayerExpandCollapseKeyBindings(bodyLayerStack.getTreeLayer(), 
																		 bodyLayerStack.getSelectionLayer()));
		
        // --------------------------------
        // finalization
        // --------------------------------
        
        natTable.configure();

        // --------------------------------
        // misc config
        // --------------------------------

        // add tooltip
        new ScopeTooltip(natTable, bodyDataProvider);

        // add theme configuration. automatically detect if we are in dark mode or not
        // this config happens only at the start of the table. It doesn't change automatically
        // in the middle of the system switch mode
        
        ThemeConfiguration defaultConfiguration = Theme.isDarkThemeActive() ? 
        			new DarkScopeTableStyleConfiguration(this.natTable, bodyDataProvider):
        			new ScopeTableStyleConfiguration(bodyDataProvider);

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
		treeData.getMetricManager().addMetricListener(this);
		
		// Fix issue #145: do not listen to table resizing
		// fix issue #199: resizing table should at least show 1 metric column
		resizeListener = new ResizeListener(this);
		natTable.addControlListener(resizeListener);
		natTable.getDisplay().addFilter(SWT.MouseDown, resizeListener);
		natTable.getDisplay().addFilter(SWT.MouseUp, resizeListener);

		// Fix issue #204: has to call header font configuration manually
		fontConfig.configureHeaderFont(natTable.getConfigRegistry());
        visualRefresh();
	}
	
	
	/****
	 * Get the nattable widget of this table
	 * @return
	 */
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
	
	
	/***
	 * Hide or show metric columns based on the visibility type.
	 * 
	 * @apiNote This method will force to hide metrics that should be hidden
	 *          as specified by metric's YAML file. 
	 */
	public void initializeHideShowMetricColumns() {
    	var hideShowLayer  = bodyLayerStack.getColumnHideShowLayer();
		var visibleColumns = hideShowLayer.getColumnCount();
		
		for(int i=1; i<visibleColumns; i++) {
        	
    		var metric = bodyDataProvider.getMetric(i);
    		
    		if (metric.getVisibility() == VisibilityType.HIDE)
    			hideColumn(i);
		}
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
		
	
	/****
	 * Retrieve the indexes of the hidden columns
	 * @return
	 */
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
	
	
	/*****
	 * Select a row in the table. It isn't guaranteed that the row will be made visible
	 * @param row
	 */
	public void setSelection(int row) {
		bodyLayerStack.getSelectionLayer().selectRow(0, row, false, false);
	}
	
	
	/*****
	 * Clear the current selection
	 */
	public void clearSelection() {
		bodyLayerStack.getSelectionLayer().clear();
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
			
			listeners.forEach(l -> l.select(scope));
			
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
		pack(false);
	}
	
	/***
	 * Resize all the visible metric columns
	 * 
	 * @param keepTreeColumn
	 * 			true if the tree column has to be kept persistent if possible
	 */
	public void pack(boolean keepTreeColumn) {		
		final int TREE_COLUMN_WIDTH  = 350;
		final int SORT_SYMBOL_WIDTH  = 11;
		
		// ---------------------------------------------------------------
		// pack the columns based on the title or the content of the cell
		// ---------------------------------------------------------------
		DataLayer bodyDataLayer = bodyLayerStack.getBodyDataLayer();

        // metric columns (if any)
		// the width is the max between the title of the column and the cell value 
		Point metricFontSize = getMetricColumnSize();
    	final ColumnHideShowLayer hideShowLayer = bodyLayerStack.getColumnHideShowLayer();
    	int visibleColumns  = hideShowLayer.getColumnCount();
    	
    	GC gc = new GC(natTable.getDisplay());
    	Font genericFont = FontManager.getFontGeneric();
    	Font metricFont  = FontManager.getMetricFont();
    	
    	gc.setFont(genericFont);
    	
    	TableFitting.ColumnFittingMode mode = TableFitting.getFittingMode();
    	
    	// the header needs to pad with 2 character to allow the triangle to be visible 
    	int totSize = 0;
    	int widthFirstMetricColumn = 0;
    	//
    	// compute and resize the metric columns first.
    	// the tree column is resized the last one since it has the highest priority and
    	// will get the width whatever remains (if exist)
    	//
    	for(int i=1; i<visibleColumns; i++) {
        	Point columnSize = metricFontSize;

        	int dataIndex = hideShowLayer.getColumnIndexByPosition(i);
    		var metric   = bodyDataProvider.getMetric(dataIndex);
    		
    		if (metric.getAnnotationType() == AnnotationType.NONE || 
    			metric instanceof DerivedMetric ||
    			metric.getDisplayFormat() instanceof MetricValuePredefinedFormat) {
    			
    			// since this metric has no annotation (like percent),
    			// we should compute its particular size.
    			// let assume the root always has the highest and longest text value.
    			// this assumption may be wrong, but it's better than traversing all the data to
    			// find the longest text
    			var textValue = metric.getMetricTextValue(getRoot()) + STRING_PADDING;
    			
    			// Fix issue #203: use the metric font for the metric data 
    			gc.setFont(metricFont);
    			columnSize = gc.textExtent(textValue);
    			gc.setFont(genericFont);
    		}
    		int colWidth = columnSize.x;
    		
    		if (mode == TableFitting.ColumnFittingMode.FIT_BOTH) {
        		// List of metrics is based on column position, while the current display is based on index.
        		// We need to convert from an index to a position.
        		String title = metric.getDisplayName() + STRING_PADDING;
        		Point titleSize = gc.textExtent(title);
    			colWidth = Math.max(titleSize.x + SORT_SYMBOL_WIDTH, columnSize.x);
    		}
    		
    		int pixelWidth = GUIHelper.convertHorizontalDpiToPixel(colWidth);
        	bodyDataLayer.setColumnWidthByPosition(dataIndex, pixelWidth);
        	totSize += pixelWidth;
        	
        	if (i==1)
        		widthFirstMetricColumn = pixelWidth;
    	}
    	
    	// compute the size of the tree column
    	// if the total size is less than the display, we can use the percentage for the tree column
    	// otherwise we should specify explicitly the width
		int areaWidth = GUIHelper.convertHorizontalDpiToPixel(getTableWidth());
    	
    	// tree column: the width is the max between 
    	//  - TREE_COLUMN_WIDTH, 
    	//  - the current width 
    	//  - the calculated recommended width
		int recommendedWidth = areaWidth-totSize;
		int w = Math.max(TREE_COLUMN_WIDTH, recommendedWidth);
		if (keepTreeColumn) {
			int treeColumnWidth  = GUIHelper.convertHorizontalDpiToPixel(bodyDataLayer.getColumnWidthByPosition(0));
			w = Math.max(treeColumnWidth, w);
		}
		if (w >= areaWidth) {
			// avoid setting to negative number if areaWidth is smaller than widthFirstMetricColumn
			// This occurs during the unit test, but can also happen in the real world.
			w = Math.max(TREE_COLUMN_WIDTH - 10, areaWidth - widthFirstMetricColumn);
		}
		bodyDataLayer.setColumnWidthByPosition(0, w);
		
		// Now, compute the ideal size of the row's height
		int pixelV = GUIHelper.convertVerticalDpiToPixel(metricFontSize.y);

		bodyDataLayer.setDefaultRowHeight(pixelV + 4);
		columnHeaderDataLayer.setDefaultRowHeight(pixelV + 4);
		
    	gc.dispose();
	}
	
	
	private int getTableWidth() {
    	Rectangle area = natTable.getClientArea();
    	if (area.width < 10) {
    		area = natTable.getShell().getClientArea();
    		area.width -= 20;
    	}

    	return area.width;
	}

	private Point getMetricColumnSize() {
		final GC gc = new GC(natTable.getDisplay());		
		
		gc.setFont(FontManager.getMetricFont());
		String text = TEXT_METRIC_COLUMN + STRING_PADDING;
		Point extent = gc.stringExtent(text);
		Point size   = new Point(extent.x, extent.y + 2);
		
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
		if (natTable == null) {
			return;
		}
		final var reorderLayer = bodyLayerStack.getColumnReorderLayer();
		final var listOrder = reorderLayer.getColumnIndexOrder();
		
		natTable.refresh();
		
		var newListOrder = reorderLayer.getColumnIndexOrder();
		int diff = newListOrder.size() - listOrder.size();
		
		// Fix issue #214 (metric column position is accidentally reset)
		//
		// Let assume the tree column (index=0) is always constant
		// This means we need to move the column to the original order
		// plus the difference between old list and the new list
		// If the list of the original one:
		//   [0, 3, 1, 4, 2]
		// and the new reset list:
		//   [0, 1, 2, 3, 4, 5]
		// so the new position should be:
		//   [0, 1, 4, 2, 3, 5]
		//   [0, 1, 4, 2, 5, 3]
		//
		// Since the tree column is static (always 0 position)
		// then the index 3 (now its index is 4) has to move to position 1+1, and 
		// index 4 (now its index is 5) moves to position 3+1
		for(int i=1; i<listOrder.size(); i++) {
			int order1 = listOrder.get(i);

			final var oldPosition = order1 + diff;
			final var newPosition = newListOrder.get(i + diff);
			
			if (newPosition == oldPosition) 
				continue;
			
			reorderLayer.reorderColumnPosition(oldPosition, i+diff);
			newListOrder = reorderLayer.getColumnIndexOrder();
		}
	}

	
	/****
	 * Return the list of the path of the selected node (if exist).
	 * The path must contain the list of cct id from the root to the
	 *  selected node.
	 *  
	 *  @see expandAndSelectNode
	 *  
	 * @return array of cct id if a node is selected, empty otherwise.
	 */
	public int[] getPathOfSelectedNode() {
		Scope currentNode = getSelection();
		if (currentNode == null)
			return new int[0];
		
		IScopeTreeData treeData = (IScopeTreeData) this.bodyLayerStack.getTreeRowModel().getTreeData();	
		FastList<Scope> reversePath = null;
		int []expandedNodes = null;

		// preserve the selection
		reversePath  = (FastList<Scope>) treeData.getPath(currentNode);
		expandedNodes = new int[reversePath.size()];
		
		int i=0;
		for(var scope: reversePath) {
			expandedNodes[i] = scope.getCCTIndex();
			i++;
		}
		return expandedNodes;
	}
	
	
	/****
	 * Expand the tree for a given path, and select the last row.
	 *  
	 * @param expandedNodes
	 * 			Path from root to the last node. This argument is usually 
	 * 			the result of {@code getPathOfSelectedNode}
	 * 
	 * @see getPathOfSelectedNode
	 */
	public void expandAndSelectNode(int []expandedNodes) {		
		final ScopeTreeLayer treeLayer = bodyLayerStack.getTreeLayer();

		int lastRow = 0;
		for(var cctId: expandedNodes) {
			int row = bodyDataProvider.indexOfRowBasedOnCCT(cctId);
			if (row < 0) {
				break;
			}
			lastRow = row;
			treeLayer.expandTreeRow(row);
		}
		// select the latest common path
		this.setSelection(lastRow);
	}

	
	/*****
	 * The same as refresh, but this time it will reset the root of the tree
	 * and reconstruct completely the tree.
	 */
	public void reset(RootScope root) {
		if (natTable == null)
			return;

		// this will disruptively change the table structure and the metric as well
		// the method setRoot should keep listen to the list of metrics
		setRoot(root);
		
		// new data: need to add listener to the change in the metrics
		IScopeTreeData treeData = (IScopeTreeData) this.bodyLayerStack.getTreeRowModel().getTreeData();	
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
        	
        	final Scope node = getSelection();
        	if (node != null) {
    			listeners.forEach(l -> l.select(node));
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
		dispose();
	}
	
	
	public void dispose() {
		if (listeners == null || listeners.isEmpty())
			return;
		
		natTable.getDisplay().removeFilter(SWT.MouseDown, resizeListener);
		natTable.getDisplay().removeFilter(SWT.MouseUp, resizeListener);
		natTable.removeControlListener(resizeListener);
		
		IScopeTreeData treeData = (IScopeTreeData) bodyLayerStack.getTreeRowModel().getTreeData();	
		treeData.getMetricManager().removeMetricListener(this);

        bodyLayerStack.getSelectionLayer().removeLayerListener(this);
        
        bodyLayerStack.dispose();
        
        listeners.clear();
	}
	
	@Override
	public void traverseOrExpand(int index) {
		ScopeTreeRowModel treeRowModel = bodyLayerStack.getTreeRowModel();
		Scope scope = treeRowModel.getTreeData().getDataAtIndex(index);
		traverseOrExpand(scope);
	}
	
	
	@Override
	public List<Scope> traverseOrExpand(Scope scope) {
		ScopeTreeRowModel treeRowModel = bodyLayerStack.getTreeRowModel();
		if (!treeRowModel.isChildrenVisible(scope)) {
			int index = treeRowModel.getTreeData().indexOf(scope);
			bodyLayerStack.expand(index);
		}
		return treeRowModel.getTreeData().getChildren(scope);
	}
	
	
	@Override
	public int getSortedColumn() {
		ScopeTreeRowModel treeRowModel = bodyLayerStack.getTreeRowModel();
		List<Integer> sortedIndexes = treeRowModel.getSortedColumnIndexes();
		Integer sortedIndex = sortedIndexes.get(0);

		Collection<Integer> hiddenIndexes = bodyLayerStack.getColumnHideShowLayer().getHiddenColumnIndexes();
		if (hiddenIndexes != null && 
			!hiddenIndexes.isEmpty() &&
			hiddenIndexes.contains(sortedIndex)) {
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


	@Override
	/**
	 * {@inheritDoc}
	 * <p>
	 * Called when there is an update in the list of metrics (like a new metric).
	 * We want the listener for metric changes to be here so that we can be sure
	 * to make some adaptation AFTER refreshing the tree data. 
	 */
	public void propertyChange(PropertyChangeEvent evt) {
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
		int []shiftedIndexes = Arrays.copyOf(hiddenIndexes, hiddenIndexes.length);
		
		ColumnHideShowLayer hideShowLayer = bodyLayerStack.getColumnHideShowLayer();

		refresh();

		if (evt.getPropertyName().equals(org.hpctoolkit.db.local.event.EventList.PROPERTY_INSERT)) {
			var newValue = evt.getNewValue();
			if (newValue instanceof BaseMetric metric) {
				int index = metric.getIndex();
				for(int i=0; i<hiddenIndexes.length; i++)
					shiftedIndexes[i] = shiftedIndexes[i] + index + SHIFTED_INDEX;
				
				hideShowLayer.showAllColumns();
				hideShowLayer.hideColumnIndexes(shiftedIndexes);
			}
		}
		pack();
	}
}
