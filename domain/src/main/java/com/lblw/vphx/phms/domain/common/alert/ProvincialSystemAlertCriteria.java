package com.lblw.vphx.phms.domain.common.alert;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ProvincialSystemAlertCriteria {

  private String code;
  private String subCode;
  private String issueSourceType;
  private Operator operator;
  private String threshold;
}
