package edu.rice.cs.hpcviewer.ui.internal;

import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;

import edu.rice.cs.hpc.data.experiment.scope.CallSiteScope;
import edu.rice.cs.hpc.data.experiment.scope.LineScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpcsetting.fonts.FontManager;
import edu.rice.cs.hpcviewer.ui.ProfilePart;
import edu.rice.cs.hpcviewer.ui.util.Utilities;


/*****************************************************
 * 
 * A class to listen to mouse events inside a tree viewer
 * <p>
 * If there is a "click" event on a tree item, it detects we can display the source code or not.
 *  
 *
 *****************************************************/
public class ScopeMouseListener implements Listener 
{
	final private GC gc;
	final private TreeViewer treeViewer;
	
	final private ProfilePart profilePart;
	
	/**
	 * initialization with the gc of the tree
	 * @param TreeViewer the tree viewer. It cannot be null
	 * @param EPartService part service to activate an editor
	 * @param EModelService to find existing editor
	 * @param MApplication the application
	 */
	public ScopeMouseListener( TreeViewer treeViewer, 
							   ProfilePart profilePart) {
		
		this.treeViewer   = treeViewer;
		this.profilePart  = profilePart;
		
		gc  = new GC(treeViewer.getTree().getDisplay());
		gc.setFont(FontManager.getFontGeneric());
	}
	

	public void dispose() {
		gc.dispose();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	@Override
	public void handleEvent(Event event) {

		if(event.button != 1) {
			// yes, we only allow the first button
			return;
		}
		
		// get the item
		TreeItem item = treeViewer.getTree().getItem(new Point(event.x, event.y));
		if (item != null)
			checkIntersection(event, item);
	}
	
	/***
	 * check whether the mouse click intersect with an icon or a text in the tree item
	 * if it intersects with the icon, we display the callsite.
	 * it if intersects with the text, we display the source code
	 * otherwise, do nothing
	 * 
	 * @param event
	 * @param item : current item selected or pointed by the mouse
	 * @return
	 */
	private void checkIntersection(Event event, TreeItem item) {
		Rectangle recImage = item.getImageBounds(0);	// get the image location (if exist)
		Rectangle recText  = item.getTextBounds(0);
		
		boolean inImage = (recImage.x<event.x && recImage.x+recImage.width>event.x);
		boolean inText  = (recText.x<event.x  && recText.x+recText.width>event.x);
		 
		// verify if the user click on the icon
		if(inImage) {
			// Check the object of the click/select item
	        TreeSelection selection = (TreeSelection) treeViewer.getSelection();
	        Object o = selection.getFirstElement();
	        
	        // we will treat this click if the object is Scope
	        if(o instanceof Scope) {
	        	Scope scope = (Scope) o;
	            // show the call site in case this one exists
	            if(scope instanceof CallSiteScope) {
	            	// get the call site scope
	            	CallSiteScope callSiteScope = (CallSiteScope) scope;
	            	LineScope lineScope = callSiteScope.getLineScope();
	            	displaySourceCode(lineScope);
	            }
	        }
		} else if(inText){
			// Check the object of the click/select item
	        TreeSelection selection = (TreeSelection) treeViewer.getSelection();
	        Object o = selection.getFirstElement();
	        
	        // we will treat this click if the object is Scope.Node
	        if(o instanceof Scope) {
	        	if (o instanceof CallSiteScope) {
	        		CallSiteScope cs = (CallSiteScope) o;
	        		// the line number in xml is started from zero, while the source
	        		//	code starts from 1
	        		int line = 1 + cs.getLineScope().getFirstLineNumber();
	        		
	        		if (gc != null && line>0) {
	        			// a hack to know whether we click on the line number text
	        			// or the name of the node (procedure name)
    		        	Point p = gc.textExtent(":" + line);
    		        	if (p.x+recText.x >= event.x) {
    		        		displaySourceCode( cs.getLineScope() );
        	            	return;
    		        	}
	        		}
	        	}
	        	displaySourceCode( (Scope)o );
	        }
		}
	}
	
	/**
	 * special source code display when user click a tree node
	 * @param scope
	 */
	private void displaySourceCode( Scope scope ) {
		
		if (scope == null || !Utilities.isFileReadable(scope))
			return;
		
		profilePart.addEditor(scope);
		
		// keep focus to the viewer 
		treeViewer.getTree().setFocus();
	}	
}
