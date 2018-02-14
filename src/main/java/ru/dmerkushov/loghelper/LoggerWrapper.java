/*
 * Copyright 2018 dmerkushov.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.dmerkushov.loghelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ru.dmerkushov.loghelper.configure.LogHelperConfigurator;

/**
 * Class that slightly extends the functionality of
 * <code>java.util.logging.Logger</code><br>
 *
 * Usage:<br>
 * <code>LoggerWrapper loggerWrapper = LoggerWrapper.getLoggerWrapper ("Logger wrapper name");<br>
 * Logger logger = loggerWrapper.getLogger ();
 * </code>
 *
 * @author Dmitriy Merkushov
 * @see java.util.logging.Logger
 */
public class LoggerWrapper {

	Logger logger;
	protected Level defaultLevel = Level.ALL;
	String name;

	static {
		LogHelperConfigurator.configure ();
	}

	protected LoggerWrapper (String name) {
		logger = Logger.getLogger (name);
		logger.setLevel (defaultLevel);

		this.name = name;

		LogHelper.registerLoggerWrapper (this);
	}

	/**
	 * Get the current JUL logging level
	 *
	 * @return
	 */
	public Level getJulLevel () {
		return getLogger ().getLevel ();
	}

	/**
	 * Set the JUL level
	 *
	 * @param level
	 */
	public synchronized void setJulLevel (Level level) {
		logger.setLevel (level);
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
	 * Get this instance's name
	 *
	 * @return
	 */
	public String getName () {
		return name;
	}

	public void addLoggerHandler (Handler handler) {
		logger.addHandler (handler);
	}

	/**
	 * Remove a handler from the linked logger
	 *
	 * @param handler
	 */
	public void removeLoggerHandler (Handler handler) {
		logger.removeHandler (handler);
	}

	/**
	 * Get an array of handlers from the linked logger
	 *
	 * @return
	 */
	public Handler[] getLoggerHandlers () {
		return logger.getHandlers ();
	}

	/**
	 * Remove all handlers from the linked logger
	 */
	public void removeAllLoggerHandlers () {
		for (Handler handler : getLoggerHandlers ()) {
			removeLoggerHandler (handler);
		}
	}

	/**
	 * Log a method entry.
	 * <p>
	 * This is a convenience method that can be used to log entry to a method. A
	 * LogRecord with message "ENTRY", log level FINER, and the given
	 * sourceMethod and sourceClass is logged.
	 * <p>
	 */
	public void entering () {
		StackTraceElement caller = StackTraceUtils.getCallerStackTraceElement ();
		logger.entering (caller.getClassName (), caller.getMethodName ());
	}

	/**
	 * Log a method entry, with an array of parameters.
	 * <p>
	 * This is a convenience method that can be used to log entry to a method. A
	 * LogRecord with message "ENTRY" (followed by a format {N} indicator for
	 * each entry in the parameter array), log level FINER, and the given
	 * sourceMethod, sourceClass, and parameters is logged.
	 * <p>
	 *
	 * @param methodParams array of parameters to the method being entered
	 */
	public void entering (Object... methodParams) {
		StackTraceElement caller = StackTraceUtils.getCallerStackTraceElement ();
		logger.entering (caller.getClassName (), caller.getMethodName (), methodParams);
	}

	/**
	 * Log a method return.
	 * <p>
	 * This is a convenience method that can be used to log returning from a
	 * method. A LogRecord with message "RETURN", log level FINER, and the given
	 * sourceMethod and sourceClass is logged.
	 * <p>
	 */
	public void exiting () {
		StackTraceElement caller = StackTraceUtils.getCallerStackTraceElement ();
		logger.exiting (caller.getClassName (), caller.getMethodName ());
	}

	/**
	 * Log a method return, with result object.
	 * <p>
	 * This is a convenience method that can be used to log returning from a
	 * method. A LogRecord with message "RETURN {0}", log level FINER, and the
	 * gives sourceMethod, sourceClass, and result object is logged.
	 * <p>
	 *
	 * @param result Object that is being returned
	 */
	public void exiting (Object result) {
		StackTraceElement caller = StackTraceUtils.getCallerStackTraceElement ();
		logger.exiting (caller.getClassName (), caller.getMethodName (), result);
	}

	/**
	 * Log properties at Level.CONFIG
	 *
	 * @param props
	 * @param msg
	 */
	public void logProperties (Properties props, String msg) {
		ByteArrayOutputStream propsBaos = new ByteArrayOutputStream ();

		if (props == null) {
			props = new Properties ();
		}

		try {
			if (msg != null) {
				props.store (propsBaos, msg);
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
			return;
		}

		logger.config (propsStr);
	}

	/**
	 * Log preferences at Level.CONFIG
	 *
	 * @param prefs
	 * @param msg
	 */
	public void logPreferences (Preferences prefs, String msg) {
		StackTraceElement caller = StackTraceUtils.getCallerStackTraceElement ();

		if (prefs == null) {
			prefs = Preferences.userNodeForPackage (LoggerWrapper.class);
		}
		if (msg == null) {
			msg = "Automatic comment from LoggerWrapper";
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream ();
		try {
			prefs.exportSubtree (baos);
		} catch (IOException ex) {
			throwing (ex);
			return;
		} catch (BackingStoreException ex) {
			throwing (ex);
			return;
		}
		String xmlStr = baos.toString ();
		DocumentBuilder db;
		try {
			db = DocumentBuilderFactory.newInstance ().newDocumentBuilder ();
		} catch (ParserConfigurationException ex) {
			throwing (ex);
			return;
		}
		ByteArrayInputStream bais = new ByteArrayInputStream (xmlStr.getBytes ());
		Document doc;
		try {
			doc = db.parse (bais);
		} catch (SAXException ex) {
			throwing (ex);
			return;
		} catch (IOException ex) {
			throwing (ex);
			return;
		}
		logDomNode (msg, doc, Level.CONFIG, caller);
	}

	/**
	 * Log a Throwable at Level.FINER, with associated Throwable information.
	 * <p>
	 * This is a convenience method to log that a method is terminating by
	 * throwing an exception. The logging is done using the FINER level.
	 * <p>
	 * If the logger is currently enabled for the given message level then the
	 * given arguments are stored in a LogRecord which is forwarded to all
	 * registered output handlers. The LogRecord's message is set to "THROW".
	 * <p>
	 * Note that the thrown argument is stored in the LogRecord thrown property,
	 * rather than the LogRecord parameters property. Thus is it processed
	 * specially by output Formatters and is not treated as a formatting
	 * parameter to the LogRecord message property.
	 * <p>
	 *
	 * @param t The Throwable that is being thrown.
	 * @see Logger#throwing(java.lang.String, java.lang.String,
	 * java.lang.Throwable)
	 */
	public void throwing (Throwable t) {
		StackTraceElement caller = StackTraceUtils.getCallerStackTraceElement ();
		if (caller != null) {
			logger.throwing (caller.getClassName (), caller.getMethodName () + "():" + caller.getLineNumber (), t);
		} else {
			logger.throwing ("(UnknownSourceClass)", "(unknownSourceMethod)", t);
		}

	}

	/**
	 * Log a message at Level.FINER, with associated Throwable information.
	 * <p>
	 * If the logger is currently enabled for Level.FINER then the given
	 * arguments are stored in a LogRecord which is forwarded to all registered
	 * output handlers.
	 * <p>
	 * Note that the thrown argument is stored in the LogRecord thrown property,
	 * rather than the LogRecord parameters property. Thus is it processed
	 * specially by output Formatters and is not treated as a formatting
	 * parameter to the LogRecord message property.
	 * <p>
	 *
	 * @param msg The string message (or a key in the message catalog)
	 * @param t Throwable associated with log message.
	 * @see Logger#throwing(java.lang.String, java.lang.String,
	 * java.lang.Throwable)
	 * @see Logger#logp(java.util.logging.Level, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.Throwable)
	 */
	public void throwing (String msg, Throwable t) {
		StackTraceElement caller = StackTraceUtils.getCallerStackTraceElement ();
		if (caller != null) {
			logger.logp (Level.FINER, caller.getClassName (), caller.getMethodName () + "():" + caller.getLineNumber (), msg, t);
		} else {
			logger.logp (Level.FINER, "(UnknownSourceClass)", "(unknownSourceMethod)", msg, t);
		}
	}

	/**
	 * Log a SEVERE message, specifying source class and method, with no
	 * arguments.
	 * <p>
	 * If the logger is currently enabled for the given message level then the
	 * given message is forwarded to all the registered output Handler objects.
	 * <p>
	 *
	 * @param msg The string message (or a key in the message catalog)
	 */
	public void severe (String msg) {
		StackTraceElement caller = StackTraceUtils.getCallerStackTraceElement ();
		if (caller != null) {
			logger.logp (Level.SEVERE, caller.getClassName (), caller.getMethodName () + "():" + caller.getLineNumber (), msg);
		} else {
			logger.logp (Level.SEVERE, "(UnknownSourceClass)", "(unknownSourceMethod)", msg);
		}
	}

	/**
	 * Log a SEVERE message with a Throwable attached, specifying source class
	 * and method, with no arguments.
	 * <p>
	 * If the logger is currently enabled for the given message level then the
	 * given message is forwarded to all the registered output Handler objects.
	 * <p>
	 *
	 * @param msg The string message
	 * @param t The Throwable
	 */
	public void severe (String msg, Throwable t) {
		StackTraceElement caller = StackTraceUtils.getCallerStackTraceElement ();
		if (caller != null) {
			logger.logp (Level.SEVERE, caller.getClassName (), caller.getMethodName () + "():" + caller.getLineNumber (), msg, t);
		} else {
			logger.logp (Level.SEVERE, "(UnknownSourceClass)", "(unknownSourceMethod)", msg, t);
		}
	}

	/**
	 * Log a WARNING message, specifying source class and method
	 * <p>
	 * If the logger is currently enabled for the given message level then the
	 * given message is forwarded to all the registered output Handler objects.
	 * <p>
	 *
	 * @param msg The string message (or a key in the message catalog)
	 */
	public void warning (String msg) {
		StackTraceElement caller = StackTraceUtils.getCallerStackTraceElement ();
		if (caller != null) {
			logger.logp (Level.WARNING, caller.getClassName (), caller.getMethodName () + "():" + caller.getLineNumber (), msg);
		} else {
			logger.logp (Level.WARNING, "(UnknownSourceClass)", "(unknownSourceMethod)", msg);
		}
	}

	/**
	 * Log a WARNING message with a Throwable attached, specifying source class
	 * and method
	 * <p>
	 * If the logger is currently enabled for the given message level then the
	 * given message is forwarded to all the registered output Handler objects.
	 * <p>
	 *
	 * @param msg The string message
	 * @param t The Throwable
	 */
	public void warning (String msg, Throwable t) {
		StackTraceElement caller = StackTraceUtils.getCallerStackTraceElement ();
		if (caller != null) {
			logger.logp (Level.WARNING, caller.getClassName (), caller.getMethodName () + "():" + caller.getLineNumber (), msg, t);
		} else {
			logger.logp (Level.WARNING, "(UnknownSourceClass)", "(unknownSourceMethod)", msg, t);
		}
	}

	/**
	 * Log an INFO message, specifying source class and method, with no
	 * arguments.
	 * <p>
	 * If the logger is currently enabled for the given message level then the
	 * given message is forwarded to all the registered output Handler objects.
	 * <p>
	 *
	 * @param msg The string message (or a key in the message catalog)
	 */
	public void info (String msg) {
		StackTraceElement caller = StackTraceUtils.getCallerStackTraceElement ();
		if (caller != null) {
			logger.logp (Level.INFO, caller.getClassName (), caller.getMethodName () + "():" + caller.getLineNumber (), msg);
		} else {
			logger.logp (Level.INFO, "(UnknownSourceClass)", "(unknownSourceMethod)", msg);
		}
	}

	/**
	 * Log a CONFIG message, specifying source class and method, with no
	 * arguments.
	 * <p>
	 * If the logger is currently enabled for the given message level then the
	 * given message is forwarded to all the registered output Handler objects.
	 * <p>
	 *
	 * @param msg The string message (or a key in the message catalog)
	 */
	public void config (String msg) {
		StackTraceElement caller = StackTraceUtils.getCallerStackTraceElement ();
		if (caller != null) {
			logger.logp (Level.CONFIG, caller.getClassName (), caller.getMethodName () + "():" + caller.getLineNumber (), msg);
		} else {
			logger.logp (Level.CONFIG, "(UnknownSourceClass)", "(unknownSourceMethod)", msg);
		}
	}

	/**
	 * Log a FINE message, specifying source class and method, with no
	 * arguments.
	 * <p>
	 * If the logger is currently enabled for the given message level then the
	 * given message is forwarded to all the registered output Handler objects.
	 * <p>
	 *
	 * @param msg The string message (or a key in the message catalog)
	 */
	public void fine (String msg) {
		StackTraceElement caller = StackTraceUtils.getCallerStackTraceElement ();
		if (caller != null) {
			logger.logp (Level.FINE, caller.getClassName (), caller.getMethodName () + "():" + caller.getLineNumber (), msg);
		} else {
			logger.logp (Level.FINE, "(UnknownSourceClass)", "(unknownSourceMethod)", msg);
		}
	}

	/**
	 * Log a FINER message, specifying source class and method, with no
	 * arguments.
	 * <p>
	 * If the logger is currently enabled for the given message level then the
	 * given message is forwarded to all the registered output Handler objects.
	 * <p>
	 *
	 * @param msg The string message (or a key in the message catalog)
	 */
	public void finer (String msg) {
		StackTraceElement caller = StackTraceUtils.getCallerStackTraceElement ();
		if (caller != null) {
			logger.logp (Level.FINER, caller.getClassName (), caller.getMethodName () + "():" + caller.getLineNumber (), msg);
		} else {
			logger.logp (Level.FINER, "(UnknownSourceClass)", "(unknownSourceMethod)", msg);
		}
	}

	/**
	 * Log a FINEST message, specifying source class and method, with no
	 * arguments.
	 * <p>
	 * If the logger is currently enabled for the given message level then the
	 * given message is forwarded to all the registered output Handler objects.
	 * <p>
	 *
	 * @param msg The string message (or a key in the message catalog)
	 */
	public void finest (String msg) {
		StackTraceElement caller = StackTraceUtils.getCallerStackTraceElement ();
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
		StackTraceElement caller = StackTraceUtils.getCallerStackTraceElement ();

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
		StackTraceElement caller = StackTraceUtils.getCallerStackTraceElement ();

		logDomNode (msg, node, Level.FINER, caller);
	}

	/**
	 * Log a DOM node at a given logging level
	 *
	 * @param msg The message to show with the node, or null if no message
	 * needed
	 * @param node
	 * @param level
	 */
	public void logDomNode (String msg, Node node, Level level) {
		StackTraceElement caller = StackTraceUtils.getCallerStackTraceElement ();

		logDomNode (msg, node, level, caller);
	}

	/**
	 * Log a DOM node at a given logging level and a specified caller
	 *
	 * @param msg The message to show with the node, or null if no message
	 * needed
	 * @param node
	 * @param level
	 * @param caller The caller's stack trace element
	 * @see ru.dmerkushov.loghelper.StackTraceUtils#getMyStackTraceElement()
	 */
	public void logDomNode (String msg, Node node, Level level, StackTraceElement caller) {
		String toLog = (msg != null ? msg + "\n" : "DOM node:\n") + domNodeDescription (node, 0);

		if (caller != null) {
			logger.logp (level, caller.getClassName (), caller.getMethodName () + "():" + caller.getLineNumber (), toLog);
		} else {
			logger.logp (level, "(UnknownSourceClass)", "(unknownSourceMethod)", toLog);
		}
	}

	/**
	 * Form a DOM node textual representation recursively
	 *
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
	 *
	 * @param level
	 */
	public void setLevel (Level level) {

		this.defaultLevel = level;
		logger.setLevel (level);
		for (Handler handler : logger.getHandlers ()) {
			handler.setLevel (level);
		}
	}

	/**
	 *
	 * @param name
	 * @return
	 * @deprecated Use {@link LogHelper#getLoggerWrapper(java.lang.String) }
	 * instead
	 */
	public static LoggerWrapper getLoggerWrapper (String name) {
		return LogHelper.getLoggerWrapper (name);
	}

}
