package com.wynd.vop.framework.transfer.transform;

import com.wynd.vop.framework.transfer.DomainTransferObjectMarker;
import com.wynd.vop.framework.transfer.ProviderTransferObjectMarker;

/**
 * The contract for transforming a provider {@link ProviderTransferObjectMarker} object to a domain
 * {@link DomainTransferObjectMarker} object.
 * <p>
 * Implementations should declare the generic parameters with the specific classes involved in the transformation.
 *
 * @param <P> must extend ProviderTransferObjectMarker - the "source" provider object from the provider layer
 * @param <D> must extend DomainTransferObjectMarker - the "target" domain object from the service layer
 *

 */
public abstract class AbstractProviderToDomain<P extends ProviderTransferObjectMarker, D extends DomainTransferObjectMarker>
		implements BaseTransformer<P, D> {

	/**
	 * The contract for transforming a {@link ProviderTransferObjectMarker} provider object from the provider layer (the source)
	 * to a {@link DomainTransferObjectMarker} object from the service layer (the target).
	 * <p>
	 * Implementations should declare the generic parameters with the specific classes involved in the transformation.
	 *
	 * @param providerObject the type of the provider object to transform
	 * @return D the type of the transformed equivalent domain object
	 */
	@Override
	public abstract D convert(P providerObject);

}
