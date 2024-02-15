package com.lblw.vphx.phms.domain.provider.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProviderRequestAddress {
  private String city;
  private String postalCode;
}
