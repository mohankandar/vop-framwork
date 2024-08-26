package com.wynd.vop.framework.transfer.jaxb.adapters;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.Assert.*;

public class XMLGregorianCalendarAdapterTest  {

	@Before
	public void setUp() throws Exception {
	}


	@After
	public void tearDown() {
	}

	@Test
	public void testMarshalDate() throws Exception {
		Date date = new Date();
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(date);
		XMLGregorianCalendarAdapter xmlGregorianCalendarAdapter = new XMLGregorianCalendarAdapter();
		XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
		String marshalDate = xmlGregorianCalendarAdapter.marshal(xmlDate);
		assertFalse(marshalDate.isEmpty());
	}

	@Test
	public void testUnMarshalDate() throws Exception {
		String date = "2014-01-07";
		XMLGregorianCalendarAdapter xmlGregorianCalendarAdapter = new XMLGregorianCalendarAdapter();
		XMLGregorianCalendar unmarshalString = xmlGregorianCalendarAdapter.unmarshal(date);
		assertNotNull(unmarshalString);
	}


}
