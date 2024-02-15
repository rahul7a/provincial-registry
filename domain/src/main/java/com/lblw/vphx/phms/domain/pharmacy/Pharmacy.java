package com.lblw.vphx.phms.domain.pharmacy;

import com.lblw.vphx.phms.domain.common.Province;
import lombok.*;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class Pharmacy {

  private String id;
  private String name;
  private String storeNumber;
  private ProvincialLocation provincialLocation;
  private Integer version;
  private ProvincialPharmacyCertificateDetails certificate;
  private String workstationHostname;
  private Pharmacist supervisingPharmacist;
  private Pharmacist defaultPharmacist;
  private Pharmacist dataRecorder;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class ProvincialLocation {
    Identifier identifier;
    String name;
    String region;
    ProvincialLocation parentLocation;
    Identifier publicHealthInsurancePermit;
    Identifier pharmacyBilling;
    Address address;
    Telecom telecom;
    LocationType locationType;
  }

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
  public static class LocationType {
    Province province;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class Telecom {
    String number;
  }
}
