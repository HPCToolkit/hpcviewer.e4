package edu.rice.cs.hpcviewer.ui.preferences;

import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

public class MainProfilePage extends AbstractPage 
{	
	private FontFieldEditor fontGenericEditor;
	private FontFieldEditor fontMetricEditor;
	private FontFieldEditor fontSourceEditor;
	
	private Group groupFont;
	
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

		groupFont = createGroupControl(parent, "Fonts", false);
		groupFont.setLayout(new GridLayout(1, false));
        
        fontGenericEditor = createFontEditor(groupFont, PreferenceConstants.ID_FONT_GENERIC, "Tree column font");        
        fontMetricEditor  = createFontEditor(groupFont, PreferenceConstants.ID_FONT_METRIC,  "Metric column font");
        fontSourceEditor  = createFontEditor(groupFont, PreferenceConstants.ID_FONT_TEXT,    "Text editor font");
        
		return parent;
	}

	
	private FontFieldEditor createFontEditor(Composite parent, String id, String label) {
        
    	FontFieldEditor editor = new FontFieldEditor(id, label, parent);
		editor.setPreferenceStore(getPreferenceStore());
		editor.load();
        
        return editor;
	}
	
	public IPreferenceStore getPreferenceStore() {
		return ViewerPreferenceManager.INSTANCE.getPreferenceStore();
	}
	}
