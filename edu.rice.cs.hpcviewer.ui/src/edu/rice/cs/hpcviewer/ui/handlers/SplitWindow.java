 package edu.rice.cs.hpcviewer.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpcbase.ui.IBasePart;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;

import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;

public class SplitWindow 
{
	@Execute
	public void execute( DatabaseCollection database, 
						 EModelService 		modelService,
						 EPartService       partService,
						 @Named(IServiceConstants.ACTIVE_PART) MPart part) {
		
		MElementContainer<MUIElement> container = part.getParent();
		MElementContainer<MUIElement> parentContainer = container.getParent();
		
		MPartStack partStack = modelService.createModelElement(MPartStack.class);
		parentContainer.getChildren().add(partStack);
		
		// move first the current active part
		modelService.move(part, partStack);
		
		// move parts with the same database
		IBasePart view = (IBasePart) part.getObject();
		if (view == null)
			return;
		
		BaseExperiment currentExperiment = view.getExperiment();
		
		List<MPart> list = modelService.findElements(container, null, MPart.class);
		for(MPart otherPart: list) {
			Object obj = otherPart.getObject();
			if (obj != null && (obj instanceof IBasePart)) {
				BaseExperiment experiment = ((IBasePart)obj).getExperiment();
				if (experiment == currentExperiment) {
					modelService.move(otherPart, partStack);
				}
			}
		}
		partService.activate(part);
	}
	
	
	@CanExecute
	public boolean canExecute(DatabaseCollection database, @Named(IServiceConstants.ACTIVE_PART) MPart part) {
		
		if (database.getNumDatabase()<2)
			return false;
		
		MElementContainer<MUIElement> container = part.getParent();
		MElementContainer<MUIElement> parentContainer = container.getParent();
		
		// Enabled if the container only has 1 part stack
		// we don't want to have too many part stacks. It will be a mess
		// Users should do it by themselves if they want  lot of part stacks
		
		return parentContainer.getChildren().size()==1;
	}
		
}