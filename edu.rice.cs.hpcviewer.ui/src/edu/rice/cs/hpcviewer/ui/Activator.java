package edu.rice.cs.hpcviewer.ui;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import edu.rice.cs.hpc.data.util.JavaValidator;
import edu.rice.cs.hpcviewer.ui.resources.IconManager;
import edu.rice.cs.hpcviewer.ui.util.ApplicationProperty;


/**
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator 
{

	private static final String OPTION_VERSION_SHORT = "-v";
	private static final String OPTION_VERSION_LONG  = "--version";

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		if (!JavaValidator.isCorrectJavaVersion())
			System.exit(0);
		
		Activator.context = bundleContext;
		String args[] = Platform.getApplicationArgs();
		for (String arg: args) {
			if (arg.equals(OPTION_VERSION_SHORT) || arg.equals(OPTION_VERSION_LONG)) {
				String release = ApplicationProperty.getVersion();
				System.out.println("Release: " + release);
				System.exit(0);
			}
		}
		IconManager.getInstance().init();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}
