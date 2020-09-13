package edu.rice.cs.hpctraceviewer.ui.blamestat;

public class StatisticItem 
{
	public String procedureName;
	public float  percent;
	
	StatisticItem(String procName, float percent) {
		this.procedureName = procName;
		this.percent 	   = percent;
	}

}
