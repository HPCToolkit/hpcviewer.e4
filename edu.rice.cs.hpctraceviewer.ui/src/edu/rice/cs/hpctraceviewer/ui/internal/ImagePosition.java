// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctraceviewer.ui.internal;


import org.eclipse.swt.graphics.Image;

public class ImagePosition {
	final public Image image;
	final public int position;
	
	public ImagePosition(int position, Image image) 
	{
		this.image = image;
		this.position = position;
	}
}
