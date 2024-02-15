package com.lblw.vphx.phms.domain.common.alert;

import com.lblw.vphx.phms.domain.common.response.Severity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ProvincialSystemAlert {

  private String systemIdentifier;
  private String name;
  private ProvincialSystemAlertStatus status;
  private String startTimeStamp;
  private Action action;
  private ActionSuppressionType actionSuppressionType;
  private String snowTicketReference;
  private String webhookUrl;
  private ProvincialSystem provincialSystem;
  private Severity severity;
  private String windowSizeInMinutes;
  private ProvincialSystemAlertCriteria provincialSystemAlertCriteria;
  private CriteriaEvaluationType criteriaEvaluationType;
}
