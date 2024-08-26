package com.wynd.vop.framework.transfer.transform;

import com.wynd.vop.framework.transfer.DomainTransferObjectMarker;
import com.wynd.vop.framework.transfer.PartnerTransferObjectMarker;

/**
 * The contract for transforming a partner {@link PartnerTransferObjectMarker} object to a domain
 * {@link DomainTransferObjectMarker} object.
 * <p>
 * Implementations should declare the generic parameters with the specific classes involved in the transformation.
 *
 * @param <P> must extend PartnerTransferObjectMarker - the "source" partner object from the partner client
 * @param <D> must extend DomainTransferObjectMarker - the "target" domain object from the service layer
 *

 */
public abstract class AbstractPartnerToDomain<P extends PartnerTransferObjectMarker, D extends DomainTransferObjectMarker>
		implements BaseTransformer<P, D> {

	/**
	 * The contract for transforming a {@link PartnerTransferObjectMarker} partner object from the partner client (the source)
	 * to a {@link DomainTransferObjectMarker} object from the service layer (the target).
	 * <p>
	 * Implementations should declare the generic parameters with the specific classes involved in the transformation.
	 *
	 * @param partnerObject the type of the partner object to transform
	 * @return D the type of the transformed equivalent domain object
	 */
	@Override
	public abstract D convert(P partnerObject);

}
