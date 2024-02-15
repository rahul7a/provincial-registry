package com.lblw.vphx.phms.transformation.request.preprocessors;

import com.lblw.vphx.phms.domain.common.request.ProvincialRequest;
import com.lblw.vphx.phms.domain.common.request.hl7v3.Request;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * This class is the base request preProcessor for all transaction preProcessor(s) hl7v3.Request(s).
 * Will have common template PreProcessing across transactions
 */
@Component
public class BaseTemplateRequestTemplatePreProcessor
    implements RequestTemplatePreProcessor<Request<? extends ProvincialRequest>> {
  @Override
  public Mono<Request<? extends ProvincialRequest>> preProcess(
      Request<? extends ProvincialRequest> request) {
    return Mono.just(request);
  }
}
