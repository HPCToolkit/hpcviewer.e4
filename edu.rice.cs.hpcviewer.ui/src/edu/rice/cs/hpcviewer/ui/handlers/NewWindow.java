 
package edu.rice.cs.hpcviewer.ui.handlers;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
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
		
		ntw.setX(x+100);
		ntw.setY(y+100);
		
		// show it to the user
		String elementId = ntw.getElementId() + "." + application.getChildren().size();
		ntw.setElementId(elementId);
		
		ms.bringToTop(ntw);
		application.setSelectedElement(ntw);
		
        List<String> tags = new ArrayList<>();
        tags.add("categoryTag:hpcview");
        List<MUIElement> elementsWithTags = ms.findElements(ntw, null, null, tags);
		
        for(MUIElement element: elementsWithTags) {

        	element.setVisible(false);
        	ps.hidePart((MPart) element, true);
        }
        //Shell newShell = (Shell) ntw.getWidget();
        
        //database.openDatabase(newShell, application, ps, ms, null);
    }
}