// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcviewer.ui.internal;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;

import edu.rice.cs.hpcmetric.IFilterable;
import edu.rice.cs.hpcviewer.ui.base.ITableViewPart;

public abstract class AbstractView extends CTabItem implements ITableViewPart, IFilterable 
{
	protected AbstractView(CTabFolder parent, int style) {
		super(parent, style);
	}
}
