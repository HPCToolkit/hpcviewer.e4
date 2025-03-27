// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcviewer.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hpctoolkit.db.local.experiment.Experiment;
import edu.rice.cs.hpcmetric.MetricFilterInput;
import edu.rice.cs.hpcviewer.ui.ProfilePart;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.internal.AbstractView;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.Active;

public class ShowMetrics
{
	@Inject EPartService partService;
	@Inject DatabaseCollection database;
		
	
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_PART) MPart part, 
						@Active Shell shell, 
						MWindow window,
						IEventBroker eventBroker) {

		if (database == null || database.isEmpty(window))
			return;
			
		if (part == null)
			return;

		Object obj = part.getObject();
		if (!(obj instanceof ProfilePart))
			return;

		ProfilePart profilePart = (ProfilePart) obj;
		Experiment experiment = (Experiment) profilePart.getExperiment();
		if (experiment == null) {
			Logger logger = LoggerFactory.getLogger(getClass());
			logger.debug("Database not found");
			return;
		}
		AbstractView item = profilePart.getActiveView();		
		MetricFilterInput input = new MetricFilterInput(item, eventBroker);
		
		profilePart.addEditor(input);
	}
}
