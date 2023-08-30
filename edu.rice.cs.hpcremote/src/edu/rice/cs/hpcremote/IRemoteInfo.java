package edu.rice.cs.hpcremote;

import org.hpctoolkit.hpcclient.v1_0.HpcClient;

public interface IRemoteInfo 
{
	String getId();
	
	HpcClient getClient();
}