 
package edu.rice.cs.hpcviewer.ui.parts.editor;

import javax.inject.Inject;
import javax.annotation.PostConstruct;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.SWT;



public class Editor 
{
	private TextViewer textViewer;
	
	@Inject
	public Editor() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {

		textViewer = new TextViewer(parent, SWT.BORDER);
		StyledText styledText = textViewer.getTextWidget();
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(parent);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(parent);
	}
	
	
	
	
}