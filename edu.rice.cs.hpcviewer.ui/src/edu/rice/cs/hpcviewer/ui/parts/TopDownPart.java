package edu.rice.cs.hpcviewer.ui.parts;

import javax.annotation.PostConstruct;

import org.eclipse.swt.widgets.Composite;

public class TopDownPart 
{
	public TopDownPart() {
	}

    @PostConstruct
    public void createControls(Composite parent) {
    	ToolBarFactrory.createControl(parent);
    }
}
