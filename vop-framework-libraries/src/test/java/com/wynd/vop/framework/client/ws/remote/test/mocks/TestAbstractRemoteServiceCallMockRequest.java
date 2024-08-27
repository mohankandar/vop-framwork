
package com.wynd.vop.framework.client.ws.remote.test.mocks;

import com.wynd.vop.framework.transfer.PartnerTransferObjectMarker;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="someKeyVariable" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
		"someKeyVariable"
})
@XmlRootElement(name = "TestAbstractRemoteServiceCallMockRequest")
public class TestAbstractRemoteServiceCallMockRequest
		implements PartnerTransferObjectMarker {

	protected String someKeyVariable;

	/**
	 * Gets the value of the someKeyVariable property.
	 *
	 * @return
	 * 		possible object is
	 *         {@link String }
	 *
	 */
	public String getSomeKeyVariable() {
		return someKeyVariable;
	}

	/**
	 * Sets the value of the someKeyVariable property.
	 *
	 * @param value
	 *            allowed object is
	 *            {@link String }
	 *
	 */
	public void setSomeKeyVariable(final String value) {
		this.someKeyVariable = value;
	}

}
