package com.wynd.vop.framework.service;

import com.wynd.vop.framework.transfer.DomainTransferObjectMarker;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * A base Request object capable of representing the payload of a service request.
 *
 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "serviceRequest")
public class DomainRequest implements DomainTransferObjectMarker, Serializable {
	private static final long serialVersionUID = -8521125059263688741L;

	/**
	 * Instantiates a new rest request.
	 */
	public DomainRequest() {
		super();
	}

}
