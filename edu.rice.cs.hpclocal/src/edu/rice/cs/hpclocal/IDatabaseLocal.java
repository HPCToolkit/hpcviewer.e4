// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpclocal;

import edu.rice.cs.hpcbase.IDatabase;

public interface IDatabaseLocal extends IDatabase 
{
	String getDirectory();
}
