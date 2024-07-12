// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctraceviewer.ui.context;

import org.eclipse.core.commands.operations.IUndoContext;

public class BaseTraceContext implements IUndoContext 
{
	public static final String CONTEXT_OPERATION_BUFFER   = "bufferOp";
	public static final String CONTEXT_OPERATION_POSITION = "positionOp";
	public static final String CONTEXT_OPERATION_REFRESH  = "refreshOp";
	public static final String CONTEXT_OPERATION_TRACE    = "traceOp";
	public static final String CONTEXT_OPERATION_UNDOABLE = "undoableOp";
	public static final String CONTEXT_OPERATION_RESIZE   = "winResizeOp";
	
	public static final String []CONTEXTS = new String[] {CONTEXT_OPERATION_BUFFER,
														  CONTEXT_OPERATION_POSITION,
														  CONTEXT_OPERATION_REFRESH,
														  CONTEXT_OPERATION_TRACE,
														  CONTEXT_OPERATION_UNDOABLE,
														  CONTEXT_OPERATION_RESIZE} ;
	private static final String BASE_LABEL = "trace";
	
	private final String label;
	
	public BaseTraceContext(String label) {
		this.label  = getBaseLabel() + "/" + label; 
	}

	
	protected String getBaseLabel() {
		return BASE_LABEL + "/" + label; 
	}

	@Override
	public boolean matches(IUndoContext context) {
		return context == this;
	}
	
	@Override
	public String getLabel() {
		return label;
	}
}
