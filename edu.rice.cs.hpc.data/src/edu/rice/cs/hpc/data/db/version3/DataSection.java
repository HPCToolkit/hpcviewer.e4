package edu.rice.cs.hpc.data.db.version3;

public class DataSection 
{
	public long size;
	public long offset;
	
	@Override
	public String toString() {
		return "@" + offset + ": " + size + " bytes";
	}
}
