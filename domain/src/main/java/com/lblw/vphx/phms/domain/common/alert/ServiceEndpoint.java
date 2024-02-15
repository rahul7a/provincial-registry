package com.lblw.vphx.phms.domain.common.alert;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceEndpoint {
  private String url;
  private TransportProtocol transportProtocol;
  private ServiceEndPointType type;
  private MessageStandard messageStandard;
}
