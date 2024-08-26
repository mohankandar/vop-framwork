package com.wynd.vop.framework.transfer.transform;

import com.wynd.vop.framework.transfer.DomainTransferObjectMarker;
import com.wynd.vop.framework.transfer.ProviderTransferObjectMarker;

/**
 * The contract for transforming a domain {@link DomainTransferObjectMarker} object to a provider
 * {@link ProviderTransferObjectMarker} object.
 * <p>
 * Implementations should declare the generic parameters with the specific classes involved in the transformation.
 *
 * @param <D> must extend DomainTransferObjectMarker - the "source" domain object from the service layer
 * @param <P> must extend ProviderTransferObjectMarker - the "target" provider object from the provider layer
 *

 */
public abstract class AbstractDomainToProvider<D extends DomainTransferObjectMarker, P extends ProviderTransferObjectMarker>
implements BaseTransformer<D, P> {

	/**
	 * The contract for transforming a {@link DomainTransferObjectMarker} object from the service layer (the source)
	 * to a {@link ProviderTransferObjectMarker} provider object from the provider layer (the target).
	 * <p>
	 * Implementations should declare the generic parameters with the specific classes involved in the transformation.
	 *
	 * @param domainObject the type of the domain object to transform
	 * @return P the type of the transformed equivalent provider object
	 */
	@Override
	public abstract P convert(D domainObject);

}
