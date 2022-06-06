package edu.rice.cs.hpcsetting.preferences;

import java.io.IOException;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcsetting.fonts.FontManager;

/***********************************************************
 * 
 * Setting for the appearances (fonts, colors, ...)
 *
 ***********************************************************/
public class AppearencePage extends AbstractPage 
{
	public static final  String TITLE = "Appearance";
	private static final String FORMAT_UNICODE = "\\u%04x";
	
	private ViewerFontFieldEditor fontGenericEditor, fontMetricEditor, fontSourceEditor;
	
	private ViewerFontFieldEditor fontCallsiteEditor;
	private Combo glyphComboEditor;

	public AppearencePage() {
		super(TITLE);
	}

	
	
	@Override
	public boolean performOk() {
		apply();
		return true;
	}
	
	@Override
	public void apply() {		
		saveFont(fontMetricEditor ,  PreferenceConstants.ID_FONT_METRIC);
		saveFont(fontGenericEditor,  PreferenceConstants.ID_FONT_GENERIC);
		saveFont(fontSourceEditor ,  PreferenceConstants.ID_FONT_TEXT);
		saveFont(fontCallsiteEditor, PreferenceConstants.ID_FONT_CALLSITE);
		
		saveGlyphOptions();
	}

	
	@Override
	protected Control createContents(Composite parent) {
		// font area
		//
		Group groupFont = createGroupControl(parent, "Fonts", false);
		groupFont.setLayout(new GridLayout(1, false));
		
        fontGenericEditor = createFontEditor(groupFont, PreferenceConstants.ID_FONT_GENERIC, "Default font:",   FontManager.getFontGeneric());        
        fontMetricEditor  = createFontEditor(groupFont, PreferenceConstants.ID_FONT_METRIC,  "Metric column font:", FontManager.getMetricFont());
        fontSourceEditor  = createFontEditor(groupFont, PreferenceConstants.ID_FONT_TEXT,    "Text editor font:"  , FontManager.getTextEditorFont());

        // call-site area
        //
        Group groupCallsite = createGroupControl(parent, "Call-site glyph settings ", false);
        fontCallsiteEditor  = createFontEditor(groupCallsite, PreferenceConstants.ID_FONT_CALLSITE, "Glyph font:",   FontManager.getCallsiteGlyphFont());

        Composite glyphArea = new Composite(groupCallsite, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, true).span(3, 1).applyTo(glyphArea);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(glyphArea);
        
        glyphComboEditor = createGlyphOptionCombo(glyphArea);     

        FontRegistry registry = new FontRegistry();
        
        fontCallsiteEditor.setPropertyChangeListener(event->{
        	FontData value = (FontData) event.getNewValue();
        	registry.put(PreferenceConstants.ID_FONT_CALLSITE, new FontData[] {value});
        	Font font = registry.get(PreferenceConstants.ID_FONT_CALLSITE);
        	glyphComboEditor.setFont(font);
        });

        return parent;
	}
	
	
	private Combo createGlyphOptionCombo(Composite parent) {        
        createLabelControl(parent, "Glyph characters:");
        var callToChars   = ViewerPreferenceManager.DEFAULT_CALLTO;
        var callFromChars = ViewerPreferenceManager.DEFAULT_CALLFROM;
        
        assert (callToChars.length == callFromChars.length);
        
        var contents = new String[callToChars.length];
        int select = 0;
        final String currValue = ViewerPreferenceManager.INSTANCE.getCallToGlyph();
        
        for(int i=0; i<callToChars.length; i++) {
        	contents[i] = callToChars[i] + " , " + callFromChars[i] + 
        				  "   ( " + String.format(FORMAT_UNICODE, (int)callToChars[i].charAt(0))   +
        				  " , "   + String.format(FORMAT_UNICODE, (int)callFromChars[i].charAt(0)) + " )"; 
        	if (currValue.equals(callToChars[i]))
        		select = i;
        }
        var combo = createComboControl(parent, contents, SWT.READ_ONLY);
        combo.select(select);
        combo.setFont(FontManager.getCallsiteGlyphFont());
        return combo;
	}

	
	private void saveFont(ViewerFontFieldEditor editor, String id) {
		if (editor == null)
			return;
		
		FontData font = editor.getChosenFont();

		try {
			FontManager.setFontPreference(id, new FontData[] {font});
		} catch (IOException e) {
			Logger logger = LoggerFactory.getLogger(getClass());
			logger.error("Unable to store the preference " + id, e);
		}
	}
	
	private ViewerFontFieldEditor createFontEditor(Composite parent, String id, String label, Font fontDefault) {        
		ViewerFontFieldEditor editor = new ViewerFontFieldEditor(parent, id, label, fontDefault.getFontData()[0]);
		editor.setPreferenceStore(getPreferenceStore());
		editor.load();
        
        return editor;
	}
	
	
	@Override
	public IPreferenceStore getPreferenceStore() {
		return ViewerPreferenceManager.INSTANCE.getPreferenceStore();
	}
	
	
	@Override
	protected void performDefaults() {
		FontData fontGeneric = JFaceResources.getDefaultFont().getFontData()[0];
		PreferenceStore store = (PreferenceStore) getPreferenceStore();
		PreferenceConverter.setValue(store, PreferenceConstants.ID_FONT_GENERIC, fontGeneric);

		fontGenericEditor.setFontLabel(fontGeneric);
		
		FontData fontFixed = JFaceResources.getTextFont().getFontData()[0];
		PreferenceConverter.setValue(store, PreferenceConstants.ID_FONT_METRIC, fontFixed);

		fontMetricEditor.setFontLabel(fontFixed);
		fontSourceEditor.setFontLabel(fontFixed);
		
		fontGenericEditor.store();
		fontMetricEditor.store();
		fontSourceEditor.store();

		Font fontGlyph    = FontManager.getCallsiteGlyphDefaultFont(fontGeneric);
		FontData fdGlyph  = fontGlyph.getFontData()[0];
		PreferenceConverter.setValue(store, PreferenceConstants.ID_FONT_CALLSITE, fdGlyph);
				
		fontCallsiteEditor.setFontLabel(fdGlyph);
		fontCallsiteEditor.store();
		
		glyphComboEditor.setFont(fontGlyph);
		glyphComboEditor.select(ViewerPreferenceManager.DEFAULT_CALLSITE_INDEX);
		saveGlyphOptions();
	}
	
	
	private void saveGlyphOptions() {
		// if this page is not created yet, we don't need to store the settings
		if (glyphComboEditor == null)
			return;
		
		var store = getPreferenceStore();
		int index = glyphComboEditor.getSelectionIndex();
		store.setValue(PreferenceConstants.ID_CHAR_CALLTO, ViewerPreferenceManager.DEFAULT_CALLTO[index]);
		store.setValue(PreferenceConstants.ID_CHAR_CALLFROM, ViewerPreferenceManager.DEFAULT_CALLFROM[index]);
	}
	
	/*************************************************************
	 * 
	 * A customized font field editor.
	 * This class supports a method to update the label of the font
	 *
	 *************************************************************/
	static class ViewerFontFieldEditor extends FontFieldEditor
	{
		private final Composite groupFont;
		
		public ViewerFontFieldEditor(Composite parent, String id, String label, FontData defaultFont) {
			super(id, label, parent);
			
			this.groupFont = parent;
			setFontLabel(defaultFont);
		}

		/***
		 * Update the font. Needs to reflect to the value control label and
		 * the preference itself.
		 * @param font
		 */
		public void setFontLabel(FontData font) {
			Label label = getValueControl(groupFont);
			if (label == null)
				return;
			
			String fontLabel = StringConverter.asString(font);
			label.setText(fontLabel);
		}
		
		
		/****
		 * return the chosen font if the last action is "Restore defaults"
		 * Otherwise return null.
		 * 
		 * @return FontData (see the description)
		 */
		public FontData getChosenFont() {
			Label label = getValueControl(groupFont);
			String fontName = label.getText();			
			return StringConverter.asFontData(fontName);
		}
	}
}
