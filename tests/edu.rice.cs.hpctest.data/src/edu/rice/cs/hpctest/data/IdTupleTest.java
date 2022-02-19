package edu.rice.cs.hpctest.data;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;

public class IdTupleTest {
	
	private static IdTuple idt1;
	
	@BeforeClass
	public static void setup() {
		idt1 = new IdTuple(1, 2);
		
		idt1.setKindAndInterpret(IdTupleType.KIND_RANK, 0);
		idt1.setKindAndInterpret(IdTupleType.KIND_THREAD, 1);
		
		idt1.setPhysicalIndex(0, 0);
		idt1.setPhysicalIndex(1, 1);
		
		idt1.setLogicalIndex(0, 0);
		idt1.setLogicalIndex(1, 1);
	}

	@Test
	public void testIdTupleIntInt() {
		IdTuple idt0 = new IdTuple();
		
		assertTrue(idt0.compareTo(idt1) < 0);		
		assertTrue(idt1.compareTo(idt0) > 0);
		
		IdTuple idt2 = new IdTuple(2, 2);		
		idt2.setKindAndInterpret(IdTupleType.KIND_RANK, 0);
		idt2.setKindAndInterpret(IdTupleType.KIND_THREAD, 1);
		idt2.setPhysicalIndex(0, 0);
		idt2.setPhysicalIndex(0, 2);

		assertTrue(idt1.compareTo(idt2) < 0);
		assertTrue(idt2.compareTo(idt1) > 0);
		
		IdTuple idt3 = new IdTuple(1, 2);		
		idt3.setKindAndInterpret(IdTupleType.KIND_RANK, 0);
		idt3.setKindAndInterpret(IdTupleType.KIND_THREAD, 1);
		idt3.setPhysicalIndex(0, 0);
		idt3.setPhysicalIndex(1, 1);
		idt3.setLogicalIndex(0, 0);
		idt3.setLogicalIndex(1, 1);

		assertTrue(idt1.compareTo(idt3) == 0);
	}


	@Test
	public void testGetKind() {
		short kind = idt1.getKind(0);
		assertTrue(kind > 0);
		assertTrue(idt1.getKind(1) > 0);
	}


	@Test
	public void testGetIndex() {
		assertTrue( idt1.getIndex(IdTupleType.KIND_RANK) == 0 );
		assertTrue( idt1.getIndex(IdTupleType.KIND_THREAD) == 1 );
	}

	@Test
	public void testIsGPUIntIdTupleType() {
		IdTupleType types = IdTupleType.createTypeWithOldFormat();
		assertFalse( idt1.isGPU(0, types));
		assertFalse( idt1.isGPU(1, types));
	}

	@Test
	public void testIsGPUIdTupleType() {
		assertFalse( idt1.isGPU(IdTupleType.createTypeWithOldFormat()));
	}

	@Test
	public void testToStringIdTupleType() {
		assertNotNull(idt1.toString(IdTupleType.createTypeWithOldFormat()));
	}

	@Test
	public void testToStringIntIdTupleType() {
		IdTupleType types = IdTupleType.createTypeWithOldFormat();
		assertNotNull( idt1.toString(0, types));
		assertNotNull( idt1.toString(1, types));
	}

	@Test
	public void testToNumber() {
		double num = idt1.toNumber();
		assertTrue(num == 0.1d);
	}

	@Test
	public void testToLabel() {
		assertNotNull( idt1.toLabel());
	}
}
