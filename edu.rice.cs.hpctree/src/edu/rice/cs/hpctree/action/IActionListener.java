// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctree.action;

import edu.rice.cs.hpcdata.experiment.scope.Scope;

public interface IActionListener 
{
	void select(Scope scope);
}
