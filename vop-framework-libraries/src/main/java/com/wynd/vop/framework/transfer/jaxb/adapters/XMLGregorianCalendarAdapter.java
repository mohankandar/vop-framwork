package com.wynd.vop.framework.transfer.jaxb.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

public class XMLGregorianCalendarAdapter extends XmlAdapter<String, XMLGregorianCalendar> {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    // Unmarshal by converting the value type to a bound type.
    @Override
    public XMLGregorianCalendar unmarshal(String v) throws Exception {
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(v);
    }

    // Marshal by converting the bound type to a value type.
    @Override
    public String marshal(XMLGregorianCalendar v) throws Exception {
        synchronized (dateFormat) {
            return specialFormatForXmlGregorianCalander(v);
        }
    }

    // Because you cannot format an XMLGregorianCalender type, you need to generate a GregorianCalender from it first
    private String specialFormatForXmlGregorianCalander(XMLGregorianCalendar calander){
        // Convert from XMLGregorianCalender to GregorianCalender
        GregorianCalendar gCalender = calander.toGregorianCalendar();

        // Get the date
        java.util.Date date = gCalender.getTime();

        //define the type of calendar to be GregorianCalender
        dateFormat.setCalendar(gCalender);

        // Return the string version
        return dateFormat.format(date);
    }
}
