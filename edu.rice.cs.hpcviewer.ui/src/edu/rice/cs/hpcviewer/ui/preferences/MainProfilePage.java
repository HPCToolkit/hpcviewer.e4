package edu.rice.cs.hpcviewer.ui.preferences;

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

public class MainProfilePage extends AbstractPage 
{	
	private ViewerFontFieldEditor fontGenericEditor;
	private ViewerFontFieldEditor fontMetricEditor;
	private ViewerFontFieldEditor fontSourceEditor;
	
	
	public MainProfilePage(PropertiesResources resources, String title) {
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
		System.out.println("restore default");
		Font fontGeneric = JFaceResources.getDefaultFont();

		PreferenceStore store = (PreferenceStore) getPreferenceStore();
		PreferenceConverter.setValue(store, PreferenceConstants.ID_FONT_GENERIC, fontGeneric.getFontData());

		fontGenericEditor.setFontLabel(fontGeneric);
		
		Font fontFixed = JFaceResources.getTextFont();

		PreferenceConverter.setValue(store, PreferenceConstants.ID_FONT_METRIC, fontGeneric.getFontData());

		fontMetricEditor.setFontLabel(fontFixed);
		fontSourceEditor.setFontLabel(fontFixed);
	}
	
	static class ViewerFontFieldEditor extends FontFieldEditor
	{
		private final Composite groupFont;
		
		public ViewerFontFieldEditor(String id, String label, Composite parent) {
			super(id, label, parent);
			
			this.groupFont = parent;
		}

		
		public void setFontLabel(Font font) {
			Label label = getValueControl(groupFont);
			if (label == null)
				return;
			
			String fontLabel = StringConverter.asString(font.getFontData());
			label.setText(fontLabel);
		}
	}
}
