package edu.rice.cs.hpcviewer.ui.util;

public class Constants 
{
	static public final String ID_STACK_UPPER   = "edu.rice.cs.hpcviewer.ui.partstack.upper"; 
	
	static public final String ID_GRAPH_SORT    = "edu.rice.cs.hpcviewer.ui.partdescriptor.graph.sort";
	static public final String ID_GRAPH_PLOT    = "edu.rice.cs.hpcviewer.ui.partdescriptor.graph.plot";
	static public final String ID_GRAPH_HISTO   = "edu.rice.cs.hpcviewer.ui.partdescriptor.graph.histo";

	static public final String ID_VIEW_THREAD   = "edu.rice.cs.hpcviewer.ui.partdescriptor.thread";
	static public final String ID_VIEW_DATA     = "edu.rice.cs.hpcviewer.ui.partdescriptor.datacentric";
	static public final String ID_VIEW_FLAT     = "edu.rice.cs.hpcviewer.ui.partdescriptor.flat";
	static public final String ID_VIEW_BOTTOMUP = "edu.rice.cs.hpcviewer.ui.partdescriptor.bottomup";
	static public final String ID_VIEW_TOPDOWN  = "edu.rice.cs.hpcviewer.ui.partdescriptor.topdown";

	static public final String ID_VIEW_EDITOR   = "edu.rice.cs.hpcviewer.ui.partdescriptor.editor";
	
	static public final String ID_PARTS[] = {
			ID_GRAPH_SORT, ID_GRAPH_PLOT, ID_GRAPH_HISTO,
			
			ID_VIEW_THREAD, ID_VIEW_DATA, 
			ID_VIEW_FLAT, ID_VIEW_BOTTOMUP,
			ID_VIEW_TOPDOWN,
			
			ID_VIEW_EDITOR
	};
}
