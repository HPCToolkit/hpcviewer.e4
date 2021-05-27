/**
 * 
 */
package edu.rice.cs.hpcviewer.ui.internal;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import java.util.List;

import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;

import edu.rice.cs.hpcdata.experiment.BaseExperiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.DerivedMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.util.OSValidator;
import edu.rice.cs.hpcdata.util.ScopeComparator;
import edu.rice.cs.hpcsetting.fonts.FontManager;
import edu.rice.cs.hpcsetting.preferences.PreferenceConstants;
import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceManager;


/*************************************************************************
 * Main class for tree viewer by setting lazy virtual bit in this viewer
 *************************************************************************/
public class ScopeTreeViewer extends TreeViewer implements IPropertyChangeListener, Listener
{
	//public  final static String COLUMN_DATA_WIDTH  = "w"; 
	private final static float  FACTOR_BOLD_FONT   = 1.2f;
	private final static String TEXT_METRIC_COLUMN = "|8x88+88xx888x8%";
	
	private final DisposeListener disposeListener;

	private int metricColumnWidth;
	private int tableRowHeight;
	
	/**
	 * @param parent
	 * @param style
	 */
	public ScopeTreeViewer(Composite parent, int style) {
		// hack: on Windows, we have to add SWT.FULL_SELECTION to allow users
		//       to select a row in the table.
		// hack: We have to add SWT.MULTI to disable preserving selection. 
		//       This preservation is very costly for large database, and there
		//       is no official API to disable it, but SWT.MULTI did the job.
		// hack2: SWT.MULTI will cause performance degradation when sort a column
		//        we have to remove it !
		super(parent, SWT.VIRTUAL  | SWT.FULL_SELECTION | 
					  SWT.H_SCROLL | SWT.V_SCROLL 	    | 
					  /*SWT.MULTI    |*/ style );
		
		computeIdealCellBound();

		setUseHashlookup(true);
		getTree().setLinesVisible(true);
		
		PreferenceStore pref = ViewerPreferenceManager.INSTANCE.getPreferenceStore();
		pref.addPropertyChangeListener((IPropertyChangeListener) this);
		
		disposeListener = new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				dispose();
			}
		};
		
		getTree().addDisposeListener(disposeListener);

		// Fix bug #25: tooltip is not wrapped on MacOS 
		ColumnViewerToolTipSupport.enableFor(this, ToolTip.NO_RECREATE);
		
		getTree().addListener(SWT.MeasureItem, this);
	}
	
	
	/****
	 * Free allocated resources and unsubscribe events.
	 * This method has to be called once the table is disposed.
	 */
	public void dispose() {
		
		getTree().removeListener(SWT.MeasureItem, this);    		
		getTree().removeDisposeListener(disposeListener);

		PreferenceStore pref = ViewerPreferenceManager.INSTANCE.getPreferenceStore();
		pref.removePropertyChangeListener(this);
	}
	

	/****
	 * Retrieve the database used of this table
	 * @return BaseExperiment database
	 */
	public BaseExperiment getExperiment() {

		RootScope root = getRootScope();
		if (root != null)
			return root.getExperiment();
		
		return null;
	}
	
	
	/****
	 * Retrieve the root of the database in this table regardless
	 * whether the tree is zoomed or flattened.
	 * <p>
	 * <ul>
	 *  <li>For a Top-down view, the root will be the CCT root.
	 *  <li>For a bottom-up view, the root will be the caller root
	 *  <li>For a flat view, the root will be the flat root. 
	 * @return the root scope
	 */
	public RootScope getRootScope() {
		
		Object input = getInput();
		if (input instanceof RootScope) {
			return ((RootScope)input);
		}
		
		if (input instanceof Scope) {
			return ((Scope)input).getRootScope();
		}
		return null;
	}
    
	/**
	 * Return the canocalized text from the list of elements 
	 * @param sListOfTexts
	 * @param sSeparator
	 * @return
	 */
	public String getTextBasedOnColumnStatus(String []sListOfTexts, String sSeparator, 
			int startColIndex, int startTextIndex) {
		StringBuffer sBuf = new StringBuffer();
		TreeColumn columns[] = this.getTree().getColumns();
		for ( int i=startColIndex; i<columns.length; i++ ) {
			if ( columns[i].getWidth()>0 ) {
				if (sBuf.length()>0)
					sBuf.append(sSeparator);
				sBuf.append( sListOfTexts[i+startTextIndex] );
			}
		}
		return sBuf.toString();
	}
	
	
	/**
	 * retrieve the title of the columns
	 * @param iStartColIndex
	 * @param sSeparator
	 * @return
	 */
	public String getColumnTitle(int iStartColIndex, String sSeparator) {
		// get the column title first
		TreeColumn columns[] = this.getTree().getColumns();
		String sTitles[] = new String[columns.length];
		for ( int i=0; i<columns.length; i++ ) {
			sTitles[i] = "\"" + columns[i].getText().trim() + "\"";
		}
		// then get the string based on the column status
		return this.getTextBasedOnColumnStatus(sTitles, sSeparator, iStartColIndex, 0);
	}
	
	
	/****
	 * refresh the title of all metric columns.
	 * <p/>
	 * warning: this method uses linear search to see if they are metric column or not,
	 * 	so the complexity is O(n). 
	 * 
	 */
	public void refreshColumnTitle() {
		
		TreeColumn columns[] = this.getTree().getColumns();
		boolean need_to_update = false;
		
		for( int i=0; i<columns.length; i++ ) {
			
			TreeColumn column = columns[i]; 
			Object obj = column.getData();
			
			if (obj instanceof BaseMetric) {
				final String title = ((BaseMetric)obj).getDisplayName();
				column.setText(title);
				
				// -----------------------------------------------------------------
				// if the column is a derived metric, we need to refresh the table
				// 	even if the derived metric is not modified at all.
				// this solution is not optimal, but it works
				// -----------------------------------------------------------------
				boolean is_derived = (obj instanceof DerivedMetric);
				need_to_update |= is_derived;
			}
		}
		
		// -----------------------------------------------------------------
		// refresh the table if necessary
		// -----------------------------------------------------------------
		if (need_to_update) {
			try {
				getTree().setRedraw(false);
				refresh();
			} finally {
				getTree().setRedraw(true);
			}
		}
	}
	
	
	/****
	 * Add a tree metric column
	 * @param objMetric base metric
	 * @param bSorted whether the column has to be sorted or not
	 * 
	 * @return the created {@code TreeViewerColumn}
	 */
	public TreeViewerColumn addTreeColumn(BaseMetric objMetric,  
    		boolean bSorted) {
		return addTreeColumn(objMetric, bSorted, ScopeComparator.SORT_DESCENDING);
	}
	
    /**
     * Add new tree column for derived metric
     * @param objMetric metric associated with the column
     * @param bSorted flag indicating if the column will be sorted or not
     * @param sortDirection possible values: {@code ScopeComparator.SORT_DESCENDING}, {@code ScopeComparator.SORT_ASCENDING}
     * 
     * @return a column  {@code TreeViewerColumn}
     */
    public TreeViewerColumn addTreeColumn(BaseMetric objMetric,  
    		boolean bSorted, int sortDirection) {
    	
    	TreeViewerColumn colMetric = new TreeViewerColumn(this,SWT.RIGHT);	// add column
		colMetric.setLabelProvider( new MetricLabelProvider(this, objMetric) );

		TreeColumn col = colMetric.getColumn();
		
		// set the title and tooltip of the header
		final String display_name = objMetric.getDisplayName();
    	col.setText(display_name);	
    	
    	final String description = objMetric.getDescription();
    	
    	if (description != null)
    		col.setToolTipText(display_name.trim() + " - " + description);
    	else
    		col.setToolTipText(display_name);
    	
		// associate the data of this column to the metric since we
		// allowed columns to move (col position is not enough !)
    	col.setData (objMetric);
    	col.setMoveable(true);
   	
		Layout layout = getTree().getParent().getLayout();
    	setMetricColumnWidth(layout, col);
 
		ScopeSelectionAdapter selectionAdapter = new ScopeSelectionAdapter(this, colMetric);
		
		// catch event when the user sort the column on the column header
		col.addSelectionListener(selectionAdapter);

		return colMetric;
    }
    
    
    /****
     * API to clear the content of the table.
     * Use this method to empty the table so it's fast to create a column
     */
    public void clearInput() {
    	RootScope root = new RootScope(getExperiment(), "empty", RootScopeType.Invisible);
    	setInput(root);
    }
    
    /***
     * set the width of a metric column based on the content and the text of the header
     * @param layout
     * @param col
     */
    private void setMetricColumnWidth(Layout layout, TreeColumn col) {
    	Font font = col.getParent().getFont();
    	GC gc = new GC(getControl());
    	gc.setFont(font);
    	Point extent = gc.textExtent(col.getText());
    	
    	// the column header in Linux GTK is bold. Unfortunately we don't have this info
    	// The hack is to multiply the width by a constant i.e. FACTOR_BOLD_FONT
    	int colWidth = (int) Math.max(metricColumnWidth, extent.x * FACTOR_BOLD_FONT);
    	gc.dispose();
    	
    	//col.setData (COLUMN_DATA_WIDTH, colWidth);
    	col.setWidth(colWidth);
    	
		if (layout != null && layout instanceof TreeColumnLayout) {
			final ColumnPixelData data = new ColumnPixelData(colWidth, true, false);
			((TreeColumnLayout)layout).setColumnData(col, data);
		}
    }
    
    
    /********
     * Initialize the table selection by selecting the second row of the table, and
     * set the focus to the table
     * 
     * @param level the integer level to expand. If the level is negative, 
     * 			the expansion is SWT default. 
     */
    public void initSelection(int level) {
		Tree tree = getTree();
		try {
			tree.setRedraw(false);
			expandToLevel(2);
			
			// hack on Mac: need to force to get the child getItem(0) so that the row height is adjusted
			// if we just get the top of the item, the height of the row can be too small, 
			//  and the text is cropped badly.
			
			int numItems  = tree.getItemCount();
			int indexSel  = numItems > 2? 2: 0;
			TreeItem item = tree.getItem(indexSel);
			tree.select(item);
		} catch (Exception exc) {
		} finally {
			tree.setRedraw(true);
		}
    }
    
    
	/**
	 * Retrieve the selected node
	 * @return null if there is no selected node
	 */
	public Scope getSelectedNode() {
		ISelection sel = getSelection();
		if (!(sel instanceof TreeSelection))
			return null;
		
		Object o = ((TreeSelection)sel).getFirstElement();
		if (!(o instanceof Scope)) {
			return null;
		}
		return (Scope) o;
	}


    /**
     * Change the column status (hide/show) in this view only
     * 
     * @param metricMgr IMetricManager metric manager which handle the metric values. 
     * 					It can be Experiment (for most parts) or special manager (for thread view)
     * 
     * @param status : array of boolean column status based on metrics (not on column).
     *  The number of items in status has to be the same as the number of metrics<br>
     * 	true means the column is shown, hidden otherwise.
     */
    public void setColumnsStatus(IMetricManager metricMgr, boolean []status) {
    	if (getTree().isDisposed())
    		return;
		
		// list of metrics and list of columns are not the same
		// columns only has "enabled" metrics (i.e. metrics that are not null)
		// hence the number of column is always <= number of metrics
		//
		// here we try to correspond between metrics to show and the columns
		
		if (getInput() == null) return;
		
		if (metricMgr == null)  return;
				
		List<BaseMetric> metrics = metricMgr.getVisibleMetrics();
		int numMetrics = metrics.size();
		
		TreeColumn []columns = getTree().getColumns();
		boolean []toShow = new boolean[columns.length];
		int numColumn = 0;
		
		for (TreeColumn column: columns) {

			Object metric = column.getData();
			if (metric == null || !(metric instanceof BaseMetric))
				continue; // not a metric column
			
			int i=0;			
			for (i=0; i<numMetrics && !metrics.get(i).equalIndex((BaseMetric)metric); i++);
			
			// it is possible that we hide columns and then create a new derived metric
			// in this case, the number of columns status is less than the number of metrics
			if (i >= status.length) {
				for (; numColumn<toShow.length; numColumn++) {
					toShow[numColumn] = true;
				}
				break;
			}
			if (i<numMetrics && metrics.get(i).equalIndex((BaseMetric) metric)) {
				toShow[numColumn] = status[i];
				numColumn++;
			}
		}
		
		int i = -1; // reset the column index
		for (TreeColumn column : columns) {			
			if (column.getData() == null) continue; // not a metric column 
			
			i++;
			if (toShow[i]) {
				// display column
				// bug #78: we should keep the original width
				if (column.getWidth() > 1)
					continue; // it's already shown

				setMetricColumnWidth(null, column);
			} else {
				// hide column					
				if (column.getWidth() <= 0) 
					continue; // it's already hidden
				
				column.setWidth(0);
			}
		}
    }
    

    /****
     * Add user derived metric into tree column
     * @param metric
     */
	public void addUserMetricColumn(BaseMetric metric) {
		
		Tree tree = getTree();

		tree.setRedraw(false);
		TreeViewerColumn colViewer = addTreeColumn(metric, false);			
		
		// FIXME: this can take really long
		// we need to spawn to another thread

		refresh(false);
		
		expandToLevel(2);
		tree.setRedraw(true);

		// this doesn't work on Linux/GTK
		tree.showColumn(colViewer.getColumn());
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {

		final String property = event.getProperty();
		
		boolean need_to_refresh = (property.equals(PreferenceConstants.ID_FONT_GENERIC) || 
								   property.equals(PreferenceConstants.ID_FONT_METRIC)  ||
								   property.equals(PreferenceConstants.ID_DEBUG_CCT_ID) ||
								   property.equals(PreferenceConstants.ID_DEBUG_FLAT_ID) ); 
		
		if (need_to_refresh) {
			computeIdealCellBound();
			
			// make sure we resize the metric columns too (should we?)
			for(TreeColumn column: getTree().getColumns()) {
				// do not resize tree scope column 
				if (column.getData() == null)
					continue;
				
				setMetricColumnWidth(null, column);
			}
			try {
				getTree().setRedraw(false);
				
				// refresh the table, but we don't change the content
				refresh(false);
			} finally {
				getTree().setRedraw(true);
			}
		}
	}
    
	/**
	 * Returns the viewer cell at the given widget-relative coordinates, or
	 * <code>null</code> if there is no cell at that location
	 * 
	 * @param point
	 * 		the widget-relative coordinates
	 * @return the cell or <code>null</code> if no cell is found at the given
	 * 	point
	 * 
	 * @since 3.4
	 */
	/*
    public ViewerCell getCell(Point point) {
		ViewerRow row = getViewerRow(point);
		if (row != null) {
			return row.getCell(point);
		}

		return null;
	}*/
	

	@Override
	public void handleEvent(Event event) {
		switch (event.type) {
		case SWT.MeasureItem:
			if (tableRowHeight > event.height)
				event.height = tableRowHeight;
			break;
		}
	}

	
	/*****
     * Temporary solution to resize the height of the table. 
     * <p>
     * Due to Eclipse bug, we cannot decrease the height.
     * See:
     *  <ul>
     *  <li>{@link https://bugs.eclipse.org/bugs/show_bug.cgi?id=148039}
     *  <li>{@link https://bugs.eclipse.org/bugs/show_bug.cgi?id=154341}
     * </ul>
     * </p>
     * It is not recommended to resize the row height of the table since it doesn't
     * work properly on Linux.
     * <br/>
	 * Compute the ideal bounds for a cell by: 
	 * <ul>
	 *  <li>Check with font metric and font general
	 *  <li>Pick which ever has the highest value
	 * </ul>
	 * The x-bound is used for metric column, while the y-bound is used for all columns.
	 */
	public void computeIdealCellBound() {
		
		GC gc = new GC(getControl());
		
		gc.setFont(FontManager.getMetricFont());
		String text = TEXT_METRIC_COLUMN;
		if (OSValidator.isWindows()) {
			
			// FIXME: ugly hack to add some spaces for Windows
			// Somehow, Windows 10 doesn't allow to squeeze the text inside the table
			// we have to give them some spaces (2 spaces in my case).
			// A temporary fix for issue #37
			text += "xx";
		}
		Point extent = gc.stringExtent(text);
		metricColumnWidth = extent.x;
		tableRowHeight    = extent.y + 2;
		
		// check the height if we use generic font (tree column)
		// if this font is higher, we should use this height as the standard.
		
		gc.setFont(FontManager.getFontGeneric());
		extent = gc.stringExtent(text);
		tableRowHeight = Math.max(tableRowHeight, extent.y);
		
		gc.dispose();
	}
}
