package com.lblw.vphx.phms.domain.pharmacy;

import com.lblw.vphx.phms.domain.common.ProvincialProviderProfile;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Pharmacist {
  private static final String ACTIVE = "ACTIVE";
  String firstName;
  String lastName;
  String idpUserId;
  String licenceNumber;
  String licenceProvince;
  ProvincialProviderProfile provincialProvider;
  String state;

  /**
   * Retrieves the active status of the Pharmacist
   *
   * @return {@link Boolean}
   */
  public boolean isActive() {
    return ACTIVE.equalsIgnoreCase(this.state);
  }
}
