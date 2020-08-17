 
package edu.rice.cs.hpcviewer.ui.handlers;

import javax.inject.Inject;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;

public class NewWindow 
{
	static public final String ID_WINDOW = "edu.rice.cs.hpcviewer.window.main";
	
	@Inject DatabaseCollection database;
	
	@Execute
	public void createWindow(EModelService modelService, MApplication app) {
		  MTrimmedWindow newWin = (MTrimmedWindow)modelService.cloneSnippet(app, "edu.rice.cs.hpcviewer.ui.trimmedwindow.main", null);

		  app.getChildren().add(newWin);
	}
}