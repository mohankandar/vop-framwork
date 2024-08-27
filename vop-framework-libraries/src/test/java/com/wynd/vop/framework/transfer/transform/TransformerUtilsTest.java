package com.wynd.vop.framework.transfer.transform;

import com.wynd.vop.framework.messages.MessageKeys;
import com.wynd.vop.framework.messages.MessageSeverity;
import com.wynd.vop.framework.rest.provider.ProviderResponse;
import com.wynd.vop.framework.service.DomainResponse;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.http.HttpStatus;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;


public class TransformerUtilsTest {

	@Mock
	TransformerUtils.DatatypeFactoryManager datatypeFactoryManagerMock;

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Test
	public void initializeTransformerUtilsTest() {
		try {
			Constructor<TransformerUtils> constructor = TransformerUtils.class.getDeclaredConstructor();
			constructor.setAccessible(true);
			TransformerUtils transformerUtils = constructor.newInstance();
			assertNotNull(transformerUtils);
		} catch (NoSuchMethodException e) {
			fail("Exception not expected");
		} catch (SecurityException e) {
			fail("Exception not expected");
		} catch (InstantiationException e) {
			fail("Exception not expected");
		} catch (IllegalAccessException e) {
			fail("Exception not expected");
		} catch (IllegalArgumentException e) {
			fail("Exception not expected");
		} catch (InvocationTargetException e) {
			fail("Exception not expected");
		}

	}

	@Test
	public void testToDate() throws DatatypeConfigurationException {
		final Date date =
				TransformerUtils.toDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
		assertNotNull(date);
	}

	@Test
	public final void testToDateNull() throws DatatypeConfigurationException {
		final Date date = TransformerUtils.toDate(null);
		assertNull(date);
	}

	@Test
	public final void testToXMLGregorianCalendar() {
		final XMLGregorianCalendar date = TransformerUtils.toXMLGregorianCalendar(new Date(), new TransformerUtils.DatatypeFactoryManager());
		assertNotNull(date);
	}

	@Test
	public final void testToXMLGregorianCalendarExceptionHandling() {
		try {
			when(datatypeFactoryManagerMock.getDatatypeFactory()).thenThrow(new DatatypeConfigurationException());
			assertNull(TransformerUtils.toXMLGregorianCalendar(new Date(), datatypeFactoryManagerMock));
		} catch (DatatypeConfigurationException e) {
			fail("Either toXMLGregorianCalendar method in the try block did not handle this error or there is something wrong with datatypeFactoryManagerMock");
		}
	}

	@Test
	public final void testGetCurrentDate() {
		final XMLGregorianCalendar date = TransformerUtils.getCurrentDate(new TransformerUtils.DatatypeFactoryManager());
		assertNotNull(date);
	}

	@Test
	public final void datatypeFactoryManagertGetDatatypeFactoryTest() {
		try {
			TransformerUtils.DatatypeFactoryManager datatypeFactoryManager = new TransformerUtils.DatatypeFactoryManager();
			assertNotNull(datatypeFactoryManager.getDatatypeFactory());
		} catch (DatatypeConfigurationException e) {
			fail("exception should not be thrown");
		}
	}

	@Test
	public final void testTransferMessages() {
		DomainResponse from = new DomainResponse();
		from.addMessage(MessageSeverity.ERROR, HttpStatus.BAD_REQUEST, MessageKeys.NO_KEY, "ServiceMessage text");

		ProviderResponse to = new ProviderResponse();

		TransformerUtils.transferMessages(from, to);

		assertEquals(from.getMessages().get(0).getHttpStatus(), to.getMessages().get(0).getHttpStatus());
		assertEquals(from.getMessages().get(0).getStatus(), to.getMessages().get(0).getStatus());
		assertEquals(from.getMessages().get(0).getKey(), to.getMessages().get(0).getKey());
		assertEquals(from.getMessages().get(0).getSeverity().name(), to.getMessages().get(0).getSeverity());
		assertEquals(from.getMessages().get(0).getText(), to.getMessages().get(0).getText());

	}

}
