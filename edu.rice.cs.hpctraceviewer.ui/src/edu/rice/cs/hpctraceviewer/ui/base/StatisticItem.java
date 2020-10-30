package edu.rice.cs.hpctraceviewer.ui.base;

import org.eclipse.swt.graphics.Color;

public class StatisticItem 
{
	public String procedureName;
	public float  percent;
	public Color  color;
	
	public StatisticItem(String procName, Color color, float percent) {
		this.procedureName = procName;
		this.percent 	   = percent;
		this.color = color;
	}

	public String toString() {
		return color + ". " + procedureName + ": " + percent + "%";
	}
}
