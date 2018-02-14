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

/**
 *
 * @author Dmitriy Merkushov
 */
public class StackTraceUtils {

	/**
	 * Get the StackTraceElement of the method that called the caller of getCallerStackTraceElement ()
	 * @return may be null
	 */
	public static StackTraceElement getCallerStackTraceElement () {
		StackTraceElement callerStackTraceElement = null;

		StackTraceElement[] stackTraceElements = Thread.currentThread ().getStackTrace ();
		if (stackTraceElements != null) {
			if (stackTraceElements.length > 3) {
				callerStackTraceElement = stackTraceElements[3];
			}
		}

		return callerStackTraceElement;
	}
	
	/**
	 * Get the StackTraceElement of the caller of getMyStackTraceElement ()
	 * @return 
	 */
	public static StackTraceElement getMyStackTraceElement () {
		StackTraceElement myStackTraceElement = null;
		
		StackTraceElement[] stackTraceElements = Thread.currentThread ().getStackTrace ();
		if (stackTraceElements != null) {
			if (stackTraceElements.length > 2) {
				myStackTraceElement = stackTraceElements[2];
			}
		}

		return myStackTraceElement;
	}

}
