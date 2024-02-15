package com.lblw.vphx.phms.domain.common.alert;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ProvincialSystemAlertExecutionStatusTracker {

  private ProvincialSystemAlertExecution provincialSystemAlertExecution;
  private String oldStatus;
  private String newStatus;
  private String dataAudit;
}
