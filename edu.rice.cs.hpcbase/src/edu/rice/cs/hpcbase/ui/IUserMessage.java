// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcbase.ui;

public interface IUserMessage 
{
	void showErrorMessage(String str);
	void showInfo(String message);
	void showWarning(String message);
}
