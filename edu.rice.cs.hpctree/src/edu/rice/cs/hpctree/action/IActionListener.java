// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctree.action;

import org.hpctoolkit.db.local.experiment.scope.Scope;

public interface IActionListener 
{
	void select(Scope scope);
}
