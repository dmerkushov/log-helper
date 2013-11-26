/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.dmerkushov.loghelper;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Properties;

/**
 * @deprecated Use the LoggerWrapper class instead
 * @author Dmitriy Merkushov
 * @see LoggerWrapper
 */
public class LogHelper {

	public static final int LOG_TO_PRINTSTREAM = 0;
	public static final int LOG_TO_LOG4J = 1;
	public static final int LOG_TO_JAVASTANDARD = 2;
	private static int logTo = LOG_TO_PRINTSTREAM;
	public static final String LOG_LOG = "LOG  ";
	public static final String LOG_ERROR = "ERROR";
	public static final String LOG_EXCEPTION = "EXCPT";
	public static final String LOG_PROPERTIES = "PROPS";
	private static final org.apache.log4j.Logger log4jLogger = org.apache.log4j.Logger.getRootLogger ();
	private static final java.util.logging.Logger javaStandardLogger = java.util.logging.Logger.getAnonymousLogger ();
	private static final SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.S Z");
	private static boolean isInitialized = false;
	private static PrintStream logPrintStream = System.out;

	/**
	 * Common initialization of the LogHelper
	 */
	private static void initializeCommon () {
	}

	/**
	 * Initialize LogHelper to default values
	 */
	public static void initializeLogHelper () {
		LogHelper.initializePrintStream (System.out);
		isInitialized = true;
	}

	public static int getLogTo () {
		return logTo;
	}

	private static void setLogTo (int logTo) {
		LogHelper.logTo = logTo;
	}

	public static java.util.logging.Logger getJavaStandardLogger () {
		return javaStandardLogger;
	}

	public static org.apache.log4j.Logger getLog4jLogger () {
		return log4jLogger;
	}

	public static void log (String msg) {
		StackTraceElement caller = getCallerStackTraceElement ();
		log (msg, caller);
	}

	public static void log (String msg, StackTraceElement caller) {
		if (caller != null) {
			msg = sdf.format (new java.util.Date ()) + ": " + caller.getClassName () + ":" + caller.getMethodName () + "():" + caller.getLineNumber () + ": " + msg;
		} else {
			msg = sdf.format (new java.util.Date ()) + ": " + "(Unknown class)" + ":" + "(Unknown method)" + "():" + "(Unknown line number)" + ": " + msg;
		}

		LogHelper.writeToLog (msg, LOG_LOG, org.apache.log4j.Level.INFO, java.util.logging.Level.INFO);
	}

	public static void logErr (String msg) {
		StackTraceElement caller = getCallerStackTraceElement ();
		logErr (msg, caller);
	}

	public static void logErr (String msg, StackTraceElement caller) {
		if (caller != null) {
			msg = sdf.format (new java.util.Date ()) + ": " + caller.getClassName () + ":" + caller.getMethodName () + "():" + caller.getLineNumber () + ": " + msg;
		} else {
			msg = sdf.format (new java.util.Date ()) + ": " + "(Unknown class)" + ":" + "(Unknown method)" + "():" + "(Unknown line number)" + ": " + msg;
		}

		LogHelper.writeToLog (msg, LOG_ERROR, org.apache.log4j.Level.WARN, java.util.logging.Level.WARNING);
	}

	public static void logException (Throwable t) {
		String msg = sdf.format (new java.util.Date ()) + ": " + LogHelper.getFullThrowableMsg (t);
		LogHelper.writeToLog (msg, LOG_EXCEPTION, org.apache.log4j.Level.ERROR, java.util.logging.Level.SEVERE);
	}

	public static PrintStream getLogPrintStream () {
		return LogHelper.logPrintStream;
	}

	public static void setLogPrintStream (PrintStream printStream) {
		LogHelper.logPrintStream = printStream;
	}

	public static void initializePrintStream (PrintStream logPrintStream) {
		LogHelper.initializeCommon ();
		LogHelper.setLogTo (LogHelper.LOG_TO_PRINTSTREAM);
		if (!LogHelper.logPrintStream.equals (logPrintStream)) {
			LogHelper.setLogPrintStream (logPrintStream);
		}
		isInitialized = true;
	}

	public static void initializePrintStreamToFile (String filename) throws FileNotFoundException {
		PrintStream logPrintStreamToSet = new PrintStream (filename);
		initializePrintStream (logPrintStreamToSet);
		isInitialized = true;
	}

	public static void initializePrintStreamToFile (File file) throws FileNotFoundException {
		PrintStream logPrintStreamToSet = new PrintStream (file);
		initializePrintStream (logPrintStreamToSet);
		isInitialized = true;
	}

	public static void initializeLog4j () {
		LogHelper.initializeCommon ();

		log4jLogger.setLevel (org.apache.log4j.Level.ALL);
		log4jLogger.removeAllAppenders ();
		log4jLogger.addAppender (new org.apache.log4j.ConsoleAppender (new org.apache.log4j.PatternLayout ("%m\n"), "System.out"));

		LogHelper.setLogTo (LogHelper.LOG_TO_LOG4J);

		LogHelper.isInitialized = true;

	}

	public static void initializeJavaStandard () {
		LogHelper.initializeCommon ();

		javaStandardLogger.setLevel (java.util.logging.Level.ALL);
		java.util.logging.Handler[] handlers = javaStandardLogger.getHandlers ();
		for (java.util.logging.Handler handler : handlers) {
			javaStandardLogger.removeHandler (handler);
		}
		java.util.logging.Formatter formatter = new java.util.logging.Formatter () {

			@Override
			public String format (java.util.logging.LogRecord record) {
				return record.getMessage ();
			}
		};
		java.util.logging.Handler conHandler = new java.util.logging.StreamHandler (System.out, formatter);
		javaStandardLogger.addHandler (conHandler);

		LogHelper.setLogTo (LogHelper.LOG_TO_JAVASTANDARD);

		LogHelper.isInitialized = true;
	}

	public static void logEntering () {
		StackTraceElement caller = getCallerStackTraceElement ();
		logEntering (null, caller);
	}

	public static void logEntering (Object[] parameters) {
		StackTraceElement caller = getCallerStackTraceElement ();
		logEntering (parameters, caller);
	}

	public static void logEntering (Object[] parameters, StackTraceElement caller) {
		String msg = "Entering.";

		if (parameters != null) {
			for (int paramIndex = 0; paramIndex < parameters.length; paramIndex++) {
				Object parameter = parameters[paramIndex];

				if (parameter != null) {
					msg += "\nParameter " + String.valueOf (paramIndex) + " is a " + parameter.getClass ().getName () + ": >" + parameter.toString () + "<";
				} else {
					msg += "\nParameter " + String.valueOf (paramIndex) + " is null.";
				}
			}
		}

		LogHelper.log (msg, caller);
	}

	public static void logExiting () {
		StackTraceElement caller = getCallerStackTraceElement ();

		logExiting (null, true, caller);
	}

	public static void logExiting (Object returnValue) {
		StackTraceElement caller = getCallerStackTraceElement ();

		logExiting (returnValue, false, caller);
	}

	public static void logExiting (Object returnValue, StackTraceElement caller) {
		logExiting (returnValue, false, caller);
	}

	public static void logExiting (Object returnValue, boolean isVoidMethod, StackTraceElement caller) {
		String msg = "Exiting.";

		if (returnValue != null) {
			msg += " Returning " + returnValue.getClass ().getName () + ": >" + returnValue.toString () + "<";
		} else {
			if (!isVoidMethod) {
				msg += " Returning null.";
			}
		}

		LogHelper.log (msg, caller);
	}

	public static void logProperties (Properties props, String comment) {
		ByteArrayOutputStream propsBaos = new ByteArrayOutputStream ();

		try {
			props.store (propsBaos, comment);
		} catch (IOException ex) {
			LogHelper.logException (ex);
		}

		//String charset = java.nio.charset.Charset.defaultCharset ().name ();
		String charset = "UTF-8";

		String propsStr = null;
		try {
			propsStr = propsBaos.toString (charset);
		} catch (UnsupportedEncodingException ex) {
			LogHelper.logException (ex);
		}

		LogHelper.writeToLog (propsStr, LOG_PROPERTIES, org.apache.log4j.Level.INFO, java.util.logging.Level.CONFIG);
	}

	/*
	 * *************************************************************************
	 * Utility methods
	 * ************************************************************************
	 */
	private static void writeToLog (String msg, String logPrefix, org.apache.log4j.Level log4jLevel, java.util.logging.Level javaStandardLevel) {
		msg = logPrefix + " " + Thread.currentThread ().getName () + " " + msg.replaceAll ("\n", "\n" + logPrefix + " ");

		if (log4jLevel == null) {
			log4jLevel = org.apache.log4j.Level.INFO;
		}

		if (javaStandardLevel == null) {
			javaStandardLevel = java.util.logging.Level.INFO;
		}

		switch (logTo) {
			case (LOG_TO_PRINTSTREAM):
				logPrintStream.println (msg);
				break;
			case (LOG_TO_LOG4J):
				log4jLogger.log (log4jLevel, msg);
				break;
			case (LOG_TO_JAVASTANDARD):
				javaStandardLogger.log (javaStandardLevel, msg);
				break;
		}
	}

	public static StackTraceElement getCallerStackTraceElement () {
		StackTraceElement toReturn = null;

		StackTraceElement[] stackTraceElements = Thread.currentThread ().getStackTrace ();
		if (stackTraceElements != null) {
			if (stackTraceElements.length > 3) {
				toReturn = stackTraceElements[3];
			}
		}

		return toReturn;
	}

	private static String getFullThrowableMsg (Throwable t) {
		String msg = t.getClass ().getCanonicalName () + ": " + t.getMessage ();

		StackTraceElement[] stackTraceElements = t.getStackTrace ();

		if (stackTraceElements != null) {
			if (stackTraceElements.length > 0) {
				msg += "\nStack Trace:\n";

				StackTraceElement stackTraceElement = stackTraceElements[0];
				msg += "\t" + stackTraceElement.getClassName () + ":" + stackTraceElement.getMethodName () + "():" + stackTraceElement.getLineNumber ();

				for (int i = 1; i < stackTraceElements.length; i++) {
					stackTraceElement = stackTraceElements[i];
					msg += "\n\tat " + stackTraceElement.getClassName () + ":" + stackTraceElement.getMethodName () + "():" + stackTraceElement.getLineNumber ();
				}
			}
		}

		Throwable cause = t.getCause ();

		if (cause != null) {
			msg += "\nCaused by:\n" + LogHelper.getFullThrowableMsg (cause);
		}

		return msg;
	}
}
