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

import org.w3c.dom.Node;
import ru.dmerkushov.loghelper.LoggerWrapper;

/**
 *
 * @author Dmitriy Merkushov
 */
public abstract class WrappingConfigurator extends LoggerWrapperConfigurator {
	
	LoggerWrapperConfigurator wrappedConfigurator;

	public WrappingConfigurator (LoggerWrapper loggerWrapper, Node configuration) {
		super (loggerWrapper, configuration);
		
		String wrappedConfiguratorClassName = super.getConfigurationOptionValue ("ru.dmerkushov.loghelper.configure.loggerwrapper.WrappingConfigurator.wrappedConfiguratorClassName", "ru.dmerkushov.loghelper.configure.loggerwrapper.DefaultConsoleConfigurator");
		Node wrappedConfiguration = super.getConfigurationOption ("ru.dmerkushov.loghelper.configure.loggerwrapper.WrappingConfigurator.wrappedConfiguration");
		
		wrappedConfigurator = LoggerWrapperConfigurator.getInstance (wrappedConfiguratorClassName, loggerWrapper, wrappedConfiguration);
	}
	
	/**
	 * Get the wrapped LoggerWrapperConfigurator
	 * @return 
	 */
	public LoggerWrapperConfigurator getWrappedConfigurator () {
		return wrappedConfigurator;
	}

}
