package com.wynd.vop.framework.log;

import com.wynd.vop.framework.sanitize.Sanitizer;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.event.Level;

import com.fasterxml.jackson.core.io.JsonStringEncoder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Base logger class that:
 * <li>splits messages so large messages can be logged in spite of the docker 16 KB limit
 * <li>can print ASCII Art banner messages in the log
 * <p>
 *

 */
public class VopBaseLogger {

	/** Maximum length we are allowing for a single log, as dictated by docker limits */
	public static final int MAX_TOTAL_LOG_LEN = 16384;

	/** The string to prepend when a message must be split */
	private static final String SPLIT_MDC_NAME = "Split-Log-Sequence";

	/**
	 * Maximum length we are allowing for the "message" part of the log, leaving room for AuditEventData and JSON formatting and stack
	 * trace
	 */
	public static final int MAX_MSG_LENGTH = 6144;

	/** The actual logger implementation (logback under slf4j) */
	private org.slf4j.Logger logger;

	/** The space character */
	protected static final String SPACE = " ";

	/** Systems line separator */
	protected static final String NEWLINE = System.lineSeparator();

	/** Name of the root logger */
	public static final String ROOT_LOGGER_NAME = org.slf4j.Logger.ROOT_LOGGER_NAME;

	/**
	 * Maximum length we are allowing for AuditEventData and JSON formatting, etc., (everything other than the message and stack trace
	 * text
	 */
	public static final int MDC_RESERVE_LENGTH = 10240;

	/**
	 * Maximum length we are allowing for the "stack trace" part of the log, leaving room for AuditEventData and JSON formatting and
	 * message
	 */
	public static final int MAX_STACK_TRACE_TEXT_LENGTH = 6144;

	/**
	 * Create a new logger for apps.
	 *
	 * @param logger org.slf4j.Logger
	 */
	protected VopBaseLogger(final org.slf4j.Logger logger) {
		this.logger = logger;

	}

	/**
	 * Set the log level for the logger to a new logging level.
	 * <p>
	 * This method accesses the underlying log implementation (e.g. logback).
	 *
	 * @param level the org.slf4j.event.Level
	 */
	public void setLevel(final Level level) {
		((ch.qos.logback.classic.Logger) logger).setLevel(ch.qos.logback.classic.Level.toLevel(level.name()));
	}

	/**
	 * Get the current log level for the logger.
	 * If no level has been set, the ROOT_LOGGER level is returned.
	 * If ROOT_LOGGER has not been set, INFO is returned.
	 * <p>
	 * This method accesses the underlying log implementation (e.g. logback).
	 *
	 * @return Level the org.slf4j.event.Level
	 */
	public Level getLevel() {
		ch.qos.logback.classic.Level lvl = ((ch.qos.logback.classic.Logger) logger).getLevel();
		if (lvl == null) {
			lvl = ((ch.qos.logback.classic.Logger) LoggerFactory.getILoggerFactory().getLogger(ROOT_LOGGER_NAME)).getLevel();
		}
		return lvl == null ? Level.INFO : Level.valueOf(lvl.toString());
	}

	/**
	 * Get the underlying logger interface implementation (in this case, slf4j).
	 *
	 * @return Logger
	 */
	protected org.slf4j.Logger getLoggerInterfaceImpl() {
		return this.logger;
	}

	/* ================ Logger ================ */

	/**
	 * Generic logging, allowing to specify the log level, and optional marker.
	 *
	 * @param level the log level
	 * @param marker the marker (or null)
	 * @param message the message to log
	 */
	protected void sendlog(final Level level, final Marker marker, final String message, final Throwable t) {

		String stackTrace = getStackTraceAsString(t);
		List<String> logThis = splitStringToLength(
				safeMessage(message) + (stackTrace.length() > 0 ? NEWLINE + NEWLINE + stackTrace : ""),
				MAX_MSG_LENGTH);

		logStrings(logThis, marker, level);
	}

	/**
	 * Splits a string into a list of strings, each entry of which does not exceed
	 * the specified maxLengthPerString.
	 * <p>
	 * Each entry on the returned list will be comprised of whole words.
	 *
	 * @param string the string to split
	 * @return List of strings, each of which does not exceed maxLengthPerString
	 */
	private List<String> splitStringToLength(final String string, final int maxLengthPerString) {
		if (maxLengthPerString < 1) {
			throw new IllegalArgumentException(
					"int argument 'maxLengthPerString' for splitStringToLength(..) must be greater than zero.");
		}
		if (string == null) {
			return Arrays.asList("");
		}

		// return an array of strings, each of which does not exceed max allowable docker length
		ArrayList<String> returnList = new ArrayList<>();
		makeToLength(string, returnList, maxLengthPerString);
		return returnList;
	}

	/**
	 * Accumulates "words" in the string up to the maxLength, and puts each accumulation
	 * onto the addToThis list. Result is a list of strings (made up of full words), each
	 * of which does not exceed the maxLength.
	 *
	 * @param string the string to split
	 * @param addToThisList the list to add the split strings into, cannot be null
	 * @param maxLength the max length allowed for each split string, must be greater than 0
	 */
	private void makeToLength(final String string, final List<String> addToThisList, final int maxLength) {
		throwExceptionsForInvalidConditions(addToThisList, maxLength);
		if (string.length() <= maxLength) {
			addToThisList.add(string);
			return;
		}

		String[] words = string.split(SPACE);
		StringBuilder toLength = new StringBuilder("");
		boolean alreadyAdded = false;
		// accumulate words to the length specified for each addToThisList entry
		for (String word : words) {
			String originalWord = word;
			if ((toLength.length() + word.length() + 1) > maxLength) {
				while ((word.length() + 1) > maxLength) {
					toLength = new StringBuilder(addToListWhileAvoidingEmptyAdditions(addToThisList, toLength.toString()));
					addToThisList.add(word.substring(0, maxLength));
					word = word.substring(maxLength);
				}
				if (!"".equals(toLength.toString())) {
					addToThisList.add(toLength.toString());
				}
				toLength = new StringBuilder(word + SPACE); // start a new string

				// if it is the last word then adding to the list is still pending
				alreadyAdded = (originalWord.equals(words[words.length - 1]) ? Boolean.FALSE : Boolean.TRUE);
			} else {
				toLength.append(word + SPACE);
				alreadyAdded = false;
			}
		}
		if (!alreadyAdded) {
			addToThisList.add(toLength.toString());
		}
	}

	/**
	 * Avoid empty additions
	 * 
	 * @param addToThisList list of log messages that the original large message is split into
	 * @param toLength current log message being accumulated so as to not exceed max length
	 * @return
	 */
	private String addToListWhileAvoidingEmptyAdditions(final List<String> addToThisList, final String toLength) {
		String stringTobeAdded = toLength;
		if (!"".equals(stringTobeAdded)) {
			addToThisList.add(stringTobeAdded);
			stringTobeAdded = "";
		}
		return stringTobeAdded;
	}

	/**
	 * Throw exceptions for invalid conditions.
	 *
	 * @param addToThisList list of log messages that the original large message is split into
	 * @param maxLength the max length allowed for each split log message
	 */
	private void throwExceptionsForInvalidConditions(final List<String> addToThisList, final int maxLength) {
		if (addToThisList == null) {
			throw new IllegalArgumentException("List argument 'addToThisList' for makeToLength(..) must not be null.");
		}
		if (maxLength < 1) {
			throw new IllegalArgumentException("int argument 'maxLength' for makeToLength(..) must be greater than zero.");
		}
	}

	/**
	 * Logs each string in the strings list, using the marker and level supplied.
	 * <p>
	 * If there is more than one entry in the strings list, the MDC includes an entry with
	 * SPLIT_MDC_NAME and the sequential '# of #' string.
	 *
	 * @param strings the list of strings to be logged
	 * @param marker any marker (or null)
	 * @param level the log level, if null, the current logger's log level is used
	 */
	private void logStrings(final List<String> strings, final Marker marker, final Level level) {
		List<String> stringsToLog = ((strings == null) || strings.isEmpty())
				? Arrays.asList("No log message provided. This log entry records the empty log event.")
						: strings;
				Level levelToLogAt = (level == null) ? this.getLevel() : level;

				if (stringsToLog.size() < 2) {
					this.sendLogAtLevel(levelToLogAt, marker, stringsToLog.get(0), null);
					return; // all done here
				}

				String maxSequence = Integer.toString(stringsToLog.size());
				int sequence = 1;
				for (String toLog : stringsToLog) {
					MDC.put(SPLIT_MDC_NAME, Integer.toString(sequence++) + " of " + maxSequence);
					this.sendLogAtLevel(levelToLogAt, marker, toLog, null);
					MDC.remove(SPLIT_MDC_NAME);
				}
	}

	/**
	 * Get the stack trace formatted the same way as with Throwable.printStckTrace().
	 *
	 * @param t Throwable that contains the stack trace
	 * @return String the formatted stack trace
	 */
	private String getStackTraceAsString(final Throwable t) {

		if (t == null) {
			return "";
		}

		StringWriter writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		t.printStackTrace(printWriter);
		printWriter.flush();

		return writer.toString();
	}

	/**
	 * Get a string that is stripped of XSS characters,
	 * and is safe for use within a JSON context - escapes quotes, etc.
	 * <p>
	 * If {@code null} is passed as the message, then empty string ({@code ""}) will be returned.
	 *
	 * @param message
	 * @return String the escaped message, or {@code null}
	 */
	private String safeMessage(final String message) {
		return message == null ? ""
				: String.valueOf(JsonStringEncoder.getInstance()
						.quoteAsString(Sanitizer.stripXss(message)));
	}

	/**
	 * Perform genericized logging for a given log level.
	 * <p>
	 * If log level is {@code null}, DEBUG is assumed.
	 *
	 * @param level
	 * @param part
	 */
	private void sendLogAtLevel(final Level level, final Marker marker, final String part, final Throwable t) {
		if (level == null) {
			sendLogDebug(marker, part, t);
		} else {
			if (org.slf4j.event.Level.ERROR.equals(level)) {
				sendLogError(marker, part, t);
			} else if (org.slf4j.event.Level.WARN.equals(level)) {
				sendLogWarn(marker, part, t);
			} else if (org.slf4j.event.Level.INFO.equals(level)) {
				sendLogInfo(marker, part, t);
			} else if (org.slf4j.event.Level.TRACE.equals(level)) {
				sendLogTrace(marker, part, t);
			} else {
				sendLogDebug(marker, part, t);
			}
		}
	}

	/**
	 * Separate logger method for TRACE log levels.
	 * <p>
	 * Note that in this class, the '{@code t}' (Throwable) argument is likely
	 * to always be {@code null}, because {@link #sendlog(Level, Marker, String, Throwable)}
	 * always consolidates messages and stack traces for string length checks.
	 * This is required to meet the maximum allowable log length dictated by docker.
	 * <p>
	 * This method is identical to the other {@code sendLog*} methods,
	 * with the exception of the log level.
	 *
	 * @param marker the marker (or null)
	 * @param message the message to log
	 * @param t the Throwable, if needed
	 */
	private void sendLogTrace(final Marker marker, final String message, final Throwable t) {
		if (t == null) {
			if (marker == null) {
				this.logger.trace(message);
			} else {
				this.logger.trace(marker, message);
			}
		} else {
			if (marker == null) {
				this.logger.trace(message, t);
			} else {
				this.logger.trace(marker, message, t);
			}
		}
	}

	/**
	 * Separate logger method for DEBUG log levels.
	 * <p>
	 * Note that in this class, the '{@code t}' (Throwable) argument is likely
	 * to always be {@code null}, because {@link #sendlog(Level, Marker, String, Throwable)}
	 * always consolidates messages and stack traces for string length checks.
	 * This is required to meet the maximum allowable log length dictated by docker.
	 * <p>
	 * This method is identical to the other {@code sendLog*} methods,
	 * with the exception of the log level.
	 *
	 * @param marker the marker (or null)
	 * @param message the message to log
	 * @param t the Throwable, if needed
	 */
	private void sendLogDebug(final Marker marker, final String message, final Throwable t) {
		if (t == null) {
			if (marker == null) {
				this.logger.debug(message);
			} else {
				this.logger.debug(marker, message);
			}
		} else {
			if (marker == null) {
				this.logger.debug(message, t);
			} else {
				this.logger.debug(marker, message, t);
			}
		}
	}

	/**
	 * Separate logger method for INFO log levels.
	 * <p>
	 * Note that in this class, the '{@code t}' (Throwable) argument is likely
	 * to always be {@code null}, because {@link #sendlog(Level, Marker, String, Throwable)}
	 * always consolidates messages and stack traces for string length checks.
	 * This is required to meet the maximum allowable log length dictated by docker.
	 * <p>
	 * This method is identical to the other {@code sendLog*} methods,
	 * with the exception of the log level.
	 *
	 * @param marker the marker (or null)
	 * @param message the message to log
	 * @param t the Throwable, if needed
	 */
	private void sendLogInfo(final Marker marker, final String message, final Throwable t) {
		if (t == null) {
			if (marker == null) {
				this.logger.info(message);
			} else {
				this.logger.info(marker, message);
			}
		} else {
			if (marker == null) {
				this.logger.info(message, t);
			} else {
				this.logger.info(marker, message, t);
			}
		}
	}

	/**
	 * Separate logger method for WARN log levels.
	 * <p>
	 * Note that in this class, the '{@code t}' (Throwable) argument is likely
	 * to always be {@code null}, because {@link #sendlog(Level, Marker, String, Throwable)}
	 * always consolidates messages and stack traces for string length checks.
	 * This is required to meet the maximum allowable log length dictated by docker.
	 * <p>
	 * This method is identical to the other {@code sendLog*} methods,
	 * with the exception of the log level.
	 *
	 * @param marker the marker (or null)
	 * @param message the message to log
	 * @param t the Throwable, if needed
	 */
	private void sendLogWarn(final Marker marker, final String message, final Throwable t) {
		if (t == null) {
			if (marker == null) {
				this.logger.warn(message);
			} else {
				this.logger.warn(marker, message);
			}
		} else {
			if (marker == null) {
				this.logger.warn(message, t);
			} else {
				this.logger.warn(marker, message, t);
			}
		}
	}

	/**
	 * Separate logger method for ERROR log levels.
	 * <p>
	 * Note that in this class, the '{@code t}' (Throwable) argument is likely
	 * to always be {@code null}, because {@link #sendlog(Level, Marker, String, Throwable)}
	 * always consolidates messages and stack traces for string length checks.
	 * This is required to meet the maximum allowable log length dictated by docker.
	 * <p>
	 * This method is identical to the other {@code sendLog*} methods,
	 * with the exception of the log level.
	 *
	 * @param marker the marker (or null)
	 * @param message the message to log
	 * @param t the Throwable, if needed
	 */
	private void sendLogError(final Marker marker, final String part, final Throwable t) {
		if (t == null) {
			if (marker == null) {
				this.logger.error(part);
			} else {
				this.logger.error(marker, part);
			}
		} else {
			if (marker == null) {
				this.logger.error(part, t);
			} else {
				this.logger.error(marker, part, t);
			}
		}
	}

}
