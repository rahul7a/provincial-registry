package com.lblw.vphx.phms.domain.prescription;

import lombok.*;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class AuditDetails {
  private String systemId;
  private String txNumber;
  private String workFlowStatus;
  private String action;
  private String userId;
  private String auditDateTime;
}
