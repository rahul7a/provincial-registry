package com.lblw.vphx.phms.domain.common.response.hl7v3;

import com.lblw.vphx.phms.domain.common.TargetPatient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ResponseControlAct {
  private String eventRoot;
  private String eventCorrelationId;
  private TargetPatient targetPatient;
}
