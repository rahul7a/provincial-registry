package com.lblw.vphx.phms.registry.processor.processors;

import com.lblw.vphx.phms.common.internal.services.ProvincialRequestControlEnrichService;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequest;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.common.request.hl7v3.Request;
import com.lblw.vphx.phms.registry.processor.processors.common.RequestPreProcessor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * This class is the base request preProcessor for all transaction preProcessor(s) hl7v3.Request(s).
 * Will have common PreProcessing across transactions
 */
@Component
public class BaseRequestPreProcessor
    implements RequestPreProcessor<Request<? extends ProvincialRequest>> {
  private static final String PROVINCIAL_LOCATION_ID_PLACEHOLDER = "$provincialLocationId";
  private final ProvincialRequestControlEnrichService provincialRequestControlEnrichService;

  public BaseRequestPreProcessor(
      ProvincialRequestControlEnrichService provincialRequestControlEnrichService) {
    this.provincialRequestControlEnrichService = provincialRequestControlEnrichService;
  }

  @Override
  public Mono<Request<? extends ProvincialRequest>> preProcess(
      Request<? extends ProvincialRequest> request) {
    return provincialRequestControlEnrichService
        .enrich()
        .then(Mono.deferContextual(Mono::just))
        .map(
            context -> {
              var provincialRequestControl = context.get(ProvincialRequestControl.class);
              request
                  .getProvincialRequestPayload()
                  .setProvincialRequestControl(provincialRequestControl);

              var pharmacyProvincialIdentifier =
                  provincialRequestControl
                      .getPharmacy()
                      .getProvincialLocation()
                      .getIdentifier()
                      .getValue();

              if (StringUtils.isBlank(pharmacyProvincialIdentifier)) {
                return request;
              }

              if (request
                  .getRequestBodyTransmissionWrapper()
                  .getSenderRoot()
                  .contains(PROVINCIAL_LOCATION_ID_PLACEHOLDER)) {
                request
                    .getRequestBodyTransmissionWrapper()
                    .setSenderRoot(
                        request
                            .getRequestBodyTransmissionWrapper()
                            .getSenderRoot()
                            .replace(
                                PROVINCIAL_LOCATION_ID_PLACEHOLDER, pharmacyProvincialIdentifier));
              }

              if (request
                  .getRequestControlAct()
                  .getEventRoot()
                  .contains(PROVINCIAL_LOCATION_ID_PLACEHOLDER)) {
                request
                    .getRequestControlAct()
                    .setEventRoot(
                        request
                            .getRequestControlAct()
                            .getEventRoot()
                            .replace(
                                PROVINCIAL_LOCATION_ID_PLACEHOLDER, pharmacyProvincialIdentifier));
              }
              return request;
            });
  }
}
