// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.main;

import org.eclipse.swt.graphics.Image;

import edu.rice.cs.hpctraceviewer.ui.internal.ImagePosition;

public class DetailImagePosition extends ImagePosition {

	final public Image imageOriginal;
	
	public DetailImagePosition(int position, Image image, Image imageOriginal) {
		super(position, image);
		this.imageOriginal = imageOriginal;
	}

}
