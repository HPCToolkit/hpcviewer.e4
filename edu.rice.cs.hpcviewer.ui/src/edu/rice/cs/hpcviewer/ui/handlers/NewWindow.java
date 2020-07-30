 
package edu.rice.cs.hpcviewer.ui.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcviewer.ui.experiment.DatabaseCollection;

public class NewWindow 
{
	static public final String ID_WINDOW = "edu.rice.cs.hpcviewer.window.main";
	
	@Inject DatabaseCollection database;
	
	@Execute
	public void createWindow(EModelService modelService, MApplication app) {
		  MTrimmedWindow newWin = (MTrimmedWindow)modelService.cloneSnippet(app, "edu.rice.cs.hpcviewer.ui.trimmedwindow.main", null);

		  app.getChildren().add(newWin);
	}
	
	public void execute(EModelService ms, 
						MApplication  application, 
						EPartService  ps,
						@Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		
		// clone the elements from the current main window
		
		MWindow otw = (MWindow) ms.find(ID_WINDOW, application);
		MWindow ntw = (MWindow) ms.cloneElement(otw, application);
		ntw.setToBeRendered(true);
		ntw.setVisible(true);
		ntw.setOnTop(true);
		
		application.getChildren().add(ntw);
		
		// move the new window to a different position 
		int x = otw.getX();
		int y = otw.getY();
		
		Random r = new Random();
		int delta = r.nextInt(100);
		
		ntw.setX(x+100 + delta);
		ntw.setY(y+100 + delta);
		
		// show it to the user
		int numWindows = application.getChildren().size();
		String elementId = ntw.getElementId() + "." + numWindows;
		ntw.setElementId(elementId);
		ntw.setLabel("hpcviewer-" + numWindows);
		
		ms.bringToTop(ntw);
		application.setSelectedElement(ntw);
		
        List<String> tags = new ArrayList<>();
        tags.add("categoryTag:hpcview");
        List<MUIElement> elementsWithTags = ms.findElements(ntw, null, null, tags);
		
        for(MUIElement element: elementsWithTags) {

        	element.setVisible(false);
        	ps.hidePart((MPart) element, true);
        	
        	// remove the child from the parent's list of children manually
        	// Reason: sometimes Eclipse cannot remove it automatically after hiding it. Not sure why.
        	// It's harmless to hide twice.
        	
        	MElementContainer<MUIElement> parent = element.getParent();
        	parent.getChildren().remove(element);
        }
        //Shell newShell = (Shell) ntw.getWidget();
        
        //database.openDatabase(newShell, application, ps, ms, null);
    }
}