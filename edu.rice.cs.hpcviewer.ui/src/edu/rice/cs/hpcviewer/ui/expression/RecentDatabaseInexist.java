package edu.rice.cs.hpcviewer.ui.expression;

import org.eclipse.e4.core.di.annotations.Evaluate;

import edu.rice.cs.hpcbase.map.UserInputHistory;
import edu.rice.cs.hpcviewer.ui.handlers.RecentDatabase;

public class RecentDatabaseInexist 
{
	@Evaluate
	public boolean evaluate() {
		UserInputHistory history = new UserInputHistory(RecentDatabase.HISTORY_DATABASE_RECENT, 
														RecentDatabase.HISTORY_MAX);

		return history.getHistory().size()==0;
	}
}
