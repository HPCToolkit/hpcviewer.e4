package edu.rice.cs.hpcdata.db.version4;

public class DataSection 
{
	public long size;
	public long offset;
	
	@Override
	public String toString() {
		return "@" + offset + ": " + size + " bytes";
	}
}
