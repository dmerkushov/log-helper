/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.dmerkushov.loghelper.formatter;

import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 *
 * @author Dmitriy Merkushov
 */
public class DefaultFormatter extends Formatter {

	static SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.S Z");

	/**
	 * Format a logging message
	 *
	 * @param logRecord
	 * @return
	 */
	@Override
	public String format (LogRecord logRecord) {
		StringBuilder resultBuilder = new StringBuilder ();
		String prefix = logRecord.getLevel ().getName () + "\t";

		resultBuilder.append (prefix);

		resultBuilder.append (Thread.currentThread ().getName ())
				.append (" ")
				.append (sdf.format (new java.util.Date (logRecord.getMillis ())))
				.append (": ")
				.append (logRecord.getSourceClassName ())
				.append (":")
				.append (logRecord.getSourceMethodName ())
				.append (": ")
				.append (logRecord.getMessage ());

		Object[] params = logRecord.getParameters ();
		if (params != null) {
			resultBuilder.append ("\nParameters:");

			if (params.length < 1) {
				resultBuilder.append (" (none)");
			} else {
				for (int paramIndex = 0; paramIndex < params.length; paramIndex++) {
					Object param = params[paramIndex];
					if (param != null) {
						String paramString = param.toString ();

						resultBuilder.append ("\nParameter ")
								.append (String.valueOf (paramIndex))
								.append (" is a ")
								.append (param.getClass ().getName ())
								.append (": >")
								.append (paramString)
								.append ("<");
					} else {
						resultBuilder.append ("\nParameter ")
								.append (String.valueOf (paramIndex))
								.append (" is null.");
					}
				}
			}
		}

		Throwable t = logRecord.getThrown ();
		if (t != null) {
			resultBuilder.append ("\nThrowing:\n")
					.append (getFullThrowableMsg (t));
		}
		
		String result = resultBuilder.toString ().replaceAll ("\n", "\n" + prefix) + "\n";

		return result;
	}

	/**
	 * Get a full message of a throwable: its message, stack trace, and causes
	 * (other Throwables, also described recursively)
	 *
	 * @param throwable
	 * @return
	 */
	public static String getFullThrowableMsg (Throwable throwable) {
		StringBuilder resultBuilder = new StringBuilder ();
		resultBuilder.append (throwable.getClass ().getCanonicalName ())
				.append (": ")
				.append (throwable.getMessage ());

		StackTraceElement[] stackTraceElements = throwable.getStackTrace ();

		if (stackTraceElements != null) {
			if (stackTraceElements.length > 0) {
				resultBuilder.append ("\nStack Trace:\n");

				StackTraceElement stackTraceElement = stackTraceElements[0];
				resultBuilder.append ("\t")
						.append (stackTraceElement.getClassName ())
						.append (":")
						.append (stackTraceElement.getMethodName ())
						.append ("():")
						.append (stackTraceElement.getLineNumber ());

				for (int i = 1; i < stackTraceElements.length; i++) {
					stackTraceElement = stackTraceElements[i];
					resultBuilder.append ("\n\tat ")
							.append (stackTraceElement.getClassName ())
							.append (":")
							.append (stackTraceElement.getMethodName ())
							.append ("():")
							.append (stackTraceElement.getLineNumber ());
				}
			}
		}

		Throwable cause = throwable.getCause ();

		if (cause != null) {
			resultBuilder.append ("\nCaused by:\n")
					.append (getFullThrowableMsg (cause));
		}

		String fullThrowableMsg = resultBuilder.toString ();
		return fullThrowableMsg;
	}
}
