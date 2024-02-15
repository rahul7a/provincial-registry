package com.lblw.vphx.phms.domain.common.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Acknowledgment after Business Rule Validations for a Provincial Request */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProvincialRequestAcknowledgement {
  private String errorCode;
  private String errorMessage;
  private RequestAcknowledgement requestAcknowledgement;
}
