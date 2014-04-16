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
package ru.dmerkushov.loghelper.configure.loggerwrapper;

import java.io.IOException;
import org.w3c.dom.Node;
import ru.dmerkushov.loghelper.LogHelperDebug;
import ru.dmerkushov.loghelper.LoggerWrapper;
import ru.dmerkushov.loghelper.formatter.DefaultFormatter;
import ru.dmerkushov.loghelper.handler.DailyRollingFileHandler;

/**
 * Configures LoggerWrapper to use only DailyRollingFileHandler
 *
 * @author Dmitriy Merkushov
 */
public class DefaultDailyRollingConfigurator extends LoggerWrapperConfigurator {

	public DefaultDailyRollingConfigurator (LoggerWrapper loggerWrapper, Node configuration) {
		super (loggerWrapper, configuration);
	}

	/**
	 * Configures LoggerWrapper to use only DailyRollingFileHandler.
	 * The supplied configuration options may contain the following properties:
	 * <ul>
	 * <li><code>ru.dmerkushov.loghelper.SizeRollingFileHandler.pattern</code> set to the desired DailyRollingFileHandler log file name pattern.</li>
	 * </ul>
	 * If the configuration options do not contain any of the options, SizeRollingFileHandler's default are used.
	 *
	 * @return always <code>true</code> (means the call is ever succesful)
	 *
	 * @see DailyRollingFileHandler#DailyRollingFileHandler(java.lang.String)
	 * @see DailyRollingFileHandler#DailyRollingFileHandler()
	 */
	@Override
	public boolean configure () {
		loggerWrapper.removeAllLoggerHandlers ();

		String pattern = this.getConfigurationOptionValue ("ru.dmerkushov.loghelper.DailyRollingFileHandler.pattern", DailyRollingFileHandler.DEFAULT_LOG_FILENAME_PATTERN);

		DailyRollingFileHandler drfh = null;

		boolean success = true;

		try {
			drfh = new DailyRollingFileHandler (pattern);
		} catch (IllegalArgumentException ex) {
			LogHelperDebug.printError ("Could not create DailyRollingFileHandler with pattern " + pattern, ex, false);
			success = false;
		} catch (IOException ex) {
			LogHelperDebug.printError ("Could not create DailyRollingFileHandler with pattern " + pattern, ex, false);
			success = false;
		}

		drfh.setFormatter (new DefaultFormatter ());

		if (drfh != null) {
			loggerWrapper.addLoggerHandler (drfh);
		} else {
			LogHelperDebug.printError ("The created DailyRollingFileHandler is null with pattern " + pattern, false);
			success = false;
		}

		return success;
	}

}
