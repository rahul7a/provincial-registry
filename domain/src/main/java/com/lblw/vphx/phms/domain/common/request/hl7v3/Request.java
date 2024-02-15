package com.lblw.vphx.phms.domain.common.request.hl7v3;

import com.lblw.vphx.phms.domain.common.request.ProvincialRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/** Base Request for all Domain Requests within PHMS */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class Request<P extends ProvincialRequest> {
  private RequestHeader requestHeader;
  private RequestBodyTransmissionWrapper requestBodyTransmissionWrapper;
  private RequestControlAct requestControlAct;
  private P provincialRequestPayload;
}
