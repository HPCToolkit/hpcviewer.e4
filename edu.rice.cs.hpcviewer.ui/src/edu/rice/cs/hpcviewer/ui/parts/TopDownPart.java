package edu.rice.cs.hpcviewer.ui.parts;

import javax.annotation.PostConstruct;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;

public class TopDownPart 
{
	TreeViewer treeViewer;

	public TopDownPart() {
	}

    @PostConstruct
    public void createControls(Composite parent) {
    	ToolBarFactrory.createControl(parent);
    }
}
