/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.dmerkushov.loghelper;

import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 *
 * @author Dmitriy Merkushov
 */
public class LoggerFormatter extends Formatter {

	static SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.S Z");

	@Override
	public String format (LogRecord record) {
		StringBuilder resultBuilder = new StringBuilder ();
		String prefix = record.getLevel ().getName () + "\t";

		resultBuilder.append (prefix);

		resultBuilder.append (Thread.currentThread ().getName ())
				.append (" ")
				.append (sdf.format (new java.util.Date (record.getMillis ())))
				.append (": ")
				.append (record.getSourceClassName ())
				.append (":")
				.append (record.getSourceMethodName ())
				.append ("(): ")
				.append (record.getMessage ());

		Object[] params = record.getParameters ();
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

		Throwable t = record.getThrown ();
		if (t != null) {
			resultBuilder.append ("\nThrowing:\n")
					.append (getFullThrowableMsg (t));
		}

		String result = resultBuilder.toString ().replaceAll ("\n", "\n" + prefix) + "\n";

		return result;
	}

	public static String getFullThrowableMsg (Throwable t) {
		StringBuilder resultBuilder = new StringBuilder ();
		resultBuilder.append (t.getClass ().getCanonicalName ())
				.append (": ")
				.append (t.getMessage ());

		StackTraceElement[] stackTraceElements = t.getStackTrace ();

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

		Throwable cause = t.getCause ();

		if (cause != null) {
			resultBuilder.append ("\nCaused by:\n")
					.append (getFullThrowableMsg (cause));
		}

		String fullThrowableMsg = resultBuilder.toString ();
		return fullThrowableMsg;
	}
}
