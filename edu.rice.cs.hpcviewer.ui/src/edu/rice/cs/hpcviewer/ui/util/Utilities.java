/**
 * 
 */
package edu.rice.cs.hpcviewer.ui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import java.io.File;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;

import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.CallSiteScopeType;
import edu.rice.cs.hpcdata.experiment.scope.LineScope;
import edu.rice.cs.hpcdata.experiment.scope.ProcedureScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.util.OSValidator;
import edu.rice.cs.hpcdata.util.Util;
import edu.rice.cs.hpcsetting.fonts.FontManager;
import edu.rice.cs.hpcsetting.preferences.PreferenceConstants;
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


	static private Color linkColor = null;
	
	/***
	 * Retrieve the active link color
	 *
	 * @return
	 */
	static public  Color getLinkColor() {
		Display display = Display.getDefault();
		if (Display.isSystemDarkTheme()) {
			linkColor = display.getSystemColor(SWT.COLOR_LINK_FOREGROUND);
		} else {
			linkColor = display.getSystemColor(SWT.COLOR_BLUE);
		}
		return linkColor;
	}
	
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
				if(Util.isFileReadable(lineScope))
					return iconManager.getImage(IconManager.Image_CallTo);
				else
					return iconManager.getImage(IconManager.Image_CallToDisabled);
			} else {
				if(Util.isFileReadable(lineScope))
					return iconManager.getImage(IconManager.Image_CallFrom);
				else
					return iconManager.getImage(IconManager.Image_CallFromDisabled);
			}
		}
		return null;
	}
	
	
	/***
	 * Retrieve the inline navigation icon of a procedure node 
	 * @param proc ProcedureScope
	 * @return Image
	 */
	static public Image getInlineNavButton(ProcedureScope proc)
	{
		if (proc.isAlien()) {
			IconManager iconManager = IconManager.getInstance();
			boolean readable = Util.isFileReadable(proc);
			
			if (readable) {
				return iconManager.getImage(IconManager.Image_InlineTo);
			} else {
				return iconManager.getImage(IconManager.Image_InlineToDisabled);
			}
		}
		return null;
	}
    
    
    /****
     * get the default workspace directory. 
     * A workspace directory is the location where Eclipse will store caches (plugin, libraries),
     * preferences, logs, file locks, etc.
     * We may need to store all user setting there too.
     * 
     * @return {@code String}
     */
    public static String getWorkspaceDirectory() {
		
		final String arch = System.getProperty("os.arch");

		final String subDir = ".hpctoolkit" + File.separator + 
							  "hpcviewer"   + File.separator +
							  arch;
		
		return System.getProperty("user.home") + File.separator + subDir;
    }
}
