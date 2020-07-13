package edu.rice.cs.hpctraceviewer.ui.resources;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

import edu.rice.cs.hpcbase.ui.BaseIconManager;

public class IconManager extends BaseIconManager
{

	final static public String HOME = "home-screen.png";
	final static public String ZOOM_IN_H = "zoom-in-time.png";
	final static public String ZOOM_IN_V = "zoom-in-process.png"; 
	final static public String ZOOM_OUT_H = "zoom-out-time.png";
	final static public String ZOOM_OUT_V = "zoom-out-process.png";
	final static public String GO_EAST = "go-east.png";
	final static public String GO_WEST = "go-west.png";
	final static public String GO_NORTH = "go-north.png";
	final static public String GO_SOUTH = "go-south.png";
	final static public String UNDO = "undo.png"; 
	final static public String REDO = "redo.png";
	final static public String SAVE = "save.png";
	final static public String OPEN = "open.png";
	
	
	public void init(ImageRegistry registry) 
	{
		registerImage(registry, getClass(), HOME);
		registerImage(registry, getClass(), ZOOM_IN_H);
		registerImage(registry, getClass(), ZOOM_IN_V);
		registerImage(registry, getClass(), ZOOM_OUT_H);
		registerImage(registry, getClass(), ZOOM_OUT_V);
		registerImage(registry, getClass(), GO_EAST);
		registerImage(registry, getClass(), GO_WEST);
		registerImage(registry, getClass(), GO_NORTH);
		registerImage(registry, getClass(), GO_SOUTH);
		registerImage(registry, getClass(), UNDO);
		registerImage(registry, getClass(), REDO);
		registerImage(registry, getClass(), SAVE);
		registerImage(registry, getClass(), OPEN);
	}
	
	public ImageDescriptor getObjectDescriptor(String title) 
	{
		return ImageDescriptor.createFromFile(getClass(), title);
	}
}
