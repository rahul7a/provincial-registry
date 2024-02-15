package com.lblw.vphx.phms.domain.common.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuditEvent {
  private ProvincialRequestControl provincialRequestControl;

  /** Encrypted; may have PII or PHI data */
  @JsonIgnore private String rawRequest;

  @JsonIgnore private String requestTimestamp;

  /** Encrypted; may have PII or PHI data */
  @JsonIgnore private String rawResponse;

  @JsonIgnore private String responseTimestamp;
}
