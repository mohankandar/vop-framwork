package com.wynd.vop.framework.transfer.transform;

import com.wynd.vop.framework.log.VopLogger;
import com.wynd.vop.framework.log.VopLoggerFactory;
import com.wynd.vop.framework.rest.provider.ProviderResponse;
import com.wynd.vop.framework.service.DomainResponse;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Some static utility methods to support model object transformations.
 *

 */
public final class TransformerUtils {

	/** Logger */
	static final VopLogger LOGGER = VopLoggerFactory.getLogger(TransformerUtils.class);

	// Private constructor is added to prevent this class from being instantiated
	private TransformerUtils() {
	}

	/**
	 * This class is added to substitute for the static method javax.xml.datatype.DatatypeFactory.newInstance() to aid in testing. The
	 * class can be mocked and an exception can be thrown while calling the static method to test the exception handling code. The call
	 * to the static method is wrapped in getDatatypeFactory() method in this class.
	 *
	 */
	public static class DatatypeFactoryManager {
		/**
		 * This method is a wrapper for the static method javax.xml.datatype.DatatypeFactory.newInstance() call. While testing, a mock
		 * object could deliberately throw an exception to test the error handling code.
		 *
		 * @return a DatatypeFactory instance
		 * @throws DatatypeConfigurationException
		 */
		public DatatypeFactory getDatatypeFactory() throws DatatypeConfigurationException {
			return DatatypeFactory.newInstance();
		}
	}

	/**
	 * Convert XMLGregorianCalendar to java.util.Date
	 *
	 * @param xmlDate
	 * @return Date
	 */
	public static Date toDate(final XMLGregorianCalendar xmlDate) {
		if (xmlDate == null) {
			return null;
		}
		return xmlDate.toGregorianCalendar().getTime();
	}

	/**
	 * convert java.util.Date to XMLGregorianCalendar
	 *
	 * @param date the java.util.Date
	 * @param datatypeFactoryManager the object that is used to get the datatype factory (added to ease testing, see
	 *            {@link TransformerUtils.DatatypeFactoryManager})
	 * @return XMLGregorianCalendar object
	 */
	public static XMLGregorianCalendar toXMLGregorianCalendar(final Date date,
			final TransformerUtils.DatatypeFactoryManager datatypeFactoryManager) {
		try {
			final GregorianCalendar gc = new GregorianCalendar(Locale.getDefault());
			gc.setTimeInMillis(date.getTime());
			return datatypeFactoryManager.getDatatypeFactory().newXMLGregorianCalendar(gc);

		} catch (final DatatypeConfigurationException dcEx) { // NOSONAR
			LOGGER.error(dcEx.getMessage());
			return null;
		}
	}

	/**
	 * Convert "now" to XMLGregorianCalendar
	 *
	 * @param datatypeFactoryManager the object that is used to get the datatype factory (added to ease testing, see
	 *            {@link TransformerUtils.DatatypeFactoryManager})
	 * @return XMLGregorianCalendar
	 */
	public static XMLGregorianCalendar getCurrentDate(final TransformerUtils.DatatypeFactoryManager datatypeFactoryManager) {
		try {
			return datatypeFactoryManager.getDatatypeFactory().newXMLGregorianCalendar(new GregorianCalendar(Locale.getDefault()));

		} catch (final DatatypeConfigurationException dcEx) { // NOSONAR
			LOGGER.error(dcEx.getMessage());
			return null;
		}
	}

	public static void transferMessages(DomainResponse from, ProviderResponse to) {
		from.getMessages().forEach(
				domainMessage -> to.addMessage(domainMessage.getSeverity(), domainMessage.getKey(), domainMessage.getText(), domainMessage.getHttpStatus())
		);
	}

}
