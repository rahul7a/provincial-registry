package com.lblw.vphx.phms.transformation.response.transformers.locationsummary.mappers;

import com.lblw.vphx.phms.common.databind.XPathTarget;
import com.lblw.vphx.phms.domain.location.LocationAddress;

public class LocationAddressMapper extends LocationAddress {
  @XPathTarget
  public void bindStreetAddressLine1(
      @XPathTarget.Binding(xPath = "streetAddressLine/text()") String streetAddressLine1) {
    super.setStreetAddressLine1(streetAddressLine1);
  }

  @XPathTarget
  public void bindStreetAddressLine2(
      @XPathTarget.Binding(xPath = "additionalLocator/text()") String streetAddressLine2) {
    super.setStreetAddressLine2(streetAddressLine2);
  }

  @XPathTarget
  public void bindPostalCode(@XPathTarget.Binding(xPath = "postalCode/text()") String postalCode) {
    super.setPostalCode(postalCode);
  }

  @XPathTarget
  public void bindCity(@XPathTarget.Binding(xPath = "city/text()") String city) {
    super.setCity(city);
  }

  @XPathTarget
  public void bindCountry(@XPathTarget.Binding(xPath = "country/text()") String country) {
    super.setCountry(country);
  }
}
