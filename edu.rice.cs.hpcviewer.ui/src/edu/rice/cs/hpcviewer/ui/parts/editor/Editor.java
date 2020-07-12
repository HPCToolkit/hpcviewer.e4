 
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
import edu.rice.cs.hpcviewer.ui.util.Constants;
import edu.rice.cs.hpcviewer.ui.util.Utilities;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.LineNumberRulerColumn;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;


/**********************************************
 * 
 * Class to display the content of a file .
 * <p>
 * The main method to display the file is by using
 * {@link Editor.display()} <br/>
 * 
 * This method will try to find if the object to display
 * is already there or not. If yes, then it activates the
 * file viewer. Otherwise, it will create a new viewer.
 *
 **********************************************/
public class Editor implements IUpperPart
{
	
	static final private String PROPERTY_DATA = "hpceditor.data";

	
	private SourceViewer textViewer;
	private Object input;
	private int    searchOffset;
	private FindReplaceDocumentAdapter finder;
	
	
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
	public BaseExperiment getExperiment() {
		Object obj = textViewer.getData(PROPERTY_DATA);
		
		if (obj == null)
			return null;
		
		if (obj instanceof Scope) {
			Scope scope = (Scope) obj;
			return scope.getExperiment();
		}
		if (obj instanceof BaseExperiment)
			return (BaseExperiment) obj;
		
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

	
	public boolean search(String text) {

		IRegion ir;
		
		try {
			if ((ir = finder.find(searchOffset, text, true, false, false, false)) != null) {
				
				setMarker(ir);
				searchOffset = ir.getOffset() + ir.getLength();
				return true;
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void setMarker(int lineNumber) {
		
		IDocument document = textViewer.getDocument();
		
		int maxLines = document.getNumberOfLines();
		lineNumber     = Math.max(0, Math.min(lineNumber, maxLines));

		try {
			int offset     = document.getLineOffset(lineNumber);
			int nextLine   = Math.min(lineNumber+1, maxLines);
			int nextOffset = document.getLineOffset(nextLine);
			int length     = Math.max(1, nextOffset - offset);
			
			document.addPosition(new Position(offset));
			
			TextSelection selection = new TextSelection(document, offset, length);
			textViewer.setSelection(selection, true);
			
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	
	public void setMarker(IRegion region) {
		IDocument document = textViewer.getDocument();
		try {
			document.addPosition(new Position(region.getOffset()));
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		TextSelection selection = new TextSelection(document, region.getOffset(), region.getLength());
		textViewer.setSelection(selection, true);
	}
	
	@Override
	public String getTitle() {
		return Editor.getTitle(input);
	}
	
	public static String getTitle(Object input) {
		String filename = null;
		
		if (input instanceof Scope) {
			filename = ((Scope)input).getSourceFile().getName(); 
		} else if (input instanceof BaseExperiment) {
			filename = ((BaseExperiment)input).getXMLExperimentFile().getName();
		}
		return filename;
	}

	@Override
	public String getPartDescriptorId() {
		return Constants.ID_VIEW_EDITOR;
	}

	@Override
	public void setInput(MPart part, Object input) {
		
		this.input = input;
		part.setLabel(getTitle());
		
		if (input instanceof Scope) {
			Scope scope = (Scope) input;
			
			if (!Utilities.isFileReadable(scope))
				return;
			
			FileSystemSourceFile file = (FileSystemSourceFile) scope.getSourceFile();
			
			String filename = file.getCompleteFilename();
			int lineNumber  = scope.getFirstLineNumber();

			displayFile(scope, filename, lineNumber);
			
			part.setTooltip(filename);
			
		} else if (input instanceof BaseExperiment) {
			
			BaseExperiment experiment = (BaseExperiment) input;
			String filename = experiment.getXMLExperimentFile().getAbsolutePath();
			
			displayFile(experiment, filename, 0);
			
			part.setTooltip(filename);
		}
		// add more condition for different type of objects here
		// we should make this more flexible...
	}

	
	
	/***
	 * Display the content of a file, and highligjt a specified line number (generic version).
	 * 
	 * @param obj the object that identified this editor. It has to be either a scope or an experiment
	 * @param filename the complete path of the file name
	 * @param lineNumber the line number to be revealed
	 */
	private void displayFile(Object obj, String filename, int lineNumber) {
		IDocument document = new Document();
		
		AnnotationModel annModel = new AnnotationModel();
		annModel.connect(document);
		
		String text = readLineByLineJava8(filename);
		document.set(text);

		textViewer.setDocument(document, annModel);
		textViewer.setData(PROPERTY_DATA, obj);

		finder = new FindReplaceDocumentAdapter(document);

		setMarker(lineNumber);
	}
}