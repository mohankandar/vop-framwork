
package com.wynd.vop.framework.client.ws.remote.test.mocks;

import com.wynd.vop.framework.transfer.jaxb.adapters.DateAdapter;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Date;

public class Adapter1
    extends XmlAdapter<String, Date>
{


    public Date unmarshal(String value) {
        return (DateAdapter.parseDateTime(value));
    }

    public String marshal(Date value) {
        return (DateAdapter.printDateTime(value));
    }

}
