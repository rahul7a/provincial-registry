package com.lblw.vphx.phms.domain.common.response.hl7v3;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseBodyTransmissionWrapper {
  private String transmissionUniqueIdentifier;
  private String acknowledgeTypeCode;
  private List<AcknowledgementDetails> acknowledgementDetails;
}
