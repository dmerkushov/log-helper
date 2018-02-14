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

import java.util.HashMap;

/**
 *
 * @author Dmitriy Merkushov
 */
public class LogHelper {

	final static HashMap<String, LoggerWrapper> loggerWrappers = new HashMap<String, LoggerWrapper> ();

	public static void registerLoggerWrapper (LoggerWrapper loggerWrapper) {
		synchronized (loggerWrappers) {
			loggerWrappers.put (loggerWrapper.getName (), loggerWrapper);
		}
	}

	public static LoggerWrapper getLoggerWrapper (String name) {
		LoggerWrapper loggerWrapper = loggerWrappers.get (name);
		if (loggerWrapper == null) {
			synchronized (loggerWrappers) {
				loggerWrapper = new LoggerWrapper (name);
				registerLoggerWrapper (loggerWrapper);
			}
		}

		return loggerWrapper;
	}

}
