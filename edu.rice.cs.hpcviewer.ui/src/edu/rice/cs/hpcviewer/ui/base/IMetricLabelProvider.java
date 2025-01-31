// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcviewer.ui.base;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

public interface IMetricLabelProvider 
{
	Color getBackground(Object element);
	Color getForeground(Object element);
	Font getFont(Object element);
	String getText(Object element);
	boolean isEnabled();
	
	void setScope(Object scope);
	void setMetric(Object metric);
}
