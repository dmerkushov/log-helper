/*
 * Copyright 2014 Dmitriy Merkushov
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

/*
 * Based on CustodianDailyRollingFileAppender idea by Ryan Kimber
 * See http://blog.kimb3r.com/2008/07/improving-log4j-dailyrollingfileappende.html
 */

/*
 * Based on Apache Software Foundation's DailyRollingFileAppender from Log4j 1.2.17
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.dmerkushov.loghelper.log4j.extend;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Unfortunately, this class cannot extend {@link org.apache.log4j.DailyRollingFileAppender} itself due to member visibility issues. Still, it has the same API, with some additions.
 * 
 * @author Dmitriy Merkushov
 * @see ru.dmerkushov.loghelper.log4j.extend.DailyRollingFileAppenderExt#getKeepOldLogsSeconds() 
 * @see ru.dmerkushov.loghelper.log4j.extend.DailyRollingFileAppenderExt#setKeepOldLogsSeconds(long) 
 */
public class DailyRollingFileAppenderExt extends FileAppender {

//	============================================================================
//	============================================================================
//	The source that was (re)written
//	============================================================================
//	============================================================================

	/**
	 * Default value for the number of seconds, after which the old log files will be deleted. Value: 604800 = 7*24*60*60 seconds = 7 days
	 */
	public final static long KEEPOLDLOGSSECONDS_DEFAULT = 604800;

	/**
	 * Keep the old logs for the given number of seconds, and after that delete. Default is 604800, which is 7*24*60*60 seconds = 7 days. If keepOldLogsSeconds &lt;= 0 , will keep all the old logs.
	 */
	private long keepOldLogsSeconds = KEEPOLDLOGSSECONDS_DEFAULT;
	
	/**
	 * Get the number of seconds, after which the old log files will be deleted. "0" means "Keep old logs forever". Values less than 0 are not likely.
	 * @return 
	 */
	public long getKeepOldLogsSeconds () {
		return keepOldLogsSeconds;
	}

	/**
	 * Set the number of seconds, after which the old log files will be deleted. "0" means "Keep old logs forever". Values less than 0 are set to 0.
	 * Non-parseable String values are set to default (see {@link DailyRollingFileAppenderExt#KEEPOLDLOGSSECONDS_DEFAULT}).
	 * @param secondsToKeepOldLogs 
	 */
	public void setKeepOldLogsSeconds (long secondsToKeepOldLogs) {
		long keepOldLogsSeconds;
		try {
			keepOldLogsSeconds = secondsToKeepOldLogs;
			if (keepOldLogsSeconds < 0L) {
				keepOldLogsSeconds = 0L;
			}
		} catch (NumberFormatException ex) {
			keepOldLogsSeconds = KEEPOLDLOGSSECONDS_DEFAULT;
		}
		this.keepOldLogsSeconds = keepOldLogsSeconds;
	}

	/**
	 * The default constructor does nothing.
	 */
	public DailyRollingFileAppenderExt () {
	}

	/**
	 * Instantiate a <code>DailyRollingFileAppenderExt</code> and open the
	 * file designated by <code>filename</code>. The opened filename will
	 * become the output destination for this appender.
	 *
	 * @param layout
	 * @param filename
	 * @param datePattern
	 */
	public DailyRollingFileAppenderExt (Layout layout, String filename, String datePattern) throws IOException {
		super (layout, filename, true);
		this.datePattern = datePattern;
		activateOptions ();
	}

	/**
	 * Rollover the current file to a new file.
	 *
	 * @throws java.io.IOException
	 */
	public void rollover () throws IOException {

		// Compute filename, but only if datePattern is specified
		if (datePattern == null) {
			errorHandler.error ("Missing DatePattern option in rollover().");
			return;
		}

		String datedFilename = fileName + sdf.format (now);
		// It is too early to roll over because we are still within the
		// bounds of the current interval. Rollover will occur once the
		// next interval is reached.
		if (scheduledFilename.equals (datedFilename)) {
			return;
		}

		// close current file, and rename it to datedFilename
		this.closeFile ();

		File target = new File (scheduledFilename);
		if (target.exists ()) {
			target.delete ();
		}

		File file = new File (fileName);
		boolean result = file.renameTo (target);
		if (result) {
			LogLog.debug (fileName + " -> " + scheduledFilename);
		} else {
			LogLog.error ("Failed to rename [" + fileName + "] to [" + scheduledFilename + "].");
		}

		try {
			// This will also close the file. This is OK since multiple
			// close operations are safe.
			this.setFile (fileName, true, this.bufferedIO, this.bufferSize);
		} catch (IOException e) {
			errorHandler.error ("setFile(" + fileName + ", true) call failed.");
		}
		scheduledFilename = datedFilename;

		cleanup ();
	}

	/**
	 * Clean up the old log files by checking their modification time
	 *
	 * @throws IOException
	 */
	public void cleanup () throws IOException {
		LogFileFilter logFileFilter = new LogFileFilter ();
		File logParentDir = new File (this.getFile ()).getParentFile ();

		File[] logFiles = logParentDir.listFiles (logFileFilter);

		long cleanupTimeRazor = 0L;			// By default, keep all logs
		if (keepOldLogsSeconds > 0L) {
			cleanupTimeRazor = new Date ().getTime () - 1000L * keepOldLogsSeconds;
		}

		LogLog.debug ("Log files with modified time before: " + cleanupTimeRazor + " (millis since epoch) are to be deleted");
		LogLog.debug ("Log files:");
		for (File logFile : logFiles) {
			LogLog.debug (logFile.getName () + " - modified " + logFile.lastModified ());
		}

		for (File logFile : logFiles) {
			if (logFile.lastModified () < cleanupTimeRazor) {
				boolean deleteResult = logFile.delete ();
				if (deleteResult) {
					LogLog.debug ("Deleted " + logFile.getCanonicalPath ());
				} else {
					LogLog.error ("Failed to delete [" + logFile.getCanonicalPath () + "].");
				}
			}
		}
	}

	/**
	 * File filter to filter this appender's log files
	 */
	public class LogFileFilter implements FileFilter {

		String checkAgainstFilename;
		SimpleDateFormat sdf;

		/**
		 * Create a LogFileFilter instance for the current {@link DailyRollingFileAppenderExt} instance
		 */
		public LogFileFilter () {
			this (DailyRollingFileAppenderExt.this);
		}

		/**
		 * Create a LogFileFilter instance for a given {@link DailyRollingFileAppenderExt} instance
		 *
		 * @param drfae
		 */
		public LogFileFilter (DailyRollingFileAppenderExt drfae) {
			this (new File (drfae.getFile ()), drfae.getDatePattern ());
		}

		/**
		 * Create a LogFileFilter instance for a given filename and date pattern to check against
		 *
		 * @param checkAgainst Base name of the log file
		 * @param datePattern Date pattern
		 */
		public LogFileFilter (File checkAgainst, String datePattern) {
			this.checkAgainstFilename = checkAgainst.getName ();
			this.sdf = new SimpleDateFormat (datePattern);
		}

		public boolean accept (File pathname) {
			String checkFilename = pathname.getName ();

			boolean accept = false;

			if (!pathname.isDirectory () && checkFilename.startsWith (checkAgainstFilename)) {
				accept = true;

				String checkDatePattern = checkFilename.substring (checkAgainstFilename.length ());

				if (!checkDatePattern.equals ("")) {
					try {
						sdf.parse (checkDatePattern);
					} catch (ParseException ex) {
						// ParseException means that there is not a date at the end of the filename, so the file is not a log file
						accept = false;
					}
				}
			}

			return accept;
		}

	}

//	============================================================================
//	============================================================================
//	The original DailyRollingFileAppender source, with minimum changes
//	============================================================================
//	============================================================================
	// The code assumes that the following constants are in a increasing sequence.
	static final int TOP_OF_TROUBLE = -1;
	static final int TOP_OF_MINUTE = 0;
	static final int TOP_OF_HOUR = 1;
	static final int HALF_DAY = 2;
	static final int TOP_OF_DAY = 3;
	static final int TOP_OF_WEEK = 4;
	static final int TOP_OF_MONTH = 5;

	/**
	 * The date pattern. By default, the pattern is set to "'.'yyyy-MM-dd"
	 * meaning daily rollover.
	 */
	private String datePattern = "'.'yyyy-MM-dd";

	/**
	 * The log file will be renamed to the value of the
	 * scheduledFilename variable when the next interval is entered. For
	 * example, if the rollover period is one hour, the log file will be
	 * renamed to the value of "scheduledFilename" at the beginning of
	 * the next hour. *
	 * The precise time when a rollover occurs depends on logging
	 * activity.
	 */
	private String scheduledFilename;

	/**
	 * The next time we estimate a rollover should occur.
	 */
	private long nextCheck = System.currentTimeMillis () - 1;

	Date now = new Date ();

	SimpleDateFormat sdf;

	RollingCalendar rc = new RollingCalendar ();

	int checkPeriod = TOP_OF_TROUBLE;

	// The gmtTimeZone is used only in computeCheckPeriod() method.
	static final TimeZone gmtTimeZone = TimeZone.getTimeZone ("GMT");

	/**
	 * The <b>DatePattern</b> takes a string in the same format as
	 * expected by {@link SimpleDateFormat}. This options determines the
	 * rollover schedule.
	 */
	public void setDatePattern (String pattern) {
		datePattern = pattern;
	}

	/**
	 * Returns the value of the <b>DatePattern</b> option.
	 */
	public String getDatePattern () {
		return datePattern;
	}

	public void activateOptions () {
		super.activateOptions ();
		if (datePattern != null && fileName != null) {
			now.setTime (System.currentTimeMillis ());
			sdf = new SimpleDateFormat (datePattern);
			int type = computeCheckPeriod ();
			printPeriodicity (type);
			rc.setType (type);
			File file = new File (fileName);
			scheduledFilename = fileName + sdf.format (new Date (file.lastModified ()));

		} else {
			LogLog.error ("Either File or DatePattern options are not set for appender ["
					+ name + "].");
		}
	}

	void printPeriodicity (int type) {
		switch (type) {
			case TOP_OF_MINUTE:
				LogLog.debug ("Appender [" + name + "] to be rolled every minute.");
				break;
			case TOP_OF_HOUR:
				LogLog.debug ("Appender [" + name
						+ "] to be rolled on top of every hour.");
				break;
			case HALF_DAY:
				LogLog.debug ("Appender [" + name
						+ "] to be rolled at midday and midnight.");
				break;
			case TOP_OF_DAY:
				LogLog.debug ("Appender [" + name
						+ "] to be rolled at midnight.");
				break;
			case TOP_OF_WEEK:
				LogLog.debug ("Appender [" + name
						+ "] to be rolled at start of week.");
				break;
			case TOP_OF_MONTH:
				LogLog.debug ("Appender [" + name
						+ "] to be rolled at start of every month.");
				break;
			default:
				LogLog.warn ("Unknown periodicity for appender [" + name + "].");
		}
	}

	// This method computes the roll over period by looping over the
	// periods, starting with the shortest, and stopping when the r0 is
	// different from from r1, where r0 is the epoch formatted according
	// the datePattern (supplied by the user) and r1 is the
	// epoch+nextMillis(i) formatted according to datePattern. All date
	// formatting is done in GMT and not local format because the test
	// logic is based on comparisons relative to 1970-01-01 00:00:00
	// GMT (the epoch).
	int computeCheckPeriod () {
		RollingCalendar rollingCalendar = new RollingCalendar (gmtTimeZone, Locale.getDefault ());
		// set sate to 1970-01-01 00:00:00 GMT
		Date epoch = new Date (0);
		if (datePattern != null) {
			for (int i = TOP_OF_MINUTE; i <= TOP_OF_MONTH; i++) {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat (datePattern);
				simpleDateFormat.setTimeZone (gmtTimeZone); // do all date formatting in GMT
				String r0 = simpleDateFormat.format (epoch);
				rollingCalendar.setType (i);
				Date next = new Date (rollingCalendar.getNextCheckMillis (epoch));
				String r1 = simpleDateFormat.format (next);
				//System.out.println("Type = "+i+", r0 = "+r0+", r1 = "+r1);
				if (r0 != null && r1 != null && !r0.equals (r1)) {
					return i;
				}
			}
		}
		return TOP_OF_TROUBLE; // Deliberately head for trouble...
	}

	/**
	 * This method differentiates DailyRollingFileAppender from its
	 * super class.
	 *
	 * <p>
	 * Before actually logging, this method will check whether it is
	 * time to do a rollover. If it is, it will schedule the next
	 * rollover time and then rollover.
	 *
	 */
	protected void subAppend (LoggingEvent event) {
		long n = System.currentTimeMillis ();
		if (n >= nextCheck) {
			now.setTime (n);
			nextCheck = rc.getNextCheckMillis (now);
			try {
				rollover ();
			} catch (IOException ioe) {
				if (ioe instanceof InterruptedIOException) {
					Thread.currentThread ().interrupt ();
				}
				LogLog.error ("rollOver() failed.", ioe);
			}
		}
		super.subAppend (event);
	}
}

/**
 * RollingCalendar is a helper class to DailyRollingFileAppender.
 * Given a periodicity type and the current time, it computes the
 * start of the next interval.
 *
 */
class RollingCalendar extends GregorianCalendar {

	private static final long serialVersionUID = -3560331770601814177L;

	int type = DailyRollingFileAppenderExt.TOP_OF_TROUBLE;

	RollingCalendar () {
		super ();
	}

	RollingCalendar (TimeZone tz, Locale locale) {
		super (tz, locale);
	}

	void setType (int type) {
		this.type = type;
	}

	public long getNextCheckMillis (Date now) {
		return getNextCheckDate (now).getTime ();
	}

	public Date getNextCheckDate (Date now) {
		this.setTime (now);

		switch (type) {
			case DailyRollingFileAppenderExt.TOP_OF_MINUTE:
				this.set (Calendar.SECOND, 0);
				this.set (Calendar.MILLISECOND, 0);
				this.add (Calendar.MINUTE, 1);
				break;
			case DailyRollingFileAppenderExt.TOP_OF_HOUR:
				this.set (Calendar.MINUTE, 0);
				this.set (Calendar.SECOND, 0);
				this.set (Calendar.MILLISECOND, 0);
				this.add (Calendar.HOUR_OF_DAY, 1);
				break;
			case DailyRollingFileAppenderExt.HALF_DAY:
				this.set (Calendar.MINUTE, 0);
				this.set (Calendar.SECOND, 0);
				this.set (Calendar.MILLISECOND, 0);
				int hour = get (Calendar.HOUR_OF_DAY);
				if (hour < 12) {
					this.set (Calendar.HOUR_OF_DAY, 12);
				} else {
					this.set (Calendar.HOUR_OF_DAY, 0);
					this.add (Calendar.DAY_OF_MONTH, 1);
				}
				break;
			case DailyRollingFileAppenderExt.TOP_OF_DAY:
				this.set (Calendar.HOUR_OF_DAY, 0);
				this.set (Calendar.MINUTE, 0);
				this.set (Calendar.SECOND, 0);
				this.set (Calendar.MILLISECOND, 0);
				this.add (Calendar.DATE, 1);
				break;
			case DailyRollingFileAppenderExt.TOP_OF_WEEK:
				this.set (Calendar.DAY_OF_WEEK, getFirstDayOfWeek ());
				this.set (Calendar.HOUR_OF_DAY, 0);
				this.set (Calendar.MINUTE, 0);
				this.set (Calendar.SECOND, 0);
				this.set (Calendar.MILLISECOND, 0);
				this.add (Calendar.WEEK_OF_YEAR, 1);
				break;
			case DailyRollingFileAppenderExt.TOP_OF_MONTH:
				this.set (Calendar.DATE, 1);
				this.set (Calendar.HOUR_OF_DAY, 0);
				this.set (Calendar.MINUTE, 0);
				this.set (Calendar.SECOND, 0);
				this.set (Calendar.MILLISECOND, 0);
				this.add (Calendar.MONTH, 1);
				break;
			default:
				throw new IllegalStateException ("Unknown periodicity type.");
		}
		return getTime ();
	}
}
