package com.wynd.vop.framework.log;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Standard logging markers.
 * Markers are included in the log stream, making it possible to search and aggregate based on the marker name.
 *

 */
public enum VopLogMarkers {
	/** Indicates a FATAL exception occurred (slf4j does not support FATAL log level, so this is the compromise). */
	FATAL(MarkerFactory.getMarker("FATAL")),
	/** Indicates an Exception occurred. */
	EXCEPTION(MarkerFactory.getMarker("EXCEPTION")),
	/** Indicates the log is related to a testing effort (e.g. for triage) */
	TEST(MarkerFactory.getMarker("TEST"));

	/** the marker for an enum member */
	private Marker marker;

	/** Construct an enum member */
	private VopLogMarkers(Marker marker) {
		this.marker = marker;
	}

	/**
	 * Get the org.slf4j.Marker for the enum member.
	 *
	 * @return Marker
	 */
	public Marker getMarker() {
		return this.marker;
	}
}
