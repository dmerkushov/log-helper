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
package ru.dmerkushov.loghelper.configure.loggerwrapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ru.dmerkushov.loghelper.LogHelperDebug;
import ru.dmerkushov.loghelper.LoggerWrapper;

/**
 *
 *@author Dmitriy Merkushov
 */
public abstract class LoggerWrapperConfigurator {
	
	LoggerWrapper loggerWrapper;
	Node configuration;
	
	/**
	 * 
	 * @param loggerWrapper
	 * @param configuration
	 * @throws NullPointerException if any of the parameters is null
	 */
	public LoggerWrapperConfigurator (LoggerWrapper loggerWrapper, Node configuration) throws NullPointerException {
		if (loggerWrapper == null) {
			throw new NullPointerException ("loggerWrapper");
		}
		if (configuration == null) {
			throw new NullPointerException ("configuration");
		}
		
		this.loggerWrapper = loggerWrapper;
		this.configuration = configuration;
	}
	
	/**
	 * Get the LoggerWrapper instance attached to this configurator
	 * @return 
	 */
	public LoggerWrapper getLoggerWrapper () {
		return loggerWrapper;
	}
	
	/**
	 * Get the configuration options of this configurator
	 * @return 
	 */
	public Node getConfiguration () {
		return configuration;
	}
	
	/**
	 * Set the configuration options for this configurator
	 * @param configuration
	 */
	public void setConfiguration (Node configuration) {
		this.configuration = configuration;
	}
	
	/**
	 * Get a configuration option of this configurator.
	 * @param optionName
	 * @return The configuration option node. <code>null</code> if it does not exist
	 */
	public Node getConfigurationOption (String optionName) {
		NodeList children = configuration.getChildNodes ();
		Node configurationOption = null;
		for (int childIndex = 0; childIndex < children.getLength (); childIndex++) {
			if (children.item (childIndex).getNodeName ().equals (optionName)) {
				configurationOption = children.item (childIndex);
				break;
			}
		}
		return configurationOption;
	}
	
	/**
	 * Get a configuration option value as String.
	 * @param optionName
	 * @param defaultValue
	 * @return The configuration option node value, or <code>defaultValue</code> if it does not exist
	 */
	public String getConfigurationOptionValue (String optionName, String defaultValue) {
		String optionValue;
		Node configurationOption = this.getConfigurationOption (optionName);
		if (configurationOption != null) {
			optionValue = configurationOption.getTextContent ();
		} else {
			optionValue = defaultValue;
		}
		return optionValue;
	}
	
	/**
	 * Get a configurator instance with a specified class name
	 * @param className
	 * @param loggerWrapper
	 * @param configuration
	 * @return 
	 */
	public static LoggerWrapperConfigurator getInstance (String className, LoggerWrapper loggerWrapper, Node configuration) {
		boolean goOn = true;
		
		Class<? extends LoggerWrapperConfigurator> configuratorClass = null;
		if (goOn) {
			try {
				configuratorClass = (Class<LoggerWrapperConfigurator>) Class.forName (className);
			} catch (ClassNotFoundException ex) {
				LogHelperDebug.printError ("Class not found: " + className, ex, false);
				goOn = false;
			} catch (ClassCastException ex) {
				LogHelperDebug.printError ("Class " + className + " is found, but probably is not a subclass of ru.dmerkushov.loghelper.configure.loggerwrapper.LoggerWrapperConfigurator", ex, false);
				goOn = false;
			}
		}

		Constructor<? extends LoggerWrapperConfigurator> configuratorConstructor = null;
		if (goOn) {
			try {
				configuratorConstructor = configuratorClass.getConstructor (LoggerWrapper.class, Node.class);
			} catch (NoSuchMethodException ex) {
				LogHelperDebug.printError ("Could not find the needed constructor (LoggerWrapper, Node) in " + className, ex, false);
				goOn = false;
			} catch (SecurityException ex) {
				LogHelperDebug.printError (ex.getMessage (), ex, false);
				goOn = false;
			}
		}

		LoggerWrapperConfigurator configurator = null;
		if (goOn) {
			try {
				configurator = configuratorConstructor.newInstance (loggerWrapper, configuration);
			} catch (InstantiationException ex) {
				LogHelperDebug.printError (ex.getMessage (), ex, false);
			} catch (IllegalAccessException ex) {
				LogHelperDebug.printError (ex.getMessage (), ex, false);
			} catch (IllegalArgumentException ex) {
				LogHelperDebug.printError (ex.getMessage (), ex, false);
			} catch (InvocationTargetException ex) {
				LogHelperDebug.printError (ex.getMessage (), ex, false);
			}
		}

		return configurator;
	}

	/**
	 * Configure the LoggerWrapper for the current configuration options
	 * @return <code>true</code> on success, <code>false</code> otherwise
	 */
	public abstract boolean configure ();

}
