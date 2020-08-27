 
package edu.rice.cs.hpcviewer.ui.expression;

import java.util.Iterator;

import javax.inject.Inject;
import org.eclipse.e4.core.di.annotations.Evaluate;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;

public class DatabaseMergeExpression 
{
	@Inject DatabaseCollection database;
	
	@Inject
	@Evaluate
	public boolean evaluate(MWindow window) {
		
		Iterator<BaseExperiment> iterator = database.getIterator(window);
		
		int numDb = 0;
		
		while(iterator.hasNext()) {
			Experiment exp = (Experiment) iterator.next();
			if (!exp.isMergedDatabase()) {
				numDb++;
			}
		}
		
		return numDb == 2;
	}
}
