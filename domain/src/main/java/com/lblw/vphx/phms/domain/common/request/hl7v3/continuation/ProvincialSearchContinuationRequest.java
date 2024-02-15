package com.lblw.vphx.phms.domain.common.request.hl7v3.continuation;

import com.lblw.vphx.phms.domain.common.request.continuation.ProvincialSearchContinuationCriteria;
import com.lblw.vphx.phms.domain.common.request.hl7v3.Request;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ProvincialSearchContinuationRequest<P extends ProvincialSearchContinuationCriteria>
    extends Request<P> {
  private Integer startResultNumber;
}
