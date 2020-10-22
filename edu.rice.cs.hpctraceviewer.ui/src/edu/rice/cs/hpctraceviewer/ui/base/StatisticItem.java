package edu.rice.cs.hpctraceviewer.ui.base;

public class StatisticItem 
{
	public String procedureName;
	public float  percent;
	
	public StatisticItem(String procName, float percent) {
		this.procedureName = procName;
		this.percent 	   = percent;
	}

}
