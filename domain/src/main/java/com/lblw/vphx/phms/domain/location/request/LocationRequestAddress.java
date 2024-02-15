package com.lblw.vphx.phms.domain.location.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
/** LocationRequestAddress to use in Location Search criteria */
public class LocationRequestAddress {
  private String streetAddressLine;
  private String postalCode;
  private String city;
  private String country;
}
