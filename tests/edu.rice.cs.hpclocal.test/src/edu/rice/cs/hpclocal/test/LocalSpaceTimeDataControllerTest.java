// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpclocal.test;

import static org.junit.Assert.*;

import org.junit.Test;


public class LocalSpaceTimeDataControllerTest extends BaseLocalTest
{
	@Test
	public void testGetName() {
		for(var controller: list) {
			var name = controller.getName();
			assertNotNull(name);
			assertFalse(name.isEmpty());
		}
	}

	@Test
	public void testSpaceTimeDataControllerLocal() {
		for(var controller: list) {
			var baseData = controller.getBaseData();
			assertNotNull(baseData);
			
			var color = controller.getColorTable();
			assertNotNull(color);
			
			var trace = controller.getCurrentSelectedTraceline();
			assertNull(trace);
			
			var hsize = controller.getHeaderSize();
			assertTrue(hsize >= 0);
			
			assertFalse(controller.hasTraces());
		}
	}
}
