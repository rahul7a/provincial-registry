package com.lblw.vphx.phms.domain.common.alert;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ProvincialSystemAlertExecution {

  private String systemIdentifier;
  private ProvincialSystemAlert provincialSystemAlert;
  private String windowStartTimeStamp;
  private String windowEndTimeStamp;
  private ProvincialSystemAlertExecutionStatus status;
  private String actionExecutionDetails;
  private List<ProvincialSystemAlertCriteriaEvaluation> provincialSystemAlertCriteriaEvaluations;
  private String dataAudit;
}
