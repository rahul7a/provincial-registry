package com.lblw.vphx.phms.domain.common.request.continuation;

import com.lblw.vphx.phms.domain.common.request.ProvincialRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProvincialSearchContinuationCriteria extends ProvincialRequest {
  @Schema(description = "Query id of the original request message", required = true)
  private String queryId;

  @Schema(hidden = true)
  private Integer pageSize;

  @Schema(description = "Page index of the paginated result", required = true, minimum = "1")
  private Integer pageIndex;
}
