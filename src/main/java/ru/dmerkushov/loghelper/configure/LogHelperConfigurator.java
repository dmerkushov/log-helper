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
package ru.dmerkushov.loghelper.configure;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ru.dmerkushov.loghelper.LogHelper;
import ru.dmerkushov.loghelper.LogHelperDebug;
import ru.dmerkushov.loghelper.LoggerWrapper;
import ru.dmerkushov.loghelper.configure.loggerwrapper.LoggerWrapperConfigurator;

/**
 *
 * @author Dmitriy Merkushov
 */
public class LogHelperConfigurator {

	/**
	 * The default filename for log-helper configuration, namely "loghelper-config.xml"
	 */
	public static final String DEFAULT_CONFIG_FILENAME = "loghelper-config.xml";

	/**
	 * Configure the log-helper library to the values in the default XML config file
	 * Config file name is set as a system property LogHelperConfig (via <code>-DLogHelperConfig=<i>filename</i></code>) or, if the property is not set, the config file name is set to {@link DEFAULT_CONFIG_FILENAME}
	 *
	 * @return <code>true</code> on success, <code>false</code> otherwise
	 *
	 * @see LogHelperConfigurator#DEFAULT_CONFIG_FILENAME
	 */
	public static boolean configure () {
		String configFilename = System.getProperty ("LogHelperConfig", DEFAULT_CONFIG_FILENAME);
		File configFile = new File (configFilename);

		boolean success = false;
		if (configFile.exists ()) {
			success = configure (configFile);
		} else {
			LogHelperDebug.printError ("File " + configFilename + " does not exist", false);
		}

		return success;
	}

	/**
	 * Configure the log-helper library to the values in the given XML config file
	 *
	 * @param configFile
	 * @return <code>true</code> on success, <code>false</code> otherwise
	 */
	public static boolean configure (File configFile) {
		boolean goOn = true;

		if (!configFile.exists ()) {
			LogHelperDebug.printError ("File " + configFile.getAbsolutePath () + " does not exist", false);
			goOn = false;
		}

		DocumentBuilderFactory documentBuilderFactory = null;
		DocumentBuilder documentBuilder = null;
		if (goOn) {
			documentBuilderFactory = DocumentBuilderFactory.newInstance ();

			try {
				documentBuilder = documentBuilderFactory.newDocumentBuilder ();
			} catch (ParserConfigurationException ex) {
				LogHelperDebug.printError (ex.getMessage (), ex, false);
				goOn = false;
			}
		}

		Document configDocument = null;
		if (goOn) {
			try {
				configDocument = documentBuilder.parse (configFile);
			} catch (SAXException ex) {
				LogHelperDebug.printError (ex.getMessage (), ex, false);
				goOn = false;
			} catch (IOException ex) {
				LogHelperDebug.printError (ex.getMessage (), ex, false);
				goOn = false;
			}
		}

		NodeList configNodeList = null;
		if (goOn) {
			configNodeList = configDocument.getElementsByTagName ("log-helper");
			if (configNodeList == null) {
				LogHelperDebug.printError ("configNodeList is null", false);
				goOn = false;
			} else if (configNodeList.getLength () < 1) {
				LogHelperDebug.printError ("configNodeList is empty", false);
				goOn = false;
			}
		}

		Node configNode = null;
		if (goOn) {
			configNode = configNodeList.item (0);
			if (configNode == null) {
				LogHelperDebug.printError ("configNode is null", false);
				goOn = false;
			}
		}

		boolean success = false;
		if (goOn) {
			success = configure (configNode);
		}

		return success;
	}

	/**
	 * Configure the log-helper library to the values of a DOM node. Details are described in the {@link ru.dmerkushov.loghelper.configure.LogHelperConfigurator LogHelperConfigurator class Javadoc}.
	 *
	 * @param configNode
	 * @return always <code>true</code> (means the call is ever succesful)
	 */
	public static boolean configure (Node configNode) {
		NodeList configSubnodes = configNode.getChildNodes ();

		for (int subnodeIndex = 0; subnodeIndex < configSubnodes.getLength (); subnodeIndex++) {
			Node subnode = configSubnodes.item (subnodeIndex);

			String subnodeName = subnode.getNodeName ().trim ().toLowerCase ();

			if (subnodeName.equals ("debug")) {
				if (subnode.getTextContent ().trim ().toLowerCase ().equals ("true")) {
					LogHelperDebug.setDebugEnabled (true);
				} else {
					LogHelperDebug.setDebugEnabled (false);
				}
			} else if (subnodeName.equals ("plugin")) {
				configurePlugin (subnode);
			} else if (subnodeName.equals ("jul")) {
				configureJul (subnode);
			} else if (subnodeName.equals ("loggerwrapper")) {
				configureLoggerWrapper (subnode);
			}
		}

		return true;
	}

	/**
	 * Configure a LoggerWrapper to the values of a DOM node. It MUST have an attribute named "name", storing the LoggerWrapper's name. It may have subnodes named "configurator", keeping the properties for
	 *
	 * @param configNode
	 */
	private static void configureLoggerWrapper (Node configNode) {
		Node lwNameNode = configNode.getAttributes ().getNamedItem ("name");
		String lwName;
		if (lwNameNode != null) {
			lwName = lwNameNode.getTextContent ();
		} else {
			lwName = "LoggerWrapper_" + String.valueOf ((new Date ()).getTime ());
		}

		LoggerWrapper loggerWrapper = LogHelper.getLoggerWrapper (lwName);

		NodeList lwSubnodes = configNode.getChildNodes ();
		for (int subnodeIndex = 0; subnodeIndex < lwSubnodes.getLength (); subnodeIndex++) {
			Node subnode = lwSubnodes.item (subnodeIndex);
			String subnodeName = subnode.getNodeName ().trim ().toLowerCase ();

			if (subnodeName.equals ("configurator")) {
				configureLoggerWrapperByConfigurator (loggerWrapper, subnode);
			}
		}
	}

	private static void configureLoggerWrapperByConfigurator (LoggerWrapper loggerWrapper, Node configNode) {
		boolean goOn = true;

		Node classNameNode = configNode.getAttributes ().getNamedItem ("class-name");
		String className = "(unknown)";
		if (goOn) {
			if (classNameNode != null) {
				className = classNameNode.getTextContent ().trim ();
			} else {
				Exception ex = new Exception ("No meaningful class name node for LoggerWrapper " + loggerWrapper.getName ());
				LogHelperDebug.printError (ex.getMessage (), ex, true);
				goOn = false;
			}
		}

		NodeList configSubnodes = configNode.getChildNodes ();
		Node configuration = null;
		if (goOn) {
			for (int subnodeIndex = 0; subnodeIndex < configSubnodes.getLength (); subnodeIndex++) {
				Node subnode = configSubnodes.item (subnodeIndex);
				String subnodeName = subnode.getNodeName ().trim ().toLowerCase ();

				if (subnodeName.equals ("configuration")) {
					configuration = subnode;
					break;
				}
			}
			if (configuration == null) {
				LogHelperDebug.printError ("No configuration node for configurator " + className, false);
				goOn = false;
			}
		}

		LoggerWrapperConfigurator configurator = null;
		if (goOn) {
			configurator = LoggerWrapperConfigurator.getInstance (className, loggerWrapper, configuration);
			if (configurator == null) {
				LogHelperDebug.printError ("No configurator instance returned for " + className, false);
				goOn = false;
			}
		}

		if (goOn) {
			boolean configuratorResult = configurator.configure ();
			LogHelperDebug.printMessage ("Has LoggerWrapper configurator " + className + " exited successfully? - " + configuratorResult, false);
		}
	}

	private static void configurePlugin (Node configNode) {
		boolean goOn = true;

		Node pluginConfiguratorClassNameNode = configNode.getAttributes ().getNamedItem ("plugin-configurator-class");
		if (pluginConfiguratorClassNameNode == null) {
			LogHelperDebug.printError ("No plugin-configurator-class attribute in plugin node", false);
			goOn = false;
		}

		String className = null;
		if (goOn) {
			className = pluginConfiguratorClassNameNode.getTextContent ();
		}
		if (className == null) {
			LogHelperDebug.printError ("Nothing in plugin-configurator-class attribute in plugin node", false);
			goOn = false;
		}

		Node pluginConfiguration = null;
		NodeList configSubnodes = configNode.getChildNodes ();
		for (int subnodeIndex = 0; subnodeIndex < configSubnodes.getLength (); subnodeIndex++) {
			Node subnode = configSubnodes.item (subnodeIndex);
			String subnodeName = subnode.getNodeName ();

			if ("plugin-configuration".equals (subnodeName)) {
				pluginConfiguration = subnode;
				break;
			}
		}
		if (pluginConfiguration == null) {
			LogHelperDebug.printError ("plugin-configuration node not found for plugin configurator " + className, false);
			goOn = false;
		}

		PluginConfigurator pluginConfigurator = null;
		if (goOn) {
			pluginConfigurator = PluginConfigurator.getInstance (className);
		}
		if (pluginConfigurator == null) {
			LogHelperDebug.printError ("plginConfigurator " + className + " is null", false);
			goOn = false;
		}

		boolean success = false;
		if (goOn) {
			pluginConfigurator.setConfiguration (pluginConfiguration);
			success = pluginConfigurator.configurePlugin ();
		}
		
		LogHelperDebug.printMessage ("Plugin configurator run successfully? - " + success, false);
	}

	private static void configureJul (Node configNode) {
		String julConfigFilename = configNode.getAttributes ().getNamedItem ("filename").getTextContent ();

		FileInputStream julConfigFis = null;
		try {
			julConfigFis = new FileInputStream (julConfigFilename);
		} catch (FileNotFoundException ex) {
			LogHelperDebug.printError ("When trying to configure JUL", ex, false);
		}

		if (julConfigFis != null) {
			try {
				java.util.logging.LogManager.getLogManager ().readConfiguration (julConfigFis);
			} catch (IOException ex) {
				LogHelperDebug.printError ("When trying to configure JUL", ex, false);
			} catch (SecurityException ex) {
				LogHelperDebug.printError ("When trying to configure JUL", ex, false);
			} finally {
				try {
					julConfigFis.close ();
				} catch (IOException ex) {
					LogHelperDebug.printError ("When trying to close julConfigFis", ex, false);
				}
			}
		}
	}

}
