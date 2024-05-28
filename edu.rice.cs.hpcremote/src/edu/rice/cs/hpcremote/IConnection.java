package edu.rice.cs.hpcremote;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public interface IConnection 
{
	String getHost();
	
	String getUsername();
	
	String getInstallationDirectory();

	String getProxyAgent();

	/****
	 * Get the unique ID to connect to a remote host.
	 * By default the ID should be `{@code user@hostname:installation}
	 * 
	 * @return {@code String}
	 * 			The unique ID for this remote host
	 */
	default String getId() {
		return getUsername() + "@" + getHost();
	}

	
	/***
	 * Get the private key for the current user.
	 * By default, the private key is  {@code ~/.ssh/id_rsa}
	 * which only valid on UNIX platforms
	 * 
	 * @return {@code String}
	 */
	default String getPrivateKey() {
		var home = System.getProperty("user.home");
		var key  = home + File.separator + ".ssh" + File.separator + "id_rsa";
		if (Files.isReadable(Path.of(key)))
			return key;
		
		return "";
	}

	
	/***
	 * Get the configuration file that contains the settings for proxy jump and aliases.
	 * By default it's {@code ~/.ssh/config}
	 *  
	 * @return {@code String}
	 */
	default String getConfig() {
		var configFile = System.getProperty("user.home") + File.separator + ".ssh" + File.separator + "config";
		if ( Files.isReadable(Path.of(configFile)) )
			return configFile;
		
		return null;
	}
}
