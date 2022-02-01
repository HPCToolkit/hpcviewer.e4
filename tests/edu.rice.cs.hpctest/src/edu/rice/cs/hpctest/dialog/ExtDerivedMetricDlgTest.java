package edu.rice.cs.hpctest.dialog;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcmetric.dialog.ExtDerivedMetricDlg;
import edu.rice.cs.hpctest.data.DataFactory;

public class ExtDerivedMetricDlgTest {

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());

		var exp = DataFactory.createExperiments(1);
		
		ExtDerivedMetricDlg dlg = new ExtDerivedMetricDlg(shell, exp.get(0), exp.get(0).getRootScope(RootScopeType.CallingContextTree));
		int r = dlg.open();
		var m = dlg.getMetric();
		if (m != null)
			System.out.println("return: " + r + " metric: " + m.getDisplayName() + " " + m.getFormula());
	}

}
