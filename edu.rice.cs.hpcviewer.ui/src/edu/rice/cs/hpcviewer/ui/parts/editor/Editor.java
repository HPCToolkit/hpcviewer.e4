 
package edu.rice.cs.hpcviewer.ui.parts.editor;

import javax.inject.Inject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.source.FileSystemSourceFile;
import edu.rice.cs.hpcviewer.ui.util.Utilities;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.LineNumberRulerColumn;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;



public class Editor implements ICodeEditor
{
	static final public String ID = "edu.rice.cs.hpcviewer.ui.part.editor";
	static final public String ID_DESC = "edu.rice.cs.hpcviewer.ui.partdescriptor.editor";
	
	static final private String PROPERTY_DATA = "hpceditor.data";
	
	private SourceViewer textViewer;
	
	@Inject IEventBroker broker;
	@Inject MPart part;

	@Inject
	public Editor() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		
		// add line number column to the source viewer
		CompositeRuler ruler 	   = new CompositeRuler();
		LineNumberRulerColumn lnrc = new LineNumberRulerColumn();
		ruler.addDecorator(0,lnrc);

		textViewer = new SourceViewer(parent, ruler, SWT.BORDER| SWT.MULTI | SWT.V_SCROLL);
		textViewer.setEditable(false);
		
		StyledText styledText = textViewer.getTextWidget();		
		styledText.setFont(JFaceResources.getTextFont());
		
		// make sure to set fill alignment and grab both horizontal and vertical space
		// without this, the source viewer will display only small fraction of composite
		
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		textViewer.getControl().setLayoutData(gd);
		
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

			IDocument document = new Document();
			
			AnnotationModel annModel = new AnnotationModel();
			annModel.connect(document);
			
			String text = readLineByLineJava8(filename);
			document.set(text);

			textViewer.setDocument(document, annModel);
			textViewer.setData(PROPERTY_DATA, scope);
			
			try {
				int maxLines = document.getNumberOfLines();
				
				lineNumber     = Math.max(0, Math.min(lineNumber, maxLines));
				int offset     = document.getLineOffset(lineNumber);
				int nextLine   = Math.min(lineNumber+1, maxLines);
				int nextOffset = document.getLineOffset(nextLine);
				int length     = Math.max(1, nextOffset - offset);
				
				document.addPosition(new Position(offset));
				
				TextSelection selection = new TextSelection(document, offset, length);
				textViewer.setSelection(selection, true);
				
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public BaseExperiment getExperiment() {
		Scope scope = (Scope) textViewer.getData(PROPERTY_DATA);
		
		if (scope != null)
			return scope.getExperiment();
		
		return null;
	}
	
	private static String readLineByLineJava8(String filePath) 
	{
	    StringBuilder contentBuilder = new StringBuilder();
	    try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8)) 
	    {
	        stream.forEach(s -> contentBuilder.append(s).append("\n"));
	    }
	    catch (IOException e) 
	    {
	        e.printStackTrace();
	    }
	    return contentBuilder.toString();
	}

	@Override
	public void setTitle(String title) {}

	@Override
	public void setMarker(int lineNumber) {}
}