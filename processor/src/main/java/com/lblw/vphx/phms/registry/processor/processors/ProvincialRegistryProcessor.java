package com.lblw.vphx.phms.registry.processor.processors;

import com.lblw.vphx.phms.common.logs.MessageLogger;
import com.lblw.vphx.phms.domain.common.logs.MessageType;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequest;
import com.lblw.vphx.phms.domain.common.request.hl7v3.Request;
import com.lblw.vphx.phms.domain.common.response.OperationOutcome;
import com.lblw.vphx.phms.domain.common.response.ProvincialResponse;
import com.lblw.vphx.phms.domain.common.response.Status;
import com.lblw.vphx.phms.domain.common.response.hl7v3.Response;
import com.lblw.vphx.phms.domain.common.response.hl7v3.ResponseControlAct;
import com.lblw.vphx.phms.domain.location.details.request.ProvincialLocationDetailsCriteria;
import com.lblw.vphx.phms.domain.location.details.request.hl7v3.ProvincialLocationDetailsRequest;
import com.lblw.vphx.phms.domain.location.details.response.hl7v3.ProvincialLocationDetailsResponse;
import com.lblw.vphx.phms.domain.location.request.ProvincialLocationSearchCriteria;
import com.lblw.vphx.phms.domain.location.request.hl7v3.ProvincialLocationSearchRequest;
import com.lblw.vphx.phms.domain.location.response.hl7v3.ProvincialLocationSearchResponse;
import com.lblw.vphx.phms.domain.patient.consent.request.ProvincialPatientConsentCriteria;
import com.lblw.vphx.phms.domain.patient.consent.request.hl7v3.ProvincialPatientConsentRequest;
import com.lblw.vphx.phms.domain.patient.consent.response.hl7v3.ProvincialPatientConsentResponse;
import com.lblw.vphx.phms.domain.patient.request.ProvincialPatientSearchCriteria;
import com.lblw.vphx.phms.domain.patient.request.hl7v3.ProvincialPatientSearchRequest;
import com.lblw.vphx.phms.domain.patient.response.hl7v3.ProvincialPatientSearchResponse;
import com.lblw.vphx.phms.domain.provider.request.ProvincialProviderSearchCriteria;
import com.lblw.vphx.phms.domain.provider.request.hl7v3.ProvincialProviderSearchRequest;
import com.lblw.vphx.phms.domain.provider.response.hl7v3.ProvincialProviderSearchResponse;
import com.lblw.vphx.phms.registry.processor.clientregistry.services.ClientRegistryService;
import com.lblw.vphx.phms.registry.processor.helpers.ProvincialResponseRuleEngineHelper;
import com.lblw.vphx.phms.registry.processor.mappers.*;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * This class processes incoming payloads by transforming them into HL7 V3 payloads, followed by an
 * exchange with {@link ClientRegistryService} for an HL7 V3 response payload, which is then
 * transformed into its corresponding domain model's response payload, which are then passed back.
 */
@Component
@Slf4j
public class ProvincialRegistryProcessor {

  private final ClientRegistryService clientRegistryService;
  private final ClientRegistryPatientRequestResponseMapper
      clientRegistryPatientRequestResponseMapper;
  private final ProvincialResponseRuleEngineHelper provincialResponseRuleEngineHelper;
  private final ClientRegistryProviderRequestResponseMapper
      clientRegistryProviderRequestResponseMapper;
  private final ClientRegistryPatientConsentRequestResponseMapper
      clientRegistryPatientConsentRequestResponseMapper;
  private final ClientRegistryLocationSummaryRequestResponseMapper
      clientRegistryLocationSummaryRequestResponseMapper;
  private final ClientRegistryLocationDetailsRequestResponseMapper
      clientRegistryLocationDetailsRequestResponseMapper;
  private final BaseRequestPreProcessor baseRequestPreProcessor;
  private final MessageLogger messageLogger;

  /**
   * Constructor that auto wires its dependencies.
   *
   * @param clientRegistryService An instance of {@link ClientRegistryService}
   * @param clientRegistryPatientRequestResponseMapper {@link
   *     ClientRegistryPatientRequestResponseMapper}
   * @param provincialResponseRuleEngineHelper {@link ProvincialResponseRuleEngineHelper}
   * @param clientRegistryProviderRequestResponseMapper An instance of {@link
   *     ClientRegistryProviderRequestResponseMapper}
   * @param clientRegistryLocationSummaryRequestResponseMapper {@link
   *     ClientRegistryLocationSummaryRequestResponseMapper}
   * @param baseRequestPreProcessor {@link BaseRequestPreProcessor}
   */
  @Autowired
  public ProvincialRegistryProcessor(
      ClientRegistryService clientRegistryService,
      ClientRegistryPatientRequestResponseMapper clientRegistryPatientRequestResponseMapper,
      ProvincialResponseRuleEngineHelper provincialResponseRuleEngineHelper,
      ClientRegistryProviderRequestResponseMapper clientRegistryProviderRequestResponseMapper,
      ClientRegistryPatientConsentRequestResponseMapper
          clientRegistryPatientConsentRequestResponseMapper,
      ClientRegistryLocationSummaryRequestResponseMapper
          clientRegistryLocationSummaryRequestResponseMapper,
      ClientRegistryLocationDetailsRequestResponseMapper
          clientRegistryLocationDetailsRequestResponseMapper,
      BaseRequestPreProcessor baseRequestPreProcessor,
      MessageLogger messageLogger) {
    this.clientRegistryService = clientRegistryService;
    this.clientRegistryPatientRequestResponseMapper = clientRegistryPatientRequestResponseMapper;
    this.provincialResponseRuleEngineHelper = provincialResponseRuleEngineHelper;
    this.clientRegistryProviderRequestResponseMapper = clientRegistryProviderRequestResponseMapper;
    this.clientRegistryPatientConsentRequestResponseMapper =
        clientRegistryPatientConsentRequestResponseMapper;
    this.clientRegistryLocationSummaryRequestResponseMapper =
        clientRegistryLocationSummaryRequestResponseMapper;
    this.clientRegistryLocationDetailsRequestResponseMapper =
        clientRegistryLocationDetailsRequestResponseMapper;
    this.baseRequestPreProcessor = baseRequestPreProcessor;
    this.messageLogger = messageLogger;
  }

  /**
   * This orchestrates the searching of Provincial Patient in Client Registry and returns the search
   * results back
   *
   * @param provincialPatientSearchCriteria {@link ProvincialPatientSearchCriteria} Domain model
   *     that encapsulates the search criteria for provincial patient search
   * @return provincialPatientSearchResponse {@link Mono} of {@link ProvincialPatientSearchResponse}
   *     Results of provincial patient search in Client Registry
   */
  public Mono<ProvincialPatientSearchResponse> searchProvincialPatientInClientRegistry(
      @NonNull ProvincialPatientSearchCriteria provincialPatientSearchCriteria) {
    var requestSource =
        Mono.fromCallable(
            () ->
                clientRegistryPatientRequestResponseMapper.convertSearchCriteriaToRequest(
                    provincialPatientSearchCriteria));

    return this.decorateProvincialRequestValidationWithMessageLogging(
            requestSource, provincialResponseRuleEngineHelper::validatePatientSearchRequest)
        .flatMap(
            pair -> {
              var request = pair.getLeft();
              var response = pair.getRight();

              if (havingResponseWithError(response)) {
                return Mono.just(response);
              }

              return baseRequestPreProcessor
                  .preProcess(request)
                  .cast(ProvincialPatientSearchRequest.class)
                  .flatMap(clientRegistryService::searchProvincialPatient);
            });
  }

  /**
   * This orchestrates the searching of Provincial Provider in Client Registry and returns the
   * search results back
   *
   * @param provincialProviderSearchCriteria {@link ProvincialProviderSearchCriteria}
   * @return {@link Mono} of {@link ProvincialProviderSearchResponse}
   */
  public Mono<ProvincialProviderSearchResponse> searchProvincialProviderInClientRegistry(
      @NonNull ProvincialProviderSearchCriteria provincialProviderSearchCriteria) {
    var requestSource =
        Mono.fromCallable(
            () ->
                clientRegistryProviderRequestResponseMapper.convertSearchCriteriaToRequest(
                    provincialProviderSearchCriteria));

    return this.decorateProvincialRequestValidationWithMessageLogging(
            requestSource, provincialResponseRuleEngineHelper::validateProviderSearchRequest)
        .flatMap(
            pair -> {
              var request = pair.getLeft();
              var response = pair.getRight();

              if (havingResponseWithError(response)) {
                return Mono.just(response);
              }

              return baseRequestPreProcessor
                  .preProcess(request)
                  .cast(ProvincialProviderSearchRequest.class)
                  .flatMap(clientRegistryService::searchProvincialProvider);
            });
  }
  /**
   * This orchestrates in getting of Provincial Patient Consent in Client Registry and returns
   * consent token back
   *
   * @param provincialPatientConsentCriteria {@link
   *     com.lblw.vphx.phms.domain.patient.consent.request.ProvincialPatientConsentCriteria}
   * @return {@link Mono} of {@link ProvincialPatientConsentResponse}
   */
  public Mono<ProvincialPatientConsentResponse> getProvincialPatientConsentInClientRegistry(
      @NonNull ProvincialPatientConsentCriteria provincialPatientConsentCriteria) {
    var requestSource =
        Mono.fromCallable(
            () ->
                clientRegistryPatientConsentRequestResponseMapper.convertSearchCriteriaToRequest(
                    provincialPatientConsentCriteria));

    return this.decorateProvincialRequestValidationWithMessageLogging(
            requestSource, provincialResponseRuleEngineHelper::validatePatientConsentRequest)
        .flatMap(
            pair -> {
              var request = pair.getLeft();
              var response = pair.getRight();

              if (havingResponseWithError(response)) {
                return Mono.just(response);
              }

              return baseRequestPreProcessor
                  .preProcess(request)
                  .cast(ProvincialPatientConsentRequest.class)
                  .flatMap(clientRegistryService::getProvincialPatientConsent);
            });
  }

  /**
   * This orchestrates the searching of Provincial Location in Client Registry and returns the
   * search results back
   *
   * @param provincialLocationSearchCriteria {@link ProvincialLocationSearchCriteria}
   * @return {@link Mono} of {@link ProvincialLocationSearchResponse}
   */
  public Mono<ProvincialLocationSearchResponse> searchProvincialLocationInClientRegistry(
      @NonNull ProvincialLocationSearchCriteria provincialLocationSearchCriteria) {
    var requestSource =
        Mono.fromCallable(
            () ->
                clientRegistryLocationSummaryRequestResponseMapper.convertSearchCriteriaToRequest(
                    provincialLocationSearchCriteria));

    return this.decorateProvincialRequestValidationWithMessageLogging(
            requestSource, provincialResponseRuleEngineHelper::validateLocationSearchRequest)
        .flatMap(
            pair -> {
              var request = pair.getLeft();
              var response = pair.getRight();

              if (havingResponseWithError(response)) {
                return Mono.just(response);
              }

              return baseRequestPreProcessor
                  .preProcess(request)
                  .cast(ProvincialLocationSearchRequest.class)
                  .flatMap(clientRegistryService::searchProvincialLocationSummary);
            });
  }

  /**
   * This orchestrates the searching of Provincial Location in Client Registry and returns the
   * search results back
   *
   * @param provincialLocationDetailsCriteria {@link ProvincialLocationDetailsCriteria}
   * @return provincialLocationDetailsResponse {@link ProvincialLocationDetailsResponse}
   */
  public Mono<ProvincialLocationDetailsResponse> searchProvincialLocationDetailsInClientRegistry(
      @NonNull ProvincialLocationDetailsCriteria provincialLocationDetailsCriteria) {
    var requestSource =
        Mono.fromCallable(
            () ->
                clientRegistryLocationDetailsRequestResponseMapper.convertSearchCriteriaToRequest(
                    provincialLocationDetailsCriteria));

    return this.decorateProvincialRequestValidationWithMessageLogging(
            requestSource, provincialResponseRuleEngineHelper::validateLocationDetailsRequest)
        .flatMap(
            pair -> {
              var request = pair.getLeft();
              var response = pair.getRight();

              if (havingResponseWithError(response)) {
                return Mono.just(response);
              }

              return baseRequestPreProcessor
                  .preProcess(request)
                  .cast(ProvincialLocationDetailsRequest.class)
                  .flatMap(clientRegistryService::searchProvincialLocationDetails);
            });
  }

  /**
   * This method check for Response having Error and return true if having error otherwise false
   *
   * @param response {@link Response}
   * @return boolean
   */
  private boolean havingResponseWithError(Response<?, ?> response) {
    return response
            .getProvincialResponsePayload()
            .getProvincialResponseAcknowledgement()
            .getOperationOutcome()
            .getStatus()
            .getCode()
        == Status.REJECT;
  }

  /**
   * This method helps to log the Domain Request after request Validation
   *
   * @param requestSource
   * @param validation
   * @param <P>
   * @param <Q>
   * @return
   */
  private <
          P extends Request<? extends ProvincialRequest>,
          Q extends Response<? extends ResponseControlAct, ? extends ProvincialResponse>>
      Mono<Pair<P, Q>> decorateProvincialRequestValidationWithMessageLogging(
          final Mono<P> requestSource, Function<P, Q> validation) {
    return Mono.deferContextual(
        context ->
            requestSource.map(
                request -> {
                  var response = validation.apply(request);

                  OperationOutcome operationOutcome =
                      response
                          .getProvincialResponsePayload()
                          .getProvincialResponseAcknowledgement()
                          .getOperationOutcome();

                  messageLogger.logMessage(
                      context,
                      MessageType.DOMAIN_REQUEST,
                      operationOutcome,
                      request.getProvincialRequestPayload());
                  return Pair.of(request, response);
                }));
  }
}
