package edu.rice.cs.hpcremote.data;

import java.io.IOException;

public interface IRemoteDirectoryBrowser 
{
	IRemoteDirectoryContent getContentRemoteDirectory( String directory) throws IOException;
}
