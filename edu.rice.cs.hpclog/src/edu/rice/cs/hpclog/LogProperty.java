// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpclog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;

public class LogProperty 
{
	/****
	 * Get the list of log files
	 * @return {@code List<String>}
	 */
	public static List<String> getLogFile() {
		List<String> files = new ArrayList<String>();
		ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
		
		// check if there is no log file 
		if (!(loggerFactory instanceof LoggerContext))
			return files;
		
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		
		for ( Logger logger: context.getLoggerList() ) {
			for (Iterator<Appender<ILoggingEvent>> index = logger.iteratorForAppenders(); index.hasNext(); ) {
				
				Appender<ILoggingEvent> appender = index.next();
				if (appender instanceof FileAppender) {
					String file = ((FileAppender<?>) appender).getFile();
					files.add(file);
				}
			}
		}
		return files;
	}

	
	/****
	 * Enable or disable debug level
	 * @param debug boolean true if we need to set to the debug level
	 */
	public static void setDebug(boolean debug) {
		var logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		if (logger instanceof Logger root) {
			if (debug)
				root.setLevel(Level.DEBUG);
			else
				root.setLevel(Level.ERROR);
		}
	}
}
