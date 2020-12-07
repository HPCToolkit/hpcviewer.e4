package edu.rice.cs.hpcviewer.ui.expression;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Evaluate;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;

import edu.rice.cs.hpcbase.map.UserInputHistory;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.handlers.RecentDatabase;

public class EvaluateHistoryExistance 
{
	@Inject DatabaseCollection database;

	@Evaluate
	public boolean evaluate(MWindow window) {
		UserInputHistory history = new UserInputHistory(RecentDatabase.HISTORY_DATABASE_RECENT, 
														RecentDatabase.HISTORY_MAX);

		return ( history.getHistory().size()==0 ) && database.getNumDatabase(window) <= 1;
	}
}
