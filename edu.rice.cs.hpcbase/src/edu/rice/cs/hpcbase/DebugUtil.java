// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcbase;

public class DebugUtil {
	private static final boolean debugOn = false;
	
	public static void debug(String message) {
		if (debugOn)
			System.out.println(message);
	}
	
	
	public static void debug(String prefix, String message) {
		if (debugOn)
			System.out.printf("[%s] %s%n", prefix, message);
	}
	
	
	public 	static void debugThread(String prefix, String message) {
		if (debugOn)
			System.out.printf("%2s [%s] %s%n", Thread.currentThread().getId(), prefix, message);
	}
}
