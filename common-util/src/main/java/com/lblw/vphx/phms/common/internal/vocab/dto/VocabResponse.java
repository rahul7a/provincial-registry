package com.lblw.vphx.phms.common.internal.vocab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.*;

/**
 * VocabResponse derived from rds-service
 *
 * @see <a href="https://hwl-dis-referencedata-sit1.banting.lblw.cloud/swagger-ui/index.html">Schema
 *     > VocabResponse </a> TODO: Generalize it for all Provinces
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Data
public class VocabResponse {
  @JsonProperty("QC")
  private List<VocabSchema> quebecVocabularies;
}
