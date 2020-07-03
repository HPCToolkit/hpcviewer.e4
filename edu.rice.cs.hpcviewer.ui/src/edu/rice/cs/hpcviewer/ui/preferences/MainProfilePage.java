package edu.rice.cs.hpcviewer.ui.preferences;

import java.io.IOException;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainProfilePage extends AbstractPage 
{	
	Button debugMode;
	Button cctId, flatId;
	
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
		PreferenceStore prefDebug = ViewerPreferenceManager.INSTANCE.getPreferenceStore();
		boolean debug = debugMode.getSelection();
		
		prefDebug.setValue(PreferenceConstants.ID_MODE_DEBUG, debug);
		
		try {
			prefDebug.save();
		} catch (IOException e) {
			Logger logger = LoggerFactory.getLogger(getClass());
			logger.error("Unable to save preferences", e);
		}
	}

	@Override
	protected Control createContents(Composite parent) {

		Group groupFont = createGroupControl(parent, "Debug", false);
		groupFont.setLayout(new GridLayout(1, false));
        
		debugMode = createCheckBoxControl(groupFont, "Enable debug mode");
        cctId  = createCheckBoxControl(groupFont, "Show calling-context (CCT) Id");
        flatId = createCheckBoxControl(groupFont, "Show structure (flat) Id");
		
		return parent;
	}
	
	
	@Override
	public IPreferenceStore getPreferenceStore() {
		return ViewerPreferenceManager.INSTANCE.getPreferenceStore();
	}
	
	
	@Override
	protected void performDefaults() {
	}
	
	
}
