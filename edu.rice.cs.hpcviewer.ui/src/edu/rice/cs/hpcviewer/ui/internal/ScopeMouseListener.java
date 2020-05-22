package edu.rice.cs.hpcviewer.ui.internal;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
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
import edu.rice.cs.hpcviewer.ui.parts.editor.Editor;
import edu.rice.cs.hpcviewer.ui.util.Utilities;

public class ScopeMouseListener implements Listener 
{
	final private EPartService  partService;
	final private EModelService modelService;
	final private MApplication  app;
	
	final private GC gc;
	final private TreeViewer treeViewer;
	
	/**
	 * initialization with the gc of the tree
	 * @param gc of the tree
	 */
	public ScopeMouseListener(TreeViewer treeViewer, 
			EPartService  partService, EModelService modelService,
			MApplication  app) {
		
		this.treeViewer   = treeViewer;
		this.partService  = partService;
		this.modelService = modelService;
		
		this.app = app;
		this.gc  = new GC(treeViewer.getTree().getDisplay());
	}
	

	public void dispose() {
		gc.dispose();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public void handleEvent(Event event) {

		// tell the children to handle the mouse click
		//view.mouseDownEvent(event);

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
		// display the source code if the view is not maximized
		if (partService == null)
			return;
		
		if (scope == null || !Utilities.isFileReadable(scope))
			return;
		
		final MPart part = partService.findPart(Editor.ID);
		MPartStack editorStack = (MPartStack)modelService.find("edu.rice.cs.hpcviewer.ui.partstack.upper", app);
		editorStack.getChildren().add(part);

		partService.showPart(part, PartState.ACTIVATE);
		
		System.out.println("displaysourcecode: " + scope.getSourceFile().getName() + " line " + scope.getFirstLineNumber());
	}
}
