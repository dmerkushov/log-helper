/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.dmerkushov.loghelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class that slightly extends the functionality of
 * <code>java.util.logging.Logger</code><br/>
 *
 * Usage:<br/>
 * <code>LoggerWrapper loggerWrapper = LoggerWrapper.getLoggerWrapper ("Logger wrapper name");<br/>
 * Logger logger = loggerWrapper.getLogger ();
 * </code>
 *
 * @author Dmitriy Merkushov
 * @see java.util.logging.Logger
 */
public class LoggerWrapper {

	Logger logger;
	org.apache.log4j.Logger log4jLogger;
	Log4jAppenderForLoggerWrapper log4jAppender;
	static HashMap<String, LoggerWrapper> wrappers = new HashMap<String, LoggerWrapper> ();
	Level defaultLevel = Level.ALL;

	private LoggerWrapper (String name) {
		logger = Logger.getLogger (name);
		logger.setLevel (defaultLevel);

		log4jLogger = org.apache.log4j.Logger.getLogger (name);
		log4jLogger.removeAllAppenders ();
		log4jAppender = new Log4jAppenderForLoggerWrapper (this);
		log4jLogger.addAppender (log4jAppender);
		log4jLogger.setLevel (getLog4jLevelFromJUL (defaultLevel));
	}

	/**
	 * Get a loggerWrapper instance with a console handler
	 *
	 * @param name
	 * @return
	 */
	public static synchronized LoggerWrapper getLoggerWrapper (String name) {
		LoggerWrapper result;
		if (wrappers.containsKey (name)) {
			result = wrappers.get (name);
		} else {
			result = new LoggerWrapper (name);
			wrappers.put (name, result);
		}

		return result;
	}

	/**
	 * Get the Logger object linked to this LoggerWrapper
	 *
	 * @return
	 */
	public Logger getLogger () {
		return logger;
	}

	/**
	 * Get the Log4j Logger instance linked to this LoggerWrapper. Any log
	 * record sent to this logger will be logged by the LoggerWrapper instance.
	 *
	 * @return
	 */
	public org.apache.log4j.Logger getLog4jLogger () {
		return log4jLogger;
	}

	/**
	 * Removes all handlers, then adds a console handler and a file handler with
	 * the specified filename pattern
	 *
	 * @param filenamePattern the filename pattern as in FileHandler
	 * constructor; if <code>null</code>, the FileHandler won't be added
	 * @return true if file handler has been added; console handler adding
	 * should be ever successful
	 * @see FileHandler
	 * @see FileHandler#FileHandler(java.lang.String)
	 */
	public boolean configureByDefault (String filenamePattern) {
		removeLoggerHandlers ();
		addConsoleHandler ();
		boolean fileHandlerSuccess = false;
		if (filenamePattern != null) {
			fileHandlerSuccess = addFileHandler (filenamePattern);
		}
		return fileHandlerSuccess;
	}

	/**
	 * Removes all handlers, then adds a console handler and a daily rolling
	 * file handler with the specified filename pattern
	 *
	 * @param filenamePattern the filename pattern as in FileHandler
	 * constructor; if <code>null</code>, the FileHandler won't be added
	 * @return true if file handler has been added; console handler adding
	 * should be ever successful
	 * @see DailyRollingFileHandler
	 * @see DailyRollingFileHandler#DailyRollingFileHandler(java.lang.String)
	 */
	public boolean configureByDefaultDailyRolling (String filenamePattern) {
		removeLoggerHandlers ();
		addConsoleHandler ();
		boolean fileHandlerSuccess = false;
		if (filenamePattern != null) {
			fileHandlerSuccess = addDailyRollingFileHandler (filenamePattern);
		}
		return fileHandlerSuccess;
	}

	/**
	 * Removes all handlers, then adds a console handler and a size rolling
	 * file handler with the specified filename pattern
	 *
	 * @param filenamePattern the filename pattern as in FileHandler
	 * constructor; if <code>null</code>, the FileHandler won't be added
	 * @return true if file handler has been added; console handler adding
	 * should be ever successful
	 * @see SizeRollingFileHandler
	 * @see SizeRollingFileHandler#SizeRollingFileHandler(java.lang.String)
	 */
	public boolean configureByDefaultSizeRolling (String filenamePattern) {
		removeLoggerHandlers ();
		addConsoleHandler ();
		boolean fileHandlerSuccess = false;
		if (filenamePattern != null) {
			fileHandlerSuccess = addSizeRollingFileHandler (filenamePattern);
		}
		return fileHandlerSuccess;
	}

	/**
	 * Removes all handlers, then adds a write-to-log4j handler
	 */
	public void configureByDefaultWriteToLog4j () {
		removeLoggerHandlers ();
		addWriteToLog4jHandler ();
	}

	/**
	 * Remove all handlers from the linked logger
	 */
	public void removeLoggerHandlers () {
		for (Handler handler : logger.getHandlers ()) {
			logger.removeHandler (handler);
		}
	}

	/**
	 * Add a daily rolling file handler to the linked logger with ALL logging
	 * level
	 *
	 * @param filenamePattern the filename pattern as in DailyRollingFileHandler
	 * constructor
	 * @return <code>true</code> if success, <code>false</code> otherwise
	 * (including the case <code>filenamePattern</code> is <code>null</code>)
	 * @see DailyRollingFileHandler
	 * @see DailyRollingFileHandler#DailyRollingFileHandler(java.lang.String)
	 * @see Level#ALL
	 */
	public boolean addDailyRollingFileHandler (String filenamePattern) {
		boolean isSuccess = false;

		if (filenamePattern != null) {
			DailyRollingFileHandler drfh = null;
			try {
				drfh = new DailyRollingFileHandler (filenamePattern);
			} catch (IOException ex) {
				Logger.getLogger (LoggerWrapper.class.getName ()).log (Level.SEVERE, null, ex);
			} catch (SecurityException ex) {
				Logger.getLogger (LoggerWrapper.class.getName ()).log (Level.SEVERE, null, ex);
			}

			if (drfh != null) {
				drfh.setLevel (Level.ALL);
				drfh.setFormatter (new LoggerFormatter ());
				logger.addHandler (drfh);
				isSuccess = true;
			} else {
				isSuccess = false;
			}
		}

		return isSuccess;
	}

	/**
	 * Add a size rolling file handler to the linked logger with ALL logging
	 * level
	 *
	 * @param filenamePattern the filename pattern as in SizeRollingFileHandler
	 * constructor
	 * @return <code>true</code> if success, <code>false</code> otherwise
	 * (including the case <code>filenamePattern</code> is <code>null</code>)
	 * @see SizeRollingFileHandler
	 * @see SizeRollingFileHandler#SizeRollingFileHandler(java.lang.String)
	 * @see Level#ALL
	 */
	public boolean addSizeRollingFileHandler (String filenamePattern) {
		boolean isSuccess = false;

		if (filenamePattern != null) {
			SizeRollingFileHandler srfh = null;
			try {
				srfh = new SizeRollingFileHandler (filenamePattern);
			} catch (IOException ex) {
				Logger.getLogger (LoggerWrapper.class.getName ()).log (Level.SEVERE, null, ex);
			} catch (SecurityException ex) {
				Logger.getLogger (LoggerWrapper.class.getName ()).log (Level.SEVERE, null, ex);
			}

			if (srfh != null) {
				srfh.setLevel (Level.ALL);
				srfh.setFormatter (new LoggerFormatter ());
				logger.addHandler (srfh);
				isSuccess = true;
			} else {
				isSuccess = false;
			}
		}

		return isSuccess;
	}

	/**
	 * Add a default JUL file handler to the linked logger with ALL logging
	 * level
	 *
	 * @param filenamePattern the filename pattern as in FileHandler constructor
	 * @return <code>true</code> if success, <code>false</code> otherwise
	 * (including the case <code>filenamePattern</code> is <code>null</code>)
	 * @see FileHandler
	 * @see FileHandler#FileHandler(java.lang.String)
	 * @see Level#ALL
	 */
	public boolean addFileHandler (String filenamePattern) {
		boolean isSuccess = false;

		if (filenamePattern != null) {
			FileHandler fh = null;
			try {
				fh = new FileHandler (filenamePattern, true);
			} catch (IOException ex) {
				Logger.getLogger (LoggerWrapper.class.getName ()).log (Level.SEVERE, null, ex);
			} catch (SecurityException ex) {
				Logger.getLogger (LoggerWrapper.class.getName ()).log (Level.SEVERE, null, ex);
			}

			if (fh != null) {
				fh.setLevel (defaultLevel);
				fh.setFormatter (new LoggerFormatter ());
				logger.addHandler (fh);
				isSuccess = true;
			} else {
				isSuccess = false;
			}
		}

		return isSuccess;
	}

	/**
	 * Add a console handler to the linked logger with ALL logging level
	 *
	 * @see FileHandler
	 * @see FileHandler#FileHandler(java.lang.String)
	 * @see Level#ALL
	 */
	public void addConsoleHandler () {
		ConsoleHandler ch = null;
		ch = new ConsoleHandler ();
		ch.setLevel (defaultLevel);
		ch.setFormatter (new LoggerFormatter ());
		logger.addHandler (ch);
	}
	
	public void addWriteToLog4jHandler () {
		WriteToLog4jHandler log4jHandler = null;
		log4jHandler = new WriteToLog4jHandler ();
		log4jHandler.setLevel (defaultLevel);
		log4jHandler.setFormatter (new LoggerFormatter ());
		logger.addHandler (log4jHandler);
	}

	/**
	 * Log a method entry. <p> This is a convenience method that can be used to
	 * log entry to a method. A LogRecord with message "ENTRY", log level FINER,
	 * and the given sourceMethod and sourceClass is logged. <p>
	 */
	public void entering () {
		StackTraceElement caller = LogHelperUtil.getCallerStackTraceElement ();
		logger.entering (caller.getClassName (), caller.getMethodName ());
	}

	/*public void entering (Object[] methodParams) {
	 StackTraceElement caller = LogHelperUtil.getCallerStackTraceElement ();
	 logger.entering (caller.getClassName (), caller.getMethodName (), methodParams);
	 }*/
	/**
	 * Log a method entry, with an array of parameters. <p> This is a
	 * convenience method that can be used to log entry to a method. A LogRecord
	 * with message "ENTRY" (followed by a format {N} indicator for each entry
	 * in the parameter array), log level FINER, and the given sourceMethod,
	 * sourceClass, and parameters is logged. <p>
	 *
	 * @param methodParams array of parameters to the method being entered
	 */
	public void entering (Object... methodParams) {
		StackTraceElement caller = LogHelperUtil.getCallerStackTraceElement ();
		logger.entering (caller.getClassName (), caller.getMethodName (), methodParams);
	}

	/**
	 * Log a method return. <p> This is a convenience method that can be used to
	 * log returning from a method. A LogRecord with message "RETURN", log level
	 * FINER, and the given sourceMethod and sourceClass is logged. <p>
	 */
	public void exiting () {
		StackTraceElement caller = LogHelperUtil.getCallerStackTraceElement ();
		logger.exiting (caller.getClassName (), caller.getMethodName ());
	}

	/**
	 * Log a method return, with result object. <p> This is a convenience method
	 * that can be used to log returning from a method. A LogRecord with message
	 * "RETURN {0}", log level FINER, and the gives sourceMethod, sourceClass,
	 * and result object is logged. <p>
	 *
	 * @param result Object that is being returned
	 */
	public void exiting (Object result) {
		StackTraceElement caller = LogHelperUtil.getCallerStackTraceElement ();
		logger.exiting (caller.getClassName (), caller.getMethodName (), result);
	}

	/**
	 * Log properties
	 *
	 * @param props
	 * @param comment
	 */
	public void properties (Properties props, String comment) {
		ByteArrayOutputStream propsBaos = new ByteArrayOutputStream ();

		if (props == null) {
			props = new Properties ();
		}

		try {
			if (comment != null) {
				props.store (propsBaos, comment);
			} else {
				props.store (propsBaos, "Automatic comment from LoggerWrapper");
			}
		} catch (IOException ex) {
			throwing (ex);
		}

		//String charset = java.nio.charset.Charset.defaultCharset ().name ();
		String charset = "UTF-8";

		String propsStr = null;
		try {
			propsStr = propsBaos.toString (charset);
		} catch (UnsupportedEncodingException ex) {
			throwing (ex);
		}

		logger.config (propsStr);
	}

	/**
	 * Log a Throwable at Level.FINER, with associated Throwable information.
	 * <p> This is a convenience method to log that a method is terminating by
	 * throwing an exception. The logging is done using the FINER level. <p> If
	 * the logger is currently enabled for the given message level then the
	 * given arguments are stored in a LogRecord which is forwarded to all
	 * registered output handlers. The LogRecord's message is set to "THROW".
	 * <p> Note that the thrown argument is stored in the LogRecord thrown
	 * property, rather than the LogRecord parameters property. Thus is it
	 * processed specially by output Formatters and is not treated as a
	 * formatting parameter to the LogRecord message property. <p>
	 *
	 * @param t The Throwable that is being thrown.
	 * @see Logger#throwing(java.lang.String, java.lang.String,
	 * java.lang.Throwable)
	 */
	public void throwing (Throwable t) {
		StackTraceElement caller = LogHelperUtil.getCallerStackTraceElement ();
		if (caller != null) {
			logger.throwing (caller.getClassName (), caller.getMethodName () + "():" + caller.getLineNumber (), t);
		} else {
			logger.throwing ("(UnknownSourceClass)", "(unknownSourceMethod)", t);
		}
		
	}

	/**
	 * Log a message at Level.FINER, with associated Throwable information. <p>
	 * If the logger is currently enabled for Level.FINER then the given
	 * arguments are stored in a LogRecord which is forwarded to all registered
	 * output handlers. <p> Note that the thrown argument is stored in the
	 * LogRecord thrown property, rather than the LogRecord parameters property.
	 * Thus is it processed specially by output Formatters and is not treated as
	 * a formatting parameter to the LogRecord message property. <p>
	 *
	 * @param msg The string message (or a key in the message catalog)
	 * @param t Throwable associated with log message.
	 * @see Logger#throwing(java.lang.String, java.lang.String,
	 * java.lang.Throwable)
	 * @see Logger#logp(java.util.logging.Level, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.Throwable)
	 */
	public void throwing (String msg, Throwable t) {
		StackTraceElement caller = LogHelperUtil.getCallerStackTraceElement ();
		if (caller != null) {
			logger.logp (Level.FINER, caller.getClassName (), caller.getMethodName () + "():" + caller.getLineNumber (), msg, t);
		} else {
			logger.logp (Level.FINER, "(UnknownSourceClass)", "(unknownSourceMethod)", msg, t);
		}
	}

	/**
	 * Log a SEVERE message, specifying source class and method, with no
	 * arguments. <p> If the logger is currently enabled for the given message
	 * level then the given message is forwarded to all the registered output
	 * Handler objects. <p>
	 *
	 * @param msg The string message (or a key in the message catalog)
	 */
	public void severe (String msg) {
		StackTraceElement caller = LogHelperUtil.getCallerStackTraceElement ();
		if (caller != null) {
			logger.logp (Level.SEVERE, caller.getClassName (), caller.getMethodName () + "():" + caller.getLineNumber (), msg);
		} else {
			logger.logp (Level.SEVERE, "(UnknownSourceClass)", "(unknownSourceMethod)", msg);
		}
	}

	/**
	 * Log a WARNING message, specifying source class and method, with no
	 * arguments. <p> If the logger is currently enabled for the given message
	 * level then the given message is forwarded to all the registered output
	 * Handler objects. <p>
	 *
	 * @param msg The string message (or a key in the message catalog)
	 */
	public void warning (String msg) {
		StackTraceElement caller = LogHelperUtil.getCallerStackTraceElement ();
		if (caller != null) {
			logger.logp (Level.WARNING, caller.getClassName (), caller.getMethodName () + "():" + caller.getLineNumber (), msg);
		} else {
			logger.logp (Level.WARNING, "(UnknownSourceClass)", "(unknownSourceMethod)", msg);
		}
	}

	/**
	 * Log an INFO message, specifying source class and method, with no
	 * arguments. <p> If the logger is currently enabled for the given message
	 * level then the given message is forwarded to all the registered output
	 * Handler objects. <p>
	 *
	 * @param msg The string message (or a key in the message catalog)
	 */
	public void info (String msg) {
		StackTraceElement caller = LogHelperUtil.getCallerStackTraceElement ();
		if (caller != null) {
			logger.logp (Level.INFO, caller.getClassName (), caller.getMethodName () + "():" + caller.getLineNumber (), msg);
		} else {
			logger.logp (Level.INFO, "(UnknownSourceClass)", "(unknownSourceMethod)", msg);
		}
	}

	/**
	 * Log a CONFIG message, specifying source class and method, with no
	 * arguments. <p> If the logger is currently enabled for the given message
	 * level then the given message is forwarded to all the registered output
	 * Handler objects. <p>
	 *
	 * @param msg The string message (or a key in the message catalog)
	 */
	public void config (String msg) {
		StackTraceElement caller = LogHelperUtil.getCallerStackTraceElement ();
		if (caller != null) {
			logger.logp (Level.CONFIG, caller.getClassName (), caller.getMethodName () + "():" + caller.getLineNumber (), msg);
		} else {
			logger.logp (Level.CONFIG, "(UnknownSourceClass)", "(unknownSourceMethod)", msg);
		}
	}

	/**
	 * Log a FINE message, specifying source class and method, with no
	 * arguments. <p> If the logger is currently enabled for the given message
	 * level then the given message is forwarded to all the registered output
	 * Handler objects. <p>
	 *
	 * @param msg The string message (or a key in the message catalog)
	 */
	public void fine (String msg) {
		StackTraceElement caller = LogHelperUtil.getCallerStackTraceElement ();
		if (caller != null) {
			logger.logp (Level.FINE, caller.getClassName (), caller.getMethodName () + "():" + caller.getLineNumber (), msg);
		} else {
			logger.logp (Level.FINE, "(UnknownSourceClass)", "(unknownSourceMethod)", msg);
		}
	}

	/**
	 * Log a FINER message, specifying source class and method, with no
	 * arguments. <p> If the logger is currently enabled for the given message
	 * level then the given message is forwarded to all the registered output
	 * Handler objects. <p>
	 *
	 * @param msg The string message (or a key in the message catalog)
	 */
	public void finer (String msg) {
		StackTraceElement caller = LogHelperUtil.getCallerStackTraceElement ();
		if (caller != null) {
			logger.logp (Level.FINER, caller.getClassName (), caller.getMethodName () + "():" + caller.getLineNumber (), msg);
		} else {
			logger.logp (Level.FINER, "(UnknownSourceClass)", "(unknownSourceMethod)", msg);
		}
	}

	/**
	 * Log a FINEST message, specifying source class and method, with no
	 * arguments. <p> If the logger is currently enabled for the given message
	 * level then the given message is forwarded to all the registered output
	 * Handler objects. <p>
	 *
	 * @param msg The string message (or a key in the message catalog)
	 */
	public void finest (String msg) {
		StackTraceElement caller = LogHelperUtil.getCallerStackTraceElement ();
		if (caller != null) {
			logger.logp (Level.FINEST, caller.getClassName (), caller.getMethodName () + "():" + caller.getLineNumber (), msg);
		} else {
			logger.logp (Level.FINEST, "(UnknownSourceClass)", "(unknownSourceMethod)", msg);
		}
	}

	/**
	 * Log a DOM node list at the FINER level
	 *
	 * @param msg The message to show with the list, or null if no message
	 * needed
	 * @param nodeList
	 * @see NodeList
	 */
	public void logDomNodeList (String msg, NodeList nodeList) {
		StackTraceElement caller = LogHelperUtil.getCallerStackTraceElement ();

		String toLog = (msg != null ? msg + "\n" : "DOM nodelist:\n");
		for (int i = 0; i < nodeList.getLength (); i++) {
			toLog += domNodeDescription (nodeList.item (i), 0) + "\n";
		}

		if (caller != null) {
			logger.logp (Level.FINER, caller.getClassName (), caller.getMethodName () + "():" + caller.getLineNumber (), toLog);
		} else {
			logger.logp (Level.FINER, "(UnknownSourceClass)", "(unknownSourceMethod)", toLog);
		}
	}

	/**
	 * Log a DOM node at the FINER level
	 *
	 * @param msg The message to show with the node, or null if no message
	 * needed
	 * @param node
	 * @see Node
	 */
	public void logDomNode (String msg, Node node) {
		StackTraceElement caller = LogHelperUtil.getCallerStackTraceElement ();

		String toLog =  (msg != null ? msg + "\n" : "DOM node:\n") + domNodeDescription (node, 0);

		if (caller != null) {
			logger.logp (Level.FINER, caller.getClassName (), caller.getMethodName () + "():" + caller.getLineNumber (), toLog);
		} else {
			logger.logp (Level.FINER, "(UnknownSourceClass)", "(unknownSourceMethod)", toLog);
		}
	}

	/**
	 * Form a DOM node textual representation recursively
	 * @param node
	 * @param tablevel
	 * @return 
	 */
	private String domNodeDescription (Node node, int tablevel) {
		String domNodeDescription = null;

		String nodeName = node.getNodeName ();
		String nodeValue = node.getNodeValue ();
		if (!(nodeName.equals ("#text") && nodeValue.replaceAll ("\n", "").trim ().equals (""))) {
			domNodeDescription = tabs (tablevel) + node.getNodeName () + "\n";

			NamedNodeMap attributes = node.getAttributes ();
			if (attributes != null) {
				for (int i = 0; i < attributes.getLength (); i++) {
					Node attribute = attributes.item (i);
					domNodeDescription += tabs (tablevel) + "-" + attribute.getNodeName () + "=" + attribute.getNodeValue () + "\n";
				}
			}

			domNodeDescription += tabs (tablevel) + "=" + node.getNodeValue () + "\n";

			NodeList children = node.getChildNodes ();
			if (children != null) {
				for (int i = 0; i < children.getLength (); i++) {
					String childDescription = domNodeDescription (children.item (i), tablevel + 1);
					if (childDescription != null) {
						domNodeDescription += childDescription;
					}
				}
			}
		}

		return domNodeDescription;
	}

	private String tabs (int tablevel) {
		String tabs = "";

		for (int i = 0; i < tablevel; i++) {
			tabs += "\t";
		}

		return tabs;
	}

	/**
	 * Check if a message of the given level would actually be logged by this
	 * loggerWrapper.
	 *
	 * @param level a message logging level
	 * @return true if the given message level is currently being logged.
	 */
	public boolean isLoggable (Level level) {
		return logger.isLoggable (level);
	}
	
	/**
	 * Set this level for all configured loggers
	 * @param level 
	 */
	public void setLevel (Level level) {
		
		this.defaultLevel = level;
		logger.setLevel (level);
		for (Handler handler: logger.getHandlers ()) {
			handler.setLevel (level);
		}
		
		log4jLogger.setLevel (getLog4jLevelFromJUL (level));
		
	}
	
	/**
	 * Get a Log4j logging level the same as given JUL level
	 * @param level
	 * @return 
	 */
	public static org.apache.log4j.Level getLog4jLevelFromJUL (java.util.logging.Level level) {
		org.apache.log4j.Level log4jLevel;
		if ((level.intValue () >= Level.SEVERE.intValue ()) && (level.intValue () < Level.OFF.intValue ())) {
			log4jLevel = org.apache.log4j.Level.ERROR;
		} else if (level.intValue () >= Level.WARNING.intValue ()) {
			log4jLevel = org.apache.log4j.Level.WARN;
		} else if (level.intValue () >= Level.INFO.intValue ()) {
			log4jLevel = org.apache.log4j.Level.INFO;
		} else if (level.intValue () >= Level.CONFIG.intValue ()) {
			log4jLevel = org.apache.log4j.Level.DEBUG;
		} else if (level.intValue () >= Level.FINEST.intValue ()) {
			log4jLevel = org.apache.log4j.Level.TRACE;
		} else if (level.intValue () < Level.FINEST.intValue ()) {
			log4jLevel = org.apache.log4j.Level.ALL;
		} else {
			log4jLevel = org.apache.log4j.Level.OFF;
		}
		return log4jLevel;
	}
	
	public static Level getJULLevelFromLog4j (org.apache.log4j.Level log4jLevel) {
		int log4jLevelInt = log4jLevel.toInt ();
		Level julLevel;
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
		} else {
			julLevel = Level.FINEST;
		}

		return julLevel;
	}
}
