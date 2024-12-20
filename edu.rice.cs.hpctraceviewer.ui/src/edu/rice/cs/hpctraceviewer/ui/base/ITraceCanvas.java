// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctraceviewer.ui.base;


import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;

public interface ITraceCanvas 
extends MouseListener, MouseMoveListener
{
	enum MouseState { ST_MOUSE_INIT, ST_MOUSE_NONE, ST_MOUSE_DOWN }

}
