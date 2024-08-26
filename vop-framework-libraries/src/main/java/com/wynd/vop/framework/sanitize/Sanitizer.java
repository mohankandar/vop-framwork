package com.wynd.vop.framework.sanitize;

import com.wynd.vop.framework.sanitize.SanitizerException;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Utility class for sanitizing file paths, file names, and strings to prevent XSS attacks.
 */
public class Sanitizer {

	// Private constructor to prevent instantiation
	private Sanitizer() {
	}

	/**
	 * Cleans a file path by removing invalid characters and normalizing the path.
	 * <p>
	 * This method ensures that the resulting path is safe by removing invalid characters
	 * and normalizing each segment of the path. Recognized separators are preserved.
	 *
	 * @param path the file path to clean
	 * @return the cleaned file path, or {@code null} if the input path is {@code null}
	 */
	public static String safePath(final String path) {
		if (path == null) {
			return null;
		}

		// Define recognized separators (e.g., ., :, \, /)
		final int[] recognizedSeparators = { 46, 58, 92, 47 };
		Arrays.sort(recognizedSeparators);

		StringBuilder cleanPath = new StringBuilder();
		try {
			StringBuilder part = new StringBuilder();
			for (int i = 0; i < path.length(); i++) {
				final int c = path.charAt(i);
				if (Arrays.binarySearch(recognizedSeparators, c) < 0) {
					// Append characters that are not separators to the current part
					part.append((char) c);
				} else {
					// Append the cleaned part and the separator to the final path
					cleanPath.append(safeFilename(part.toString())).append((char) c);
					// Reset the part builder for the next segment
					part = new StringBuilder();
				}
			}
			// Append any remaining part
			if (part.length() > 0) {
				cleanPath.append(safeFilename(part.toString()));
			}
		} catch (Exception e) {
			throw new SanitizerException("path information", e);
		}

		return cleanPath.toString();
	}

	/**
	 * Removes invalid characters from a file name based on Windows filename rules.
	 * <p>
	 * This method ensures that the resulting file name is safe by removing invalid characters
	 * that are not allowed in Windows file names. It considers a comprehensive list of disallowed
	 * characters.
	 *
	 * @param filename the file name to clean up
	 * @return the cleaned file name, or {@code null} if the input filename is {@code null}
	 */
	public static String safeFilename(final String filename) {
		if (filename == null) {
			return null;
		}

		// Define illegal characters for filenames (e.g., ", <, >, |, *, :, ?, /, \)
		final int[] illegalChars = { 34, 60, 62, 124, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22,
				23, 24, 25, 26, 27, 28, 29, 30, 31, 58, 42, 63, 92, 47 };
		Arrays.sort(illegalChars);

		StringBuilder cleanFilename = new StringBuilder();
		try {
			for (int i = 0; i < filename.length(); i++) {
				final int c = filename.charAt(i);
				if (Arrays.binarySearch(illegalChars, c) < 0) {
					// Append characters that are not illegal
					cleanFilename.append((char) c);
				}
			}
		} catch (Exception e) {
			throw new SanitizerException("filename", e);
		}

		return cleanFilename.toString();
	}

	/**
	 * Removes potentially malicious characters and patterns from a string to prevent XSS attacks.
	 * <p>
	 * This method sanitizes the input string by removing common XSS attack patterns and characters,
	 * such as script tags, eval expressions, and other potentially dangerous content.
	 *
	 * @param string the raw string to sanitize
	 * @return the sanitized string, or {@code null} if the input string is {@code null}
	 */
	public static String stripXss(final String string) {
		if (string == null) {
			return null;
		}

		String cleanValue = null;
		try {
			// Normalize the input string to decompose combined characters
			cleanValue = Normalizer.normalize(string, Normalizer.Form.NFD);

			// Remove null characters
			cleanValue = cleanValue.replaceAll("\0", "");

			// Remove content between <script> tags
			Pattern scriptPattern = Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE);
			cleanValue = scriptPattern.matcher(cleanValue).replaceAll("");

			// Remove src='...' and src="..." patterns
			scriptPattern = Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'",
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			cleanValue = scriptPattern.matcher(cleanValue).replaceAll("");

			scriptPattern = Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"",
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			cleanValue = scriptPattern.matcher(cleanValue).replaceAll("");

			// Remove lone </script> tags and <script ...> tags
			scriptPattern = Pattern.compile("</script>", Pattern.CASE_INSENSITIVE);
			cleanValue = scriptPattern.matcher(cleanValue).replaceAll("");

			scriptPattern = Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			cleanValue = scriptPattern.matcher(cleanValue).replaceAll("");

			// Remove eval(...) and expression(...) patterns
			scriptPattern = Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			cleanValue = scriptPattern.matcher(cleanValue).replaceAll("");

			scriptPattern = Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			cleanValue = scriptPattern.matcher(cleanValue).replaceAll("");

			// Remove javascript: and vbscript: patterns
			scriptPattern = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
			cleanValue = scriptPattern.matcher(cleanValue).replaceAll("");

			scriptPattern = Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE);
			cleanValue = scriptPattern.matcher(cleanValue).replaceAll("");

			// Remove onload= patterns
			scriptPattern = Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			cleanValue = scriptPattern.matcher(cleanValue).replaceAll("");
		} catch (Exception e) {
			throw new SanitizerException("input characters", e);
		}

		return cleanValue;
	}
}
