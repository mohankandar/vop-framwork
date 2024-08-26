package com.wynd.vop.framework.log.logback;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * A Logback rule with definition, to mask sensitive information in logs.
 * <p>
 * To create a new rule, construct a {@link VopMaskRule.Definition} using one of its constructors,
 * and then call its {@link Definition#rule()} method.
 * Note that {@link Definition#name} and {@link Definition#pattern} are required.
 */
public class VopMaskRule {

	/** POJO with rule definition values in it */
	VopMaskRule.Definition definition;

	/**
	 * Create a new logback masking rule.
	 * <p style="color:red;font-weight:bold;">
	 * This constructor is intended to be invoked only by {@link Definition#rule()}
	 * </p>
	 *
	 * @param name - a friendly name for the rule.
	 * @param prefix - a literal prefix preceding the actual search pattern
	 * @param suffix - a literal suffix following the actual search pattern
	 * @param pattern - a regular expression pattern to identify the personally identifiable information.
	 * @param unmasked - the number of characters to leave unmasked.
	 */
	VopMaskRule(final VopMaskRule.Definition definition) {

		if (definition == null) {
			throw new IllegalArgumentException("Rule Definition cannot be null.");
		}
		if (StringUtils.isBlank(definition.name) || StringUtils.isBlank(definition.pattern)
				|| definition.unmasked < 0) {
			throw new IllegalArgumentException("Name {" + definition.name + "} and pattern {" + definition.pattern
					+ "} cannot be null or blank, and unmasked {" + definition.unmasked + "} must be >= 0");
		}

		this.definition = definition;
	}

	/**
	 * Applies the masking rule to the input string.
	 *
	 * @param input - the PII that needs to be masked.
	 * @return the masked version of the input.
	 */
	String apply(String input) {
		Matcher matcher = definition.maskPattern.matcher(input);
		if (matcher.find()) {
			String match = matcher.group(1);
			int unmaskedLen = (match.length() - definition.unmasked < 0 ? 0 : match.length() - definition.unmasked);
			String mask = StringUtils.repeat("*", Math.min(match.length(), unmaskedLen));
			String replacement = mask + match.substring(mask.length());
			return input.replace(match, replacement);
		}
		return input;
	}

	/**
	 * A simple POJO with data that can be used to create a new rule instance.
	 * <p>
	 * Logback expects specific fields with getters and setters to be available
	 * in the Definition class: name, prefix, suffix, pattern, unmasked.
	 * <p>
	 * During initialization, logback uses the {@code vop-framework-logback-starter.xml}
	 * tag names by convention to construct the names of methods and classes it will
	 * expect to be available for its use.
	 * In this case, the {@code <rule>} tag name is used to find the {@link VopMaskRules#addRule(VopMaskRule.Definition)},
	 * from which this inner class name is determined.
	 *
	 * @see VopMaskRule
	 * @see ch.qos.logback.classic.joran.JoranConfigurator
	 */
	public static class Definition {

		/** A friendly name for the rule, required */
		private String name;
		/** A regex prefix to the main pattern, can be null/empty */
		private String prefix = "";
		/** A regex suffix to the main pattern, can be null/empty */
		private String suffix = "";
		/** The regex by which matches are identified, required */
		private String pattern;
		/** The number of characters to be left unmasked, default 0 */
		private int unmasked = 0;

		/** Compiled pattern used to remove braces from a string */
		Pattern bracesPattern = Pattern.compile("[{}]+");
		/** Compiled masking pattern */
		Pattern maskPattern;

		/* ***************************** CONSTRUCTORS ***************************** */

		/**
		 * Instantiates a new rule definition POJO.
		 * <p>
		 * This is the constructor called by logback.
		 * Values are set later in its process.
		 */
		public Definition() {
			this("", "");
		}

		/**
		 * Instantiates a new rule definition POJO.
		 *
		 * @param name - a friendly name for the rule.
		 * @param pattern - a regular expression pattern to identify the personally identifiable information.
		 */
		public Definition(String name, String pattern) {
			this(name, "", "", pattern, 0);
		}

		/**
		 * Instantiates a new rule definition POJO.
		 *
		 * @param name - a friendly name for the rule, required
		 * @param prefix - a literal prefix preceding the actual search pattern, or null/empty
		 * @param suffix - a literal suffix following the actual search pattern, or null/empty
		 * @param pattern - a regular expression pattern to identify the personally identifiable info, required
		 * @param unmasked - the number of characters to leave unmasked, default 0
		 */
		public Definition(String name, String prefix, String suffix, String pattern, int unmasked) {
			setName(name);
			setPrefix(prefix);
			setSuffix(suffix);
			setPattern(pattern);
			setUnmasked(unmasked);
		}

		/* ***************************** PROPERTIES ***************************** */

		/**
		 * The friendly rule name.
		 *
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * The friendly rule name.
		 * <p>
		 * Called by logback to set the value, if a {@code <name>} tag was provided in the config.
		 *
		 * @param name
		 *            the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * The literal prefix preceding the actual search pattern
		 *
		 * @return the prefix
		 */
		public String getPrefix() {
			return prefix;
		}

		/**
		 * The literal prefix preceding the actual search pattern
		 * <p>
		 * Called by logback to set the value, if a {@code <prefix>} tag was provided in the config.
		 *
		 * @param prefix - the prefix to set
		 */
		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}

		/**
		 * The literal suffix following the actual search pattern
		 *
		 * @return the suffix
		 */
		public String getSuffix() {
			return suffix;
		}

		/**
		 * The literal suffix following the actual search pattern
		 * <p>
		 * Called by logback to set the value, if a {@code <suffix>} tag was provided in the config.
		 *
		 * @param suffix - the suffix to set
		 */
		public void setSuffix(String suffix) {
			this.suffix = suffix;
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
		 * <p>
		 * Called by logback to set the value, if a {@code <pattern>} tag was provided in the config.
		 *
		 * @param pattern - the regex pattern to set
		 */
		public void setPattern(String pattern) {
			this.pattern = pattern;
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
		 * The number of characters to leave unmasked
		 * <p>
		 * Called by logback to set the value, if a {@code <unmasked>} tag was provided in the config.
		 *
		 * @param unmasked - the unmasked to set
		 */
		public void setUnmasked(int unmasked) {
			this.unmasked = unmasked;
		}

		/* ***************************** BEHAVIORS ***************************** */

		/**
		 * Create a Rule instance from this definition.
		 * <p>
		 * During initialization, logback uses the {@code vop-framework-logback-starter.xml}
		 * tag names by convention to construct the names of methods and classes it will
		 * expect to be available for its use.
		 * In this case, the {@code <rule>} tag name prescribes the method to get a .
		 *
		 * @return the mask rule
		 */
		public VopMaskRule rule() {
			if (StringUtils.isNotBlank(this.pattern)) {
				this.maskPattern = parseAndCompile();
			}
			return new VopMaskRule(this);
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

		/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
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
			// reflectUpToClass arg is not inclusive, so must be set to the superclass
			return EqualsBuilder.reflectionEquals(this, obj, false, super.getClass(),
					"definition", "bracesPattern", "maskPattern");
		}
	}
}