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
	
	
	@Override
	public String toString() {
		return id();
	}
	
	
	@Override
	public int hashCode() {
		return path.hashCode();
	}
	
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof IDatabaseIdentification) {
			IDatabaseIdentification other = (IDatabaseIdentification) o;
			return id().equals(other.id());
		}
		return false;
	}
}
