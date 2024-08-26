package com.wynd.vop.framework.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * This class is intended to supplement the configurable logback masking that is built in to the VOP Framework.
 * <br/>
 * <b>Before using this class in your logging statements</b>, confirm that the masking is not already being done by the logger,
 * or consider adding a regex filter to the {@code logback-spring.xml} configuration.
 * <br/>
 * For more information, see <a
 * href="https://github.com/department-of-veterans-affairs/vop-reference-person/blob/master/docs/log-audit-management.md">Log and Audit
 * Management</a>
 * <p>
 * This class provides:
 * <ul>
 * <li>Simple generic methods with sensible defaults, such as {@link #mask(String)}, {@link #maskStart(String)} and
 * {@link #maskEnd(String)}.
 * <li>More flexible generic methods, such as {@link #mask(String, char, int, int, int)}, {@link #maskStart(String, char, int, int)}
 * and
 * {@link #maskEnd(String, char, int, int)}.
 * </ul>
 * The framework team can add more masking methods to this class upon request.
 * <p>
 * This code was lifted from a pending apache commons-lang PR for StringUtils submitted by greenman18523,
 * with methods added to suit the purposes of VOP Framework.
 * <br/>
 * See <a
 * href="https://github.com/apache/commons-lang/pull/332/files#diff-3f4c835875ddc8a211c0272e8be51394">GitHub: apache/commons-lang: Add
 * methods allowing masking of Strings</a>
 *

 */
public class MaskUtil {

	/** The default masking character if one is not specified */
	public static final char DEFAULT_MASK_CHAR = '*';

	/** The default maximum unmasked characters, if {@code maxUnmasked} is not specified */
	private static final int DEFAULT_MAX_UNMASKED = 4;
	/** The default minimum masked characters, if {@code minMasked} is not specified */
	private static final int DEFAULT_MIN_MASKED = 1;

	/**
	 * Do not instantiate.
	 */
	private MaskUtil() {
		throw new IllegalAccessError("MaskUtil is a static class. Do not instantiate it.");
	}

	/**
	 * <p>Masks the given {@code str} by replacing all characters of the String.
	 *
	 * <p>The result will have all characters replaced by {@value #DEFAULT_MASK_CHAR}.
	 *
	 * <p>For {@code null} or {@link String#isEmpty() empty} {@code str}, the same {@link String} is returned.
	 *
	 * <pre>
	 * StringUtils.mask(null)      =  null
	 * StringUtils.mask("")        =  ""
	 * StringUtils.mask("t")       =  "*"
	 * StringUtils.mask("test")    =  "****"
	 * StringUtils.mask("testing") =  "*******"
	 * </pre>
	 *
	 * <p>This is equivalent to calling <tt>mask(str, '{@value #DEFAULT_MASK_CHAR}', str.length(), 0, 0)</tt>
	 *
	 * @param str the String to mask it's content
	 * @return the masked String
	 *
	 * @see #mask(String, char, int, int, int)
	 */
	public static String mask(String str) {
		return mask(str, DEFAULT_MASK_CHAR, (str == null ? 0 : str.length()), 0, 0);
	}

	/**
	 * <p>Masks the given {@code str} by replacing at least {@value #DEFAULT_MIN_MASKED} character, starting from the first character
	 * of the String.
	 *
	 * <p>The result will have up to the last {@value #DEFAULT_MAX_UNMASKED} characters with the same value as the original
	 * {@code str} with the rest being replaced by {@value #DEFAULT_MASK_CHAR}.
	 *
	 * <p>For {@code null} or {@link String#isEmpty() empty} {@code str}, the same {@link String} is returned.
	 *
	 * <pre>
	 * StringUtils.maskStart(null)      =  null
	 * StringUtils.maskStart("")        =  ""
	 * StringUtils.maskStart("t")       =  "*"
	 * StringUtils.maskStart("test")    =  "*est"
	 * StringUtils.maskStart("testing") =  "***ting"
	 * </pre>
	 *
	 * <p>This is equivalent to calling <tt>maskStart(str, '{@value #DEFAULT_MASK_CHAR}', {@value #DEFAULT_MIN_MASKED},
	 * {@value #DEFAULT_MAX_UNMASKED})</tt>
	 *
	 * @param str the String to mask it's content
	 * @return the masked String
	 *
	 * @see #maskStart(String, char, int, int)
	 */
	public static String maskStart(String str) {
		return maskStart(str, DEFAULT_MASK_CHAR, DEFAULT_MIN_MASKED, DEFAULT_MAX_UNMASKED);
	}

	/**
	 * <p>Masks the given {@code str} by replacing at least {@value #DEFAULT_MIN_MASKED} character, starting from the last character
	 * of the String.
	 *
	 * <p>The result will have up to the first {@value #DEFAULT_MAX_UNMASKED} characters with the same value as the original
	 * {@code str} with the rest being replaced by {@value #DEFAULT_MASK_CHAR}.
	 *
	 * <p>For {@code null} or {@link String#isEmpty() empty} {@code str}, the same {@link String} is returned.
	 *
	 * <pre>
	 * StringUtils.maskEnd(null)      =  null
	 * StringUtils.maskEnd("")        =  ""
	 * StringUtils.maskEnd("t")       =  "*"
	 * StringUtils.maskEnd("test")    =  "tes*"
	 * StringUtils.maskEnd("testing") =  "test***"
	 * </pre>
	 *
	 * <p>This is equivalent to calling <tt>maskEnd(str, '{@value #DEFAULT_MASK_CHAR}', {@value #DEFAULT_MIN_MASKED},
	 * {@value #DEFAULT_MAX_UNMASKED})</tt>
	 *
	 * @param str the String to mask it's content
	 * @return the masked String
	 *
	 * @see #maskEnd(String, char, int, int)
	 */
	public static String maskEnd(String str) {
		return maskEnd(str, DEFAULT_MASK_CHAR, DEFAULT_MIN_MASKED, DEFAULT_MAX_UNMASKED);
	}

	/**
	 * <p>Masks the given {@code str} by replacing, starting from the first character of the {@link String}, at least
	 * {@code minMasked} characters.
	 *
	 * <p>The result will have up to the last {@code maxUnmasked} characters with the same value as the original
	 * {@code str} with the rest being replaced by {@value #DEFAULT_MASK_CHAR}.
	 *
	 * <p>For {@code null} or {@link String#isEmpty() empty} {@code str}, the same {@link String} is returned.
	 *
	 * <p>Negative values for {@code minMasked} and {@code maxUnmasked} are set to zero.
	 *
	 * <pre>
	 * StringUtils.maskStart(null, *, *, *)       =  null
	 * StringUtils.maskStart("", *, *, *)         =  ""
	 * StringUtils.maskStart("test", *, -1, 4)    =  "test"
	 * StringUtils.maskStart("test", *, 0, 4)     =  "test"
	 * StringUtils.maskStart("test", 'X', 1, 4)   =  "Xest"
	 * StringUtils.maskStart("test", 'X', 1, 2)   =  "XXst"
	 * StringUtils.maskStart("test", 'X', 1, -1)  =  "XXXX"
	 * StringUtils.maskStart("test", 'X', 1, 0)   =  "XXXX"
	 * </pre>
	 *
	 * <p>This is equivalent to calling {@code mask(str, mask, minMasked, 0, maxUnmasked)}
	 *
	 * @param str the String to mask it's content
	 * @param mask the character used as the mask
	 * @param minMasked the minimum number of characters to mask
	 * @param maxUnmasked the maximum number of characters that will remain unmasked
	 *
	 * @return the masked String
	 *
	 * @see #mask(String, char, int, int, int)
	 * @see #maskEnd(String, char, int, int)
	 *
	 * @since 3.8
	 */
	public static String maskStart(final String str, final char mask, int minMasked, int maxUnmasked) {
		return mask(str, mask, minMasked, 0, maxUnmasked);
	}

	/**
	 * <p>Masks the given {@code str} by replacing, starting from the last character of the {@link String}, at least
	 * {@code minMasked} characters.
	 *
	 * <p>The result will have up to the first {@code maxUnmasked} characters with the same value as the original
	 * {@code str} with the rest being replaced by {@code mask}.
	 *
	 * <p>For {@code null} or {@link String#isEmpty() empty} {@code str}, the same {@link String} is returned.
	 *
	 * <p>Negative values for {@code minMasked} and {@code maxUnmasked} are set to zero.
	 *
	 * <pre>
	 * StringUtils.maskEnd(null, *, *, *)       =  null
	 * StringUtils.maskEnd("", *, *, *)         =  ""
	 * StringUtils.maskEnd("test", *, -1, 4)    =  "test"
	 * StringUtils.maskEnd("test", *, 0, 4)     =  "test"
	 * StringUtils.maskEnd("test", 'X', 1, 4)   =  "tesX"
	 * StringUtils.maskEnd("test", 'X', 1, 2)   =  "teXX"
	 * StringUtils.maskEnd("test", 'X', 1, -1)  =  "XXXX"
	 * StringUtils.maskEnd("test", 'X', 1, 0)   =  "XXXX"
	 * </pre>
	 *
	 * <p>This is equivalent to calling {@code mask(str, mask, minMasked, maxUnmasked, 0)}
	 *
	 * @param str the String to mask it's content
	 * @param mask the character used as the mask
	 * @param minMasked the minimum number of characters to mask
	 * @param maxUnmasked the maximum number of characters that will remain unmasked
	 *
	 * @return the masked String
	 *
	 * @see #mask(String, char, int, int, int)
	 * @see #maskStart(String, char, int, int)
	 *
	 * @since 3.8
	 */
	public static String maskEnd(final String str, final char mask, int minMasked, int maxUnmasked) {
		return mask(str, mask, minMasked, maxUnmasked, 0);
	}

	/**
	 * <p>Masks the given {@code str} by replacing at least {@code minMasked} characters.
	 *
	 * <p>The result will have up to {@code maxUnmaskedStart} + {@code maxUnmaskedEnd} characters with the same value as
	 * the original {@code str} with the rest being replaced by {@code mask}.
	 *
	 * <p>For {@code null} or {@link String#isEmpty() empty} {@code str}, the same {@link String} is returned.
	 *
	 * <p>Negative values for {@code minMasked}, {@code maxUnmaskedStart} and {@code maxUnmaskedEnd} are set to zero.
	 *
	 * <pre>
	 * StringUtils.maskStart(null, *, *, *, *)       =  null
	 * StringUtils.maskStart("", *, *, *, *)         =  ""
	 * StringUtils.maskStart("test", *, -1, 2, 2)    =  "test"
	 * StringUtils.maskStart("test", *, 0, 2, 2)     =  "test"
	 * StringUtils.maskStart("test", *, 4, *, *)     =  "XXXX"
	 * StringUtils.maskStart("test", *, 0, 2, 2)     =  "test"
	 * StringUtils.maskStart("test", *, 1, 2, 2)     =  "tXst"
	 * StringUtils.maskStart("test", *, 2, 2, 2)     =  "tXXt"
	 * StringUtils.maskStart("test", *, 1, 1, 1)     =  "tXXt"
	 * StringUtils.maskStart("test", *, 1, 2, 1)     =  "teXt"
	 * StringUtils.maskStart("test", *, 1, 1, 2)     =  "tXst"
	 * </pre>
	 *
	 * @param str the String to mask it's content
	 * @param mask the character used as the mask
	 * @param minMasked the minimum number of characters to mask
	 * @param maxUnmaskedStart the maximum number of characters that will remain unmasked from the start
	 * @param maxUnmaskedEnd the maximum number of characters that will remain unmasked from the end
	 *
	 * @return the masked String
	 *
	 * @see #maskStart(String, char, int, int)
	 * @see #maskEnd(String, char, int, int)
	 *
	 * @since 3.8
	 */
	public static String mask(final String str, final char mask, int minMasked, int maxUnmaskedStart, int maxUnmaskedEnd) {
		if (StringUtils.isEmpty(str)) {
			// Nothing to mask
			return str;
		}
		if (minMasked < 0) {
			minMasked = 0;
		}
		if (maxUnmaskedStart < 0) {
			maxUnmaskedStart = 0;
		}
		if (maxUnmaskedEnd < 0) {
			maxUnmaskedEnd = 0;
		}

		final int strLength = str.length();
		final int maskLength;
		final int maskStart;
		if (strLength <= minMasked) {
			maskLength = strLength;
			maskStart = 0;
		} else if (strLength < (long) minMasked + maxUnmaskedStart + maxUnmaskedEnd) {
			// long to avoid int overflow
			maskLength = minMasked;
			maskStart = computeMaskStart(strLength, maskLength, minMasked, maxUnmaskedStart, maxUnmaskedEnd);
		} else {
			maskLength = strLength - maxUnmaskedStart - maxUnmaskedEnd;
			maskStart = maxUnmaskedStart;
		}

		if (maskLength == 0) {
			// Fast path, no need to mask
			return str;
		}

		final char[] values = str.toCharArray();
		Arrays.fill(values, maskStart, maskStart + maskLength, mask);

		return new String(values);
	}

	/**
	 * Computes the maskStart position in situations where the length of the string to mask
	 * is less than the combined length of the specified: mask length + mask start position + mask end position.
	 * 
	 * @param strLength the length of the string that will be masked
	 * @param maskLength the length of the mask itself
	 * @param minMasked the minimum numbers of characters to mask
	 * @param maxUnmaskedStart the maximum number of characters that will remain unmasked from the start
	 * @param maxUnmaskedEnd the maximum number of characters that will remain unmasked from the end
	 * @return the computed starting position for the mask
	 */
	private static int computeMaskStart(int strLength, int maskLength, int minMasked, int maxUnmaskedStart, int maxUnmaskedEnd) {
		int diff = Math.abs(maxUnmaskedStart - maxUnmaskedEnd);
		if (diff == 0) {
			maxUnmaskedStart = (strLength - minMasked) / 2;
		} else {
			int remainingChars = strLength - maskLength;
			if (diff > remainingChars) {
				if (maxUnmaskedStart > maxUnmaskedEnd) {
					maxUnmaskedStart = remainingChars;
				} else {
					maxUnmaskedStart = 0;
				}
			} else {
				if (maxUnmaskedStart > maxUnmaskedEnd) {
					maxUnmaskedStart = remainingChars - diff;
				} else {
					maxUnmaskedStart = diff;
				}
			}
		}

		return maxUnmaskedStart;
	}
}
