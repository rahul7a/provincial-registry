package com.lblw.vphx.phms.domain.prescriber;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lblw.vphx.phms.domain.common.Constants;
import java.util.Map;
import lombok.*;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class Prescriber {
  // TODO: Rename to provincialId
  private String provincialIdentifier;
  private String firstName;
  private String lastName;
  private String licenseNumber;
  private String licensingProvince;
  // TODO: Remove once roleCode is renamed to type
  private String type;
  private Boolean activeFlag;
  // TODO: Rename to systemId
  private String systemIdentifier;
  private String prescriberTypeCode;
  private Boolean outOfProvince;
  private String prescriberProvincialName;
  private String prescriberProvincialLocationIdentifier;

  @JsonProperty("prescriberType")
  private void mapPrescriberType(Map<String, Object> prescriberType) {
    if (prescriberType != null) {
      this.prescriberTypeCode = (String) prescriberType.get(Constants.CODE);
    }
  }

  @JsonProperty("licenseProvince")
  private void mapLicensingProvince(Map<String, Object> licensingProvince) {
    if (licensingProvince != null) {
      this.licensingProvince = (String) licensingProvince.get(Constants.CODE);
    }
  }

  @JsonProperty("phmsProviderData")
  private void mapProvincialIdentifier(Map<String, String> phmsProviderData) {
    if (phmsProviderData != null) {
      this.provincialIdentifier = phmsProviderData.get(Constants.NIUI);
    }
  }
}
