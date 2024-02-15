package com.lblw.vphx.phms.domain.common.response.hl7v3;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QueryAcknowledgement {
  private String queryResponseCode;
  private String resultCurrentQuantity;
  private String resultTotalQuantity;
  private String resultRemainingQuantity;
}
