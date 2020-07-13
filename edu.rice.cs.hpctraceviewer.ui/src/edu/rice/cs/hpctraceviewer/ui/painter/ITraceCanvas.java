package edu.rice.cs.hpctraceviewer.ui.painter;


import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;

public interface ITraceCanvas 
extends MouseListener, MouseMoveListener
{
	enum MouseState { ST_MOUSE_INIT, ST_MOUSE_NONE, ST_MOUSE_DOWN };

}
