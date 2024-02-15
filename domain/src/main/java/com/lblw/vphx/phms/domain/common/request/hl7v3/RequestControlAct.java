package com.lblw.vphx.phms.domain.common.request.hl7v3;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class RequestControlAct {
  private String eventRoot;
  private String eventCorrelationId;
}
