// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.util;

public interface IConstants 
{
	String TOPIC_STATISTICS    = "trace/stat";
	String TOPIC_BLAME         = "trace/blame";
	String TOPIC_DEPTH_UPDATE  = "trace/depth/update";
	String TOPIC_FILTER_RANKS  = "trace/filter";
	String TOPIC_COLOR_MAPPING = "trace/colorMap";

	String URI_CONTRIBUTOR     = "platform:/plugin/edu.rice.cs.hpctraceviewer.ui";
	String ID_DATA_OPERATION   = "trace/op/undo";

	String MAX_DEPTH_LABEL = "IconMaxDepth";
	String MAX_DEPTH_FILE  =  "platform:/plugin/edu.rice.cs.hpctraceviewer.ui/resources/max-depth16.png";

	int COLUMN_COLOR_WIDTH_PIXELS = 14;
}
