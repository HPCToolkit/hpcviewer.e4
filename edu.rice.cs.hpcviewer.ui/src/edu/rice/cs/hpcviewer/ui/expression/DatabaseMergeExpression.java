// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcviewer.ui.expression;

import javax.inject.Inject;
import org.eclipse.e4.core.di.annotations.Evaluate;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;

public class DatabaseMergeExpression 
{
	@Inject DatabaseCollection database;
	
	@Inject
	@Evaluate
	public boolean evaluate(MWindow window) {
		
		var iterator = database.getIterator(window);
		if (iterator == null)
			return false;
		
		int numDb = 0;
		
		while(iterator.hasNext()) {
			var exp = iterator.next().getExperimentObject();
			
			// hack - hack - hack
			// forcing to convert to Experiment object since the class has 
			//  isMergedDatabase method
			Experiment experiment = (Experiment) exp;
			
			if (!experiment.isMergedDatabase()) {
				numDb++;
			}
		}
		
		return numDb >= 2;
	}
}
