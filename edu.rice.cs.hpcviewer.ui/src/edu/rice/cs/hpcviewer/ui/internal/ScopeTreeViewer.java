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
import org.eclipse.swt.widgets.Layout;
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
import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.DerivedMetric;
import edu.rice.cs.hpc.data.experiment.metric.IMetricManager;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.util.OSValidator;
import edu.rice.cs.hpc.data.util.ScopeComparator;
import edu.rice.cs.hpcsetting.fonts.FontManager;
import edu.rice.cs.hpcsetting.preferences.PreferenceConstants;
import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceManager;


/**
 * we set lazy virtual bit in this viewer
 */
public class ScopeTreeViewer extends TreeViewer implements IPropertyChangeListener
{
	public final static String COLUMN_DATA_WIDTH = "w"; 
	private final static float FACTOR_BOLD_FONT  = 1.2f;
	private final static String TEXT_METRIC_COLUMN = "8x88+88xx888x8%";
	
	private int metricColumnWidth;
	private DisposeListener disposeListener;
	
	/**
	 * @param parent
	 * @param style
	 */
	public ScopeTreeViewer(Composite parent, int style) {
		super(parent, SWT.VIRTUAL | SWT.FULL_SELECTION | style);
		init();
	}

	private void init() 
	{
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
		
		gc.dispose();
	}
	
	
	/****
	 * Free allocated resources and unsubscribe events.
	 * This method has to be called once the table is disposed.
	 */
	public void dispose() {
		getTree().removeDisposeListener(disposeListener);

		PreferenceStore pref = ViewerPreferenceManager.INSTANCE.getPreferenceStore();
		pref.removePropertyChangeListener(this);
	}
	

	/****
	 * Retrieve the database of this table
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
	 * For a Top-down view, the root will be the CCT root.
	 * For a bottom-up view, the root will be the caller root
	 * For a flat view, the root will be the flat root. 
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
			refresh();
		}
	}
	
    /**
     * Add new tree column for derived metric
     * @param objMetric metric associated with the column
     * @param bSorted flag indicating if the column will be sorted or not
     * 
     * @return a column
     */
    public TreeViewerColumn addTreeColumn(BaseMetric objMetric,  
    		boolean bSorted) {
    	
    	TreeViewerColumn colMetric = new TreeViewerColumn(this,SWT.RIGHT);	// add column
		colMetric.setLabelProvider( new MetricLabelProvider(this, objMetric) );

		TreeColumn col = colMetric.getColumn();
		
		// set the title and tooltip of the header
		final String display_name = objMetric.getDisplayName();
    	col.setText(display_name);	
    	
    	final String description = objMetric.getDescription();
    	
    	if (description != null)
    		col.setToolTipText(display_name.trim() + " - " + objMetric.getDescription());
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
		
		if(bSorted) {
			selectionAdapter.setSorter(ScopeComparator.SORT_DESCENDING);
		}
		return colMetric;
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
    	
    	col.setData (COLUMN_DATA_WIDTH, colWidth);
    	col.setWidth(colWidth);
    	
		if (layout instanceof TreeColumnLayout) {
			final ColumnPixelData data = new ColumnPixelData(colWidth, true, false);
			((TreeColumnLayout)layout).setColumnData(col, data);
		}
    }
    
    
    /********
     * Initialize the table selection by selecting the second row of the table, and
     * set the focus to the table
     */
    public void initSelection() {
		
		// scroll to the top. This works on mac, but doesn't work on Linux/GTK
		
		final Tree tree = getTree();
		
		// Issue #34: mac requires to delay the selection after initialization
		
		tree.getDisplay().asyncExec(() -> {
			
			// issue #36
			// Linux/GTK only: if a user already select an item, we shouldn't expand it
			//
			// issue #34 (macOS only): we need to refresh and expand the table after sorting
			// otherwise the tree items are not visible
			if (!OSValidator.isMac() && tree.getSelectionCount() > 0) {
				return;
			}
			expandToLevel(2);
			
			try {
				TreeItem item = tree.getTopItem().getItem(0);
				tree.showItem(item);
				tree.select(item);
			} catch (Exception e) {
			}
		});
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
     * @param status : array of boolean column status based on metrics (not on column).
     *  The number of items in status has to be the same as the number of metrics<br>
     * 	true means the column is shown, hidden otherwise.
     */
    public void setColumnsStatus(IMetricManager metricMgr, boolean []status) {
    	if (getTree().isDisposed())
    		return;
		
		TreeColumn []columns = getTree().getColumns();

		boolean []toShow = new boolean[columns.length];
		int numColumn = 0;
		
		// list of metrics and list of columns are not the same
		// columns only has "enabled" metrics (i.e. metrics that are not null)
		// hence the number of column is always <= number of metrics
		//
		// here we try to correspond between metrics to show and the columns
		
		Object obj = getInput();
		if (obj == null) return;
		
		if (metricMgr == null)
			return;
				
		List<BaseMetric> metrics = metricMgr.getVisibleMetrics();
		int numMetrics = metrics.size();
		
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

			int iWidth = 0;
			if (toShow[i]) {
				// display column
				// bug #78: we should keep the original width
				if (column.getWidth() > 1)
					continue; // it's already shown

				if (iWidth <= 0) {
	       			// Laks: bug no 131: we need to have special key for storing the column width
	        		Object o = column.getData(ScopeTreeViewer.COLUMN_DATA_WIDTH);
	       			if((o != null) && (o instanceof Integer) ) {
	       				iWidth = ((Integer)o).intValue();
	       			} else {
		        		iWidth = 120;
	       			}
				}
				
			} else {
				// hide column					
				if (column.getWidth() <= 0) 
					continue; // it's already hidden
				
	   			Integer objWidth = Integer.valueOf( column.getWidth() );
	   			
	   			// bug no 131: we need to have special key for storing the column width
	   			column.setData(ScopeTreeViewer.COLUMN_DATA_WIDTH, objWidth);
	   			
			}
			// for other OS other than Linux, we need to set the width explicitly
			// the layout will not take affect until users move or resize columns in the table
			// eclipse bug: forcing to refresh the table has no effect either
			
			column.setWidth(iWidth);
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
		refresh();
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
			// refresh the table, but we don't change the content
			refresh(false);
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
}
