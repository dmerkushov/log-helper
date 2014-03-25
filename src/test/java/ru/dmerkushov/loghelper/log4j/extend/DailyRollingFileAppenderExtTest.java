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
package ru.dmerkushov.loghelper.log4j.extend;

import java.io.IOException;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author dmerkushov
 */
public class DailyRollingFileAppenderExtTest {

	public DailyRollingFileAppenderExtTest () {
	}

	@Before
	public void setUp () {
	}

	@After
	public void tearDown () {
	}

	/**
	 * Test of getKeepOldLogsSeconds method, of class DailyRollingFileAppenderExt.
	 */
	@Test
	public void test () throws InterruptedException {

		DOMConfigurator.configure ("example/log4j.xml");
		org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger (this.getClass ().getCanonicalName ());
		
		int statements = 500;

		for (int i = 0; i < statements; i++) {
			logger.fatal ("Fatal message " + i, new IOException ("Fatal IOException " + i));
			System.out.println ("Statement " + i + " of " + statements + " written down. Sleeping...");
			Thread.sleep (2000l);
		}

		System.out.println ("Over");

	}
}
