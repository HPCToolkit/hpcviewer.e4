/**
 * 
 */
package edu.rice.cs.hpcviewer.ui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import edu.rice.cs.hpc.data.experiment.source.FileSystemSourceFile;
import edu.rice.cs.hpc.data.experiment.source.SourceFile;
import edu.rice.cs.hpc.data.experiment.scope.CallSiteScope;
import edu.rice.cs.hpc.data.experiment.scope.CallSiteScopeType;
import edu.rice.cs.hpc.data.experiment.scope.LineScope;
import edu.rice.cs.hpc.data.experiment.scope.ProcedureScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.util.OSValidator;
import edu.rice.cs.hpcsetting.fonts.FontManager;
import edu.rice.cs.hpcsetting.preferences.PreferenceConstants;
import edu.rice.cs.hpcviewer.ui.resources.ColorManager;
import edu.rice.cs.hpcviewer.ui.resources.IconManager;


/**
 * Class providing auxiliary utilities methods.
 * Remark: it is useless to instantiate this class since all its methods are static !
 * @author laksono
 *
 */
public class Utilities 
{
	
	static final public String NEW_LINE = System.getProperty("line.separator");

	static final public Styler STYLE_ACTIVE_LINK = new StyledString.Styler() {

		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.foreground = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
			textStyle.font 		 = FontManager.getFontGeneric();
		}
		
	};
	
	static final public Styler STYLE_INACTIVE_LINK = new StyledString.Styler() {
		
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.font = FontManager.getFontGeneric();;
		}
	};
	
	/** font style for clickable line number in a call site */
	static final public Styler STYLE_COUNTER = new StyledString.Styler() {
		
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.foreground = Display.getDefault().getSystemColor(SWT.COLOR_DARK_CYAN);
			textStyle.font = FontManager.getFontGeneric();
		}
	};
	
	/** font style for unclickable line number in a callsite */
	static final public Styler STYLE_DECORATIONS = new StyledString.Styler() {
		
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.foreground = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY);
			textStyle.font = FontManager.getFontGeneric();
		}
	};


	
	
	/*****
	 * check if two font data are equal (name, height and style)
	 * 
	 * @param fontTarget
	 * @param fontSource
	 * @return
	 */
	static public boolean isDifferentFontData(FontData fontTarget[], FontData fontSource[]) {
		boolean isChanged = false;
		for (int i=0; i<fontTarget.length; i++) {
			if (i < fontSource.length) {
				FontData source = fontSource[i];
				// bug: if the height is not common, we just do nothing, consider everything work fine
				if (source.getHeight()<4 || source.getHeight()>99)
					return false;
				
				FontData target = fontTarget[i];
				isChanged = !( target.getName().equals(source.getName()) &&
						(target.getHeight()==source.getHeight()) && 
						(target.getStyle()==source.getStyle()) ) ;
				
				if (isChanged)
					// no need to continue the loop
					return isChanged;
			}
		}
		return isChanged;
	}
	

	
	static public void resetView ( TreeViewer tree )
	{
		TreeItemManager objItem = new TreeItemManager();
		resetView(objItem, tree);
	}
	
	/**
	 * refresh a particular view
	 * To save memory allocation, we ask an instance of TreeItemManager
	 * @param objItemManager
	 * @param tree
	 */
	static private void resetView ( TreeItemManager objItemManager, TreeViewer tree) {
		//resetViewRowHeight(tree);
		// save the context first
		objItemManager.saveContext(tree);
		// refresh
		tree.refresh();
		// restore the context
		objItemManager.restoreContext(tree);
	}
	
	/**
	 * activate a listener to reset Row Height for Windows only
	 * @param tree
	 */
	static public Listener listenerToResetRowHeight ( TreeViewer tree ) {
		
		final int MARGIN_FONT = 10;
		
		Tree treeItem = tree.getTree();
		
		// resize the table row height using a MeasureItem listener
		Listener measurementListener = new Listener() {
			public void handleEvent(Event event) {

				// get font height (from preferences) for each font

				FontData fData[] = FontManager.getFontDataPreference(PreferenceConstants.ID_FONT_METRIC);
				int objFontMetricHeight  = fData[0].getHeight();
				
				fData = FontManager.getFontDataPreference(PreferenceConstants.ID_FONT_GENERIC);
				int objFontGenericHeight = fData[0].getHeight();
				
				event.height = Math.min(objFontMetricHeight, objFontGenericHeight) + MARGIN_FONT;
			} // end handleEvent
		}; // end measurementListener
		
		treeItem.addListener(SWT.MeasureItem, measurementListener);
		return measurementListener;
	}
	
	/**
	 * refresh size of rows for a particular view - non Windows
	 * @param tree
	 */
	static public void resetViewRowHeight ( TreeViewer tree ) {
		if (!OSValidator.isWindows()) { 
			int saveWidth = tree.getTree().getColumn(0).getWidth();
			tree.getTree().getColumn(0).setWidth(saveWidth==0?1:0);
			tree.getTree().getColumn(0).setWidth(saveWidth);
		}
	}
	


	/**	
	 * Insert an item on the top on the tree/table with additional image if not null
	 * @param treeViewer : the tree viewer
	 * @param imgScope : the icon for the tree node
	 * @param arrText : the label of the items (started from  col 0..n-1)
	 */
	static public void insertTopRow(TreeViewer treeViewer, Image imgScope, String []arrText) {
		if(arrText == null)
			return;
    	TreeItem item = new TreeItem(treeViewer.getTree(), SWT.BOLD, 0);
    	if(imgScope != null)
    		item.setImage(0,imgScope);

    	// Laksono 2009.03.09: add background for the top row to distinguish with other scopes
    	item.setBackground(ColorManager.getColorTopRow());
    	// make monospace font for all metric columns
    	item.setFont(FontManager.getMetricFont());
    	item.setFont(0, FontManager.getFontGeneric()); // The tree has the original font
    	// put the text on the table
    	item.setText(arrText);
    	// set the array of text as the item data 
    	// we will use this information when the table is sorted (to restore the original top row)
    	item.setData(arrText);
	}

	/**
	 * Retrieve the top row items into a list of string
	 * @param treeViewer
	 * @return
	 */
	public static String[] getTopRowItems( TreeViewer treeViewer ) {
		// for dynamic views, the table is initially empty
		if (treeViewer.getTree().getItemCount() == 0)
			return null;
		
		TreeItem item = treeViewer.getTree().getItem(0);
		String []sText= null; // have to do this to avoid error in compilation;
		if(item.getData() instanceof Scope) {
			// the table has been zoomed-out
		} else {
			// the table is in original form or flattened or zoom-in
			Object o = item.getData();
			if(o != null) {
				Object []arrObj = (Object []) o;
				if(arrObj[0] instanceof String) {
					sText = (String[]) item.getData(); 
				}
			}
		}
		return sText;
	}
	/**
	 * Return an image depending on the scope of the node.
	 * The criteria is based on ScopeTreeCellRenderer.getScopeNavButton()
	 * @param scope
	 * @return
	 */
	static public Image getScopeNavButton(Object scope) {
		IconManager iconManager = IconManager.getInstance();
		
		if (scope instanceof RootScope)
			return iconManager.getImage(IconManager.Image_MetricAggregate);
		
		if (scope instanceof CallSiteScope) {
			CallSiteScope scopeCall = (CallSiteScope) scope;
        	LineScope lineScope = (scopeCall).getLineScope();
			if (((CallSiteScope) scope).getType() == CallSiteScopeType.CALL_TO_PROCEDURE) {
				if(Utilities.isFileReadable(lineScope))
					return iconManager.getImage(IconManager.Image_CallTo);
				else
					return iconManager.getImage(IconManager.Image_CallToDisabled);
			} else {
				if(Utilities.isFileReadable(lineScope))
					return iconManager.getImage(IconManager.Image_CallFrom);
				else
					return iconManager.getImage(IconManager.Image_CallFromDisabled);
			}
		}
		return null;
	}
	
	static public Image getInlineNavButton(ProcedureScope proc)
	{
		if (proc.isAlien()) {
			IconManager iconManager = IconManager.getInstance();
			boolean readable = Utilities.isFileReadable(proc);
			
			if (readable) {
				return iconManager.getImage(IconManager.Image_InlineTo);
			} else {
				return iconManager.getImage(IconManager.Image_InlineToDisabled);
			}
		}
		return null;
	}

    /**
     * Verify if the file exist or not.
     * Remark: we will update the flag that indicates the availability of the source code
     * in the scope level. The reason is that it is less time consuming (apparently) to
     * access to the scope level instead of converting and checking into FileSystemSourceFile
     * level.
     * @param scope
     * @return true if the source is available. false otherwise
     */
    static public boolean isFileReadable(Scope scope) {
    	// check if the source code availability is already computed
    	if(scope.iSourceCodeAvailability == Scope.SOURCE_CODE_UNKNOWN) {
    		SourceFile newFile = (scope.getSourceFile());
    		if (newFile != null && !newFile.getName().isEmpty()) {
        		if( (newFile != SourceFile.NONE)
            			|| ( newFile.isAvailable() )  ) {
            			if (newFile instanceof FileSystemSourceFile) {
            				FileSystemSourceFile objFile = (FileSystemSourceFile) newFile;
            				if(objFile != null) {
            					// find the availability of the source code
            					if (objFile.isAvailable()) {
            						scope.iSourceCodeAvailability = Scope.SOURCE_CODE_AVAILABLE;
            						return true;
            					} 
            				}
            			}
            		}
    		}
    	} else
    		// the source code availability is already computed, we just reuse it
    		return (scope.iSourceCodeAvailability == Scope.SOURCE_CODE_AVAILABLE);
    	// in this level, we don't think the source code is available
		scope.iSourceCodeAvailability = Scope.SOURCE_CODE_NOT_AVAILABLE;
		return false;
    }
    
}
