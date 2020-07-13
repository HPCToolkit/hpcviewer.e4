package edu.rice.cs.hpctraceviewer.ui.main;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;

import edu.rice.cs.hpctraceviewer.ui.resources.IconManager;
import edu.rice.cs.hpctraceviewer.ui.operation.RedoOperationAction;
import edu.rice.cs.hpctraceviewer.ui.operation.UndoOperationAction;

public class TraceCoolBar {

	final Action home;
	final Action tZoomIn;
	final Action tZoomOut;
	final Action pZoomIn;
	final Action pZoomOut;
	final Action save;
	final Action open;
	final Action goEast;
	final Action goWest;
	final Action goNorth;
	final Action goSouth;
	
	
	public TraceCoolBar(final IToolBarManager toolbar, final ITraceViewAction action, int style) 
	{
		IconManager iconManager = new IconManager();
		
		/***************************************************
		 * Buttons
		 **************************************************/
		
		final ImageDescriptor homeSamp = iconManager.getObjectDescriptor(IconManager.HOME); 
		home = new Action(null, homeSamp) {
			public void run() {
				action.home();
			}
		};
		this.createAction(toolbar, home, "Reset the view to display the whole trace");
		
		toolbar.add(new Separator());
		
		final ImageDescriptor zoomInTim = iconManager.getObjectDescriptor(IconManager.ZOOM_IN_H);
		tZoomIn = new Action(null, zoomInTim) {
			public void run() {
				action.timeZoomIn();
			}		
		};
		this.createAction(toolbar, tZoomIn, "Zoom in along the time axis");
		
		final ImageDescriptor zoomOutTim = iconManager.getObjectDescriptor(IconManager.ZOOM_OUT_H);
		tZoomOut = new Action(null, zoomOutTim) {
			public void run() {
				action.timeZoomOut();
			}		
		};
		this.createAction(toolbar, tZoomOut, "Zoom out along the time axis");
		
		ImageDescriptor zoomInProc = iconManager.getObjectDescriptor(IconManager.ZOOM_IN_V);
		pZoomIn = new Action(null, zoomInProc) {
			public void run() {
				action.processZoomIn();
			}		
		};
		this.createAction(toolbar, pZoomIn, "Zoom in along the process axis");
		
		ImageDescriptor zoomOutProc = iconManager.getObjectDescriptor(IconManager.ZOOM_OUT_V);
		pZoomOut = new Action(null, zoomOutProc) {
			public void run() {
				action.processZoomOut();
			}		
		};
		this.createAction(toolbar, pZoomOut, "Zoom out along the process axis");


		toolbar.add(new Separator());

		
		final ImageDescriptor eastDesc = iconManager.getObjectDescriptor(IconManager.GO_EAST);
		goEast = new Action(null, eastDesc) {
			public void run() {
				action.goEast();
			}		
		};
		this.createAction(toolbar, goEast, "Scroll left one step along the time axis");

		final ImageDescriptor westDesc = iconManager.getObjectDescriptor(IconManager.GO_WEST);
		goWest = new Action(null, westDesc) {
			public void run() {
				action.goWest();
			}		
		};
		this.createAction(toolbar, goWest, "Scroll right one step along the time axis");
		

		final ImageDescriptor northDesc = iconManager.getObjectDescriptor(IconManager.GO_NORTH);
		goNorth = new Action(null, northDesc) {
			public void run() {
				action.goNorth();
			}		
		};
		this.createAction(toolbar, goNorth, "Scroll up one step along the process axis");

		
		final ImageDescriptor southDesc = iconManager.getObjectDescriptor(IconManager.GO_SOUTH);
		goSouth = new Action(null, southDesc) {
			public void run() {
				action.goSouth();
			}		
		};
		this.createAction(toolbar, goSouth, "Scroll down one step along the process axis");
		
		toolbar.add(new Separator());
		
		ImageDescriptor undoSamp = iconManager.getObjectDescriptor(IconManager.UNDO);
		UndoOperationAction undo = new UndoOperationAction(undoSamp);
		this.createAction(toolbar, undo, "Undo the last action");
				
		ImageDescriptor redoSamp = iconManager.getObjectDescriptor(IconManager.REDO);
		RedoOperationAction redo = new RedoOperationAction(redoSamp);
		this.createAction(toolbar, redo, "Redo the last undo");
				
		toolbar.add(new Separator());
		
		ImageDescriptor saveSamp = iconManager.getObjectDescriptor(IconManager.SAVE);
		save = new Action(null, saveSamp) {
			public void run() {
				action.save();
			}		
		};
		this.createAction(toolbar, save, "Save the current view configuration to a file");
				
		ImageDescriptor openSamp = iconManager.getObjectDescriptor(IconManager.OPEN);
		open = new Action(null, openSamp) {
			public void run() {
				action.open();
			}		
		};
		this.createAction(toolbar, open, "Open a saved view configuration");
	}

	/*****
	 * Finalize an action by setting the tooltop and insert it into the toolbar
	 * 
	 * @param toolbar
	 * @param action
	 * @param sDesc
	 */
	private void createAction(IToolBarManager toolbar, Action action, String sDesc) {
		action.setToolTipText(sDesc);
		action.setEnabled(false);
		toolbar.add(action);
	}
}
