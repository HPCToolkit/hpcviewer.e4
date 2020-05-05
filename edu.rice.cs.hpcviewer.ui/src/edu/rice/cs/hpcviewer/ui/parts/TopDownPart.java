package edu.rice.cs.hpcviewer.ui.parts;

import javax.annotation.PostConstruct;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class TopDownPart 
{
	TreeViewer treeViewer;

	public TopDownPart() {
	}

    @PostConstruct
    public void createControls(Composite parent) {
    	treeViewer = new TreeViewer(parent, SWT.BORDER|SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.MULTI);
    	
    	GridDataFactory.fillDefaults().grab(true, true).applyTo(treeViewer.getControl());
    }
}
