/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.dmerkushov.loghelper;

import java.util.logging.Level;

/**
 *
 * @author Dmitriy Merkushov
 */
public class Log4jAppenderForLoggerWrapper extends org.apache.log4j.AppenderSkeleton {

	LoggerWrapper loggerWrapper;

	public Log4jAppenderForLoggerWrapper (LoggerWrapper loggerWrapper) {
		this.loggerWrapper = loggerWrapper;
	}

	@Override
	protected void append (org.apache.log4j.spi.LoggingEvent event) {
		org.apache.log4j.spi.LocationInfo locationInfo = event.getLocationInformation ();
		String className = locationInfo.getClassName ();
		String methodName = locationInfo.getMethodName ();

		String msg;
		Object msgObj = event.getMessage ();
		if (msgObj instanceof Throwable) {
			msg = "LOG4J LOGGER MESSAGE: " + ((Throwable) msgObj).getMessage ();
		} else {
			msg = "LOG4J LOGGER MESSAGE: " + (String) msgObj;
		}

		org.apache.log4j.Level level = event.getLevel ();
		int log4jLevelInt = level.toInt ();
		Level julLevel = Level.SEVERE;

		if (log4jLevelInt >= org.apache.log4j.Level.FATAL_INT) {
			julLevel = Level.SEVERE;
		} else if (log4jLevelInt >= org.apache.log4j.Level.ERROR_INT) {
			julLevel = Level.SEVERE;
		} else if (log4jLevelInt >= org.apache.log4j.Level.WARN_INT) {
			julLevel = Level.WARNING;
		} else if (log4jLevelInt >= org.apache.log4j.Level.INFO_INT) {
			julLevel = Level.INFO;
		} else if (log4jLevelInt >= org.apache.log4j.Level.DEBUG_INT) {
			julLevel = Level.FINE;
		} else if (log4jLevelInt >= org.apache.log4j.Level.TRACE_INT) {
			julLevel = Level.FINER;
		}

		loggerWrapper.getLogger ().logp (julLevel, className, methodName, msg);
	}

	@Override
	public void close () {
		this.closed = true;
	}

	@Override
	public boolean requiresLayout () {
		return false;
	}
}
