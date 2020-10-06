package org.acme.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name="Profile XML DTO", description="XML rapresentation of the processed file")
@XmlRootElement(name = "Profile")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProfileDTO  {

	@XmlElement(name = "FirstName")
	String firstName;
	
	@XmlElement(name = "LastName")
	String lastName;
	
	@XmlElement(name = "Address")
	AddressDTO address;

	public ProfileDTO() {
	}
	
	
	
	@Override
	public String toString() {
		return "ProfileDTO [firstName=" + firstName + ", lastName=" + lastName + ", address=" + address + "]";
	}



	public ProfileDTO(String firstName, String lastName, AddressDTO address) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.address = address;
	}

	
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	
	public AddressDTO getAddress() {
		return address;
	}

	public void setAddress(AddressDTO address) {
		this.address = address;
	}
    
	
    
}