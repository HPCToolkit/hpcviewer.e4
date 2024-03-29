 
package edu.rice.cs.hpcviewer.ui.expression;

import java.util.Iterator;

import javax.inject.Inject;
import org.eclipse.e4.core.di.annotations.Evaluate;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;

public class DatabaseMergeExpression 
{
	@Inject DatabaseCollection database;
	
	@Inject
	@Evaluate
	public boolean evaluate(MWindow window) {
		
		Iterator<IExperiment> iterator = database.getIterator(window);
		if (iterator == null)
			return false;
		
		int numDb = 0;
		
		while(iterator.hasNext()) {
			Experiment exp = (Experiment) iterator.next();
			if (!exp.isMergedDatabase()) {
				numDb++;
			}
		}
		
		return numDb >= 2;
	}
}
