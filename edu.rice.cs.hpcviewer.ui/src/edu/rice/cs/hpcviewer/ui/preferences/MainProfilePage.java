package edu.rice.cs.hpcviewer.ui.preferences;

import java.io.IOException;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcsetting.preferences.AbstractPage;

public class MainProfilePage extends AbstractPage 
{	
	Button debugMode;
	Button cctId, flatId;
	
	public MainProfilePage(String title) {
		super(title);
	}
	
	
	@Override
	public boolean performOk() {
		apply();
		return true;
	}
	
	@Override
	public void apply() {
		PreferenceStore prefDebug = ViewerPreferenceManager.INSTANCE.getPreferenceStore();
		
		if (debugMode == null || cctId == null || flatId == null)
			return;
		
		boolean debug = debugMode.getSelection();		
		prefDebug.setValue(PreferenceConstants.ID_DEBUG_MODE, debug);
		
		boolean debugCCT = cctId.getSelection();
		prefDebug.setValue(PreferenceConstants.ID_DEBUG_CCT_ID, debugCCT);
		
		boolean debugFlat = flatId.getSelection();
		prefDebug.setValue(PreferenceConstants.ID_DEBUG_FLAT_ID, debugFlat);
		
		// save the preference in this page
		try {
			prefDebug.save();
		} catch (IOException e) {
			Logger logger = LoggerFactory.getLogger(getClass());
			logger.error("Unable to save preferences", e);
		}
	}

	@Override
	protected Control createContents(Composite parent) {
        
        String location = ViewerPreferenceManager.INSTANCE.getPreferenceStoreLocation();
        createLabelControl(parent, "Preference file: " + location);      

        createDebugPanel(parent);
        
		return parent;
	}
	
	
	private void createDebugPanel(Composite parent) {
		
		Group groupDebug = createGroupControl(parent, "Debug", false);
		groupDebug.setLayout(new GridLayout(1, false));
        
		debugMode = createCheckBoxControl(groupDebug, "Enable debug mode");
		debugMode.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				setDebugMode(debugMode.getSelection());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
        cctId  = createCheckBoxControl(groupDebug, "Show calling-context (CCT) Id");
        flatId = createCheckBoxControl(groupDebug, "Show structure (flat) Id");
		
        // initialize the debugging mode
        
        PreferenceStore pref = (PreferenceStore) getPreferenceStore();
        boolean debugMode = pref.getBoolean(PreferenceConstants.ID_DEBUG_MODE);
        setDebugMode(debugMode);
        
        boolean debugCCT = pref.getBoolean(PreferenceConstants.ID_DEBUG_CCT_ID);
        cctId.setSelection(debugCCT);
        
        boolean debugFlat = pref.getBoolean(PreferenceConstants.ID_DEBUG_FLAT_ID);
        flatId.setSelection(debugFlat);
	}
	
	private void setDebugMode(boolean enabled) {
		debugMode.setSelection(enabled);
		cctId.setEnabled(enabled);
		flatId.setEnabled(enabled);
	}
	
	
	@Override
	public IPreferenceStore getPreferenceStore() {
		return ViewerPreferenceManager.INSTANCE.getPreferenceStore();
	}
	
	
	@Override
	protected void performDefaults() {
	}
}
