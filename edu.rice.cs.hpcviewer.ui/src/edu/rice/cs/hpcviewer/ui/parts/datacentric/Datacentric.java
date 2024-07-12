// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcviewer.ui.parts.datacentric;

import javax.inject.Inject;
import org.eclipse.swt.custom.CTabFolder;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcviewer.ui.parts.topdown.TopDownPart;


public class Datacentric extends TopDownPart
{

	@Inject
	public Datacentric(CTabFolder parent, int style) {
		super(parent, style);
		setText("Datacentric view");
		setToolTipText("A view to display callinng context tree from a data-centric view");
	}
	


	@Override
	public RootScopeType getRootType() {

		return RootScopeType.DatacentricTree;
	}
}