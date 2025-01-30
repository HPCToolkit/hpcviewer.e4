// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.data.color;

import org.eclipse.swt.graphics.RGB;

/*****************************
 * 
 * Interface to create a color
 *
 *****************************/
public interface IColorGenerator 
{
	/****
	 * Generate a color for a specific procedure.
	 * The implemented class can create a color either based on the
	 * procedure name or other criteria.
	 * 
	 * @param procedureName name of the procedure
	 * @return {@code RGB} new color
	 */
	RGB createColor(String procedureName);
}
