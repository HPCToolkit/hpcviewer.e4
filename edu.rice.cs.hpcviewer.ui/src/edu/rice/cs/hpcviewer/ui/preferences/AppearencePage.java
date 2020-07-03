package edu.rice.cs.hpcviewer.ui.preferences;

import java.io.IOException;

import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcviewer.ui.resources.FontManager;

/***********************************************************
 * 
 * Setting for the appearances (fonts, colors, ...)
 *
 ***********************************************************/
public class AppearencePage extends AbstractPage 
{
	private ViewerFontFieldEditor fontGenericEditor;
	private ViewerFontFieldEditor fontMetricEditor;
	private ViewerFontFieldEditor fontSourceEditor;
	

	public AppearencePage(PropertiesResources resources, String title) {
		super(resources, title);
	}

	
	
	@Override
	public boolean performOk() {
		apply();
		return true;
	}
	
	@Override
	public void apply() {
		fontMetricEditor .store();
		fontGenericEditor.store();
		fontSourceEditor .store();
		
		saveFont(fontMetricEditor , PreferenceConstants.ID_FONT_METRIC);
		saveFont(fontGenericEditor, PreferenceConstants.ID_FONT_GENERIC);
		saveFont(fontSourceEditor , PreferenceConstants.ID_FONT_TEXT);
	}

	@Override
	protected Control createContents(Composite parent) {

		Group groupFont = createGroupControl(parent, "Fonts", false);
		groupFont.setLayout(new GridLayout(1, false));
        
        fontGenericEditor = createFontEditor(groupFont, PreferenceConstants.ID_FONT_GENERIC, "Tree column font");        
        fontMetricEditor  = createFontEditor(groupFont, PreferenceConstants.ID_FONT_METRIC,  "Metric column font");
        fontSourceEditor  = createFontEditor(groupFont, PreferenceConstants.ID_FONT_TEXT,    "Text editor font");
        
		return parent;
	}

	
	private void saveFont(ViewerFontFieldEditor editor, String id) {
		Font font = editor.getChosenFont();

		if (font != null) {
			try {
				FontManager.setFontPreference(id, font.getFontData());

			} catch (IOException e) {
				Logger logger = LoggerFactory.getLogger(getClass());
				logger.error("Unable to store the preference " + id, e);
			}
		}

	}
	
	private ViewerFontFieldEditor createFontEditor(Composite parent, String id, String label) {
        
		ViewerFontFieldEditor editor = new ViewerFontFieldEditor(id, label, parent);
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

		Font fontGeneric = JFaceResources.getDefaultFont();

		PreferenceStore store = (PreferenceStore) getPreferenceStore();
		PreferenceConverter.setValue(store, PreferenceConstants.ID_FONT_GENERIC, fontGeneric.getFontData());

		fontGenericEditor.setFontLabel(fontGeneric);
		
		Font fontFixed = JFaceResources.getTextFont();

		PreferenceConverter.setValue(store, PreferenceConstants.ID_FONT_METRIC, fontGeneric.getFontData());

		fontMetricEditor.setFontLabel(fontFixed);
		fontSourceEditor.setFontLabel(fontFixed);
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
		private Font chosenFont;
		
		public ViewerFontFieldEditor(String id, String label, Composite parent) {
			super(id, label, parent);
			
			this.groupFont = parent;
			chosenFont = null;
		}

		/***
		 * Update the font. Needs to reflect to the value control label and
		 * the preference itself.
		 * @param font
		 */
		public void setFontLabel(Font font) {
			Label label = getValueControl(groupFont);
			if (label == null)
				return;
			
			String fontLabel = StringConverter.asString(font.getFontData());
			label.setText(fontLabel);
			
			this.chosenFont = font;
		}
		
		
		/****
		 * return the chosen font if the last action is "Restore defaults"
		 * Otherwise return null.
		 * 
		 * @return Font (see the description)
		 */
		public Font getChosenFont() {
			if (chosenFont == null)
				return null;
			
			Label label = getValueControl(groupFont);
			String fontName = label.getText();
			String chosenName = StringConverter.asString(chosenFont.getFontData());
			
			if (fontName.equals(chosenName))
				return chosenFont;
			
			return null;
		}
	}

}
