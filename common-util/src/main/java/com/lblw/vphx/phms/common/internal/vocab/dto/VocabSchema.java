package com.lblw.vphx.phms.common.internal.vocab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Data
/** Vocab Schema for RDS {@link VocabResponse} */
public class VocabSchema {
  @JsonProperty("hwCode")
  public String hwCode;

  @JsonProperty("hwDescription")
  public String hwDescription;

  @JsonProperty("hwDescriptionFrench")
  public String hwDescriptionFrench;

  @JsonProperty("cerxCode")
  public String cerxCode;

  @JsonProperty("cerxDisplayName")
  public String cerxDisplayName;

  @JsonProperty("cerxDisplayNameFrench")
  public String cerxDisplayNameFrench;
}
