package com.lblw.vphx.phms.registry.processor.processors.common;

import com.lblw.vphx.phms.domain.common.request.ProvincialRequest;
import com.lblw.vphx.phms.domain.common.request.hl7v3.Request;
import reactor.core.publisher.Mono;

/**
 * TODO: To be replaced with RequestPreProcessor<DomainRequest.Request>
 *
 * <p>This interface exposes an API for preProcessing requests. PreProcessing includes compositions
 * and aggregations from external services before it can be transformed into an HL7v3 XML request
 */
public interface RequestPreProcessor<R extends Request<? extends ProvincialRequest>> {
  /**
   * Process request before invoking. Mutates request.
   *
   * @param request {@link Request} hl7v3 Request
   * @return {@link Request} after preProcessing
   */
  Mono<R> preProcess(R request);
}
