package edu.rice.cs.hpcsetting.preferences;

import java.io.IOException;

import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridLayout;
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
	public final static String TITLE = "Appearance";
	
	private ViewerFontFieldEditor fontGenericEditor;
	private ViewerFontFieldEditor fontMetricEditor;
	private ViewerFontFieldEditor fontSourceEditor;
	

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
		saveFont(fontMetricEditor , PreferenceConstants.ID_FONT_METRIC);
		saveFont(fontGenericEditor, PreferenceConstants.ID_FONT_GENERIC);
		saveFont(fontSourceEditor , PreferenceConstants.ID_FONT_TEXT);
	}

	@Override
	protected Control createContents(Composite parent) {

		Group groupFont = createGroupControl(parent, "Fonts", false);
		groupFont.setLayout(new GridLayout(1, false));
		
        fontGenericEditor = createFontEditor(groupFont, PreferenceConstants.ID_FONT_GENERIC, "Default column font",   FontManager.getFontGeneric());        
        fontMetricEditor  = createFontEditor(groupFont, PreferenceConstants.ID_FONT_METRIC,  "Metric column font", FontManager.getMetricFont());
        fontSourceEditor  = createFontEditor(groupFont, PreferenceConstants.ID_FONT_TEXT,    "Text editor font"  , FontManager.getTextEditorFont());
        
		return parent;
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
