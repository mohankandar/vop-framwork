package com.wynd.vop.framework.transfer.transform;

import com.wynd.vop.framework.transfer.DomainTransferObjectMarker;
import com.wynd.vop.framework.transfer.PartnerTransferObjectMarker;
import org.junit.Test;

import static org.junit.Assert.fail;

public class AbstractDomainToPartnerTest {

	@Test
	public void convertTest() {
		AbstractDomainToPartner<DomainTransferObjectMarker, PartnerTransferObjectMarker> transformer = new AbstractDomainToPartner<DomainTransferObjectMarker, PartnerTransferObjectMarker>() {

			@Override
			public PartnerTransferObjectMarker convert(final DomainTransferObjectMarker domainObject) {
				return null;
			}
		};
		try {
			transformer.convert(null);
		} catch (Exception e) {
			fail("exception should not be thrown");
		}
	}

}
