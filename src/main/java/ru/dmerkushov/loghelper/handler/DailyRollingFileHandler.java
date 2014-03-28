/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.dmerkushov.loghelper.handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.logging.ErrorManager;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/**
 * Simple daily rolling file handler
 *
 * @author Dmitriy Merkushov
 */
public class DailyRollingFileHandler extends StreamHandler {

	private String pattern;
	private String previousFilename;
	private static SimpleDateFormat dateFormat = new SimpleDateFormat ("yyyy-MM-dd");
	private static SimpleDateFormat timeFormat = new SimpleDateFormat ("HH:mm:ss.S Z");
	private long unique = 0;
	private FileOutputStream fos;
	private File file;

	/**
	 * Get the filename pattern of the log file, as it was supplied to the {@link ru.dmerkushov.loghelper.handler.DailyRollingFileHandler#DailyRollingFileHandler(java.lang.String) constructor}
	 * @return 
	 */
	public String getPattern () {
		return pattern;
	}

	/**
	 * Get the date format used by the handler
	 * @return 
	 */
	public static SimpleDateFormat getDateFormat () {
		return dateFormat;
	}

	/**
	 * Get the time format used by the handler
	 * @return 
	 */
	public static SimpleDateFormat getTimeFormat () {
		return timeFormat;
	}

	/**
	 * Get the current unique log number
	 * @return 
	 */
	public long getUnique () {
		return unique;
	}

	/**
	 * Get the {@link java.io.FileOutputStream} instance for this handler where the last record has been logged (or, if none has been logged by this handler, where the first must be)
	 * @return 
	 */
	public FileOutputStream getFos () {
		return fos;
	}

	/**
	 * Get the {@link java.io.File} instance for this handler where the last record has been logged (or, if none has been logged by this handler, where the first must be)
	 * @return 
	 */
	public File getFile () {
		return file;
	}


	/**
	 * Create a daily rolling file handler with the pattern "log_%d_%u".
	 *
	 * @throws IOException
	 * @see DailyRollingFileHandler#DailyRollingFileHandler(java.lang.String)
	 */
	public DailyRollingFileHandler () throws IOException {
		super ();
		this.pattern = "log_%d_%u";
		this.previousFilename = generateFilename (new java.util.Date ());
	}

	/**
	 * Create a daily rolling file handler.
	 *
	 * @param pattern The pattern of the file name, containing <code>%d</code>
	 * to indicate the place of the date in the file name, and <code>%u</code>
	 * to indicate the place of a unique numeric identifier. The date will be
	 * formatted as <code>yyyy-MM-dd</code> 	 * for <code>SimpleDateFormat</code>.<br> If no <code>%d</code> is
	 * found, the date is added at the end of the filename. The same
	 * about <code>%u</code>.
	 * @throws IOException if could not open the file for appending
	 * @throws IllegalArgumentException if the pattern is illegal
	 * @see SimpleDateFormat
	 */
	public DailyRollingFileHandler (String pattern) throws IllegalArgumentException, IOException {
		super ();

		if (pattern.length () < 1) {
			throw new IllegalArgumentException ("Pattern length is less than 1");
		}

		if (!pattern.contains ("%d")) {
			pattern += "%d";
		}
		if (!pattern.contains ("%u")) {
			pattern += "%u";
		}

		this.pattern = pattern;
		this.previousFilename = generateFilename (new java.util.Date ());
		file = new File (previousFilename);
		fos = new FileOutputStream (file, true);
		super.setOutputStream (fos);
//		firstLogRecord ();
	}

	@Override
	public synchronized void publish (LogRecord record) {
		if (!isLoggable (record)) {
			return;
		}

		String filename = generateFilename (new java.util.Date (record.getMillis ()));

		// Change the log file
		if (!previousFilename.equals (filename)) {
			file = new File (filename);
			try {
				fos = new FileOutputStream (file, true);
			} catch (IOException ex) {
				super.reportError (null, ex, ErrorManager.GENERIC_FAILURE);
			}
			try {
				Thread.sleep (50);
			} catch (InterruptedException ex) {
				super.reportError (null, ex, ErrorManager.GENERIC_FAILURE);
			}
			previousFilename = filename;
			super.setOutputStream (fos);
		}

		super.publish (record);

		super.flush ();
	}

	private synchronized String generateFilename (java.util.Date date) {
		String dateStr = dateFormat.format (date);

		String prePattern = pattern.replaceAll ("%d", dateStr);

		String filename = prePattern.replaceAll ("%u", String.valueOf (unique));

		// Moving to a new date. Maybe we should find a new unique number
		if (!filename.equals (previousFilename)) {
			File logFile = new File (filename);
			while (logFile.exists ()) {
				unique++;
				filename = prePattern.replaceAll ("%u", String.valueOf (unique));
				logFile = new File (filename);
			}
		}

		return filename;
	}

	private void firstLogRecord () {
		java.util.Date date = new java.util.Date ();
		LogRecord record = new LogRecord (Level.INFO, "Logging began on " + dateFormat.format (date) + " at " + timeFormat.format (date));
		record.setMillis (date.getTime ());
		publish (record);
	}
}
