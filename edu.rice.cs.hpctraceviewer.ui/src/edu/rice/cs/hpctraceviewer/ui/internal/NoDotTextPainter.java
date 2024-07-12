// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctraceviewer.ui.internal;

import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.swt.graphics.GC;

public class NoDotTextPainter extends TextPainter {

	@Override
	protected String getTextToDisplay(ILayerCell cell, GC gc, int availableLength, String text) {

        if (isTrimText()) {
            text = text.trim();
        }
        return text;	
	}
}
