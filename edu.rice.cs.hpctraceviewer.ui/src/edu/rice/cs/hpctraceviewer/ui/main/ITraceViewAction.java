package edu.rice.cs.hpctraceviewer.ui.main;

public interface ITraceViewAction {

	public void home();

	public void timeZoomIn();
	public void timeZoomOut();
	
	public void processZoomIn();
	public void processZoomOut();

	public void save();
	public void open();
	
	public void goUp();
	public void goDown();
	public void goRight();
	public void goLeft();
	
	public boolean canProcessZoomIn();
	public boolean canProcessZoomOut();

	public boolean canTimeZoomIn();
	public boolean canTimeZoomOut();
	
    public boolean canGoRight();    
    public boolean canGoLeft();
    public boolean canGoUp();
    public boolean canGoDown();
}
