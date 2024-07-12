// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcbase;

public interface IEditorInput extends IEditorViewerInput
{
	
	String getContent();
	
	int getLine();
}
