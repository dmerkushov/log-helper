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

import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Logger;
import org.w3c.dom.Node;
import ru.dmerkushov.loghelper.LogHelperDebug;
import ru.dmerkushov.loghelper.LoggerWrapper;
import ru.dmerkushov.loghelper.handler.SizeRollingFileHandler;

/**
 * Configures LoggerWrapper to use only SizeRollingFileHandler
 *
 * @author Dmitriy Merkushov
 */
public class DefaultSizeRollingConfigurator extends LoggerWrapperConfigurator {

	/**
	 * Create a DefaultSizeRollingConfigurator instance.
	 * The supplied configuration options may contain the following properties:
	 * <ul>
	 * <li><code>ru.dmerkushov.loghelper.SizeRollingFileHandler.pattern</code> set to the desired SizeRollingFileHandler log file name pattern.</li>
	 * <li><code>ru.dmerkushov.loghelper.SizeRollingFileHandler.maxLogSize</code> set to the desired SizeRollingFileHandler maximum log size (decimal number). By default, the size is set in bytes, but the value may end with "K" to indicate that the size is set in KiB, "M" (MiB), "G" (GiB), or "T" (TiB).</li>
	 * </ul>
	 * If the configuration options do not contain any of the options, SizeRollingFileHandler's defaults are used.
	 * 
	 * @param loggerWrapper
	 * @param configuration 
	 * @see LoggerWrapperConfigurator#LoggerWrapperConfigurator(ru.dmerkushov.loghelper.LoggerWrapper, org.w3c.dom.Node)
	 */
	public DefaultSizeRollingConfigurator (LoggerWrapper loggerWrapper, Node configuration) {
		super (loggerWrapper, configuration);
	}

	/**
	 * Configures LoggerWrapper
	 *
	 * @return always <code>true</code> (means the call is ever succesful)
	 * @see SizeRollingFileHandler#SizeRollingFileHandler(java.lang.String, long)
	 */
	public boolean configure () {
		Logger logger = loggerWrapper.getLogger ();
		Handler[] handlers = logger.getHandlers ();

		for (Handler handler : handlers) {
			logger.removeHandler (handler);
		}

		String pattern = this.getConfigurationOptionValue ("ru.dmerkushov.loghelper.SizeRollingFileHandler.pattern", SizeRollingFileHandler.DEFAULT_LOG_FILENAME_PATTERN);
		String maxLogSizeStr = this.getConfigurationOptionValue ("ru.dmerkushov.loghelper.SizeRollingFileHandler.maxLogSize", String.valueOf (SizeRollingFileHandler.DEFAULT_LOG_SIZE_BOUND));

		long maxLogSize = parseMaxLogSize (maxLogSizeStr);

		SizeRollingFileHandler srfh = new SizeRollingFileHandler (pattern, maxLogSize);

		logger.addHandler (srfh);
		
		return true;
	}

	/**
	 * Parse the maxLogSize parameter to a <code>long</code>. Defaults to {@link SizeRollingFileHandler#DEFAULT_LOG_SIZE_BOUND}
	 * @param maxLogSizeStr
	 * @return 
	 */
	long parseMaxLogSize (String maxLogSizeStr) {
		long maxLogSize = SizeRollingFileHandler.DEFAULT_LOG_SIZE_BOUND;

		if (maxLogSizeStr != null) {
			maxLogSizeStr = maxLogSizeStr.trim ().toUpperCase ();
			
			if (maxLogSizeStr.endsWith ("T")) {
				maxLogSizeStr = maxLogSizeStr.substring (0, maxLogSizeStr.length () - 1);
				try {
					maxLogSize = Long.parseLong (maxLogSizeStr) * 1024L * 1024L * 1024L * 1024L;
				} catch (NumberFormatException ex) {
					LogHelperDebug.printError ("Could not parse a long in " + maxLogSizeStr, ex, false);
				}
			} else if (maxLogSizeStr.endsWith ("G")) {
				maxLogSizeStr = maxLogSizeStr.substring (0, maxLogSizeStr.length () - 1);
				try {
					maxLogSize = Long.parseLong (maxLogSizeStr) * 1024L * 1024L * 1024L;
				} catch (NumberFormatException ex) {
					LogHelperDebug.printError ("Could not parse a long in " + maxLogSizeStr, ex, false);
				}
			} else if (maxLogSizeStr.endsWith ("M")) {
				maxLogSizeStr = maxLogSizeStr.substring (0, maxLogSizeStr.length () - 1);
				try {
					maxLogSize = Long.parseLong (maxLogSizeStr) * 1024L * 1024L;
				} catch (NumberFormatException ex) {
					LogHelperDebug.printError ("Could not parse a long in " + maxLogSizeStr, ex, false);
				}
			} else if (maxLogSizeStr.endsWith ("K")) {
				maxLogSizeStr = maxLogSizeStr.substring (0, maxLogSizeStr.length () - 1);
				try {
					maxLogSize = Long.parseLong (maxLogSizeStr) * 1024L;
				} catch (NumberFormatException ex) {
					LogHelperDebug.printError ("Could not parse a long in " + maxLogSizeStr, ex, false);
				}
			} else {
				try {
					maxLogSize = Long.parseLong (maxLogSizeStr);
				} catch (NumberFormatException ex) {
					LogHelperDebug.printError ("Could not parse a long in " + maxLogSizeStr, ex, false);
				}
			}
		}
		return maxLogSize;
	}

}
