package edu.rice.cs.hpctraceviewer.ui.preferences;

public class TracePreferenceConstants 
{
	public final static int RENDERING_MIDPOINT  = 0;
	public final static int RENDERING_RIGHTMOST = 1;
	
	public final static String []renderingOptions = {"Midpoint painting", "Rightmost painting"};
	
	public final static String PREF_RENDER_OPTION = "trace/render";	
	public final static String PREF_MAX_THREADS   = "trace/threads";
	public final static String PREF_TOOLTIP_DELAY = "trace/tooltip";

	public final static int DEFAULT_MAX_THREADS   = 10;
	public final static int DEFAULT_TOOLTIP_DELAY = 2000;

}
