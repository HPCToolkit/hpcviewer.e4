// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcbase.ui;

import edu.rice.cs.hpcbase.IEditorViewerInput;
import edu.rice.cs.hpcbase.ThreadViewInput;

public interface IProfilePart extends IMainPart 
{

	/***
	 * Display an editor in the top folder
	 * 
	 * @param input 
	 * 			The object input. Warning: its value can be anything.
	 * 
	 * @return {@code IUpperPart}
	 * 			The editor object if successful, {@code null} otherwise.
	 */
	IUpperPart addEditor(IEditorViewerInput input);
	
	
	/**
	 * Special adding a view
	 * 
	 * @param input
	 */
	void addThreadView(ThreadViewInput input);
}
