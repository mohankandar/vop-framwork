package com.wynd.vop.framework.messages;

import org.junit.Test;
import org.slf4j.event.Level;

import static org.junit.Assert.assertTrue;

public class MessageSeverityTest {

	@Test
	public final void testValue() {
		assertTrue("FATAL".equals(MessageSeverity.FATAL.value()));
	}

	@Test
	public final void testFromValue() {
		assertTrue(MessageSeverity.TRACE.equals(MessageSeverity.fromValue("TRACE")));
	}

	@Test
	public final void testGetLevel() {
		assertTrue(Level.ERROR.equals(MessageSeverity.FATAL.getLevel()));
		assertTrue(Level.TRACE.equals(MessageSeverity.TRACE.getLevel()));
	}

}
