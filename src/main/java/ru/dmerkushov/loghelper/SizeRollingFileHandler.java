/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.dmerkushov.loghelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.logging.ErrorManager;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 *
 * @author shandr
 */
public class SizeRollingFileHandler extends Handler {

	/**
	 * The default value for log size bound, namely 10MiB = 10485760 bytes
	 */
	public static final long DEFAULT_LOG_SIZE_BOUND = 10 * 1024 * 1024;
	/**
	 * The minimum value of log size bound, namely 1KiB = 1024 bytes
	 */
	public static final long MINIMUM_LOG_SIZE_BOUND = 1024;
	/**
	 * Default log filename pattern, namely "log_%d_%u"
	 */
	public static final String DEFAULT_LOG_FILENAME_PATTERN = "log_%d_%u";
	private String pattern;
	private File logFile;
	private long logSizeBound;
	private java.util.Date creationDate = new java.util.Date ();
	private static SimpleDateFormat renameDateFormat = new SimpleDateFormat ("yyyy-MM-dd_HH-mm-ss.SZ");
	private static SimpleDateFormat patternDateFormat = new SimpleDateFormat ("yyyy-MM-dd");
	private long unique = 0;
	private FileOutputStream fos;

	/**
	 * Create a size rolling file handler.
	 *
	 * @param pattern The pattern of the file name, containing <code>%d</code>
	 * to indicate the place of the date in the file name, and <code>%u</code>
	 * to indicate the place of a unique numeric identifier. The date will be
	 * formatted as <code>yyyy-MM-dd</code> 	 * for <code>SimpleDateFormat</code>.<br/> If no <code>%d</code> is
	 * found, the date is added at the end of the filename. The same *
	 * about <code>%u</code>.
	 * @param maxLogSize Maximum log file size in bytes. If it is less than
	 * MINIMUM_LOG_SIZE_BOUND, it is set to MINIMUM_LOG_SIZE_BOUND
	 * @see SizeRollingFileHandler#MINIMUM_LOG_SIZE_BOUND
	 */
	public SizeRollingFileHandler (String pattern, long maxLogSize) {
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
		setLogSizeBound (maxLogSize);

		createNewLogFile ();
	}

	/**
	 * Create a size rolling file handler with default filename pattern and log
	 * file size bound
	 *
	 * @see SizeRollingFileHandler#DEFAULT_LOG_FILENAME_PATTERN
	 * @see SizeRollingFileHandler#DEFAULT_LOG_SIZE_BOUND
	 */
	public SizeRollingFileHandler () throws IllegalArgumentException, IOException {
		this (DEFAULT_LOG_FILENAME_PATTERN, DEFAULT_LOG_SIZE_BOUND);
	}

	/**
	 * Create a size rolling file handler with default log file size bound
	 *
	 * @param pattern The pattern of the log filename, *
	 * containing <code>%u</code> to indicate the place of a unique numeric
	 * identifier. If no <code>%u</code> is found, it is added at the end of the
	 * pattern.
	 * @see SizeRollingFileHandler#DEFAULT_LOG_SIZE_BOUND
	 */
	public SizeRollingFileHandler (String pattern) throws IllegalArgumentException, IOException {
		this (pattern, DEFAULT_LOG_SIZE_BOUND);
	}

	/**
	 * Create a size rolling file handler with default log filename pattern
	 *
	 * @param maxLogSize Maximum log file size in bytes. If it is less than
	 * MINIMUM_LOG_SIZE_BOUND, it is set to MINIMUM_LOG_SIZE_BOUND
	 * @see SizeRollingFileHandler#MINIMUM_LOG_SIZE_BOUND
	 */
	public SizeRollingFileHandler (long maxLogSize) throws IllegalArgumentException, IOException {
		this (DEFAULT_LOG_FILENAME_PATTERN, maxLogSize);
	}

	@Override
	public synchronized void publish (LogRecord record) {

		if (!isLoggable (record)) {
			return;
		}

		String recordMsg = getFormatter ().format (record);
		long msgLength = recordMsg.length ();

		long currentLogFileLength = logFile.length ();

		if (currentLogFileLength + msgLength > logSizeBound) {
			renameOldLogFile ();
			createNewLogFile ();
		}

		try {
			fos.write (recordMsg.getBytes ());
		} catch (IOException ex) {
			reportError ("Could not write a record of a log file: " + logFile.getAbsolutePath (), ex, ErrorManager.WRITE_FAILURE);
		}

		flush ();
	}

	/**
	 * Return the <tt>Formatter</tt> for this <tt>Handler</tt>. If the
	 * superclass's getFormatter () method returns null, creates a new
	 * <tt>LoggerFormatter</tt> instance and sets it as the formatter.
	 *
	 * @return the <tt>Formatter</tt> (may <b>not</b> be null).
	 * @see Formatter
	 * @see LoggerFormatter
	 */
	@Override
	public Formatter getFormatter () {
		Formatter formatter = super.getFormatter ();
		if (formatter == null) {
			formatter = new LoggerFormatter ();
			setFormatter (formatter);
		}

		return formatter;
	}

	private void renameOldLogFile () {
		String dateText = renameDateFormat.format (new java.util.Date ());

		File renameTo = new File (generateFilename (pattern + "_renamed_" + dateText));

		boolean renameResult = logFile.renameTo (renameTo);
		if (!renameResult) {
			reportError ("Error renaming old log file: " + logFile.getAbsolutePath () + " to: " + renameTo.getAbsolutePath (), null, ErrorManager.GENERIC_FAILURE);
		}
	}

	private void createNewLogFile () {
		logFile = new File (generateFilename (pattern));

		boolean createSuccess = false;
		try {
			createSuccess = logFile.createNewFile ();
		} catch (IOException ex) {
			reportError ("Could not create log file (with exception): " + logFile.getAbsolutePath (), ex, ErrorManager.OPEN_FAILURE);
		}
		if (!createSuccess) {
			reportError ("Could not create log file (no exception thrown): " + logFile.getAbsolutePath (), null, ErrorManager.OPEN_FAILURE);
		}

		try {
			fos = new FileOutputStream (logFile);
		} catch (FileNotFoundException ex) {
			reportError ("FileNotFoundException when creating FileOutputStream for log file: " + logFile.getAbsolutePath (), ex, ErrorManager.OPEN_FAILURE);
		}

		Formatter formatter = getFormatter ();
		if (formatter != null) {
			String head = formatter.getHead (this);
			try {
				fos.write (head.getBytes ());
			} catch (IOException ex) {
				reportError ("IOException when writing head for log file: " + logFile.getAbsolutePath (), ex, ErrorManager.GENERIC_FAILURE);
			}
		}

	}

	private synchronized String generateFilename (String pattern) {

		String generatedFilename = pattern.replaceAll ("%u", String.valueOf (unique)).replaceAll ("%d", patternDateFormat.format (creationDate));
		File generatedFile = new File (generatedFilename);
		while (generatedFile.exists ()) {
			unique++;
			generatedFilename = pattern.replaceAll ("%u", String.valueOf (unique)).replaceAll ("%d", patternDateFormat.format (creationDate));;
			generatedFile = new File (generatedFilename);
		}

		return generatedFilename;
	}

	@Override
	public void flush () {
		try {
			fos.flush ();
		} catch (IOException ex) {
			reportError (null, ex, ErrorManager.FLUSH_FAILURE);
		}
	}

	@Override
	public void close () throws SecurityException {
		try {
			fos.close ();
		} catch (IOException ex) {
			reportError (null, ex, ErrorManager.CLOSE_FAILURE);
		}
	}

	public long getLogSizeBound () {
		return logSizeBound;
	}

	public void setLogSizeBound (long logSizeBound) {
		if (logSizeBound < MINIMUM_LOG_SIZE_BOUND) {
			logSizeBound = MINIMUM_LOG_SIZE_BOUND;
		}

		this.logSizeBound = logSizeBound;
	}

	public String getPattern () {
		return pattern;
	}
}
