package org.acme.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "Address XML DTO", description = "XML rapresentation of a profile address ")

@XmlAccessorType(XmlAccessType.FIELD)
public class AddressDTO {

	@XmlElement(name = "StreetName")
	String streetName;

	@XmlElement(name = "StreetNumberBase")
	String streetNumberBase;

	@XmlElement(name = "PostalCode")
	String postalCode;

	@XmlElement(name = "City")
	String city;

	public String getStreetName() {
		return streetName;
	}

	public void setStreetName(String streetName) {
		this.streetName = streetName;
	}

	public String getStreetNumberBase() {
		return streetNumberBase;
	}

	public void setStreetNumberBase(String streetNumberBase) {
		this.streetNumberBase = streetNumberBase;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * must be present in order to unmarshall the xml into objects
	 */
	public AddressDTO() {
		
	}
	
	public AddressDTO(String streetName, String streetNumberBase, String postalCode, String city) {
		super();
		this.streetName = streetName;
		this.streetNumberBase = streetNumberBase;
		this.postalCode = postalCode;
		this.city = city;
	}

	@Override
	public String toString() {
		return "AddressDTO [streetName=" + streetName + ", streetNumberBase=" + streetNumberBase + ", postalCode="
				+ postalCode + ", city=" + city + "]";
	}

	
	
}
