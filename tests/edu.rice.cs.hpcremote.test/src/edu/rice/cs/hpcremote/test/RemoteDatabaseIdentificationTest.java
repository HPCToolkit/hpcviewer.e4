// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcremote.test;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.rice.cs.hpcremote.InvalidRemoteIdentficationException;
import edu.rice.cs.hpcremote.RemoteDatabaseIdentification;

public class RemoteDatabaseIdentificationTest {

	@Test
	public void testRemoteDatabaseIdentification() throws InvalidRemoteIdentficationException {
		var rdi = new RemoteDatabaseIdentification();
		assertTrue(rdi.getHost().isEmpty());
		assertTrue(rdi.getUsername().isEmpty());
		assertTrue(rdi.getPath().isEmpty());
	}


	@Test
	public void testRemoteDatabaseIdentificationId() throws InvalidRemoteIdentficationException {
		var rdi = new RemoteDatabaseIdentification("install*user@hostname.com:/path");
		assertEquals("user", rdi.getUsername());
		assertEquals("hostname.com", rdi.getHost());
		assertEquals("/path", rdi.getPath());
		assertEquals("install", rdi.getRemoteInstallation());
		
		// without installation info
		assertThrows(InvalidRemoteIdentficationException.class, () -> new RemoteDatabaseIdentification("user@hostname.com:/path"));
		
		// empty installation
		rdi = new RemoteDatabaseIdentification("*user@hostname.com:/path");
		assertEquals("", rdi.getRemoteInstallation());
		
		// empty user
		rdi = new RemoteDatabaseIdentification("install*@hostname.com:/path");
		assertEquals("", rdi.getUsername());
	}


	@Test
	public void testRemoteDatabaseIdentificationManual() throws InvalidRemoteIdentficationException {
		var rdi = new RemoteDatabaseIdentification("hostname.com", "/path", "user");
		assertEquals("user", rdi.getUsername());
		assertEquals("hostname.com", rdi.getHost());
		assertEquals("/path", rdi.getPath());
		assertTrue(rdi.getRemoteInstallation().isEmpty());
	}

	@Test
	public void testId() throws InvalidRemoteIdentficationException {
		final String id = "install*user@hostname.com:/path";
		var rdi = new RemoteDatabaseIdentification(id);
		assertEquals(id, rdi.id());
	}

	
	@Test
	public void testToString() {
		final String id = "install*user@hostname.com:/path";
		assertFalse(id.toString().isEmpty());
	}
}
