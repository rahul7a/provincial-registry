package com.lblw.vphx.phms.rules.model;

import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.response.hl7v3.DetectedIssue;
import java.time.Instant;
import java.util.List;
import lombok.*;

/** PHMS object representation of a decision table's execution. */
@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@Builder
public class DroolsEngineResponseValidation {
  @NonNull private Province province;
  @NonNull private String identifierType;
  private String acknowledgementCode;
  private String acknowledgementDetailsCode;
  private String acknowledgementDetailsTypeCode;
  private String acknowledgementDetailsLocation;
  private String queryResponseCode;
  private List<DetectedIssue> detectedIssues;
  private int matchingIndex;
  private Instant consentValidityStartDateTime;
}
