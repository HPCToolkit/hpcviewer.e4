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
		
		prefDebug.setValue(PreferenceConstants.ID_DEBUG_MODE, debug);
		
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
		debugMode.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				setDebugMode(debugMode.getSelection());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
        cctId  = createCheckBoxControl(groupFont, "Show calling-context (CCT) Id");
        flatId = createCheckBoxControl(groupFont, "Show structure (flat) Id");
		
        // initialize the debugging mode
        
        PreferenceStore pref = (PreferenceStore) getPreferenceStore();
        boolean debugMode = pref.getBoolean(PreferenceConstants.ID_DEBUG_MODE);
        setDebugMode(debugMode);
        
        boolean debugCCT = pref.getBoolean(PreferenceConstants.ID_DEBUG_CCT_ID);
        cctId.setSelection(debugCCT);
        
        boolean debugFlat = pref.getBoolean(PreferenceConstants.ID_DEBUG_FLAT_ID);
        flatId.setSelection(debugFlat);
        
		return parent;
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
