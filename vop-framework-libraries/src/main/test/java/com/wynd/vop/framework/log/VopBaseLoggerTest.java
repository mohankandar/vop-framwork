package com.wynd.vop.framework.log;

import com.wynd.vop.framework.AbstractBaseLogTester;
import java.util.Arrays;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.MDC;
import org.slf4j.event.Level;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class VopBaseLoggerTest extends AbstractBaseLogTester {

	public static final int MAX_MSG_LENGTH = 6144;

	@Test
	public void testGetSetLevel() {
		VopLogger logger = VopLoggerFactory.getLogger(VopBanner.class);
		Level level = logger.getLevel();
		assertNotNull(level);
		logger.setLevel(Level.INFO);
		assertTrue(Level.INFO.equals(logger.getLevel()));
		logger.info("Test message");
	}

	@Test
	public void testMakeToLength_firstWordIsBiggerThanMaxLength() {
		VopLogger logger = VopLoggerFactory.getLogger(VopBanner.class);
		LinkedList<String> listOfLogMessages = new LinkedList<String>();
		ReflectionTestUtils.invokeMethod(logger, "makeToLength", StringUtils.repeat("a", MAX_MSG_LENGTH) + "extraSuffix",
				listOfLogMessages,
				MAX_MSG_LENGTH);
		assertTrue(listOfLogMessages.get(0).equals(StringUtils.repeat("a", MAX_MSG_LENGTH)));
		assertTrue(listOfLogMessages.get(1).equals("extraSuffix "));
	}

	@Test
	public void testMakeToLength_firstWordIsBiggerThan2TimesMaxLength() {
		VopLogger logger = VopLoggerFactory.getLogger(VopBanner.class);
		LinkedList<String> listOfLogMessages = new LinkedList<String>();
		ReflectionTestUtils.invokeMethod(logger, "makeToLength",
				StringUtils.repeat("a", MAX_MSG_LENGTH * 2) + "extraSuffix plus a few more words", listOfLogMessages, MAX_MSG_LENGTH);
		assertTrue(listOfLogMessages.get(0).equals(StringUtils.repeat("a", MAX_MSG_LENGTH)));
		assertTrue(listOfLogMessages.get(1).equals(StringUtils.repeat("a", MAX_MSG_LENGTH)));
		assertTrue(listOfLogMessages.get(2).equals("extraSuffix plus a few more words "));
	}

	@Test
	public void testMakeToLength_thirdWordIsBiggerThanMaxLength() {
		VopLogger logger = VopLoggerFactory.getLogger(VopBanner.class);
		LinkedList<String> listOfLogMessages = new LinkedList<String>();
		ReflectionTestUtils.invokeMethod(logger, "makeToLength",
				"few words " + StringUtils.repeat("a", MAX_MSG_LENGTH) + "extraSuffix plus a few more words", listOfLogMessages,
				MAX_MSG_LENGTH);
		assertTrue(listOfLogMessages.get(0).equals("few words "));
		assertTrue(listOfLogMessages.get(1).equals(StringUtils.repeat("a", MAX_MSG_LENGTH)));
		assertTrue(listOfLogMessages.get(2).equals("extraSuffix plus a few more words "));
	}

	@Test
	public void testMakeToLength_thirdWordIsBiggerThan2TimesMaxLength() {
		VopLogger logger = VopLoggerFactory.getLogger(VopBanner.class);
		LinkedList<String> listOfLogMessages = new LinkedList<String>();
		ReflectionTestUtils.invokeMethod(logger, "makeToLength",
				"few words " + StringUtils.repeat("a", MAX_MSG_LENGTH * 2) + "extraSuffix plus a few more words", listOfLogMessages,
				MAX_MSG_LENGTH);
		assertTrue(listOfLogMessages.get(0).equals("few words "));
		assertTrue(listOfLogMessages.get(1).equals(StringUtils.repeat("a", MAX_MSG_LENGTH)));
		assertTrue(listOfLogMessages.get(2).equals(StringUtils.repeat("a", MAX_MSG_LENGTH)));
		assertTrue(listOfLogMessages.get(3).equals("extraSuffix plus a few more words "));
	}

	@Test
	public void testMakeToLength_LastWordIsBiggerThanMaxLength() {
		VopLogger logger = VopLoggerFactory.getLogger(VopBanner.class);
		LinkedList<String> listOfLogMessages = new LinkedList<String>();
		ReflectionTestUtils.invokeMethod(logger, "makeToLength",
				"words before last word " + StringUtils.repeat("a", MAX_MSG_LENGTH) + "extraSuffix", listOfLogMessages,
				MAX_MSG_LENGTH);
		assertTrue(listOfLogMessages.get(0).equals("words before last word "));
		assertTrue(listOfLogMessages.get(1).equals(StringUtils.repeat("a", MAX_MSG_LENGTH)));
		assertTrue(listOfLogMessages.get(2).equals("extraSuffix "));
	}

	@Test
	public void testMakeToLength_LastWordIsBiggerThan2TimesMaxLength() {
		VopLogger logger = VopLoggerFactory.getLogger(VopBanner.class);
		LinkedList<String> listOfLogMessages = new LinkedList<String>();
		ReflectionTestUtils.invokeMethod(logger, "makeToLength",
				"words before last word " + StringUtils.repeat("a", MAX_MSG_LENGTH * 2) + "extraSuffix", listOfLogMessages,
				MAX_MSG_LENGTH);
		assertTrue(listOfLogMessages.get(0).equals("words before last word "));
		assertTrue(listOfLogMessages.get(1).equals(StringUtils.repeat("a", MAX_MSG_LENGTH)));
		assertTrue(listOfLogMessages.get(2).equals(StringUtils.repeat("a", MAX_MSG_LENGTH)));
		assertTrue(listOfLogMessages.get(3).equals("extraSuffix "));
	}

	@Test
	public void testLogStrings_MdcIsPreserved() {
		VopLogger logger = VopLoggerFactory.getLogger(VopBanner.class);
		LinkedList<String> listOfLogMessages = new LinkedList<>(Arrays.asList("lorem", "ipsum", "dolor"));

		MDC.clear(); // start with empty MDC for verification
		MDC.put("foo", "bar");
		ReflectionTestUtils.invokeMethod(logger, "logStrings",
				listOfLogMessages, null, Level.INFO);

		Map<String, String> mdc = MDC.getCopyOfContextMap();

		assertEquals(1, mdc.size());
		assertEquals("bar", mdc.get("foo"));
	}

}
