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
				.append ("(): ")
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
					.append (LoggerWrapper.getFullThrowableMsg (t));
		}

		String result = resultBuilder.toString ().replaceAll ("\n", "\n" + prefix) + "\n";

		return result;
	}
}
