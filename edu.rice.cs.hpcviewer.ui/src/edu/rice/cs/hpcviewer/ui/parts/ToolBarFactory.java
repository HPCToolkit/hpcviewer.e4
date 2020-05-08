package edu.rice.cs.hpcviewer.ui.parts;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;

public class ToolBarFactory {

	public ToolBarFactory() {
	}

	static public void createControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		
		Composite composite = new Composite(parent, SWT.BORDER);
		composite.setLayout(new GridLayout(1, false));
		GridData gd_composite = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_composite.widthHint = 406;
		composite.setLayoutData(gd_composite);
		
		CoolBar coolBar = new CoolBar(composite, SWT.FLAT);
		coolBar.setSize(150, 30);
		
		ToolBar toolBar = new ToolBar(coolBar, SWT.FLAT | SWT.RIGHT);
		
		ToolItem tltmZoomin = new ToolItem(toolBar, SWT.NONE);
		tltmZoomin.setText("Zoom-in");
		
		ToolItem tltmZoomout = new ToolItem(toolBar, SWT.NONE);
		tltmZoomout.setText("Zoom-out");
		
		ToolItem tltmHotpath = new ToolItem(toolBar, SWT.NONE);
		tltmHotpath.setText("Hot-path");
		
		ToolItem tltmMetric = new ToolItem(toolBar, SWT.NONE);
		tltmMetric.setText("Metric");

		createCoolItem(coolBar, toolBar);

		
		ToolBar toolBar_1 = new ToolBar(coolBar, SWT.FLAT | SWT.RIGHT);
		
		ToolItem tltmIncr = new ToolItem(toolBar_1, SWT.NONE);
		tltmIncr.setText("Incr");
		
		ToolItem tltmDecr = new ToolItem(toolBar_1, SWT.NONE);
		tltmDecr.setText("Decr");

		createCoolItem(coolBar, toolBar_1);

		TreeViewer treeViewer = new TreeViewer(parent, SWT.BORDER);
		Tree tree = treeViewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
	}
	
    
    static private void createCoolItem(CoolBar coolBar, ToolBar toolBar) {

		CoolItem coolItem = new CoolItem(coolBar, SWT.NONE);
		coolItem.setControl(toolBar);
		Point size = toolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		coolItem.setSize(size);
    	
    }
}
