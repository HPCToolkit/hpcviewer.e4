// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcviewer.ui.actions;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

import edu.rice.cs.hpcbase.ViewerDataEvent;
import edu.rice.cs.hpcdata.experiment.metric.DerivedMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcmetric.dialog.ExtDerivedMetricDlg;

/****************************************************
 * 
 * Class to handle adding a user derived metrics
 *
 ****************************************************/
public class UserDerivedMetric 
{
	private final RootScope 	 root;
	private final IEventBroker   eventBroker;
	private final IMetricManager metricMgr;

	/****
	 * Constructor to initialize a user derived metric action
	 * @param root RootScope
	 * @param metricMgr a IMetricManager object. Thread view has a special metric manager than others.
	 * @param eventBroker IEventBroker
	 */
	public UserDerivedMetric(RootScope root, IMetricManager metricMgr, IEventBroker eventBroker) {
		
		this.root 		 = root;
		this.eventBroker = eventBroker;
		this.metricMgr   = metricMgr;
	}
	
	
	/***
	 * Add a new metric.
	 * IF a user click the cancel button, there is no metric is added.
	 */
	public void addNewMeric() {
		final Display display = Display.getDefault();
		
		ExtDerivedMetricDlg dialog = new ExtDerivedMetricDlg(display.getActiveShell(), metricMgr, root);
	
		if (dialog.open() == Window.OK) {
			
			final DerivedMetric metric = dialog.getMetric();
			metricMgr.addDerivedMetric(metric);
			
			ViewerDataEvent data = new ViewerDataEvent(metricMgr, metric);
			eventBroker.post(ViewerDataEvent.TOPIC_HPC_ADD_NEW_METRIC, data);
		}
	}
}
