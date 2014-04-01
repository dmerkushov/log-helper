/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
