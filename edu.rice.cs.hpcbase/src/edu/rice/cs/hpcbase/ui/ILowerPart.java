// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcbase.ui;

import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpcbase.IDatabase;


public interface ILowerPart 
{
	
	void createContent(Composite parent);
	
	void setInput(IDatabase database, Object input);
	
	Object getInput();
	
	void activate();
}
