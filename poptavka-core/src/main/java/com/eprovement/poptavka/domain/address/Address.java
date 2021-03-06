package com.eprovement.poptavka.domain.address;

import com.eprovement.poptavka.domain.common.DomainObject;
import com.eprovement.poptavka.domain.enums.AddressType;
import com.eprovement.poptavka.util.orm.OrmConstants;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

/**
 * Represents an address in the system, e.g. client's permanent address.
 * From all localities it contains reference only to the {@link com.eprovement.poptavka.domain.enums.LocalityType#CITY}
 * - other levels such as district, region and country can be found easily by city.
 *
 * @author Juraj Martinka
 *         Date: 29.1.11
 */
@Entity
public class Address extends DomainObject {

    @Enumerated(value = EnumType.STRING)
    @Column(length = OrmConstants.ENUM_FIELD_LENGTH)
    private AddressType addressType;

    @ManyToOne
    @NotNull
    private Locality city;

    @NotBlank
    private String street;

    private String zipCode;

    private String houseNum;

    private String flatNum;

    public Locality getCity() {
        return city;
    }

    public void setCity(Locality city) {
        this.city = city;
    }

    public AddressType getAddressType() {
        return addressType;
    }

    public void setAddressType(AddressType addressType) {
        this.addressType = addressType;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getHouseNum() {
        return houseNum;
    }

    public void setHouseNum(String houseNum) {
        this.houseNum = houseNum;
    }

    public String getFlatNum() {
        return flatNum;
    }

    public void setFlatNum(String flatNum) {
        this.flatNum = flatNum;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Address");
        sb.append("{addressType=").append(addressType);
        sb.append(", city=").append(city);
        sb.append(", street='").append(street).append('\'');
        sb.append(", zipCode='").append(zipCode).append('\'');
        sb.append(", houseNum='").append(houseNum).append('\'');
        sb.append(", flatNum='").append(flatNum).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
