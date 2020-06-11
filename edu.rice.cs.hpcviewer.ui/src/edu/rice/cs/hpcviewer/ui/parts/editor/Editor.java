 
package edu.rice.cs.hpcviewer.ui.parts.editor;

import javax.inject.Inject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.source.FileSystemSourceFile;
import edu.rice.cs.hpcviewer.ui.util.Utilities;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
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
public class Editor implements ICodeEditor
{
	static final public String STACK_ID = "edu.rice.cs.hpcviewer.ui.partstack.upper";
	static final public String ID 		= "edu.rice.cs.hpcviewer.ui.part.editor";
	static final public String ID_DESC  = "edu.rice.cs.hpcviewer.ui.partdescriptor.editor";
	
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

	/****
	 * Main method to display the file.
	 * 
	 * @param modelService
	 * @param partService
	 * @param app
	 * @param obj The object to display (either a scope or a database)
	 */
	static public void display(EModelService modelService, 
			EPartService partService, 
			MApplication  app,
			Object obj) {
		
		if (obj == null)
			return;
		
		String filename = null;
		
		if (obj instanceof Scope) {
			filename = ((Scope)obj).getSourceFile().getName(); 
		} else if (obj instanceof BaseExperiment) {
			filename = ((BaseExperiment)obj).getXMLExperimentFile().getAbsolutePath();
		}
		
		Collection<MPart> listParts = partService.getParts();
		for(MPart mp : listParts) {
			if (mp.getElementId().equals(filename)) {
				
				if (mp.getObject() == null) {
					partService.showPart(mp, PartState.CREATE);
				}
				MPart shownPart = partService.showPart(mp, PartState.VISIBLE);

				Editor editor = (Editor) shownPart.getObject();
				editor.displayFile(obj);
				
				return;
			}
		}

		final MPart part = partService.createPart(Editor.ID_DESC);

		part.setLabel(filename);
		part.setElementId(filename);

		MPartStack editorStack = (MPartStack)modelService.find(STACK_ID, app);
		editorStack.getChildren().add(part);

		MPart shownPart = partService.showPart(part, PartState.VISIBLE);
		Editor editor = (Editor) shownPart.getObject();
		editor.displayFile(obj);
	}

	/***
	 * Display the file of the object. 
	 * If the object is a scope, it will display the source code.
	 * If the object is an experiment database, it will display the XML file
	 * 
	 * @param obj
	 */
	public void displayFile(Object obj) {
		
		if (obj instanceof Scope) {
			Scope scope = (Scope) obj;
			
			if (!Utilities.isFileReadable(scope))
				return;
			
			FileSystemSourceFile file = (FileSystemSourceFile) scope.getSourceFile();
			
			String filename = file.getCompleteFilename();
			int lineNumber  = scope.getFirstLineNumber();

			displayFile(scope, filename, lineNumber);
			
		} else if (obj instanceof BaseExperiment) {
			
			BaseExperiment experiment = (BaseExperiment) obj;
			String filename = experiment.getXMLExperimentFile().getAbsolutePath();
			
			displayFile(experiment, filename, 0);
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
	public void displayFile(Object obj, String filename, int lineNumber) {
		IDocument document = new Document();
		
		AnnotationModel annModel = new AnnotationModel();
		annModel.connect(document);
		
		String text = readLineByLineJava8(filename);
		document.set(text);

		textViewer.setDocument(document, annModel);
		textViewer.setData(PROPERTY_DATA, obj);
		
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

	@Override
	public void setTitle(String title) {}

	@Override
	public void setMarker(int lineNumber) {}
}