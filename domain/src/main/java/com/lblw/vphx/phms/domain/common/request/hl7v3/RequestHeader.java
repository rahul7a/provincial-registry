package com.lblw.vphx.phms.domain.common.request.hl7v3;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestHeader {
  private String eSignature;
  private String iSignature;
  private String bearerToken;
}
