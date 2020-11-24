 
package edu.rice.cs.hpcviewer.ui.handlers;

import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcbase.map.UserInputHistory;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;

public abstract class RecentDatabase 
{
	public  static final String HISTORY_DATABASE_RECENT = "recent";
	public  static final int    HISTORY_MAX = 10;
	private static final String ID_DATA_ECP = "viewer/recent";
		
	
	@AboutToShow
	public void aboutToShow( List<MMenuElement> items, 
			DatabaseCollection database, 
			EModelService modelService, 
			MWindow window ) {
		
		UserInputHistory history = new UserInputHistory(HISTORY_DATABASE_RECENT, HISTORY_MAX);
		if (history.getHistory().size() == 0)
			return;
		
		for(String db: history.getHistory()) {
			
			MDirectMenuItem menu = modelService.createModelElement(MDirectMenuItem.class);
			
			menu.setElementId(db);
			menu.setLabel(db);
			menu.setContributionURI(getURI());
			menu.getTransientData().put(ID_DATA_ECP, db);
			
			items.add(menu);
		}
	}
	
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell,
						MApplication application, 
						MDirectMenuItem menu, 
						EModelService modelService, 
						EPartService partService) {
	
		String db = (String) menu.getTransientData().get(ID_DATA_ECP);
		execute(application, modelService, partService, shell, db);
	}
	
	protected abstract String getURI();
	
	protected abstract void execute(MApplication application, 
									EModelService modelService, 
									EPartService partService, 
									Shell shell, 
									String database);
}