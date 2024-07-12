// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctraceviewer.ui.util;

public interface IConstants 
{
	public static final String TOPIC_STATISTICS    = "trace/stat";
	public static final String TOPIC_BLAME         = "trace/blame";
	public static final String TOPIC_DEPTH_UPDATE  = "trace/depth/update";
	public static final String TOPIC_FILTER_RANKS  = "trace/filter";
	public static final String TOPIC_COLOR_MAPPING = "trace/colorMap";

	public static final String URI_CONTRIBUTOR     = "platform:/plugin/edu.rice.cs.hpctraceviewer.ui";
	public static final String ID_DATA_OPERATION   = "trace/op/undo";

	public final static String MAX_DEPTH_LABEL = "IconMaxDepth";
	public final static String MAX_DEPTH_FILE  =  "platform:/plugin/edu.rice.cs.hpctraceviewer.ui/resources/max-depth16.png";

	public static final int COLUMN_COLOR_WIDTH_PIXELS = 14;
}
