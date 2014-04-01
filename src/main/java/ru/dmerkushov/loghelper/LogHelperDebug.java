/*
 * Copyright 2014 dmerkushov.
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

import ru.dmerkushov.loghelper.formatter.LoggerFormatter;

/**
 * This class should only be used by log-helper classes and theit derivatives.
 * It enables log-helper classes of outputting debug and error messages to System.out and System.err, respectively.
 * Note that since the size of the messages is not limited, a one should consider redirecting output when debugging log-helper.
 * @author Dmitriy Merkushov
 */
public class LogHelperDebug {
	
	static boolean debugEnabled = false;
	
	/**
	 * Set the debug enabled flag.
	 * @param debugEnabled 
	 */
	public static synchronized void setDebugEnabled (boolean debugEnabled) {
		LogHelperDebug.debugEnabled = debugEnabled;
	}

	/**
	 * Check the debug enabled flag
	 * @return 
	 */
	public static boolean isDebugEnabled () {
		return debugEnabled;
	}
	
	/**
	 * Print a message to {@link System.out}, with an every-line prefix: "log-helper DEBUG: "
	 * @param message
	 * @param force <code>true</code> if we need to override the debug enabled flag (i.e. the message is REALLY important), <code>false</code> otherwise
	 */
	public static void printMessage (String message, boolean force) {
		if (isDebugEnabled () || force) {
			String toOutput = "log-helper DEBUG: " + message.replaceAll ("\n", "\nlog-helper DEBUG: ");
			System.out.println (toOutput);
		}
	}
	
	/**
	 * Print a message to {@link System.err}, with an every-line prefix: "log-helper ERROR: "
	 * @param message
	 * @param force <code>true</code> if we need to override the debug enabled flag (i.e. the message is REALLY important), <code>false</code> otherwise
	 */
	public static void printError (String message, boolean force) {
		if (isDebugEnabled () || force) {
			String toOutput = "log-helper ERROR: " + message.replaceAll ("\n", "\nlog-helper ERROR: ");
			System.err.println (toOutput);
		}
	}
	
	/**
	 * Print a message to {@link System.err}, with an every-line prefix: "log-helper ERROR: ", and specifying a full stack trace of a {@link java.lang.Throwable Throwable{
	 * @param message
	 * @param throwable
	 * @param force <code>true</code> if we need to override the debug enabled flag (i.e. the message is REALLY important), <code>false</code> otherwise
	 */
	public static void printError (String message, Throwable throwable, boolean force) {
		if (isDebugEnabled () || force) {
			StringBuilder outputBuilder = new StringBuilder ();
			outputBuilder.append (message).append ("\nThrowable:\n");
			outputBuilder.append (LoggerFormatter.getFullThrowableMsg (throwable));
			String fullMessage = outputBuilder.toString ();
			printError (fullMessage, force);
		}
	}
}
