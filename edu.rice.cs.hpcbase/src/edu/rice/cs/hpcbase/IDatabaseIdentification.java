package edu.rice.cs.hpcbase;

public interface IDatabaseIdentification 
{
	/***
	 * Return a unique identification for this database
	 * 
	 * @return {@code String}
	 */
	String id();
	
	default boolean equals(IDatabaseIdentification other) {
		return id().equals(other.id());
	}
}
