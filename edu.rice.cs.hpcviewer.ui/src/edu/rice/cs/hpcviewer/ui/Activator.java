// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcviewer.ui;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import org.hpctoolkit.db.local.util.JavaValidator;


/**
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator 
{

	private static final String OPTION_VERSION_SHORT = "-v";
	private static final String OPTION_VERSION_LONG  = "--version";

	public void start(BundleContext bundleContext) throws Exception {
		if (JavaValidator.getJavaVersionInt() < JavaValidator.JAVA_SUPPORTED_17) {
			System.exit(0);
		}
		
		String[] args = Platform.getApplicationArgs();
		for (String arg: args) {
			if (arg.equals(OPTION_VERSION_SHORT) || arg.equals(OPTION_VERSION_LONG)) {
				System.out.println("Release: " + BuildInfo.VERSION);
				System.exit(0);
			}
		}
	}

	public void stop(BundleContext bundleContext) throws Exception {
		// free resources and stop the remote connection here?
	}

}
