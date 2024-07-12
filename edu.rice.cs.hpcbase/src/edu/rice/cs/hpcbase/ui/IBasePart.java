// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcbase.ui;

import edu.rice.cs.hpcbase.IDatabase;
import edu.rice.cs.hpcdata.experiment.BaseExperiment;

/****
 * 
 * Top interface for hpcviewer parts (views and editors)
 * 
 * Each part must implement two methods:
 * <ol>
 * <li> setInput(MPart, Object) : to set the input.</li>
 * <li> getExperiment(): to identify with which experiment database the part is belong to.</li> 
 * </ol>
 */
public interface IBasePart 
{
	/****
	 * Set the database (local or remote) in this part
	 * 
	 * @param input
	 * 			{@code IDatabase} input object for this main part
	 */
	void setInput(IDatabase input);

	
	/***
	 * Get the database input of this part
	 * @return
	 */
	IDatabase getInput();
	
	/****
	 * Get the Experiment object of this part.
	 * This method is deprecated, please use {@link getInput} method instead. 
	 * @return
	 */
	@Deprecated
	BaseExperiment getExperiment();
}
