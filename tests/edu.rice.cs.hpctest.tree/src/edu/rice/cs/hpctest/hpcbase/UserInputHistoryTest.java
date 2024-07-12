// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctest.hpcbase;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcbase.map.UserInputHistory;

public class UserInputHistoryTest 
{
	static final String NAME = "Test-hpcviewer";
	static final String TOP_LINE = "TopLine";

	static UserInputHistory history;
	
	@BeforeClass
	public static void setUpBeforeClass() {
		history = new UserInputHistory(NAME);
		history.addLine(TOP_LINE);
	}

	@AfterClass
	public static void tearDownAfterClass() {
		history.clear();
	}

	@Test
	public void testGetName() {
		var name = history.getName();
		assertEquals(NAME, name);
	}

	@Test
	public void testGetDepth() {
		var depth = history.getDepth();
		assertTrue(depth > 0);
	}

	@Test
	public void testGetHistory() {
		var h = history.getHistory();
		assertNotNull(h);
		assertFalse(h.isEmpty());
	}

	@Test
	public void testAddLine() {
		var histo1 = history.getHistory();
		var depth1 = histo1.size();

		final String NEWLINE = "AnotherLine";
		history.addLine(NEWLINE);
		
		var histo2 = history.getHistory();
		var depth2 = histo2.size();
		
		assertTrue(depth2 == depth1 + 1);
		assertTrue(histo2.get(0).equals(NEWLINE));
	}
}
