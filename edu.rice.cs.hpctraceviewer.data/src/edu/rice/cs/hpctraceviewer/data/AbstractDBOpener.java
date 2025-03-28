// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.data;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

import org.hpctoolkit.db.local.experiment.InvalExperimentException;


/**
 * An interface for the DBOpeners. Specifically, it is implemented by
 * {@link RemoteTraceOpener} and {@link LocalDBOpener}. Its main purpose is to
 * create a {@link SpaceTimeDataController} from the connection to the database
 * (be it local or remote), but it also partially handles closing that connection.
 * 
 * @author Philip Taffet
 * 
 */
public abstract class AbstractDBOpener 
{

	/**
	 * This prepares the database for retrieving data and creates a
	 * SpaceTimeDataController from that data. The local implementation
	 * (LocalDBOpener) should return a SpaceTimeDataControllerLocal while the
	 * remote implementation (RemoteDBOpener) should return a
	 * SpaceTimeDataControllerRemote.
	 * 
	 * @param statusMgr progress monitor
	 * @return
	 * @throws IOException 
	 * @throws InvalExperimentException 
	 */
	public abstract SpaceTimeDataController openDBAndCreateSTDC(IProgressMonitor statusMgr)
			throws IOException, InvalExperimentException;

	
	// Our current policy on closing: Except for back-to-back connections to the
	// same server, we should close the server when we are making a new
	// connection, local or remote.

	
	/*****
	 * closing the database.
	 * The caller is responsible to call this method to terminate the connection (in case of remote database)
	 * or closing local file (local database)
	 */
	public abstract void end();
}
