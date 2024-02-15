package com.lblw.vphx.phms.registry.processor.processors.common;

import com.lblw.vphx.phms.domain.common.DomainRequest;
import com.lblw.vphx.phms.domain.common.ProvincialRequestData;
import reactor.core.publisher.Mono;

/**
 * TODO: To be replacement for RequestPreProcessor<HL7v3.Request>
 *
 * <p>This interface exposes an API for preProcessing {@link DomainRequest}. PreProcessing includes
 * aggregations from external services into {@link ProvincialRequestData<DomainRequest>} before it
 * can be transformed into a raw template request
 */
public interface DomainRequestPreProcessor<
    R extends DomainRequest, S extends ProvincialRequestData<R>> {

  /**
   * Process request before invoking. Mutates request.
   *
   * @param request {@link DomainRequest } domain Request
   * @return {@link DomainRequest} after preProcessing
   */
  Mono<S> preProcess(R request);
}
