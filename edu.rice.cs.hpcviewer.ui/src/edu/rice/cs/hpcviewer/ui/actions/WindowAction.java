package edu.rice.cs.hpcviewer.ui.actions;

import java.util.List;

import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpcbase.ui.IBasePart;

public class WindowAction 
{

	public static void SplitWindow(EModelService modelService, EPartService partService, MPart part) {
		
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
}
