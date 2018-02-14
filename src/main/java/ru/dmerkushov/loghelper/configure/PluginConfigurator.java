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
package ru.dmerkushov.loghelper.configure;

import org.w3c.dom.Node;
import ru.dmerkushov.loghelper.LogHelperDebug;
import ru.dmerkushov.loghelper.LoggerWrapper;

/**
 *
 *@author Dmitriy Merkushov
 */
public abstract class PluginConfigurator {
	
	/**
	 * The default constructor does nothing
	 */
	public PluginConfigurator () {}

	/**
	 * Get a configurator instance with a specified class name
	 *
	 * @param className
	 * @return
	 */
	public static PluginConfigurator getInstance (String className) {
		boolean goOn = true;

		Class<? extends PluginConfigurator> configuratorClass = null;
		if (goOn) {
			try {
				configuratorClass = (Class<PluginConfigurator>) Class.forName (className);
			} catch (ClassNotFoundException ex) {
				LogHelperDebug.printError ("Class not found: " + className, ex, false);
				goOn = false;
			} catch (ClassCastException ex) {
				LogHelperDebug.printError ("Class " + className + " is found, but probably is not a subclass of ru.dmerkushov.loghelper.configure.loggerwrapper.LoggerWrapperConfigurator", ex, false);
				goOn = false;
			}
		}
		if (configuratorClass == null) {
			LogHelperDebug.printError ("Class " + className + " is null", false);
			goOn = false;
		}

		PluginConfigurator configurator = null;
		if (goOn) {
			try {
				configurator = configuratorClass.newInstance ();
			} catch (InstantiationException ex) {
				LogHelperDebug.printError ("Exception when instantiating " + className, ex, false);
			} catch (IllegalAccessException ex) {
				LogHelperDebug.printError ("Exception when instantiating " + className, ex, false);
			}
		}

		return configurator;
	}
	
	/**
	 * Set the configuration node for the plugin
	 * @param configuration 
	 */
	public abstract void setConfiguration (Node configuration);
	
	/**
	 * Configure the plugin
	 * @return <code>true</code> on success, <code>false</code> otherwise
	 */
	public abstract boolean configurePlugin ();

}
