package edu.rice.cs.hpcremote;

public interface IConnection 
{
	String getHost();
	
	String getUsername();
	
	String getPrivateKey();
	
	String getInstallationDirectory();
	
	String getId();
}
