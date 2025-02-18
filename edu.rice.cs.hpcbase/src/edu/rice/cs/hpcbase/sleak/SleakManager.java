// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcbase.sleak;

import java.util.Hashtable;

import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.widgets.Display;

/******************************************************
 * 
 * Managing sleak objects across different windows 
 * 
 * To use SleakManager, make sure the tracing configuration
 * is set correctly for the following attributes:
 * 
 *     org.eclipse.ui/debug=true 
 *     org.eclipse.ui/trace/graphics=true
 *     
 * and environment variable HPCTOOLKIT_MEMLEAK=1
 * 
 ******************************************************/
public class SleakManager {

	private static final String VAR_MEMLEAK = "HPCTOOLKIT_MEMLEAK";
	
	static final private Hashtable<Display, Sleak> lists = new Hashtable<Display, Sleak>(1);
	
	
	/************
	 * retrieve the sleak object of this display iff HPCTOOLKIT_MEMLEAK has value "1" or "T"
	 * 
	 * @param display
	 * 
	 * @return sleak object if the environement variable is true, null otherwise
	 ************/
	static public Sleak getSleak(final Display display) {
		
		Sleak sleak = null;

		String mem = System.getenv(VAR_MEMLEAK);
		
		// we activate sleak if the value of variable HPCTOOLKIT_MEMLEAK is not "f" or "0"
		
		if ( mem != null &&  !mem.isEmpty()) {
			boolean memleak = !(mem.equalsIgnoreCase("f") || mem.equals("0"));
			
			if (memleak) {
				// memleak is on. Check if it's already on for this display.
				// we don't want to have two sleaks work on the same display
				
				sleak = lists.get(display);

				if (sleak == null) {
					
					DeviceData data = display.getDeviceData();
					if (data.tracking) {
						sleak = new Sleak();
						lists.put(display, sleak);
					}
				}
			}
		}
		return sleak;
	}
	
	
	/************
	 * initialize sleak. If sleak is not activated by the environment variable HPCTOOLKIT_MEMLEAK,
	 * it won't be started.
	 * 
	 * @param display
	 ************/
	static public void init(final Display display) {
		Sleak sleak = getSleak( display );
		if (sleak != null) {
			sleak.open();
		}
	}
}
