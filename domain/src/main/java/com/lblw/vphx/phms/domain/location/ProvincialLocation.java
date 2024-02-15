package com.lblw.vphx.phms.domain.location;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lblw.vphx.phms.domain.coding.Coding;
import com.lblw.vphx.phms.domain.common.SystemIdentifier;
import com.lblw.vphx.phms.domain.common.Telecom;
import com.lblw.vphx.phms.domain.pharmacy.ProvincialPharmacyCertificateDetails;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(callSuper = true)
@Schema(description = "Service Delivery Location as registered in Provincial Location Registry")
public class ProvincialLocation {
  private SystemIdentifier identifier;

  @Schema(
      description =
          "Provincial ID as registered in provincial registry, else"
              + "will be '9999999999' if the location is outside province or"
              + "will be '8888888888' if the location is in province but information is not available or unresolved",
      example = "1000016723")
  private String provincialId;

  @Schema(description = "Name of the location", example = "Pharmacie Bon soins")
  private String name;

  @Schema(
      description =
          "Type of the location as appropriate system Coding."
              + "Type can be any service delivery location like Hospital center, pharmacy, laboratory...")
  private Coding locationType;

  @Schema(description = "Address of the location")
  private LocationAddress address;

  @Schema(description = "Region of the location")
  private String region;

  private ProvincialLocation parentLocation;
  private SystemIdentifier permit;
  private SystemIdentifier publicHealthInsurancePermit;
  private SystemIdentifier pharmacyBilling;

  @Schema(description = "Telephone number")
  private Telecom telecom;

  @JsonIgnore private ProvincialPharmacyCertificateDetails certificate;
}
