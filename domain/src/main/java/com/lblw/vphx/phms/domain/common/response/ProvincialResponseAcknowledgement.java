package com.lblw.vphx.phms.domain.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Acknowledgment after Business Rule Validations for a Provincial Response */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProvincialResponseAcknowledgement {
  private AuditEvent auditEvent;
  private OperationOutcome operationOutcome;
}
