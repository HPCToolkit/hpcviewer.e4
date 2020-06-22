package edu.rice.cs.hpcviewer.ui.handlers;

import java.io.IOException;
import java.net.URL;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.e4.ui.workbench.lifecycle.PreSave;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessAdditions;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessRemovals;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcviewer.ui.experiment.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.resources.IconManager;

public class LifeCycle 
{
	@Inject EPartService partService;
	@Inject IEventBroker broker;
	@Inject EModelService modelService;

	@Inject DatabaseCollection databaseCollection;

	private Image listImages[];

	@PostContextCreate
	public void startup(IEclipseContext context, @Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		
		// setup a list of images 
		// Note: this is for Windows. On mac, we don't need this.
		
		Display display = Display.getDefault();
		
		listImages = new Image[IconManager.Image_Viewer.length];
		int i = 0;
		
		for (String imageName : IconManager.Image_Viewer) {
			try {
				URL url = FileLocator.toFileURL(new URL(imageName));
				listImages[i] = new Image(display, url.getFile());
				i++;
			} catch (IOException e) {
			}
		}
		Window.setDefaultImages(listImages);
	}
	
	@PreDestroy
	void preDestro() {
		if (listImages != null) {
			for(Image image: listImages) {
				if (!image.isDisposed())
					image.dispose();
			}
		}
	}

	@PreSave
	void preSave(IEclipseContext workbenchContext) {
	}

	@ProcessAdditions
	void processAdditions(IEclipseContext workbenchContext) {
	}

	@ProcessRemovals
	void processRemovals(IEclipseContext workbenchContext) {}

}
