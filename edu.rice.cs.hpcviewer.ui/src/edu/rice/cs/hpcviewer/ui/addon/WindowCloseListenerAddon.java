// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcviewer.ui.addon;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.IWindowCloseHandler;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.event.Event;

import edu.rice.cs.hpcremote.ICollectionOfConnections;

/***
 * 
 * This class is a modified version of WindowCloseListenerAddon from fixpro78 application
 * 
 * See the original code at:
 * 
 * https://github.com/fipro78/e4translationexample/blob/master/org.fipro.e4.translation/src/org/fipro/e4/translation/WindowCloseListenerAddon.java
 *
 */

// @PostConstruct will not work as workbench gets instantiated after the processing of the add-ons
// hence this approach uses method injection

@SuppressWarnings("restriction")
public class WindowCloseListenerAddon 
{
	public static final String ID_WINDOW_EXTRA = "edu.rice.cs.hpcviewer.ui.trimmedwindow.main";

	@Inject
	MApplication application;
	
	@Inject
	EPartService partService;
	
	@Inject
	EModelService modelService;
	
	@Inject
	DatabaseCollection database;
	
	// as the IWorkbench is not available at creation time, we need to annotate it with
	// @Optional so it gets reinjected once it is created
	
	@Inject
	@Optional
	IWorkbench workbench;
	
	// while initializing a class, all annotated methods are sequentially called
	// therefore we need to annotate the following methods with @Optional as the
	// events are not present at the creation time of this add-on instance
	
	@Inject
	@Optional
	private void subscribeApplicationCompleted(@UIEventTopic(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE) Event event) {
		if (workbench != null ){
			registerCloseHandler(application);
		}
	}
	
	@Inject
	@Optional
	private void subscribeTopicChildrenChanged(@UIEventTopic(UIEvents.ElementContainer.TOPIC_CHILDREN) Event event) {
		Object changedObj = event.getProperty(UIEvents.EventTags.ELEMENT);
		
		// only interested in changes to application
		if (changedObj instanceof MApplication app && workbench != null) {
			registerCloseHandler(app);			
		}
	}

	private void registerCloseHandler(MApplication application) {
		
		// each window gets its own close handler as we want to add a modal confirm dialog
		for (MWindow window : application.getChildren()) {
			
			IWindowCloseHandler closeHandler = win -> {
				database.removeAllDatabases(win, modelService, partService);
				ICollectionOfConnections.disconnectAll((Shell) win.getWidget());

				return true;
			};
			
			// Mostly MWindow contexts are lazily created by renderers
			// therefore it does not need to be set already at this point
			if (window.getContext() != null) {
				window.getContext().set(IWindowCloseHandler.class, closeHandler);
			}
			else {
				((EObject) window).eAdapters().add(new AdapterImpl() {
					@Override
					public void notifyChanged(Notification notification) {
						if (notification.getFeatureID(MWindow.class) != BasicPackageImpl.WINDOW__CONTEXT) {
							return;
						}
						IEclipseContext windowContext = (IEclipseContext) notification.getNewValue();
						if (windowContext != null) {
							windowContext.set(IWindowCloseHandler.class, closeHandler);
						}
					}
				});
			}
		}
	}
}