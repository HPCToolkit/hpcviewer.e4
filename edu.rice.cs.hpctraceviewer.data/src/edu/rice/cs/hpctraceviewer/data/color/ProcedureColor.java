// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.data.color;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.TreeSet;

import org.eclipse.swt.graphics.Color;

public class ProcedureColor 
{
	static final private String SEPARATOR_PROCNAME = ", ";
	
	public AbstractCollection<String> procName;
	public Color  color;

	
	public ProcedureColor() {
		procName = new TreeSet<String>();
	}
	
	public ProcedureColor(String name, Color color) {
		procName = new TreeSet<String>();
		procName.add(name);
		
		this.color = color;
	}
	
	@Override
	public String toString() {
		return  color + ": " + getProcedure();
	}
	
	
	/***
	 * get the name of the procedure
	 * @return String
	 */
	public String getProcedure() {
		String proc = "";
		Iterator<String> iterator = procName.iterator();
		while(iterator.hasNext()) {
			proc += iterator.next();
			if (iterator.hasNext()) 
				proc += SEPARATOR_PROCNAME;
		}
		return proc;
	}
}
