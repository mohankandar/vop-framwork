package com.wynd.vop.framework.log;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class VopLogMarkersTest {

	@Test
	public final void testReferenceLogMarkers() {
		assertNotNull(VopLogMarkers.FATAL.getMarker());
		assertNotNull(VopLogMarkers.EXCEPTION.getMarker());
		assertNotNull(VopLogMarkers.TEST.getMarker());

		assertTrue("FATAL".equals(VopLogMarkers.FATAL.getMarker().getName()));
		assertTrue("EXCEPTION".equals(VopLogMarkers.EXCEPTION.getMarker().getName()));
		assertTrue("TEST".equals(VopLogMarkers.TEST.getMarker().getName()));
	}

}
