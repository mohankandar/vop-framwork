package com.wynd.vop.framework.transfer.transform;

import com.wynd.vop.framework.transfer.DomainTransferObjectMarker;
import com.wynd.vop.framework.transfer.ProviderTransferObjectMarker;
import org.junit.Test;

import static org.junit.Assert.fail;

public class AbstractProviderToDomainTest {

	@Test
	public void convertTest() {
		AbstractProviderToDomain<ProviderTransferObjectMarker, DomainTransferObjectMarker> transformer =
				new AbstractProviderToDomain<ProviderTransferObjectMarker, DomainTransferObjectMarker>() {

					@Override
					public DomainTransferObjectMarker convert(final ProviderTransferObjectMarker providerObject) {
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
