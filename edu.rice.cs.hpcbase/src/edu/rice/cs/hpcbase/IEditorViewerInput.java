// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcbase;

import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpcbase.ui.IUpperPart;

public interface IEditorViewerInput extends IBaseInput
{
	/****
	 * Create an editor based on the parent tab folder
	 * 
	 * @param parent
	 * 			{@code CTabFolder} composite parent
	 * 
	 * @return {@code IUpperPart}
	 * 			The new created editor
	 * 		
	 */
	IUpperPart createViewer(Composite parent);
	
	/****
	 * Check if the content of the editor depends on the activated view.
	 * 
	 * @return {@code boolean} true if the content depends on the view.
	 */
	boolean needToTrackActivatedView();
}
