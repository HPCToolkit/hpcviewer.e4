// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcsetting.preferences;

import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.swt.widgets.Shell;


public class ViewerPreferenceDialog extends PreferenceDialog 
{	
	public ViewerPreferenceDialog(Shell parentShell) {
		super(parentShell, new PreferenceManager());		
	}

	
	public void addPage(final String label, IPreferencePage page) {		
		PreferenceNode pNode = new PreferenceNode(label);
		pNode.setPage(page);
		getPreferenceManager().addToRoot(pNode);
	}
	
	public void addPage(final String parent, final String label, IPreferencePage page) {		
		PreferenceNode pNode = new PreferenceNode(label);
		pNode.setPage(page);
		getPreferenceManager().addTo(parent, pNode);
	}
	
	@Override
	public int open() {
		super.create();
		
		getShell().setText("Preferences");
		getTreeViewer().expandAll();
		
		return super.open();
	}
}
