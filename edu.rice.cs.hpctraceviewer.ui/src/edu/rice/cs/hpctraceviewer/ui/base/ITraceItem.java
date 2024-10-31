// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctraceviewer.ui.base;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpcbase.ui.IBaseItem;

public interface ITraceItem extends IBaseItem 
{
	void createContent(ITracePart parentPart, IEclipseContext context, IEventBroker broker, Composite parentComposite);

}
