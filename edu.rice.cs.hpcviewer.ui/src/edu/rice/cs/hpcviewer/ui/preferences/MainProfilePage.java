package edu.rice.cs.hpcviewer.ui.preferences;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.osgi.service.prefs.Preferences;

public  class MainProfilePage extends AbstractPage 
{
	private final static String ID_FONT_GENERIC     = "hpcviewer.font.generic"; 
	private final static String ID_FONT_METRIC      = "hpcviewer.font.metric";
	
	public final static String  PREF_FILENAME       = "hpcviewer.prefs";

    final static String NODE_HPC = "edu.rice.cs.hpcviewer";

    private FontFieldEditor fontGenericEditor;
	private FontFieldEditor fontMetricEditor;
	
	private IPreferenceStore preferenceStore;
	String  fileLocation;
	
	public MainProfilePage(PropertiesResources resources, String title) {
		super(resources, title);
		
		fileLocation = initLocation();
	}

	
	private String initLocation() {
		Location location = Platform.getInstanceLocation();
				
		try {
			String directory = location.getURL().getFile();
			URL url = new URL("file", null, directory + "/" + PREF_FILENAME);
			
			return url.getFile();

		} catch (Exception e) {

			e.printStackTrace();
		}
		return null;
	}
	
	
	@Override
	public boolean performOk() {
		apply();
		return true;
	}
	
	@Override
	public void apply() {
		fontMetricEditor.store();
		fontGenericEditor.store();
		
		System.out.println("applied");
	}

	@Override
	protected Control createContents(Composite parent) {


        Group group = createGroupControl(parent, "Fonts", false);
        group.setLayout(new GridLayout(1, false));
        
        fontGenericEditor = createFontControl(group, ID_FONT_GENERIC, "Tree column font");
        fontGenericEditor.setPreferenceStore(getPreferenceStore());
        fontGenericEditor.load();
        
        fontMetricEditor = createFontControl(group, ID_FONT_METRIC, "Metric column font");
        fontMetricEditor.setPreferenceStore(getPreferenceStore());
        fontMetricEditor.load();
        
		return parent;
	}

	
	@Override
	public IPreferenceStore getPreferenceStore() {
		if (preferenceStore == null) {
			preferenceStore = new PreferenceStore(fileLocation);
		}
		return preferenceStore;
	}
}
