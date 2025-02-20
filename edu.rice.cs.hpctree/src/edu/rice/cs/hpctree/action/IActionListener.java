// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctree.action;

import edu.rice.cs.hpcdata.experiment.scope.Scope;

public interface IActionListener 
{
	void select(Scope scope);
}
