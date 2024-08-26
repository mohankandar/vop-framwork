package com.wynd.vop.framework.constants;

/**
 * Collection of constants for use with annotations. This will help avoid duplicate literals
 * in the code.
 *
 */
public final class VopConstants {
	/*
	 * BipConstants for use with java.lang.SuppressWarnings to
	 * ignore unchecked class casting
	 *
	 * @see java.lang.SuppressWarnings
	 */

	/** Constant to suppress unchecked */
	public static final String UNCHECKED = "unchecked";

	/*
	 * BipConstants specifically useful in exceptions
	 */

	/** Constant for Interceptor Exception banner text */
	public static final String INTERCEPTOR_EXCEPTION = "Interceptor Exception";
	/** Constant for ExceptionHandlingUtils ResolveRuntimeException banner text */
	public static final String RESOLVE_EXCEPTION = "ResolveRuntimeException Failed";
	/** */
	public static final String ILLEGALSTATE_STATICS = " is a class for statics. Do not instantiate it.";

	/*
	 * BipConstants for MIME and Media Types
	 */

	/** MIME multipart/mixed */
	public static final String MIME_MULTIPART_MIXED = "multipart/mixed";

	/**
	 * This is a class for statics. Do not instantiate it.
	 */
	private VopConstants() {
		throw new IllegalStateException(
				VopConstants.class.getSimpleName() + " is a class for statics.  Do not instantiate it.");
	}
}
