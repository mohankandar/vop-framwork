package com.wynd.vop.framework.transfer.transform;

import com.wynd.vop.framework.transfer.DomainTransferObjectMarker;
import com.wynd.vop.framework.transfer.PartnerTransferObjectMarker;

/**
 * The contract for transforming a domain {@link DomainTransferObjectMarker} object to a partner
 * {@link PartnerTransferObjectMarker} object.
 * <p>
 * Implementations should declare the generic parameters with the specific classes involved in the transformation.
 *
 * @param <D> must extend DomainTransferObjectMarker - the "source" domain object from the service layer
 * @param <P> must extend PartnerTransferObjectMarker - the "target" partner object from the partner client
 *

 */
public abstract class AbstractDomainToPartner<D extends DomainTransferObjectMarker, P extends PartnerTransferObjectMarker>
implements BaseTransformer<D, P> {

	/**
	 * The contract for transforming a {@link DomainTransferObjectMarker} object from the service layer (the source)
	 * to a {@link PartnerTransferObjectMarker} partner object from the partner client (the target).
	 * <p>
	 * Implementations should declare the generic parameters with the specific classes involved in the transformation.
	 *
	 * @param domainObject the type of the domain object to transform
	 * @return P the type of the transformed equivalent partner object
	 */
	@Override
	public abstract P convert(D domainObject);

}
