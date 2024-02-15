package com.lblw.vphx.phms.transformation.request.preprocessors;

import com.lblw.vphx.phms.domain.common.request.ProvincialRequest;
import com.lblw.vphx.phms.domain.common.request.hl7v3.Request;
import reactor.core.publisher.Mono;

/** TODO: RequestPreProcessors to target ProvincialRequestData instead of hl7v3.Request */
public interface RequestTemplatePreProcessor<R extends Request<? extends ProvincialRequest>> {
  /**
   * Process request before invoking
   *
   * @param request {@link Request} Response is hl7-v3
   * @return {@link Request}after the transaction
   */
  Mono<R> preProcess(R request);
}
