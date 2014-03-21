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
public class WriteLog4jToJulAppender extends org.apache.log4j.AppenderSkeleton {

	LoggerWrapper loggerWrapper;

	public WriteLog4jToJulAppender (LoggerWrapper loggerWrapper) {
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
		Level julLevel = LoggerWrapper.getJULLevelFromLog4j (level);

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
