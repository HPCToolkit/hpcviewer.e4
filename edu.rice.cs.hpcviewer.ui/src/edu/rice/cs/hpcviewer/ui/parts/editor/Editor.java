 
package edu.rice.cs.hpcviewer.ui.parts.editor;

import javax.inject.Inject;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.source.FileSystemSourceFile;
import edu.rice.cs.hpcviewer.ui.util.Utilities;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.SWT;



public class Editor implements ICodeEditor
{
	static final public String ID = "edu.rice.cs.hpcviewer.ui.part.editor";
	
	private SourceViewer textViewer;
	
	@Inject IEventBroker broker;

	@Inject
	public Editor() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {

		textViewer = new SourceViewer(parent, null, SWT.BORDER| SWT.MULTI | SWT.V_SCROLL);
		
		StyledText styledText = textViewer.getTextWidget();		
		styledText.setFont(JFaceResources.getTextFont());
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(styledText);
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(parent);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(parent);
	}
	
	@PreDestroy
	public void preDestroy() {
	}

	@Override
	public void setData(Object obj) {
		if (obj != null && obj instanceof Scope) {
			Scope scope = (Scope) obj;

			if (!Utilities.isFileReadable(scope))
				return;
			
			FileSystemSourceFile file = (FileSystemSourceFile) scope.getSourceFile();
			
			String filename = file.getCompleteFilename();
			int lineNumber  = scope.getFirstLineNumber();
		}
	}

	@Override
	public void setTitle(String title) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMarker(int lineNumber) {
		// TODO Auto-generated method stub
		
	}
}