/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.dmerkushov.loghelper;

/**
 *
 * @author Dmitriy Merkushov
 */
public class LogHelperUtil {

	public static StackTraceElement getCallerStackTraceElement () {
		StackTraceElement toReturn = null;

		StackTraceElement[] stackTraceElements = Thread.currentThread ().getStackTrace ();
		if (stackTraceElements != null) {
			if (stackTraceElements.length > 3) {
				toReturn = stackTraceElements[3];
			}
		}

		return toReturn;
	}

}
