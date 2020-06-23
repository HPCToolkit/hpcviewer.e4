 
package edu.rice.cs.hpcviewer.ui.handlers;

import java.util.List;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

import edu.rice.cs.hpcviewer.ui.util.Constants;

public class NewWindow 
{
	static public final String ID_WINDOW = "edu.rice.cs.hpcviewer.window.main";
	
	@Execute
	public void execute(EModelService ms, MApplication application) {
		
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
		
		System.out.println("new window : " + elementId +",  old window: " + otw.getElementId());
		
		ms.bringToTop(ntw);	
		
		for(String id: Constants.ID_PARTS) {
			List<MPart> listOfParts = ms.findElements(ntw, id, null);
			
			if (listOfParts == null)
				continue;
			
			for(MPart part: listOfParts) {
				part.setVisible(false);
			}
		}
		
		MMenu mainMenu = ntw.getMainMenu();
	}
}