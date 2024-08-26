package com.wynd.vop.framework.transfer;

/**
 * This marker interface identifies data POJO classes that exist as domain layer (e.g. business layer) objects.
 * A typical purpose for this interface would be to mark objects that are root in the class hierarchies,
 * and/or their contained members as specifically being objects that live in the domain/business layer,
 * as opposed to other POJOs or request/response objects from the REST APIs or partner WSDLs.
 *

 */
public interface DomainTransferObjectMarker extends VopBaseTransferMarker {
}
