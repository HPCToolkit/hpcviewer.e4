// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctraceviewer.ui.base;

import edu.rice.cs.hpctraceviewer.data.color.ProcedureColor;

public class StatisticItem implements Comparable<StatisticItem> 
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

	@Override
	public int compareTo(StatisticItem o) {
		return percent > o.percent ? 1 : percent < o.percent ? -1 : 0;
	}
}
