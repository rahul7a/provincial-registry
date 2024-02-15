package com.lblw.vphx.phms.registry.processor.clientregistry.services;

import static com.lblw.vphx.phms.common.constants.MessageLogsConstants.MESSAGE_PAYLOAD_TEMPLATE_REQUEST_CONTEXT;

import com.lblw.vphx.phms.common.constants.MessageLogsConstants;
import com.lblw.vphx.phms.common.exceptions.ProvincialProcessorException;
import com.lblw.vphx.phms.common.logs.MessageLogger;
import com.lblw.vphx.phms.common.province.client.ProvincialWebClient;
import com.lblw.vphx.phms.configurations.ProvincialRequestProperties;
import com.lblw.vphx.phms.domain.common.MessageProcess;
import com.lblw.vphx.phms.domain.common.logs.MessagePayload;
import com.lblw.vphx.phms.domain.common.logs.MessageType;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequest;
import com.lblw.vphx.phms.domain.common.request.hl7v3.Request;
import com.lblw.vphx.phms.domain.common.response.*;
import com.lblw.vphx.phms.domain.common.response.hl7v3.Response;
import com.lblw.vphx.phms.domain.common.response.hl7v3.ResponseControlAct;
import com.lblw.vphx.phms.domain.location.details.request.hl7v3.ProvincialLocationDetailsRequest;
import com.lblw.vphx.phms.domain.location.details.response.hl7v3.ProvincialLocationDetailsResponse;
import com.lblw.vphx.phms.domain.location.request.hl7v3.ProvincialLocationSearchRequest;
import com.lblw.vphx.phms.domain.location.response.hl7v3.ProvincialLocationSearchResponse;
import com.lblw.vphx.phms.domain.patient.consent.request.hl7v3.ProvincialPatientConsentRequest;
import com.lblw.vphx.phms.domain.patient.consent.response.hl7v3.ProvincialPatientConsentResponse;
import com.lblw.vphx.phms.domain.patient.request.hl7v3.ProvincialPatientSearchRequest;
import com.lblw.vphx.phms.domain.patient.response.hl7v3.ProvincialPatientSearchResponse;
import com.lblw.vphx.phms.domain.provider.request.hl7v3.ProvincialProviderSearchRequest;
import com.lblw.vphx.phms.domain.provider.response.hl7v3.ProvincialProviderSearchResponse;
import com.lblw.vphx.phms.registry.processor.helpers.ProvincialResponseRuleEngineHelper;
import com.lblw.vphx.phms.transformation.services.TransformationService;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/** Implements calling DSQ Client registry */
@Slf4j
@Service
public class QuebecClientRegistryService implements ClientRegistryService {

  private static final String EXCEPTION_TRYING_TO_CONVERT_DOMAIN_TO_REQUEST =
      "Caught exception trying to convert domain to request.";

  private final TransformationService<?> transformationService;

  private final ProvincialRequestProperties provincialRequestProperties;
  private final MessageLogger messageLogger;

  private final ProvincialWebClient provincialWebClient;
  private final ProvincialResponseRuleEngineHelper provincialResponseRuleEngineHelper;
  /**
   * public constructor
   *
   * @param transformationService {@link TransformationService}
   * @param provincialRequestProperties {@link ProvincialRequestProperties}
   * @param messageLogger {@link MessageLogger}
   * @param provincialWebClient {@link ProvincialWebClient}
   * @param provincialResponseRuleEngineHelper {@link ProvincialResponseRuleEngineHelper}
   */
  public QuebecClientRegistryService(
      TransformationService<?> transformationService,
      ProvincialRequestProperties provincialRequestProperties,
      MessageLogger messageLogger,
      ProvincialWebClient provincialWebClient,
      ProvincialResponseRuleEngineHelper provincialResponseRuleEngineHelper) {
    this.transformationService = transformationService;
    this.provincialRequestProperties = provincialRequestProperties;
    this.messageLogger = messageLogger;
    this.provincialWebClient = provincialWebClient;
    this.provincialResponseRuleEngineHelper = provincialResponseRuleEngineHelper;
  }
  /**
   * This interacts with DSQ Client Registry Service, in order to query for a provincial patient
   * using HL7 V3 request payload and returns the corresponding HL7 V3 response.
   *
   * @param provincialPatientSearchRequest {@link ProvincialPatientSearchRequest} encapsulates the
   *     HL7 V3's Search provincial patient template, populated with search criteria.
   * @return {@link Mono} of {@link ProvincialPatientSearchResponse} encapsulates the HL7 V3
   *     Response for Provincial patient template.
   */
  @Override
  public Mono<ProvincialPatientSearchResponse> searchProvincialPatient(
      ProvincialPatientSearchRequest provincialPatientSearchRequest) {
    final var transactionProperties =
        provincialRequestProperties
            .getRequest()
            .getTransaction()
            .get(MessageProcess.PATIENT_SEARCH.getName());
    return getProvincialResponse(
        provincialPatientSearchRequest,
        transactionProperties,
        response ->
            response.map(provincialResponseRuleEngineHelper::validatePatientSearchResponse));
  }
  /**
   * This interacts with DSQ Client Registry Service, in order to query for a provincial provider
   * using HL7 V3 request payload and returns the corresponding HL7 V3 response.
   *
   * @param provincialProviderSearchRequest {@link ProvincialProviderSearchRequest} encapsulates the
   *     HL7 V3's Search provincial provider template, populated with search criteria.
   * @return {@link Mono} of {@link ProvincialProviderSearchResponse}
   */
  @Override
  public Mono<ProvincialProviderSearchResponse> searchProvincialProvider(
      ProvincialProviderSearchRequest provincialProviderSearchRequest) {
    final var transactionProperties =
        provincialRequestProperties
            .getRequest()
            .getTransaction()
            .get(MessageProcess.PROVIDER_SEARCH.getName());
    return getProvincialResponse(
        provincialProviderSearchRequest,
        transactionProperties,
        response ->
            response.map(provincialResponseRuleEngineHelper::validateProviderSearchResponse));
  }
  /**
   * This interacts with DSQ Client Registry Service, in order to get a provincial patient consent
   * token using HL7 V3 request payload and returns the corresponding HL7 V3 response.
   *
   * @param provincialPatientConsentRequest {@link ProvincialPatientConsentRequest} encapsulates the
   *     HL7 V3's provincial patient Consent template, populated with criteria.
   * @return {@link Mono} of {@link ProvincialPatientConsentResponse}
   */
  @Override
  public Mono<ProvincialPatientConsentResponse> getProvincialPatientConsent(
      ProvincialPatientConsentRequest provincialPatientConsentRequest) {
    final var transactionProperties =
        provincialRequestProperties
            .getRequest()
            .getTransaction()
            .get(MessageProcess.PATIENT_CONSENT.getName());
    return getProvincialResponse(
        provincialPatientConsentRequest,
        transactionProperties,
        response ->
            response.map(provincialResponseRuleEngineHelper::validateConsentSearchResponse));
  }

  @Override
  public Mono<ProvincialLocationSearchResponse> searchProvincialLocationSummary(
      ProvincialLocationSearchRequest provincialLocationSearchRequest) {
    final var transactionProperties =
        provincialRequestProperties
            .getRequest()
            .getTransaction()
            .get(MessageProcess.LOCATION_SEARCH.getName());
    return getProvincialResponse(
        provincialLocationSearchRequest,
        transactionProperties,
        response ->
            response.map(provincialResponseRuleEngineHelper::validateLocationSearchResponse));
  }
  /**
   * This interacts with DSQ Client Registry Service, in order to query for a location details using
   * HL7 V3 request payload and returns the corresponding HL7 V3 response.
   *
   * @param request {@link ProvincialLocationDetailsRequest} encapsulates the HL7 V3's Search
   *     location details template, populated with search criteria.
   * @return {@link Mono} of {@link ProvincialLocationDetailsResponse} encapsulates the HL7 V3
   *     Response for location details template.
   */
  @Override
  public Mono<ProvincialLocationDetailsResponse> searchProvincialLocationDetails(
      final ProvincialLocationDetailsRequest request) {
    final var transactionProperties =
        provincialRequestProperties
            .getRequest()
            .getTransaction()
            .get(MessageProcess.LOCATION_DETAILS.getName());
    return getProvincialResponse(
        request,
        transactionProperties,
        response ->
            response.map(provincialResponseRuleEngineHelper::validateLocationDetailsResponse));
  }
  /**
   * Utility method to decorate a request with {@link * MessageLogger#logMessage} side effects
   *
   * <p>The function is generic, so it can be used with any type of request. The request source is a
   * Mono of the request type, and the request transformation is a function that takes a request and
   * returns a Mono of the raw request
   *
   * @param requestSource A Mono<Req> that represents the request that will be transformed.
   * @param requestTransformation This is the function that will be used to convert the request into
   *     a string.
   * @return A Mono<String>
   */
  private <R extends Request<? extends ProvincialRequest>>
      Mono<String> decorateRequestTransformWithMessageLogging(
          Mono<R> requestSource, Function<R, Mono<String>> requestTransformation) {
    return Mono.deferContextual(
            context ->
                requestSource.flatMap(
                    request ->
                        requestTransformation
                            .apply(request)
                            .doOnNext(
                                rawRequest ->
                                    messageLogger.logMessage(
                                        context,
                                        MessageType.PROVINCIAL_REQUEST,
                                        OperationOutcome.builder()
                                            .status(
                                                ResponseStatus.builder()
                                                    .code(Status.ACCEPT)
                                                    .text(Strings.EMPTY)
                                                    .build())
                                            .build(),
                                        request.getProvincialRequestPayload(),
                                        MessagePayload.builder()
                                            .value(rawRequest)
                                            .messagePayloadTemplate(
                                                context.getOrDefault(
                                                    MESSAGE_PAYLOAD_TEMPLATE_REQUEST_CONTEXT, null))
                                            .build()))
                            .doOnError(
                                e ->
                                    messageLogger.logMessage(
                                        context,
                                        MessageType.PROVINCIAL_REQUEST,
                                        OperationOutcome.builder()
                                            .status(
                                                ResponseStatus.builder()
                                                    .code(Status.REJECT)
                                                    .text(Strings.EMPTY)
                                                    .build())
                                            .build(),
                                        request.getProvincialRequestPayload()))
                            .onErrorMap(
                                e ->
                                    new ProvincialProcessorException(
                                        EXCEPTION_TRYING_TO_CONVERT_DOMAIN_TO_REQUEST, e))))
        .contextWrite(
            context ->
                context.putAll(
                    Context.of(
                            MessageLogsConstants.LOG_SOURCE_CONTEXT,
                                com.lblw.vphx.phms.domain.common.Service.builder()
                                    .code(ServiceCode.VPHX_PHMS)
                                    .serviceOutageStatus(Strings.EMPTY)
                                    .build(),
                            MessageLogsConstants.LOG_TARGET_CONTEXT,
                                com.lblw.vphx.phms.domain.common.Service.builder()
                                    .code(ServiceCode.QC_REGISTRIES)
                                    .serviceOutageStatus(Strings.EMPTY)
                                    .build())
                        .readOnly()));
  }

  /**
   * Given a raw response, transform it into a domain response and log the transformation
   *
   * @param rawResponse The raw response from the API
   * @return A Mono<Res>
   */
  private <S extends Response<? extends ResponseControlAct, ? extends ProvincialResponse>>
      Mono<S> buildDomainResponseFromRawResponse(
          final String rawResponse, final UnaryOperator<Mono<S>> postProcess) {
    return decorateResponseTransformWithMessageLogging(
        Mono.just(rawResponse),
        response ->
            transformationService
                .transformResponse(response)
                .map(domainResponse -> (S) domainResponse)
                .transform(postProcess));
  }

  /**
   * Takes a provincial request, and returns a provincial response
   *
   * @param provincialRequest The request object that will be sent to the provincial system.
   * @param transactionProperties The configuration for the provincial transaction.
   * @return A Mono<Res>
   */
  private <
          R extends Request<? extends ProvincialRequest>,
          S extends Response<? extends ResponseControlAct, ? extends ProvincialResponse>>
      Mono<S> getProvincialResponse(
          final R provincialRequest,
          final ProvincialRequestProperties.TransactionProperties transactionProperties,
          final UnaryOperator<Mono<S>> postProcess) {
    return buildRawRequestFromDomainRequest(provincialRequest)
        .flatMap(
            rawRequest -> {
              HttpHeaders headers = new HttpHeaders();
              headers.set(
                  transactionProperties.getHeader().getSoapAction(),
                  transactionProperties.getHeader().getValue());
              return provincialWebClient
                  .callClientRegistry(transactionProperties.getUri(), headers, rawRequest)
                  .map(HttpEntity::getBody);
            })
        .flatMap(response -> buildDomainResponseFromRawResponse(response, postProcess));
  }

  /**
   * Given a domain request, transform it into a raw request and log the transformation
   *
   * @param domainRequest The request object that is passed in from the client.
   * @return A Mono<String>
   */
  private <R extends Request<? extends ProvincialRequest>>
      Mono<String> buildRawRequestFromDomainRequest(final R domainRequest) {
    return decorateRequestTransformWithMessageLogging(
        Mono.just(domainRequest), transformationService::transformRequest);
  }

  /**
   * Utility method to decorate a response with {@link * MessageLogger#logMessage} side effects
   *
   * @param responseSource A Mono<String> that represents the raw response from the provincial DIS.
   * @param responseTransformation a function that takes a String (the raw response) and returns a
   *     Mono<Res> (the transformed response)
   * @return A Mono<Res>
   */
  private <R extends Response> Mono<R> decorateResponseTransformWithMessageLogging(
      Mono<String> responseSource, Function<String, Mono<R>> responseTransformation) {
    return Mono.deferContextual(
            context ->
                responseSource.flatMap(
                    rawResponse ->
                        responseTransformation
                            .apply(rawResponse)
                            .doOnNext(
                                response ->
                                    messageLogger.logMessage(
                                        context,
                                        MessageType.DOMAIN_RESPONSE,
                                        response
                                            .getProvincialResponsePayload()
                                            .getProvincialResponseAcknowledgement()
                                            .getOperationOutcome(),
                                        response.getProvincialResponsePayload(),
                                        MessagePayload.builder().value(rawResponse).build()))
                            .onErrorMap(
                                e ->
                                    new ProvincialProcessorException(
                                        EXCEPTION_TRYING_TO_CONVERT_DOMAIN_TO_REQUEST, e))))
        .contextWrite(
            context -> {
              var requestSource =
                  context.getOrDefault(
                      MessageLogsConstants.LOG_SOURCE_CONTEXT,
                      com.lblw.vphx.phms.domain.common.Service.builder()
                          .code(ServiceCode.UNKNOWN)
                          .serviceOutageStatus(Strings.EMPTY)
                          .build());
              var responseTarget = requestSource;

              return context.putAll(
                  Context.of(
                          MessageLogsConstants.LOG_SOURCE_CONTEXT,
                          com.lblw.vphx.phms.domain.common.Service.builder()
                              .code(ServiceCode.QC_REGISTRIES)
                              .serviceOutageStatus(Strings.EMPTY)
                              .build(),
                          MessageLogsConstants.LOG_TARGET_CONTEXT,
                          responseTarget)
                      .readOnly());
            });
  }
}
