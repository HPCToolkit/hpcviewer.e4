// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.data.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.mockito.Mockito;

import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.TraceDisplayAttribute;

public class TraceDisplayAttributeTest 
{
	@Test
	public void testComputeDisplayTimeUnit() {
		TraceDisplayAttribute attr = new TraceDisplayAttribute();
		
		SpaceTimeDataController stdc = Mockito.mock(SpaceTimeDataController.class);
		when(stdc.getTimeUnit()).thenReturn(TimeUnit.MILLISECONDS);
		
		attr.setTime(10, 20);
		var unit = attr.computeDisplayTimeUnit(stdc.getTimeUnit());
		
		assertNotNull(unit);
		assertEquals(TimeUnit.MILLISECONDS, unit);
		
		attr.setTime(10, 10 * 1000); // make sure the time end is bigger than 1000
		unit = attr.computeDisplayTimeUnit(stdc.getTimeUnit());
		
		assertNotNull(unit);
		assertEquals(TimeUnit.SECONDS, unit);
	}

	@Test
	public void testDecrement() {
		TraceDisplayAttribute attr = new TraceDisplayAttribute();
		TimeUnit unit = TimeUnit.HOURS;
		var decUnit = attr.decrement(unit);
		assertEquals(decUnit, TimeUnit.MINUTES);
		
		unit = TimeUnit.NANOSECONDS;     // the smallest unit
		decUnit = attr.decrement(unit);
		assertEquals(unit, decUnit);     // no change
	}

	@Test
	public void testIncrement() {
		TraceDisplayAttribute attr = new TraceDisplayAttribute();
		TimeUnit unit = TimeUnit.HOURS;
		var incUnit = attr.increment(unit);
		assertEquals(incUnit, TimeUnit.DAYS);

		assertFalse(attr.canIncrement(incUnit)); // cannot increase time unit of DAYS
		
		var otherUnit = attr.increment(incUnit);
		assertEquals(otherUnit, incUnit);   // no change
	}

	@Test
	public void testSetProcess() {
		TraceDisplayAttribute attr = new TraceDisplayAttribute();
		attr.setProcess(1, 2);
		
		assertEquals(1, attr.getProcessBegin());
		assertEquals(2, attr.getProcessEnd());
	}

	@Test
	public void testSameTrace() {
		TraceDisplayAttribute attr = new TraceDisplayAttribute();
		attr.setProcess(1, 2);
		attr.setTime(100, 200);
		
		var copy = new TraceDisplayAttribute();
		copy.copy(attr);
		
		assertTrue(copy.sameTrace(attr));
		
		assertEquals(100, copy.getTimeBegin());
		assertEquals(200, copy.getTimeEnd());
		
		assertTrue(copy.sameDepth(attr));
	}

	@Test
	public void testSetDepth() {
		TraceDisplayAttribute attr = new TraceDisplayAttribute();
		attr.setDepth(4);
		assertEquals(4, attr.getDepth());
	}

}
