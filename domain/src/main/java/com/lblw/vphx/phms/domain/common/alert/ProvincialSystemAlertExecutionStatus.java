package com.lblw.vphx.phms.domain.common.alert;

/** Status Type for ProvincialSystemAlertExecution request {@link ProvincialSystemAlertExecution} */
public enum ProvincialSystemAlertExecutionStatus {
  EVALUATION_IN_PROGRESS,
  ACTION_EXECUTED,
  ACTION_SUPPRESSED,
  ACTION_FAILED,
  EXPIRED,
  RESOLVED,
}
