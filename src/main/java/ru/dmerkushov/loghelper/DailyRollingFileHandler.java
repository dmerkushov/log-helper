/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.dmerkushov.loghelper;

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
 * @author shandr
 * @version 0.0 NOT TESTED!
 */
public class DailyRollingFileHandler extends StreamHandler {

	private String pattern;
	private String previousFilename;
	private static SimpleDateFormat dateFormat = new SimpleDateFormat ("yyyy-MM-dd");
	private static SimpleDateFormat timeFormat = new SimpleDateFormat ("HH:mm:ss.S Z");
	private long unique = 0;
	private FileOutputStream fos;

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
	 * formatted as <code>yyyy-MM-dd</code> 	 * for <code>SimpleDateFormat</code>.<br/> If no <code>%d</code> is
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
		fos = new FileOutputStream (previousFilename, true);
		super.setOutputStream (fos);
//		firstLogRecord ();
	}

	@Override
	public synchronized void publish (LogRecord record) {
		String filename = generateFilename (new java.util.Date (record.getMillis ()));

		if (!previousFilename.equals (filename)) {
			try {
				fos = new FileOutputStream (filename, true);
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
//			firstLogRecord ();			// WARNING: firstLogRecord () runs publish() recursively! Use with care
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
