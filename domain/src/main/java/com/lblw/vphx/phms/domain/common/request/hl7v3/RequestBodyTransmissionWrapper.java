package com.lblw.vphx.phms.domain.common.request.hl7v3;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestBodyTransmissionWrapper {
  private String transmissionUniqueIdentifier;
  private String transmissionCreationDateTime;
  private String processingCode;
  private String senderRoot;
  private String senderApplicationId;
  private String senderApplicationName;
}
