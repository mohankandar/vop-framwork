package com.wynd.vop.framework.log.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.boolex.EventEvaluatorBase;
import com.wynd.vop.framework.exception.VopRuntimeException;
import com.wynd.vop.framework.log.VopLogger;
import com.wynd.vop.framework.log.VopLoggerFactory;
import com.wynd.vop.framework.messages.MessageKeys;
import com.wynd.vop.framework.messages.MessageSeverity;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * A logback "evaluator filter" to mask sensitive data that can be identified
 * by regular expression.
 * <p>
 * See <a href="https://logback.qos.ch/manual/filters.html#evalutatorFilter">
 * https://logback.qos.ch/manual/filters.html#evalutatorFilter</a>
 *

 *
 */
public class VopMaskingFilter extends EventEvaluatorBase<ILoggingEvent> {
	/** Class logger */
	private static final VopLogger LOGGER = VopLoggerFactory.getLogger(VopMaskingFilter.class);
	/** Constant for an empty string */
	private static final String EMPTY = "";

	/*
	 * Note that {@link EventEvaluatorBase} already has
	 * a public {@code name} field to contain the friendly name
	 */
	/** A regex prefix to the main pattern */
	private String prefix = "";
	/** A regex suffix to the main pattern */
	private String suffix = "";
	/** The regex by which matches are identified */
	private String pattern;
	/** The number of characters to be left unmasked */
	private int unmasked = 0;

	/** Compiled masking pattern */
	Pattern maskPattern;

	/** Compiled pattern used to remove braces from a string */
	Pattern bracesPattern = Pattern.compile("[{}]+");

	/* ***************************** CONSTRUCTORS ***************************** */

	/**
	 * Construct a logback filter that masks the log message and any message arguments based on regular expression matches.
	 * <p>
	 * If this constructor is used, you must manually - at a minimum - provide a value
	 * to {@link #setPattern(String)}.
	 * Otherwise, use {@link #VopMaskingFilter(String, String, String, String, int)}.
	 * <p>
	 * As a side note, logging cannot be used in filter constructors because
	 * logback has not completed initializing the logging ecosystem.
	 */
	public VopMaskingFilter() {
		this(EMPTY, EMPTY, EMPTY, EMPTY, 0);
	}

	/**
	 * Construct a logback filter that masks the log message and any message arguments based on regular expression matches.
	 * <p>
	 * <b>Do not instantiate filters that already exist in the logback config xml file(s).</b>
	 * Filters must have unique names, and the {@link EventEvaluatorBase#setName(String)} method can only be called one time.
	 * When attempting to set the name again, logback will fail the filter and ignore it.
	 * <p>
	 * As a side note, logging cannot be used in filter constructors because
	 * logback has not completed initializing the logging ecosystem.
	 *
	 * @param name - a human friendly name for this mask
	 * @param prefix - a literal prefix that must appear immediately before the pattern (or the match fails)
	 * @param suffix - a literal suffix that must appear immediately after the pattern (or the match fails)
	 * @param pattern - the pattern that must be matched for masking to occur
	 * @param unmasked - the masked value will leave these characters at the end of the value unmasked
	 */
	public VopMaskingFilter(String name, String prefix, String suffix, String pattern, int unmasked) {
		/*
		 * logback allows name to only be set one time,
		 * so if this is called from the default constructor during logback initialization
		 * we must let logback give it the name from its filter config later in the init phase.
		 */
		if (StringUtils.isNotBlank(name)) {
			super.setName(name);
		}
		this.prefix = prefix;
		this.suffix = suffix;
		this.pattern = pattern;
		this.unmasked = unmasked;
	}

	/* ***************************** BEHAVIORS ***************************** */

	/*
	 * (non-Javadoc)
	 *
	 * @see ch.qos.logback.core.boolex.EventEvaluator#evaluate(java.lang.Object)
	 */
	@Override
	public boolean evaluate(ILoggingEvent event) throws EvaluationException {
		if (StringUtils.isBlank(pattern)) {
			return false;
		}

		try {
			if (maskPattern == null) {
				this.maskPattern = parseAndCompile();
			}

			// mask message
			String message = mask(event.getMessage());
			updateMessage(event, message);

			// mask message arguments
			Object[] args = event.getArgumentArray();
			if (args != null) {
				for (int i = 0; i < args.length; i++) {
					args[i] = mask((String) args[i]);
				}
			}
			updateArgs(event, args);
		} catch (Exception e) { // NOSONAR intentionally broad catch
			throw new EvaluationException("Could not apply mask due to " + e.getClass().getSimpleName(), e);
		}

		// return true so that the event get logged
		return true;
	}

	/**
	 * Places regex grouping parens around the pattern, and establishes the prefix as a regex lookbehind
	 * and the suffix as a regex lookahead. The result is a clearly defined grouping regex of one group.
	 *
	 * @return the complete pattern, including prefix and suffix
	 */
	private Pattern parseAndCompile() {
		String parsedPrefix = StringUtils.isBlank(this.prefix) ? "" : "(?<=" + this.prefix + ")(?:\\s*)";
		String parsedSuffix = StringUtils.isBlank(this.suffix) ? "" : "(?:\\s*)(?=" + this.suffix + ")";
		String validatedPattern = pattern.startsWith("(") ? this.pattern : "(" + this.pattern + ")";
		return compile(parsedPrefix + validatedPattern + parsedSuffix, Pattern.DOTALL | Pattern.MULTILINE);
	}

	/**
	 * Applies the masking rule to the input string.
	 *
	 * @param input - the PII that needs to be masked.
	 * @return the masked version of the input.
	 */
	private String mask(String input) {
		Matcher matcher = this.maskPattern.matcher(input);
		if (matcher.find()) {
			String match = matcher.group(1);
			int unmaskedLen = (match.length() - this.unmasked < 0 ? 0 : match.length() - this.unmasked);
			String mask = StringUtils.repeat("*", Math.min(match.length(), unmaskedLen));
			String replacement = mask + match.substring(mask.length());
			return input.replace(match, replacement);
		}
		return input;
	}

	/**
	 * Uses reflection to replace the {@code event} argument
	 * with the value of the {@code updatedMessage} argument.
	 *
	 * @param event - the logging event to modify
	 * @param updatedMessage - the message that will replace the existing message
	 * @throws VopRuntimeException - if some programming issue causes reflection to fail
	 */
	protected void updateMessage(ILoggingEvent event, String updatedMessage) {
		try {
			Field field = event.getClass().getDeclaredField("message");
			field.setAccessible(true);
			field.set(event, updatedMessage);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			LOGGER.error("Programming error: could not set log message due to " + e.getClass().getSimpleName());
			throw new VopRuntimeException(MessageKeys.VOP_DEV_ILLEGAL_INVOCATION, MessageSeverity.ERROR, HttpStatus.BAD_REQUEST,
					e.getClass().getName(), "set()", "Field [message]", ILoggingEvent.class.getName());
		}
	}

	/**
	 * Uses reflection to replace the {@code event} argument
	 * with the value of the {@code updatedMessage} argument.
	 *
	 * @param event - the logging event to modify
	 * @param args - the message arguments that will replace the existing args
	 * @throws VopRuntimeException - if some programming issue causes reflection to fail
	 */
	protected void updateArgs(ILoggingEvent event, Object[] args) {
		try {
			Field field = event.getClass().getDeclaredField("argumentArray");
			field.setAccessible(true);
			field.set(event, args);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			LOGGER.error("Programming error: could not set log message due to " + e.getClass().getSimpleName());
			throw new VopRuntimeException(MessageKeys.VOP_DEV_ILLEGAL_INVOCATION, MessageSeverity.ERROR, HttpStatus.BAD_REQUEST,
					e.getClass().getName(), "set()", "Field [argumentArray]", ILoggingEvent.class.getName());
		}
	}

	/* ***************************** PROPERTIES ***************************** */

	/**
	 * The literal prefix preceding the actual search pattern.
	 *
	 * @return the prefix
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * The literal prefix preceding the actual search pattern.
	 * Changing this value causes the masking pattern to be recompiled.
	 * <p>
	 * Called by logback to set the value, if a {@code <prefix>} tag was provided in the config.
	 *
	 * @param prefix - the prefix to set
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
		maskPattern = null;
	}

	/**
	 * The literal suffix following the actual search pattern.
	 *
	 * @return the suffix
	 */
	public String getSuffix() {
		return suffix;
	}

	/**
	 * The literal suffix following the actual search pattern.
	 * Changing this value causes the masking pattern to be recompiled.
	 * <p>
	 * Called by logback to set the value, if a {@code <suffix>} tag was provided in the config.
	 *
	 * @param suffix - the suffix to set
	 */
	public void setSuffix(String suffix) {
		this.suffix = suffix;
		maskPattern = null;
	}

	/**
	 * The regular expression pattern to identify the personally identifiable information.
	 *
	 * @return the regex pattern
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * The regular expression pattern to identify the personally identifiable information.
	 * Changing this value causes the masking pattern to be recompiled.
	 * <p>
	 * Called by logback to set the value, if a {@code <pattern>} tag was provided in the config.
	 *
	 * @param pattern - the regex pattern to set
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
		maskPattern = null;
	}

	/**
	 * The number of characters to leave unmasked
	 *
	 * @return the unmasked
	 */
	public int getUnmasked() {
		return unmasked;
	}

	/**
	 * The number of characters to leave unmasked.
	 * <p>
	 * Called by logback to set the value, if a {@code <unmasked>} tag was provided in the config.
	 *
	 * @param unmasked - the unmasked to set
	 */
	public void setUnmasked(int unmasked) {
		this.unmasked = unmasked;
	}

	/* ***************************** OVERRIDES ***************************** */

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((super.getName() == null) ? 0 : super.getName().hashCode());
		result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
		result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
		result = prime * result + ((suffix == null) ? 0 : suffix.hashCode());
		result = prime * result + unmasked;
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		// superclass must be included to include "name" field, so must also exclude super's "started" field
		// reflectUpToClass arg is not inclusive, so must be set to the super.superclass
		return EqualsBuilder.reflectionEquals(this, obj, false, super.getClass().getSuperclass(),
				"started", "EMPTY", "LOGGER", "bracesPattern", "maskPattern");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "VopMaskingFilter [name=" + super.getName() + ", prefix=" + prefix + ", suffix=" + suffix + ", pattern=" + pattern
				+ ", unmasked=" + unmasked + "]";
	}
}
