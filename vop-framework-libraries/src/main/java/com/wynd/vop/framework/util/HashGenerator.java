package com.wynd.vop.framework.util;

import com.wynd.vop.framework.log.VopLogger;
import com.wynd.vop.framework.log.VopLoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for generating hashes using different algorithms.
 */
public final class HashGenerator {

	/** The Constant LOGGER. */
	private static final VopLogger LOGGER = VopLoggerFactory.getLogger(HashGenerator.class);

	/** The Constant MD5_DIGEST_ALGORITHM. */
	public static final String MD5_DIGEST_ALGORITHM = "MD5";

	/** The Constant MD5_HASH_LENGTH. */
	public static final int MD5_HASH_LENGTH = 32;

	/**
	 * Constructor to prevent instantiation.
	 */
	private HashGenerator() {
		throw new IllegalAccessError("HashGenerator is a static class. Do not instantiate it.");
	}

	/**
	 * Gets the MD5 hash for input string.
	 *
	 * @param strInput the string input
	 * @return the MD5 hash
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 */
	public static String getMd5ForString(final String strInput) {
		return getGivenHashForString(strInput, MD5_DIGEST_ALGORITHM);
	}

	/**
	 * Gets the given hash for string.
	 *
	 * @param strInput the str input
	 * @param algorithm the algorithm
	 * @return the given hash for string
	 */
	private static String getGivenHashForString(final String strInput, final String algorithm) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance(algorithm);
			digest.update(strInput.getBytes());
			byte[] byteData = digest.digest();

			return DatatypeConverter.printHexBinary(byteData).toLowerCase();

		} catch (NoSuchAlgorithmException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

}
