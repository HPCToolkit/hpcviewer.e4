 
package edu.rice.cs.hpcviewer.ui.parts.editor;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcbase.IBaseInput;
import edu.rice.cs.hpcbase.IEditorInput;
import edu.rice.cs.hpcbase.ui.AbstractUpperPart;
import edu.rice.cs.hpcbase.ui.ILowerPart;
import edu.rice.cs.hpcdata.experiment.BaseExperiment;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcgraph.GraphEditorInput;
import edu.rice.cs.hpcsetting.fonts.FontManager;
import edu.rice.cs.hpcsetting.preferences.PreferenceConstants;
import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceManager;
import edu.rice.cs.hpcviewer.ui.dialogs.SearchDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.LineNumberRulerColumn;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Font;
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
public class Editor extends AbstractUpperPart implements IPropertyChangeListener
{
	
	private static final String PROPERTY_DATA = "hpceditor.data";

	private final LineNumberRulerColumn lnrc ;
	private final DialogSettings dialogSettings;
	private final SourceViewer textViewer;
	
	private int    searchOffsetStart, searchOffsetEnd;
	private FindReplaceDocumentAdapter finder;
	

	/******
	 * Create a new editor tab based on the parent folder.
	 * Editor is a {@link CTabItem} object.
	 * @param parent
	 * @param style
	 */
	public Editor(CTabFolder parent, int style) {
		super(parent, style);
		setShowClose(true);
		
		Composite container = new Composite(parent, SWT.NONE);
		
		// add line number column to the source viewer
		CompositeRuler ruler 	   = new CompositeRuler();
		lnrc = new LineNumberRulerColumn();

		Font font = FontManager.getTextEditorFont();

		lnrc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
		lnrc.setFont(font);
		ruler.addDecorator(0,lnrc);

		textViewer = new SourceViewer(container, ruler, SWT.BORDER| SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		textViewer.setEditable(false);
		
		StyledText styledText = textViewer.getTextWidget();
		styledText.setFont(font);
		
		// make sure to set fill alignment and grab both horizontal and vertical space
		// without this, the source viewer will display only small fraction of composite
		
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		textViewer.getControl().setLayoutData(gd);
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(container);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(container);
		
		ViewerPreferenceManager.INSTANCE.getPreferenceStore().addPropertyChangeListener(this);
		dialogSettings = new DialogSettings(PROPERTY_DATA);

		setControl(container);
		setMenu();
		setKeyCommand();
	}
	
	
	@Override
	public void dispose() {
		ViewerPreferenceManager.INSTANCE.getPreferenceStore().removePropertyChangeListener(this);
		super.dispose();
	}
	
	public boolean hasEqualInput(IBaseInput input) {
		if (input == null) return false;
		
		var myId = textViewer.getData(PROPERTY_DATA);
		var theirId = input.getId();
		
		return myId.equals(theirId);
	}
	
	
	private void find() {
		SearchDialog searchDialog = new SearchDialog(getControl().getShell(), SWT.NONE);
		searchDialog.setInput(this, dialogSettings);
		searchDialog.open();
	}
	
	/****
	 * create the context menu for this source text viewer.
	 */
	private void setMenu() {
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(manager -> {
			manager.add(new Action("Find") {
				@Override
				public void run() {
					find();
				}
			});
		});
		
		StyledText control = textViewer.getTextWidget();
		Menu ctxMenu = menuManager.createContextMenu(control);
		// Fix issue #162: hide the context menu by default
		ctxMenu.setVisible(false);
		control.setMenu(ctxMenu);
	}
	
	
	private void setKeyCommand() {
		StyledText textPart = textViewer.getTextWidget();
		textPart.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if ( e.stateMask == SWT.MOD1 && (e.keyCode == 'f' || e.keyCode == 'F')) {
					find();
				}
			}
		});
	}
	
	/*****
	 * Search a text in the source viewer
	 * @param text
	 * 			Text to search
	 * @param forward
	 * 			search direction: {@code true} goes forward, {@code false} backward
	 * @param caseSensitive
	 * 			boolean true if the search has to be case sensitive. false otherwise
	 * @param wholeWord
	 * 			true if the search is exclusively whole word.
	 * @param wrap
	 * 			true if the search wrap to the other side of the file
	 * @return 
	 * 		boolean true if the text is found
	 * @throws Exception 
	 */
	public boolean search(String text, boolean forward, boolean caseSensitive, boolean wholeWord, boolean wrap) throws Exception {

		IRegion ir;
		int offset = forward ? searchOffsetEnd : searchOffsetStart;
		
		// make sure we allow user cursor position
		// if the cursor position is the same as the previous selected region, we should use
		// the last cursor position to avoid searching in the same region
		
		ITextSelection selection = (ITextSelection) textViewer.getSelectionProvider().getSelection();
		if (selection != null) {
			int selOffset = selection.getOffset();
			if (selOffset < searchOffsetStart || selOffset > searchOffsetEnd)
				offset = selection.getOffset();
		}
		
		if ((ir = finder.find(offset, text, forward, caseSensitive, wholeWord, false)) != null) {				
			setMarker(ir);
			searchOffsetStart = ir.getOffset();
			searchOffsetEnd = ir.getOffset() + ir.getLength();
			return true;
		} else if (wrap) {
			// cannot find a region, try to search from the beginning
			IDocument document = textViewer.getDocument();
			offset = forward ? 0 : document.getLength();			
			if ((ir = finder.find(offset, text, forward, caseSensitive, wholeWord, false)) != null) {					
				setMarker(ir);
				searchOffsetStart = ir.getOffset();
				searchOffsetEnd = ir.getOffset() + ir.getLength();
				return true;				
			}
		}
		return false;
	}

	@Override
	public void setMarker(int lineNumber) {
		
		IDocument document = textViewer.getDocument();
		
		int maxLines = document.getNumberOfLines()-1;
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
			// no need to catch?
			LoggerFactory.getLogger(getClass()).warn(e.getMessage(), e);
		}
		
		TextSelection selection = new TextSelection(document, region.getOffset(), region.getLength());
		textViewer.setSelection(selection, true);
	}
	
	
	@Override
	public String getTitle() {
		Object input = textViewer.getData(PROPERTY_DATA);
		return getTitle(input);
	}
	
	
	String getTitle(Object input) {
		String filename = null;
		
		if (input instanceof String) {
			filename = (String) input;
		} else if (input instanceof Scope) {
			filename = ((Scope)input).getSourceFile().getName(); 
		} else if (input instanceof BaseExperiment) {
			filename = ((BaseExperiment)input).getExperimentFile().getName();
		} else if (input instanceof GraphEditorInput) {
			filename = ((GraphEditorInput)input).toString();
		}
		return filename;
	}


	@Override
	public void setInput(IBaseInput input) {
		setText(input.getShortName());
		
		setToolTipText(input.getLongName());
		
		var name = input.getLongName();
		var title = input.getShortName();
		
		setText(title);
		setToolTipText(name);
		
		var id = input.getId();
		
		IEditorInput editorInput = (IEditorInput) input;
		var data = editorInput.getContent();
		var line = editorInput.getLine();
		
		displayContent(id, data, line);
	}


	private void displayContent(String id, String text, int lineNumber) {
		IDocument document = new Document();
		
		AnnotationModel annModel = new AnnotationModel();
		annModel.connect(document);
		document.set(text);
		textViewer.setDocument(document, annModel);
		textViewer.setData(PROPERTY_DATA, id);

		finder = new FindReplaceDocumentAdapter(document);

		setMarker(lineNumber);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		final String property = event.getProperty();

		if (property.equals(PreferenceConstants.ID_FONT_TEXT)) {
			StyledText text = textViewer.getTextWidget();
			if (text == null)
				return;
			
			Font font = FontManager.getTextEditorFont();
			text.setFont(font);
			lnrc.setFont(font);
			
			textViewer.refresh();
		}
	}


	@Override
	public void setFocus() {
		if (textViewer != null)
			textViewer.getControl().setFocus();
	}


	@Override
	public void refresh(ILowerPart lowerPart) {
		// no need to refresh the content
	}
}