package edu.rice.cs.hpctraceviewer.config;

public class TracePreferenceConstants 
{
	public final static int RENDERING_MIDPOINT  = 0;
	public final static int RENDERING_RIGHTMOST = 1;
	
	public final static int COLOR_NAME_BASED  = 0;
	public final static int COLOR_RANDOM      = 1;
	
	public final static String []renderingOptions = {"Midpoint painting", "Rightmost painting"};
	public final static String []colorOptions     = {"Name-based color",  "Random color"};
	
	public final static String PREF_COLOR_OPTION  = "trace/color";	
	public final static String PREF_RENDER_OPTION = "trace/render";	
	public final static String PREF_MAX_THREADS   = "trace/threads";
	public final static String PREF_TOOLTIP_DELAY = "trace/tooltip";
	public final static String PREF_GPU_TRACES    = "trace/gpu";

	public final static int DEFAULT_MAX_THREADS    = 10;
	public final static int DEFAULT_TOOLTIP_DELAY  = 2000;
	public final static boolean DEFAULT_GPU_TRACES = true;
}
