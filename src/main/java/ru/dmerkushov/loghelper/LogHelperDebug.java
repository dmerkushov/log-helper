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
 *
 * @author Dmitriy Merkushov
 */
public class LogHelperDebug {
	
	static boolean debugEnabled = false;
	
	public static synchronized void setDebugEnabled (boolean debugEnabled) {
		LogHelperDebug.debugEnabled = debugEnabled;
	}

	public static boolean isDebugEnabled () {
		return debugEnabled;
	}
	
	public static void printMessage (String message) {
		if (isDebugEnabled ()) {
			String toOutput = "log-helper DEBUG: " + message.replaceAll ("\n", "\nlog-helper DEBUG: ");
			System.out.println (toOutput);
		}
	}
	
	public static void printError (String message) {
		if (isDebugEnabled ()) {
			String toOutput = "log-helper ERROR: " + message.replaceAll ("\n", "\nlog-helper ERROR: ");
			System.err.println (toOutput);
		}
	}
	
	public static void printError (String message, Throwable throwable) {
		if (isDebugEnabled ()) {
			StringBuilder outputBuilder = new StringBuilder ();
			outputBuilder.append (message).append ("\nThrowable:\n");
			outputBuilder.append (LoggerFormatter.getFullThrowableMsg (throwable));
			String fullMessage = outputBuilder.toString ();
			printError (fullMessage);
		}
	}
}
