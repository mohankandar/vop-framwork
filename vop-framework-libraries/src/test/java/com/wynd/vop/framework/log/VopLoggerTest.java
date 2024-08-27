package com.wynd.vop.framework.log;

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.github.lalyos.jfiglet.FigletFont;
import com.wynd.vop.framework.AbstractBaseLogTester;
import com.wynd.vop.framework.exception.VopRuntimeException;
import com.wynd.vop.framework.messages.MessageKey;
import com.wynd.vop.framework.messages.MessageKeys;
import com.wynd.vop.framework.messages.MessageSeverity;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.springframework.boot.test.system.OutputCaptureRule;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
public class VopLoggerTest extends AbstractBaseLogTester {

	private VopLogger logger = super.getLogger(VopLoggerTest.class);
	private static final MessageKey TEST_KEY = MessageKeys.NO_KEY;

	private final VopBanner banner = VopBanner.newBanner("TEST BANNER", Level.INFO);
	private static final String MESSAGE = "Test message";
	private static final Marker MARKER = VopLogMarkers.TEST.getMarker();
	private static final VopRuntimeException EXCEPTION =
			new VopRuntimeException(TEST_KEY, MessageSeverity.ERROR, HttpStatus.BAD_REQUEST);

	/** The output capture. */
	@Rule
	public OutputCaptureRule outputCapture = new OutputCaptureRule();

	@SuppressWarnings("rawtypes")
	@Mock
	private ch.qos.logback.core.Appender mockAppender;

	// Captor is genericised with ch.qos.logback.classic.spi.LoggingEvent
	@Captor
	private ArgumentCaptor<ch.qos.logback.classic.spi.LoggingEvent> captorLoggingEvent;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		logger.setLevel(Level.DEBUG);
		logger.getLoggerBoundImpl().addAppender(mockAppender);
	}

	/**
	 * Assert output that was directed through the console.
	 *
	 * @param level the level
	 * @param banner the banner (use @{code null} for non-banner assertions)
	 * @param message the message
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void assertConsoleBanner(final Level level, final VopBanner banner, final String message, final Exception e)
			throws IOException {
		// output capture
		assertConsoleBanner(level, banner, message, e, 1);
	}

	/**
	 * Assert output that was directed through the console.
	 *
	 * @param level the level
	 * @param banner the banner (use @{code null} for non-banner assertions)
	 * @param message the message
	 * @param e the Exception
	 * @param captureCount the number of times the logs need to be counted while being appended
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings("unchecked")
	private void assertConsoleBanner(final Level level, final VopBanner banner, final String message, final Exception e,
			final int captureCount) throws IOException {
		// output capture
		final String outString = outputCapture.toString();

		if (banner != null) {
			String expected = String.valueOf(JsonStringEncoder.getInstance()
					.quoteAsString(FigletFont.convertOneLine(VopBanner.FONT_FILE, level.name() + ": " + banner.getBannerText())));
			Assert.assertTrue(StringUtils.contains(StringUtils.normalizeSpace(outString), StringUtils.normalizeSpace(expected)));
		}
		// always assert the message
		Assert.assertTrue(outString.contains(message));

		if (e != null) {
			// appender capture
			verify(mockAppender, times(captureCount)).doAppend(captorLoggingEvent.capture());
			final List<ch.qos.logback.classic.spi.LoggingEvent> loggingEvents = captorLoggingEvent.getAllValues();

			if (captureCount == 1) {
				assertNotNull(loggingEvents.get(0));
				assertTrue(
						loggingEvents.stream().map(event -> event.toString()).collect(Collectors.joining(" "))
						.contains(e.getClass().getName()));
			}
		}
	}

	/**
	 * Assert output that was directed through the console.
	 *
	 * @param level the level
	 * @param message the message
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void assertConsole(final Level level, final String message, final Exception e) throws IOException {
		assertConsoleBanner(level, null, message, e);
	}

	/**
	 * Enable TRACE log level logging. By default TRACE is turned off.
	 * <p>
	 * ALWAYS {@link #disableTrace()} when TRACE level testing is done.
	 */
	private void enableTrace() {
		logger.setLevel(Level.TRACE);
	}

	/** Disable TRACE log level logging. By default TRACE is turned off. */
	private void disableTrace() {
		logger.setLevel(Level.DEBUG);
	}

	@Test
	public final void testGetLoggerInterfaceImpl() {
		org.slf4j.Logger slf4j = logger.getLoggerInterfaceImpl();
		assertNotNull(slf4j);
	}

	@Test
	public final void testGetLoggerBoundImpl() {
		ch.qos.logback.classic.Logger logback = logger.getLoggerBoundImpl();
		assertNotNull(logback);
		assertTrue(ch.qos.logback.classic.Logger.class.isAssignableFrom(logback.getClass()));
	}

	@Test
	public final void testLargeMessage() {
		logger.setLevel(Level.DEBUG);
		String message = StringUtils.repeat("test ", 16000);
		logger.debug(message);

		message.length();
		int captureCount = 0;

		// if ((mdcReserveLength + messageLength + stackTraceLength) > MAX_TOTAL_LOG_LEN) {
		// if (messageLength >= com.wynd.vop.framework.log.BipBaseLogger.MAX_MSG_LENGTH) {
		// String[] splitMessages = ReflectionTestUtils.invokeMethod(logger, "splitMessages", message);
		// captureCount = splitMessages.length;
		// } else {
		// captureCount = 1;
		// }
		//
		// if (stackTraceLength >= com.wynd.vop.framework.log.BipBaseLogger.MAX_STACK_TRACE_TEXT_LENGTH) {
		// String[] splitstackTrace = ReflectionTestUtils.invokeMethod(logger, "splitStackTraceText", stackTrace);
		// captureCount = captureCount + splitstackTrace.length;
		// } else if (stackTraceLength != 0) {
		// captureCount = captureCount + 1;
		// }
		//
		// } else {
		// captureCount = 1;
		// }

		try {
			assertConsoleBanner(Level.DEBUG, null, "test ", null, captureCount);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Should not have thrown exception");
		}
	}

	@Test
	public final void testLargeMessageWithLargeStackTrace() {

		Exception e = new VopRuntimeException(TEST_KEY,
				MessageSeverity.ERROR, HttpStatus.BAD_REQUEST);
		ArrayList<StackTraceElement> stackTraces = new ArrayList<>();
		for (int i = 0; i < 6; i++) {
			stackTraces.addAll(Arrays.asList(e.getStackTrace()));
		}
		StackTraceElement stackTraceElement[] = new StackTraceElement[2];
		e.setStackTrace(stackTraces.toArray(stackTraceElement));
		logger.error(StringUtils.repeat("test ", 1600), e);

		StringUtils.repeat("test ", 1600);
	}

	@Test
	public final void testDebugBannerString() throws IOException {
		banner.setLevel(Level.DEBUG);
		logger.debug(banner, MESSAGE);
		assertConsoleBanner(Level.DEBUG, banner, MESSAGE, null);
		banner.setLevel(Level.DEBUG);
	}

	@Test
	public final void testDebugBannerStringObject() throws IOException {
		banner.setLevel(Level.DEBUG);
		logger.debug(banner, "{}", MESSAGE);
		assertConsoleBanner(Level.DEBUG, banner, MESSAGE, null);
		banner.setLevel(Level.DEBUG);
	}

	@Test
	public final void testDebugBannerStringObjectObject() throws IOException {
		banner.setLevel(Level.DEBUG);
		logger.debug(banner, "{} {}", MESSAGE, MESSAGE);
		assertConsoleBanner(Level.DEBUG, banner, MESSAGE + " " + MESSAGE, null);
	}

	@Test
	public final void testDebugBannerStringObjectArray() throws IOException {
		banner.setLevel(Level.DEBUG);
		logger.debug(banner, "{} {}", new Object[] { MESSAGE, MESSAGE });
		assertConsoleBanner(Level.DEBUG, banner, MESSAGE + " " + MESSAGE, null);
	}

	@Test
	public final void testDebugBannerStringThrowable() throws IOException {
		banner.setLevel(Level.DEBUG);
		logger.debug(banner, MESSAGE, EXCEPTION);
		assertConsoleBanner(Level.DEBUG, banner, MESSAGE, EXCEPTION);
	}

	@Test
	public final void testInfoBannerString() throws IOException {
		banner.setLevel(Level.INFO);
		logger.info(banner, MESSAGE);
		assertConsoleBanner(Level.INFO, banner, MESSAGE, null);
		banner.setLevel(Level.DEBUG);
	}

	@Test
	public final void testInfoBannerStringObject() throws IOException {
		banner.setLevel(Level.INFO);
		logger.info(banner, "{}", MESSAGE);
		assertConsoleBanner(Level.INFO, banner, MESSAGE, null);
		banner.setLevel(Level.DEBUG);
	}

	@Test
	public final void testInfoBannerStringObjectObject() throws IOException {
		banner.setLevel(Level.INFO);
		logger.info(banner, "{} {}", MESSAGE, MESSAGE);
		assertConsoleBanner(Level.INFO, banner, MESSAGE + " " + MESSAGE, null);
		banner.setLevel(Level.DEBUG);
	}

	@Test
	public final void testInfoBannerStringObjectArray() throws IOException {
		banner.setLevel(Level.INFO);
		logger.info(banner, "{} {}", new Object[] { MESSAGE, MESSAGE });
		assertConsoleBanner(Level.INFO, banner, MESSAGE + " " + MESSAGE, null);
		banner.setLevel(Level.DEBUG);
	}

	@Test
	public final void testInfoBannerStringThrowable() throws IOException {
		logger.setLevel(Level.INFO);
		banner.setLevel(Level.INFO);
		logger.info(banner, MESSAGE, EXCEPTION);
		assertConsoleBanner(Level.INFO, banner, MESSAGE, EXCEPTION);
		logger.setLevel(Level.DEBUG);
		banner.setLevel(Level.DEBUG);

	}

	@Test
	public final void testWarnBannerString() throws IOException {
		banner.setLevel(Level.WARN);
		logger.warn(banner, MESSAGE);
		assertConsoleBanner(Level.WARN, banner, MESSAGE, null);
		banner.setLevel(Level.DEBUG);
	}

	@Test
	public final void testWarnBannerStringObject() throws IOException {
		banner.setLevel(Level.WARN);
		logger.warn(banner, "{}", MESSAGE);
		assertConsoleBanner(Level.WARN, banner, MESSAGE, null);
		banner.setLevel(Level.DEBUG);
	}

	@Test
	public final void testWarnBannerStringObjectArray() throws IOException {
		banner.setLevel(Level.WARN);
		logger.warn(banner, "{} {}", new Object[] { MESSAGE, MESSAGE });
		assertConsoleBanner(Level.WARN, banner, MESSAGE + " " + MESSAGE, null);
		banner.setLevel(Level.DEBUG);
	}

	@Test
	public final void testWarnBannerStringObjectObject() throws IOException {
		banner.setLevel(Level.WARN);
		logger.warn(banner, "{} {}", MESSAGE, MESSAGE);
		assertConsoleBanner(Level.WARN, banner, MESSAGE + " " + MESSAGE, null);
		banner.setLevel(Level.DEBUG);
	}

	@Test
	public final void testWarnBannerStringThrowable() throws IOException {
		logger.setLevel(Level.INFO);
		banner.setLevel(Level.WARN);
		logger.warn(banner, MESSAGE, EXCEPTION);
		assertConsoleBanner(Level.WARN, banner, MESSAGE, EXCEPTION);
		banner.setLevel(Level.DEBUG);
		logger.setLevel(Level.DEBUG);
	}

	@Test
	public final void testErrorBannerString() throws IOException {
		banner.setLevel(Level.ERROR);
		logger.error(banner, MESSAGE);
		assertConsoleBanner(Level.ERROR, banner, MESSAGE, null);
		banner.setLevel(Level.DEBUG);
	}

	@Test
	public final void testErrorBannerStringObject() throws IOException {
		banner.setLevel(Level.ERROR);
		logger.error(banner, "{}", MESSAGE);
		assertConsoleBanner(Level.ERROR, banner, MESSAGE, null);
		banner.setLevel(Level.DEBUG);
	}

	@Test
	public final void testErrorBannerStringObjectObject() throws IOException {
		banner.setLevel(Level.ERROR);
		logger.error(banner, "{} {}", MESSAGE, MESSAGE);
		assertConsoleBanner(Level.ERROR, banner, MESSAGE + " " + MESSAGE, null);
		banner.setLevel(Level.DEBUG);
	}

	@Test
	public final void testErrorBannerStringObjectArray() throws IOException {
		banner.setLevel(Level.ERROR);
		logger.error(banner, "{} {}", new Object[] { MESSAGE, MESSAGE });
		assertConsoleBanner(Level.ERROR, banner, MESSAGE + " " + MESSAGE, null);
		banner.setLevel(Level.DEBUG);
	}

	@Test
	public final void testErrorBannerStringThrowable() throws IOException {
		banner.setLevel(Level.ERROR);
		logger.error(banner, MESSAGE, EXCEPTION);
		assertConsoleBanner(Level.ERROR, banner, MESSAGE, EXCEPTION);
		banner.setLevel(Level.DEBUG);
	}

	@Test
	public final void testGetName() throws IOException {
		String name = logger.getName();
		assertNotNull(name);
		assertTrue(VopLoggerTest.class.getName().equals(name));
	}

	@Test
	public final void testIsTraceEnabled() throws IOException {
		boolean is = logger.isTraceEnabled();
		assertTrue(!is);
	}

	@Test
	public final void testTraceString() throws IOException {
		// trace is disabled
		logger.trace(MESSAGE);
		assertTrue("".equals(outputCapture.toString()));
	}

	@Test
	public final void testTraceStringObject() throws IOException {
		// trace is disabled
		logger.trace("{}", MESSAGE);
		assertTrue("".equals(outputCapture.toString()));
	}

	@Test
	public final void testTraceStringObjectObject() throws IOException {
		// trace is disabled
		logger.trace("{} {}", MESSAGE, MESSAGE);
		assertTrue("".equals(outputCapture.toString()));
	}

	@Test
	public final void testTraceStringObjectArray() throws IOException {
		// trace is disabled
		logger.trace("{} {}", new Object[] { MESSAGE, MESSAGE });
		assertTrue("".equals(outputCapture.toString()));
	}

	@Test
	public final void testTraceStringThrowable() throws IOException {
		// trace is disabled
		logger.trace(MESSAGE, EXCEPTION);
		assertTrue("".equals(outputCapture.toString()));
	}

	@Test
	public final void testLogString() throws IOException {
		enableTrace();
		logger.log(Level.TRACE, MESSAGE);
		assertConsole(Level.TRACE, MESSAGE, null);
		disableTrace();
	}

	@Test
	public final void testLogStringObject() throws IOException {
		logger.log(Level.DEBUG, "{}", MESSAGE);
		assertConsole(Level.DEBUG, MESSAGE, null);
	}

	@Test
	public final void testLogStringObjectObject() throws IOException {
		logger.log(Level.INFO, "{} {}", MESSAGE, MESSAGE);
		assertConsole(Level.INFO, MESSAGE + " " + MESSAGE, null);
	}

	@Test
	public final void testLogStringObjectArray() throws IOException {
		logger.log(Level.WARN, "{} {}", new Object[] { MESSAGE, MESSAGE });
		assertConsole(Level.WARN, MESSAGE + " " + MESSAGE, null);
	}

	@Test
	public final void testLogStringThrowable() throws IOException {
		logger.setLevel(Level.ERROR);
		logger.log(Level.ERROR, MESSAGE, EXCEPTION);
		assertConsole(Level.ERROR, MESSAGE, EXCEPTION);
		logger.setLevel(Level.DEBUG);
	}

	@Test
	public final void testIsTraceEnabledMarker() throws IOException {
		boolean is = logger.isTraceEnabled(MARKER);
		assertTrue(!is);
	}

	@Test
	public final void testTraceMarkerString() throws IOException {
		// trace is disabled
		logger.trace(MARKER, MESSAGE);
		assertTrue("".equals(outputCapture.toString()));
	}

	@Test
	public final void testTraceMarkerStringObject() throws IOException {
		// trace is disabled
		logger.trace(MARKER, "{}", MESSAGE);
		assertTrue("".equals(outputCapture.toString()));
	}

	@Test
	public final void testTraceMarkerStringObjectObject() throws IOException {
		// trace is disabled
		logger.trace(MARKER, "{} {}", MESSAGE, MESSAGE);
		assertTrue("".equals(outputCapture.toString()));
	}

	@Test
	public final void testTraceMarkerStringObjectArray() throws IOException {
		// trace is disabled
		logger.trace(MARKER, "{} {}", new Object[] { MESSAGE, MESSAGE });
		assertTrue("".equals(outputCapture.toString()));
	}

	@Test
	public final void testTraceMarkerStringThrowable() throws IOException {
		// trace is disabled
		logger.trace(MARKER, "{} {}", EXCEPTION);
		assertTrue("".equals(outputCapture.toString()));
	}

	@Test
	public final void testIsDebugEnabled() throws IOException {
		boolean is = logger.isDebugEnabled();
		assertTrue(is);
	}

	@Test
	public final void testDebugString() throws IOException {
		logger.debug(MESSAGE);
		assertConsole(Level.DEBUG, MESSAGE, null);
	}

	@Test
	public final void testDebugStringObject() throws IOException {
		logger.debug("{}", MESSAGE);
		assertConsole(Level.DEBUG, MESSAGE, null);
	}

	@Test
	public final void testDebugStringObjectObject() throws IOException {
		logger.debug("{} {}", MESSAGE, MESSAGE);
		assertConsole(Level.DEBUG, MESSAGE + " " + MESSAGE, null);
	}

	@Test
	public final void testDebugStringObjectArray() throws IOException {
		logger.debug("{} {}", new Object[] { MESSAGE, MESSAGE });
		assertConsole(Level.DEBUG, MESSAGE + " " + MESSAGE, null);
	}

	@Test
	public final void testDebugStringThrowable() throws IOException {
		logger.debug(MESSAGE, EXCEPTION);
		assertConsole(Level.DEBUG, MESSAGE, EXCEPTION);
	}

	@Test
	public final void testIsDebugEnabledMarker() throws IOException {
		boolean is = logger.isDebugEnabled(MARKER);
		assertTrue(is);
	}

	@Test
	public final void testDebugMarkerString() throws IOException {
		logger.debug(MARKER, MESSAGE);
		assertConsole(Level.DEBUG, MESSAGE, null);
	}

	@Test
	public final void testDebugMarkerStringObject() throws IOException {
		logger.debug(MARKER, "{}", MESSAGE);
		assertConsole(Level.DEBUG, MESSAGE, null);
	}

	@Test
	public final void testDebugMarkerStringObjectObject() throws IOException {
		logger.debug(MARKER, "{} {}", MESSAGE, MESSAGE);
		assertConsole(Level.DEBUG, MESSAGE + " " + MESSAGE, null);
	}

	@Test
	public final void testDebugMarkerStringObjectArray() throws IOException {
		logger.debug(MARKER, "{} {}", new Object[] { MESSAGE, MESSAGE });
		assertConsole(Level.DEBUG, MESSAGE + " " + MESSAGE, null);
	}

	@Test
	public final void testDebugMarkerStringThrowable() throws IOException {
		logger.debug(MARKER, MESSAGE, EXCEPTION);
		assertConsole(Level.DEBUG, MESSAGE, EXCEPTION);
	}

	@Test
	public final void testIsInfoEnabled() throws IOException {
		boolean is = logger.isInfoEnabled();
		assertTrue(is);
	}

	@Test
	public final void testInfoString() throws IOException {
		logger.info(MESSAGE);
		assertConsole(Level.INFO, MESSAGE, null);
	}

	@Test
	public final void testInfoStringObject() throws IOException {
		logger.info("{}", MESSAGE);
		assertConsole(Level.INFO, MESSAGE, null);
	}

	@Test
	public final void testInfoStringObjectObject() throws IOException {
		logger.info("{} {}", MESSAGE, MESSAGE);
		assertConsole(Level.INFO, MESSAGE + " " + MESSAGE, null);
	}

	@Test
	public final void testInfoStringObjectArray() throws IOException {
		logger.info("{} {}", new Object[] { MESSAGE, MESSAGE });
		assertConsole(Level.INFO, MESSAGE + " " + MESSAGE, null);
	}

	@Test
	public final void testInfoStringThrowable() throws IOException {
		logger.setLevel(Level.INFO);
		logger.info(MESSAGE, EXCEPTION);
		assertConsole(Level.INFO, MESSAGE, EXCEPTION);
		logger.setLevel(Level.DEBUG);
	}

	@Test
	public final void testIsInfoEnabledMarker() throws IOException {
		boolean is = logger.isInfoEnabled(MARKER);
		assertTrue(is);
	}

	@Test
	public final void testInfoMarkerString() throws IOException {
		logger.info(MARKER, MESSAGE);
		assertConsole(Level.INFO, MESSAGE, null);
	}

	@Test
	public final void testInfoMarkerStringObject() throws IOException {
		logger.info(MARKER, "{}", MESSAGE);
		assertConsole(Level.INFO, MESSAGE, null);
	}

	@Test
	public final void testInfoMarkerStringObjectObject() throws IOException {
		logger.info(MARKER, "{} {}", MESSAGE, MESSAGE);
		assertConsole(Level.INFO, MESSAGE + " " + MESSAGE, null);
	}

	@Test
	public final void testInfoMarkerStringObjectArray() throws IOException {
		logger.info(MARKER, "{} {}", new Object[] { MESSAGE, MESSAGE });
		assertConsole(Level.INFO, MESSAGE + " " + MESSAGE, null);
	}

	@Test
	public final void testInfoMarkerStringThrowable() throws IOException {
		logger.info(MARKER, MESSAGE, EXCEPTION);
		assertConsole(Level.INFO, MESSAGE, EXCEPTION);
	}

	@Test
	public final void testIsWarnEnabled() throws IOException {
		boolean is = logger.isWarnEnabled();
		assertTrue(is);
	}

	@Test
	public final void testWarnString() throws IOException {
		logger.warn(MESSAGE);
		assertConsole(Level.WARN, MESSAGE, null);
	}

	@Test
	public final void testWarnStringObject() throws IOException {
		logger.warn("{}", MESSAGE);
		assertConsole(Level.WARN, MESSAGE, null);
	}

	@Test
	public final void testWarnStringObjectArray() throws IOException {
		logger.warn("{} {}", new Object[] { MESSAGE, MESSAGE });
		assertConsole(Level.WARN, MESSAGE + " " + MESSAGE, null);
	}

	@Test
	public final void testWarnStringObjectObject() throws IOException {
		logger.warn("{} {}", MESSAGE, MESSAGE);
		assertConsole(Level.WARN, MESSAGE + " " + MESSAGE, null);
	}

	@Test
	public final void testWarnStringThrowable() throws IOException {
		logger.warn(MESSAGE, EXCEPTION);
		assertConsole(Level.WARN, MESSAGE, EXCEPTION);
	}

	@Test
	public final void testIsWarnEnabledMarker() throws IOException {
		boolean is = logger.isWarnEnabled(MARKER);
		assertTrue(is);
	}

	@Test
	public final void testWarnMarkerString() throws IOException {
		logger.warn(MARKER, MESSAGE);
		assertConsole(Level.WARN, MESSAGE, null);
	}

	@Test
	public final void testWarnMarkerStringObject() throws IOException {
		logger.warn(MARKER, "{}", MESSAGE);
		assertConsole(Level.WARN, MESSAGE, null);
	}

	@Test
	public final void testWarnMarkerStringObjectObject() throws IOException {
		logger.warn(MARKER, "{} {}", MESSAGE, MESSAGE);
		assertConsole(Level.WARN, MESSAGE + " " + MESSAGE, null);
	}

	@Test
	public final void testWarnMarkerStringObjectArray() throws IOException {
		logger.warn(MARKER, "{} {}", new Object[] { MESSAGE, MESSAGE });
		assertConsole(Level.WARN, MESSAGE + " " + MESSAGE, null);
	}

	@Test
	public final void testWarnMarkerStringThrowable() throws IOException {
		logger.warn(MARKER, MESSAGE, EXCEPTION);
		assertConsole(Level.WARN, MESSAGE, EXCEPTION);
	}

	@Test
	public final void testIsErrorEnabled() throws IOException {
		boolean is = logger.isErrorEnabled();
		assertTrue(is);
	}

	@Test
	public final void testErrorString() throws IOException {
		logger.error(MESSAGE);
		assertConsole(Level.ERROR, MESSAGE, null);
	}

	@Test
	public final void testErrorStringObject() throws IOException {
		logger.error("{}", MESSAGE);
		assertConsole(Level.ERROR, MESSAGE, null);
	}

	@Test
	public final void testErrorStringObjectObject() throws IOException {
		logger.error("{} {}", MESSAGE, MESSAGE);
		assertConsole(Level.ERROR, MESSAGE + " " + MESSAGE, null);
	}

	@Test
	public final void testErrorStringObjectArray() throws IOException {
		logger.error("{} {}", new Object[] { MESSAGE, MESSAGE });
		assertConsole(Level.ERROR, MESSAGE + " " + MESSAGE, null);
	}

	@Test
	public final void testErrorStringThrowable() throws IOException {
		logger.error(MESSAGE, EXCEPTION);
		assertConsole(Level.ERROR, MESSAGE, EXCEPTION);
	}

	@Test
	public final void testIsErrorEnabledMarker() throws IOException {
		boolean is = logger.isErrorEnabled(MARKER);
		assertTrue(is);
	}

	@Test
	public final void testErrorMarkerString() throws IOException {
		logger.error(MARKER, MESSAGE);
		assertConsole(Level.ERROR, MESSAGE, null);
	}

	@Test
	public final void testErrorMarkerStringObject() throws IOException {
		logger.error(MARKER, "{}", MESSAGE);
		assertConsole(Level.ERROR, MESSAGE, null);
	}

	@Test
	public final void testErrorMarkerStringObjectObject() throws IOException {
		logger.error(MARKER, "{} {}", MESSAGE, MESSAGE);
		assertConsole(Level.ERROR, MESSAGE + " " + MESSAGE, null);
	}

	@Test
	public final void testErrorMarkerStringObjectArray() throws IOException {
		logger.error(MARKER, "{} {}", new Object[] { MESSAGE, MESSAGE });
		assertConsole(Level.ERROR, MESSAGE + " " + MESSAGE, null);
	}

	@Test
	public final void testErrorMarkerStringThrowable() throws IOException {
		logger.error(MARKER, MESSAGE, EXCEPTION);
		assertConsole(Level.ERROR, MESSAGE, EXCEPTION);
	}

	@Test
	public final void testLogMarkerString() throws IOException {
		enableTrace();
		logger.log(Level.TRACE, MARKER, MESSAGE);
		assertConsole(Level.TRACE, MESSAGE, null);
		disableTrace();
	}

	@Test
	public final void testLogMarkerStringObject() throws IOException {
		logger.log(Level.DEBUG, MARKER, "{}", MESSAGE);
		assertConsole(Level.DEBUG, MESSAGE, null);
	}

	@Test
	public final void testLogMarkerStringObjectObject() throws IOException {
		logger.log(Level.INFO, MARKER, "{} {}", MESSAGE, MESSAGE);
		assertConsole(Level.INFO, MESSAGE + " " + MESSAGE, null);
	}

	@Test
	public final void testLogMarkerStringObjectArray() throws IOException {
		logger.log(Level.WARN, MARKER, "{} {}", new Object[] { MESSAGE, MESSAGE });
		assertConsole(Level.WARN, MESSAGE + " " + MESSAGE, null);
	}

	@Test
	public final void testLogMarkerStringThrowable() throws IOException {
		logger.setLevel(Level.ERROR);
		logger.log(Level.ERROR, MARKER, MESSAGE, EXCEPTION);
		assertConsole(Level.ERROR, MESSAGE, EXCEPTION);
		logger.setLevel(Level.DEBUG);
	}

	@Test
	public final void testNullMessage() throws IOException {
		logger.log(Level.ERROR, null);
		assertConsole(Level.ERROR, "", null);
	}

	@Test(expected=IllegalArgumentException.class)
	public final void testMakeToLengthExceptionForNullList() {
		ReflectionTestUtils.invokeMethod(logger, "makeToLength", "string", null, 1000);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testMakeToLengthExceptionInvalidMaxLength() {
		ReflectionTestUtils.invokeMethod(logger, "makeToLength", "string", new LinkedList<String>(), -1000);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testSplitStringToLengthException() {
		ReflectionTestUtils.invokeMethod(logger, "splitStringToLength", "string", -1000);
	}

	@Test
	public final void testSplitStringToLengthForNullString() {
		List<String> list = ReflectionTestUtils.invokeMethod(logger, "splitStringToLength", null, 1000);
		assertTrue(list.size() == 1);
		assertTrue(list.get(0).equals(""));
	}

	@Test
	public final void testLogStrings() throws IOException {
		List<String> list = null;
		Level level = null;
		ReflectionTestUtils.invokeMethod(logger, "logStrings", list, null, level);
		List<String> strings = Arrays.asList(new String[] { "No log message provided. This log entry records the empty log event." });
		assertConsole(logger.getLevel(), strings.get(0), null);
	}
}
