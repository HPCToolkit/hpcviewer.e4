// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcmetric.test;

import static org.junit.Assert.*;

import org.eclipse.swtbot.swt.finder.SWTBot;

import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.rice.cs.hpcdata.experiment.metric.DerivedMetric;
import edu.rice.cs.hpcdata.experiment.metric.MetricType;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric.AnnotationType;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcmetric.dialog.ExtDerivedMetricDlg;
import edu.rice.cs.hpctest.util.TestDatabase;
import edu.rice.cs.hpctest.util.ViewerTestCase;

@RunWith(SWTBotJunit4ClassRunner.class)
public class ExtDerivedMetricDlgTest extends ViewerTestCase
{
    private SWTBot bot;

    @Override
    @Before
    public void setUp() {
    	super.setUp();
        bot = new SWTBot();
    }


	@Test
	public void testGetMetric() throws Exception {
		var experiments = TestDatabase.getExperiments();

		for (var exp: experiments) {
			var root = exp.getRootScope(RootScopeType.CallingContextTree);
			
			var dialog = new ExtDerivedMetricDlg(shell, exp, root);
			dialog.create();
			dialog.getShell().open();
			
			var dialogShell = bot.shell("");
			assertNotNull(dialogShell);
			
			var metricNameText = bot.comboBox(0);
			assertNotNull(metricNameText);
			
			final var label = "new-metric";
			metricNameText.setText(label);
			
			var metricFormulaText = bot.text(0);
			assertNotNull(metricFormulaText);
			
			final var formula = "$0 + 10";
			metricFormulaText.setText(formula);
			
			bot.button("OK").click();
			
			var metric = dialog.getMetric();
			
			assertTrue(label.equals(metric.getDisplayName()));
			
			assertNotNull(metric.getFormula());
		}
	}
	

	@Test
	public void testSetAndGetMetric() throws Exception {
		var experiments = TestDatabase.getExperiments();
		
		shell.getDisplay().syncExec(() -> {
			for(var exp: experiments) {
				var root = exp.getRootScope(RootScopeType.CallingContextTree);
				
				var metrics = exp.getMetricList();
				String expression = "$0 + 10";
				int id = metrics.size() + 1;
				String newId = String.valueOf(id);
				
				var dialog = new ExtDerivedMetricDlg(shell, exp, root);
								
				var dm = new DerivedMetric(exp, expression, "new-metric", newId, id, AnnotationType.PERCENT, MetricType.INCLUSIVE);
				dialog.setMetric(dm);

				dialog.create();
								
				dialog.getShell().open();

				dialog.okPressed();
				
				var metric = dialog.getMetric();
				assertNotNull(metric);
				
				assertTrue(dm.getDisplayName().equals(metric.getDisplayName()));
				assertTrue(dm.getFormula().equals(metric.getFormula()));
			}
		});
	}

}
