 
package edu.rice.cs.hpcviewer.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcdata.experiment.BaseExperiment;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcmetric.MetricFilterInput;
import edu.rice.cs.hpcviewer.ui.ProfilePart;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.internal.AbstractBaseViewItem;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.CanExecute;

public class ShowMetrics 
{
	static final private String ID_MENU_URI = "bundleclass://edu.rice.cs.hpcviewer.ui/" + ShowMetrics.class.getName();
	
	@Inject EPartService partService;
	@Inject DatabaseCollection database;
	
	
	
	@AboutToShow
	public void aboutToShow( List<MMenuElement> items, 
							 EModelService modelService, 
							 MWindow window ) {
		if (!canExecute(window))
			return;
		
		Iterator<BaseExperiment> iterator = database.getIterator(window);

		while(iterator.hasNext()) {
			Experiment exp = (Experiment) iterator.next();
			
			String path    = exp.getDefaultDirectory().getAbsolutePath();
			String label   = path;
			
			if (exp.isMergedDatabase()) {
				label = "[Merged] " + label;
			}
			MDirectMenuItem menu = modelService.createModelElement(MDirectMenuItem.class);
			
			menu.setElementId(path);
			menu.setLabel(label);
			menu.setContributionURI(ID_MENU_URI);
			
			items.add(menu);
		}		
	}
	
	
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_PART) MPart part, 
						@Active Shell shell, 
						MWindow window,
						IEventBroker eventBroker) {

		if (database == null || database.isEmpty(window))
			return;
			
		if (part == null)
			return;

		Object obj = part.getObject();
		if (obj == null || (!(obj instanceof ProfilePart)))
			return;

		ProfilePart profilePart = (ProfilePart) obj;
		Experiment experiment = (Experiment) profilePart.getExperiment();
		if (experiment == null) {
			Logger logger = LoggerFactory.getLogger(getClass());
			logger.debug("Database not found");
			return;
		}
		AbstractBaseViewItem item = profilePart.getActiveView();
		
		RootScope root = experiment.getRootScope(RootScopeType.CallingContextTree);
		TreeColumn []columns = item.getScopeTreeViewer().getTree().getColumns();
		MetricFilterInput input = new MetricFilterInput(root, columns, true);
		
		profilePart.addEditor(input);
	}
	
	
	@CanExecute
	public boolean canExecute(MWindow window) {		
		return database.getNumDatabase(window)>0;
	}
		
}