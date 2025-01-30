// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcbase.ui;

import edu.rice.cs.hpcbase.IBaseInput;

public interface IUpperPart 
{
	String getTitle ();
	
	void setInput(IBaseInput input);
	
	boolean hasEqualInput(IBaseInput input);
	
	void setMarker(int lineNumber);
	
	void setFocus();
}
