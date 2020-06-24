 
package edu.rice.cs.hpcviewer.ui.handlers;

import java.util.Iterator;
import java.util.List;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpcviewer.ui.experiment.DatabaseCollection;

public class Close 
{
	static final private String ID_MENU_URI = "bundleclass://edu.rice.cs.hpcviewer.ui/" + Close.class.getName();
	
	@AboutToShow
	public void aboutToShow(List<MMenuElement> items, DatabaseCollection database, EModelService modelService, MWindow window) {
		
		Iterator<BaseExperiment> iterator = database.getIterator(window);
		while(iterator.hasNext()) {
			BaseExperiment exp = iterator.next();
			
			MDirectMenuItem menu = modelService.createModelElement(MDirectMenuItem.class);
			
			menu.setLabel(exp.getDefaultDirectory().getAbsolutePath());
			menu.setContributionURI(ID_MENU_URI);
			menu.setContributorURI("platform:/edu.rice.cs.hpcviewer.ui");
			menu.setObject(exp);
			
			items.add(menu);
		}
	}
	
	@Execute
	public void execute( IEclipseContext context, IWorkbench workbench) {
		System.out.println("hhhhhhh");
	}
}