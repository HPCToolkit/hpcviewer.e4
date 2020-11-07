package edu.rice.cs.hpclog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;

public class LogProperty 
{
	/****
	 * Get the list of log files
	 * @return {@code String}
	 */
	public static List<String> getLogFile() {
		List<String> files = new ArrayList<String>();
		ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
		
		// check if there is no log file 
		if (!(loggerFactory instanceof LoggerContext))
			return List.of();
		
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


}
