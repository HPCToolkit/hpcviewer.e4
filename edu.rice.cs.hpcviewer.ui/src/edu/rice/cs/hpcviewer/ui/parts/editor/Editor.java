 
package edu.rice.cs.hpcviewer.ui.parts.editor;

import javax.inject.Inject;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.swt.widgets.Composite;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import edu.rice.cs.hpcviewer.ui.experiment.ExperimentAddOn;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.SWT;



public class Editor 
{
	private TextViewer textViewer;
	private PartEventHandler eventHandler;
	
	@Inject IEventBroker broker;

	@Inject
	public Editor() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {

		textViewer = new TextViewer(parent, SWT.BORDER);
		
		StyledText styledText = textViewer.getTextWidget();		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(styledText);
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(parent);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(parent);
		
		eventHandler = new PartEventHandler(textViewer);
		
		broker.subscribe(ExperimentAddOn.EVENT_HPC_NEW_DATABASE, eventHandler);
	}
	
	@PreDestroy
	public void preDestroy() {
		broker.unsubscribe(eventHandler);
	}

	static private class PartEventHandler implements EventHandler
	{
		final private TextViewer textViewer;

		PartEventHandler(TextViewer textViewer) {
			this.textViewer = textViewer;
		}
		
		@Override
		public void handleEvent(Event event) {
			
			String []names = event.getPropertyNames();
			if (names == null)
				return;
			
			StringBuffer buff = new StringBuffer();
			for (String name : names) {
				buff.append(name);
				buff.append("\n");
			}
			
			StyledText styledText = textViewer.getTextWidget();
			styledText.setText(buff.toString());
		}
		
	}
}