package com.lblw.vphx.phms.domain.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lblw.vphx.phms.domain.pharmacy.ProvincialPharmacyCertificateDetails;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Pharmacy {

  // TODO: rename pharmacyId -> id
  @JsonProperty("id")
  private String pharmacyId;

  private String provincialLocationIdentifier;
  private String name;
  private String storeId;
  private String licenceNumber;
  private ProvincialLocation provincialLocation;
  private Integer version;
  private ProvincialPharmacyCertificateDetails provincialPharmacyCertificate;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class Identifier {
    String type;
    String value;
    String assigner;
    String system;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class ProvincialLocation {
    Identifier identifier;
    String name;
    String region;
    Object parentLocation;
    Object publicHealthInsurancePermit;
    Object pharmacyBilling;
    Address address;
    Telecom telecom;
    LocationType locationType;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class Address {
    String streetAddressLine1;
    String streetAddressLine2;
    String postalCode;
    String city;
    String country;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class Telecom {
    String number;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class LocationType {
    Province province;
  }
}
