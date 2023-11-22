package edu.rice.cs.hpclocal;

import edu.rice.cs.hpcbase.IDatabaseIdentification;

public class LocalDatabaseIdentification implements IDatabaseIdentification 
{
	private final String path;
	
	public LocalDatabaseIdentification(String path) {
		this.path = path;
	}
	
	@Override
	public String id() {
		return path;
	}
}
