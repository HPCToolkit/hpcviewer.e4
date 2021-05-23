package edu.rice.cs.hpctraceviewer.ui.base;

import edu.rice.cs.hpctraceviewer.data.color.ProcedureColor;

public class StatisticItem 
{
	public ProcedureColor procedure;
	public float  percent;
	
	public StatisticItem(ProcedureColor procedure, float percent) {
		this.procedure = procedure;
		this.percent   = percent;
	}

	public String toString() {
		return procedure + ": " + percent + "%";
	}
}
