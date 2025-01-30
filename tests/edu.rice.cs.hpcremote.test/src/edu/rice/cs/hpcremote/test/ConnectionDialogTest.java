// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcremote.test;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.rice.cs.hpcremote.InvalidRemoteIdentficationException;
import edu.rice.cs.hpcremote.RemoteDatabaseIdentification;
import edu.rice.cs.hpcremote.ui.ConnectionDialog;
import edu.rice.cs.hpctest.util.ViewerTestCase;

public class ConnectionDialogTest extends ViewerTestCase
{
	@Test
	/**
	 * Testing for empty case
	 */
	public void testInit() {		
		var display = shell.getDisplay();
		
		display.syncExec(() -> {
			ConnectionDialog dialog = new ConnectionDialog(shell);
			dialog.create();
			
			// Simulate user input
			dialog.getShell().open();
			
			// the default id is never empty
			var id = dialog.getId();
			assertNotNull(id);
			assertFalse(id.isEmpty());
			
			var username = dialog.getUsername();
			assertNotNull(username);
			assertFalse(username.isEmpty());
			
			var host = dialog.getHost();
			assertNull(host);
			
			var dir = dialog.getInstallationDirectory();
			assertNull(dir);
			
			var agent = dialog.getProxyAgent();
			assertNull(agent);
			
			var config = dialog.getConfig();
			assertNull(config);
			
			var close = dialog.close();
			assertTrue(close);
        });
	}

	
	@Test
	/***
	 * Testing for non-empty input
	 * 
	 * @throws InvalidRemoteIdentficationException
	 */
	public void testWithRemoteIdentification() throws InvalidRemoteIdentficationException {
		
		final var installPath = "install/path";
		final var user = "user";
		final var host = "host";
		final var dbPath = "database/path";
		
		// this is according to RemoteDatabaseIdentification format
		// it can change in the future
		final var anotherId = installPath + "*" + user + "@" + host + ":" + dbPath;
		
		RemoteDatabaseIdentification []remoteId = {
				new RemoteDatabaseIdentification(host, dbPath, user),
				new RemoteDatabaseIdentification(anotherId)
		};
		
		var display = shell.getDisplay();

		for(var rid: remoteId) {

			display.syncExec(() -> {
				// force to have install path for all cases
				rid.setRemoteInstallation(installPath);

				ConnectionDialog dialog = new ConnectionDialog(shell, rid);
				dialog.create();
				
				// Simulate user input
				dialog.getShell().open();
				
				// the default id is never empty
				var id = dialog.getId();
				assertNotNull(id);
				assertFalse(id.isEmpty());
				
				var username = dialog.getUsername();
				assertTrue(user.equals(username));
				
				assertTrue(host.equals(dialog.getHost()));
				
				var dir = dialog.getInstallationDirectory();
				assertTrue(installPath.equals(dir));
	        });
		}
	}
}
