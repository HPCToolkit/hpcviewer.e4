package edu.rice.cs.hpclog;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;


/**
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator 
{
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		configureLogbackInBundle(bundleContext.getBundle());
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
	}

	private void configureLogbackInBundle(Bundle bundle) throws JoranException, IOException {
		
		final String workDir = getWorkspaceDirectory();
		System.setProperty("log.dir", workDir);
		
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		JoranConfigurator jc = new JoranConfigurator();
		jc.setContext(context);
		context.reset();

		// this assumes that the logback.xml file is in the root of the bundle.
		URL logbackConfigFileUrl = FileLocator.find(bundle, new Path("logback.xml"),null);
		if (logbackConfigFileUrl == null) {
			System.err.println("file logback.xml does not exist");
			return;
		}
		jc.doConfigure(logbackConfigFileUrl.openStream());
	}
    
    
    /****
     * get the default workspace directory. 
     * A workspace directory is the location where Eclipse will store caches (plugin, libraries),
     * preferences, logs, file locks, etc.
     * We may need to store all user setting there too.
     * 
     * @return {@code String}
     */
    public static String getWorkspaceDirectory() {
		
		final String arch = System.getProperty("os.arch");

		final String subDir = ".hpctoolkit" + File.separator + 
							  "hpcviewer"   + File.separator +
							  arch;
		
		return System.getProperty("user.home") + File.separator + subDir;
    }
}
