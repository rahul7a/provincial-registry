package com.lblw.vphx.phms.domain.common.response.hl7v3;

import com.lblw.vphx.phms.domain.common.response.ProvincialResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class Response<T extends ResponseControlAct, P extends ProvincialResponse> {
  private ResponseHeader responseHeader;
  private ResponseBodyTransmissionWrapper responseBodyTransmissionWrapper;
  private T responseControlAct;
  private P provincialResponsePayload;
  private QueryAcknowledgement queryAcknowledgement;
  private List<DetectedIssue> detectedIssues;
}
