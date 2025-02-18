// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.handlers;

import java.util.List;

import javax.inject.Named;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;
import edu.rice.cs.hpctraceviewer.ui.context.BaseTraceContext;
import edu.rice.cs.hpctraceviewer.ui.util.IConstants;

public abstract class AbstractOperationMenu 
{
	static final private String ID_BASE_ELEMENT = "MenuOp";

	private final String label;
	
	public AbstractOperationMenu(String label) {

		this.label = label;
	}
	
	@AboutToShow
	public void aboutToShow(List<MMenuElement> items, EModelService modelService, MPart part) {
		
		ITracePart tracePart = (ITracePart) part.getObject();
		IUndoContext context = tracePart.getContext(BaseTraceContext.CONTEXT_OPERATION_TRACE);
		IUndoableOperation[] histories = getOperationHistory(tracePart, context);
		
		int i = 0;
		for(IUndoableOperation history: histories) {
			
			MDirectMenuItem menu = modelService.createModelElement(MDirectMenuItem.class);
			menu.setElementId(ID_BASE_ELEMENT + "." + label + i);
			menu.setLabel(history.getLabel());
			menu.setContributionURI(getContributionURI());
			menu.setContributorURI(IConstants.URI_CONTRIBUTOR);
			menu.setEnabled(true);
			menu.getTransientData().put(IConstants.ID_DATA_OPERATION, history);
			
			items.add(menu);
			i++;
		}
	}


	@Execute
	public void execute(MDirectMenuItem menu, MPart part, @Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {

		IUndoableOperation op = (IUndoableOperation) menu.getTransientData().get(IConstants.ID_DATA_OPERATION);
		ITracePart tracePart = (ITracePart) part.getObject();
		try {
			execute(tracePart, op);
			
		} catch (ExecutionException e) {
			
			final String title = "Unable to execute " + op.getLabel();
			MessageDialog.openError(shell, title, e.getClass() + ": " + e.getMessage());
			
			Logger logger = LoggerFactory.getLogger(getClass());
			logger.error(title, e);
		}
	}
	
	abstract String getContributionURI();
	abstract IUndoableOperation[] getOperationHistory(ITracePart tracePart, IUndoContext context);
	abstract void execute(ITracePart tracePart, IUndoableOperation operation) throws ExecutionException;

}
