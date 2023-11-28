package edu.rice.cs.hpcsetting.preferences;

import java.io.IOException;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.rice.cs.hpclog.LogProperty;

public class DebugConfigPage extends AbstractPage 
{	
	public static final String TITLE = "Debugging";
	
	private Button debugMode;
	private Button cctId;
	private Button flatId;
	private Button featureMode;
	
	public DebugConfigPage() {
		super(TITLE);
	}
	
	
	@Override
	public boolean performOk() {
		apply();
		return true;
	}
	
	@Override
	public void apply() {
		PreferenceStore prefStore = ViewerPreferenceManager.INSTANCE.getPreferenceStore();
		
		if (debugMode == null || cctId == null || flatId == null)
			return;
		
		boolean debug = debugMode.getSelection();		
		prefStore.setValue(PreferenceConstants.ID_DEBUG_MODE, debug);
		
		boolean debugCCT = cctId.getSelection();
		prefStore.setValue(PreferenceConstants.ID_DEBUG_CCT_ID, debugCCT);
		
		boolean debugFlat = flatId.getSelection();
		prefStore.setValue(PreferenceConstants.ID_DEBUG_FLAT_ID, debugFlat);
		
		boolean featureExperimental = featureMode.getSelection();
		prefStore.setValue(PreferenceConstants.ID_FEATURE_EXPERIMENTAL, featureExperimental);
		
		// save the preference in this page
		try {
			prefStore.save();
		} catch (IOException e) {
			Logger logger = LoggerFactory.getLogger(getClass());
			logger.error("Unable to save preferences", e);
		}
	}

	@Override
	protected Control createContents(Composite parent) {
        // group of debug flags
        createDebugPanel(parent);
        
        // panel for experimental features
        createExperimentalFeaturePanel(parent);
        
		return parent;
	}
	
	
	private void createDebugPanel(Composite parent) {
		
		Group groupDebug = createGroupControl(parent, "Debug", false);
		groupDebug.setLayout(new GridLayout(1, false));
        
		debugMode = createCheckBoxControl(groupDebug, "Enable debug mode");
		debugMode.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				setDebugMode(debugMode.getSelection());
			}
		});
        cctId  = createCheckBoxControl(groupDebug, "Show calling-context (CCT) Id");
        flatId = createCheckBoxControl(groupDebug, "Show structure (flat) Id");
		
        // initialize the debugging mode
        
        PreferenceStore pref = (PreferenceStore) getPreferenceStore();
        boolean debug = pref.getBoolean(PreferenceConstants.ID_DEBUG_MODE);
        setDebugMode(debug);
        
        boolean debugCCT = pref.getBoolean(PreferenceConstants.ID_DEBUG_CCT_ID);
        cctId.setSelection(debugCCT);
        
        boolean debugFlat = pref.getBoolean(PreferenceConstants.ID_DEBUG_FLAT_ID);
        flatId.setSelection(debugFlat);
	}
	
	
	private void createExperimentalFeaturePanel(Composite parent) {
        
        var groupFeature = createGroupControl(parent, "Feature", false);
        groupFeature.setLayout(new GridLayout(1, false));

        featureMode = createCheckBoxControl(groupFeature, "Enable experimental features");
        featureMode.setToolTipText("Check the box to enable experimental features.");
        
        PreferenceStore pref = (PreferenceStore) getPreferenceStore();
        var featureEnabled = pref.getBoolean(PreferenceConstants.ID_FEATURE_EXPERIMENTAL);
        
        featureMode.setSelection(featureEnabled);
	}
	
	private void setDebugMode(boolean enabled) {
		debugMode.setSelection(enabled);
		cctId.setEnabled(enabled);
		flatId.setEnabled(enabled);
		if (!enabled) {
			cctId.setSelection(false);
			flatId.setSelection(false);
		}
		LogProperty.setDebug(enabled);
	}
	
	
	@Override
	public IPreferenceStore getPreferenceStore() {
		return ViewerPreferenceManager.INSTANCE.getPreferenceStore();
	}
	
	
	@Override
	protected void performDefaults() {
		setDebugMode(false);
		
		featureMode.setSelection(false);
	}
}
