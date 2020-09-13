package edu.rice.cs.hpctraceviewer.ui.util;

public interface IConstants 
{
	public static final String TOPIC_STATISTICS    = "trace/stat";
	public static final String TOPIC_BLAME    = "trace/blame";
	public static final String TOPIC_DEPTH_UPDATE  = "trace/depth/update";
	public static final String TOPIC_FILTER_RANKS  = "trace/filter";
	public static final String TOPIC_COLOR_MAPPING = "trace/colorMap";

	public static final String URI_CONTRIBUTOR    = "platform:/plugin/edu.rice.cs.hpctraceviewer.ui";
	public static final String ID_DATA_OPERATION   = "trace/op/undo";

	public static final int    TOOLTIP_DELAY_MS  = 2000;
}
