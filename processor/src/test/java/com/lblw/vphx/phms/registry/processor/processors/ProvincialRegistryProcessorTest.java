package com.lblw.vphx.phms.registry.processor.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.lblw.vphx.phms.common.internal.services.InternalLookupService;
import com.lblw.vphx.phms.common.internal.services.ProvincialRequestControlEnrichService;
import com.lblw.vphx.phms.common.logs.MessageLogger;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.common.request.hl7v3.RequestBodyTransmissionWrapper;
import com.lblw.vphx.phms.domain.common.request.hl7v3.RequestControlAct;
import com.lblw.vphx.phms.domain.common.response.OperationOutcome;
import com.lblw.vphx.phms.domain.common.response.ProvincialResponseAcknowledgement;
import com.lblw.vphx.phms.domain.common.response.ResponseStatus;
import com.lblw.vphx.phms.domain.common.response.Status;
import com.lblw.vphx.phms.domain.location.details.request.ProvincialLocationDetailsCriteria;
import com.lblw.vphx.phms.domain.location.details.request.hl7v3.ProvincialLocationDetailsRequest;
import com.lblw.vphx.phms.domain.location.details.response.ProvincialLocationDetails;
import com.lblw.vphx.phms.domain.location.details.response.hl7v3.ProvincialLocationDetailsResponse;
import com.lblw.vphx.phms.domain.location.request.ProvincialLocationSearchCriteria;
import com.lblw.vphx.phms.domain.location.request.hl7v3.ProvincialLocationSearchRequest;
import com.lblw.vphx.phms.domain.location.response.ProvincialLocationSummaries;
import com.lblw.vphx.phms.domain.location.response.hl7v3.ProvincialLocationSearchResponse;
import com.lblw.vphx.phms.domain.patient.consent.request.ProvincialPatientConsentCriteria;
import com.lblw.vphx.phms.domain.patient.consent.request.hl7v3.ProvincialPatientConsentRequest;
import com.lblw.vphx.phms.domain.patient.consent.response.ProvincialPatientConsent;
import com.lblw.vphx.phms.domain.patient.consent.response.hl7v3.ProvincialPatientConsentResponse;
import com.lblw.vphx.phms.domain.patient.request.ProvincialPatientSearchCriteria;
import com.lblw.vphx.phms.domain.patient.request.hl7v3.ProvincialPatientSearchRequest;
import com.lblw.vphx.phms.domain.patient.response.ProvincialPatientProfile;
import com.lblw.vphx.phms.domain.patient.response.hl7v3.ProvincialPatientSearchResponse;
import com.lblw.vphx.phms.domain.pharmacy.Pharmacist;
import com.lblw.vphx.phms.domain.pharmacy.Pharmacy;
import com.lblw.vphx.phms.domain.provider.request.ProvincialProviderSearchCriteria;
import com.lblw.vphx.phms.domain.provider.request.hl7v3.ProvincialProviderSearchRequest;
import com.lblw.vphx.phms.domain.provider.response.ProvincialProviderProfiles;
import com.lblw.vphx.phms.domain.provider.response.hl7v3.ProvincialProviderSearchResponse;
import com.lblw.vphx.phms.registry.processor.clientregistry.services.ClientRegistryService;
import com.lblw.vphx.phms.registry.processor.helpers.ProvincialResponseRuleEngineHelper;
import com.lblw.vphx.phms.registry.processor.mappers.*;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

/** Test cases to unit test {@link ProvincialRegistryProcessor} ProvincialRegistryProcessor */
class ProvincialRegistryProcessorTest {
  private ClientRegistryService clientRegistryService;
  private ClientRegistryPatientRequestResponseMapper clientRegistryPatientRequestResponseMapper;
  private ProvincialResponseRuleEngineHelper provincialResponseRuleEngineHelper;
  private ProvincialRegistryProcessor provincialRegistryProcessor;
  private ClientRegistryPatientConsentRequestResponseMapper
      clientRegistryPatientConsentRequestResponseMapper;
  private ClientRegistryProviderRequestResponseMapper clientRegistryProviderRequestResponseMapper;
  private ClientRegistryLocationSummaryRequestResponseMapper
      clientRegistryLocationSummaryRequestResponseMapper;
  private ClientRegistryLocationDetailsRequestResponseMapper
      clientRegistryLocationDetailsRequestResponseMapper;
  private BaseRequestPreProcessor baseRequestPreProcessor;
  private MessageLogger messageLogger;
  private InternalLookupService internalLookupService;
  private ProvincialRequestControlEnrichService provincialRequestControlEnrichService;
  private ProvincialRequestControl provincialRequestControl;
  private Context commonRequestContext;

  @BeforeEach
  public void beforeEach() {
    clientRegistryService = mock(ClientRegistryService.class);
    clientRegistryPatientRequestResponseMapper =
        mock(ClientRegistryPatientRequestResponseMapper.class);
    provincialResponseRuleEngineHelper = mock(ProvincialResponseRuleEngineHelper.class);
    clientRegistryPatientConsentRequestResponseMapper =
        mock(ClientRegistryPatientConsentRequestResponseMapper.class);
    clientRegistryProviderRequestResponseMapper =
        mock(ClientRegistryProviderRequestResponseMapper.class);
    clientRegistryLocationSummaryRequestResponseMapper =
        mock(ClientRegistryLocationSummaryRequestResponseMapper.class);
    clientRegistryLocationDetailsRequestResponseMapper =
        mock(ClientRegistryLocationDetailsRequestResponseMapper.class);
    internalLookupService = mock(InternalLookupService.class);
    provincialRequestControlEnrichService =
        new ProvincialRequestControlEnrichService(internalLookupService);
    baseRequestPreProcessor = new BaseRequestPreProcessor(provincialRequestControlEnrichService);
    messageLogger = mock(MessageLogger.class);

    provincialRegistryProcessor =
        new ProvincialRegistryProcessor(
            clientRegistryService,
            clientRegistryPatientRequestResponseMapper,
            provincialResponseRuleEngineHelper,
            clientRegistryProviderRequestResponseMapper,
            clientRegistryPatientConsentRequestResponseMapper,
            clientRegistryLocationSummaryRequestResponseMapper,
            clientRegistryLocationDetailsRequestResponseMapper,
            baseRequestPreProcessor,
            messageLogger);

    provincialRequestControl =
        ProvincialRequestControl.builder()
            .requestId("requestId")
            .pharmacy(Pharmacy.builder().id("pharmacyId").build())
            .build();

    commonRequestContext = Context.of(ProvincialRequestControl.class, provincialRequestControl);

    when(internalLookupService.fetchPharmacy(anyString()))
        .thenReturn(
            Mono.just(
                Pharmacy.builder()
                    .provincialLocation(
                        Pharmacy.ProvincialLocation.builder()
                            .identifier(
                                Pharmacy.Identifier.builder().value("provincialIdentifier").build())
                            .build())
                    .build()));
    when(internalLookupService.fetchDefaultPharmacist(any()))
        .thenReturn(Mono.just(Pharmacist.builder().build()));
  }

  @Test
  void whenProvincialPatientSearchResponseIsNotNull_ThenGivenFunctionsAreCalledAndVerifiedHere() {

    var domainRequest =
        ProvincialPatientSearchRequest.builder()
            .provincialRequestPayload(
                ProvincialPatientSearchCriteria.builder()
                    .provincialRequestControl(provincialRequestControl)
                    .build())
            .requestBodyTransmissionWrapper(
                RequestBodyTransmissionWrapper.builder()
                    .senderRoot("$provincialLocationId")
                    .build())
            .requestControlAct(
                RequestControlAct.builder().eventRoot("$provincialLocationId").build())
            .build();

    final ProvincialPatientSearchResponse droolResponse =
        ProvincialPatientSearchResponse.builder()
            .provincialResponsePayload(
                ProvincialPatientProfile.builder()
                    .provincialResponseAcknowledgement(
                        ProvincialResponseAcknowledgement.builder()
                            .operationOutcome(
                                OperationOutcome.builder()
                                    .status(
                                        ResponseStatus.builder()
                                            .text(Strings.EMPTY)
                                            .code(Status.ACCEPT)
                                            .build())
                                    .build())
                            .build())
                    .build())
            .build();
    when(clientRegistryPatientRequestResponseMapper.convertSearchCriteriaToRequest(any()))
        .thenReturn(domainRequest);

    when(clientRegistryService.searchProvincialPatient(any()))
        .thenReturn(Mono.just(new ProvincialPatientSearchResponse()));
    when(provincialResponseRuleEngineHelper.validatePatientSearchRequest(domainRequest))
        .thenReturn(droolResponse);
    ProvincialPatientSearchCriteria provincialPatientSearchCriteria =
        new ProvincialPatientSearchCriteria();
    final String correlationId = "correlationId";
    provincialPatientSearchCriteria.setProvincialRequestControl(
        ProvincialRequestControl.builder().requestId(correlationId).build());

    // Test
    final Mono<ProvincialPatientSearchResponse> actualResponse =
        provincialRegistryProcessor
            .searchProvincialPatientInClientRegistry(provincialPatientSearchCriteria)
            .contextWrite(commonRequestContext);
    StepVerifier.create(actualResponse)
        .assertNext(
            provincialPatientSearchResponse -> {
              verify(clientRegistryPatientRequestResponseMapper, times(1))
                  .convertSearchCriteriaToRequest(any());
              verify(clientRegistryService, times(1)).searchProvincialPatient(any());
            })
        .verifyComplete();
  }

  @Test
  void whenProvincialPatientDetailsResponseIsReceived_ThenGivenFunctionsAreCalledAndVerifiedHere() {

    var domainRequest =
        ProvincialLocationDetailsRequest.builder()
            .provincialRequestPayload(
                ProvincialLocationDetailsCriteria.builder()
                    .provincialRequestControl(provincialRequestControl)
                    .build())
            .requestBodyTransmissionWrapper(
                RequestBodyTransmissionWrapper.builder()
                    .senderRoot("$provincialLocationId")
                    .build())
            .requestControlAct(
                RequestControlAct.builder().eventRoot("$provincialLocationId").build())
            .build();

    ProvincialLocationDetailsCriteria provincialLocationDetailsCriteria =
        new ProvincialLocationDetailsCriteria();
    final String correlationId = "correlationId";
    provincialLocationDetailsCriteria.setProvincialRequestControl(
        ProvincialRequestControl.builder().requestId(correlationId).build());
    ProvincialLocationDetailsResponse provincialLocationDetailsResponse =
        ProvincialLocationDetailsResponse.builder()
            .provincialResponsePayload(
                ProvincialLocationDetails.builder()
                    .provincialResponseAcknowledgement(
                        ProvincialResponseAcknowledgement.builder()
                            .operationOutcome(
                                OperationOutcome.builder()
                                    .status(
                                        ResponseStatus.builder()
                                            .text(Strings.EMPTY)
                                            .code(Status.ACCEPT)
                                            .build())
                                    .build())
                            .build())
                    .build())
            .build();
    ProvincialProviderSearchResponse provincialProviderSearchResponse =
        ProvincialProviderSearchResponse.builder()
            .provincialResponsePayload(
                ProvincialProviderProfiles.builder()
                    .provincialResponseAcknowledgement(
                        ProvincialResponseAcknowledgement.builder()
                            .operationOutcome(
                                OperationOutcome.builder()
                                    .status(
                                        ResponseStatus.builder()
                                            .text(Strings.EMPTY)
                                            .code(Status.ACCEPT)
                                            .build())
                                    .build())
                            .build())
                    .build())
            .build();

    when(clientRegistryLocationDetailsRequestResponseMapper.convertSearchCriteriaToRequest(any()))
        .thenReturn(domainRequest);

    when(clientRegistryService.searchProvincialLocationDetails(any()))
        .thenReturn(Mono.just(new ProvincialLocationDetailsResponse()));

    when(provincialResponseRuleEngineHelper.validateLocationDetailsRequest(domainRequest))
        .thenReturn((provincialLocationDetailsResponse));

    when(provincialResponseRuleEngineHelper.validateLocationDetailsResponse(any()))
        .thenReturn(mock(ProvincialLocationDetailsResponse.class));

    when(provincialResponseRuleEngineHelper.validateProviderSearchRequest(any()))
        .thenReturn((provincialProviderSearchResponse));

    // Test
    final Mono<ProvincialLocationDetailsResponse> actualResponseMono =
        provincialRegistryProcessor
            .searchProvincialLocationDetailsInClientRegistry(provincialLocationDetailsCriteria)
            .contextWrite(commonRequestContext);
    StepVerifier.create(actualResponseMono)
        .assertNext(
            actualResponse -> {
              // assert
              verify(clientRegistryLocationDetailsRequestResponseMapper, times(1))
                  .convertSearchCriteriaToRequest(provincialLocationDetailsCriteria);
              verify(clientRegistryService, times(1)).searchProvincialLocationDetails(any());
            })
        .verifyComplete();
  }

  @Test
  void whenLocationDetailsRequestIsInvalid_thenReturnFailureResponse() {
    var domainRequest =
        ProvincialLocationDetailsRequest.builder()
            .provincialRequestPayload(
                ProvincialLocationDetailsCriteria.builder()
                    .provincialRequestControl(provincialRequestControl)
                    .build())
            .requestBodyTransmissionWrapper(
                RequestBodyTransmissionWrapper.builder()
                    .senderRoot("$provincialLocationId")
                    .build())
            .requestControlAct(
                RequestControlAct.builder().eventRoot("$provincialLocationId").build())
            .build();

    ProvincialLocationDetailsCriteria provincialLocationDetailsCriteria =
        new ProvincialLocationDetailsCriteria();
    final String correlationId = "correlationId";
    provincialLocationDetailsCriteria.setProvincialRequestControl(
        ProvincialRequestControl.builder().requestId(correlationId).build());
    ProvincialLocationDetailsResponse provincialLocationDetailsResponse =
        ProvincialLocationDetailsResponse.builder()
            .provincialResponsePayload(
                ProvincialLocationDetails.builder()
                    .provincialResponseAcknowledgement(
                        ProvincialResponseAcknowledgement.builder()
                            .operationOutcome(
                                OperationOutcome.builder()
                                    .status(
                                        ResponseStatus.builder()
                                            .text(Strings.EMPTY)
                                            .code(Status.REJECT)
                                            .build())
                                    .build())
                            .build())
                    .build())
            .build();

    when(clientRegistryLocationDetailsRequestResponseMapper.convertSearchCriteriaToRequest(any()))
        .thenReturn(domainRequest);

    when(provincialResponseRuleEngineHelper.validateLocationDetailsRequest(domainRequest))
        .thenReturn((provincialLocationDetailsResponse));

    // Test
    final Mono<ProvincialLocationDetailsResponse> actualResponseMono =
        provincialRegistryProcessor
            .searchProvincialLocationDetailsInClientRegistry(provincialLocationDetailsCriteria)
            .contextWrite(commonRequestContext);
    // assert
    StepVerifier.create(actualResponseMono)
        .assertNext(
            actualResponse ->
                assertEquals(
                    Status.REJECT,
                    actualResponse
                        .getProvincialResponsePayload()
                        .getProvincialResponseAcknowledgement()
                        .getOperationOutcome()
                        .getStatus()
                        .getCode()))
        .verifyComplete();
  }

  @Test
  void whenProvincialPatientSearchRequestIsInvalid() {
    var provincialPatientSearchCriteria =
        ProvincialPatientSearchCriteria.builder()
            .provincialRequestControl(provincialRequestControl)
            .build();
    var domainRequest =
        ProvincialPatientSearchRequest.builder()
            .provincialRequestPayload(provincialPatientSearchCriteria)
            .requestBodyTransmissionWrapper(
                RequestBodyTransmissionWrapper.builder()
                    .senderRoot("$provincialLocationId")
                    .build())
            .requestControlAct(
                RequestControlAct.builder().eventRoot("$provincialLocationId").build())
            .build();

    when(clientRegistryPatientRequestResponseMapper.convertSearchCriteriaToRequest(any()))
        .thenReturn(domainRequest);

    when(provincialResponseRuleEngineHelper.validatePatientSearchRequest(domainRequest))
        .thenReturn(
            ProvincialPatientSearchResponse.builder()
                .provincialResponsePayload(
                    ProvincialPatientProfile.builder()
                        .provincialResponseAcknowledgement(
                            ProvincialResponseAcknowledgement.builder()
                                .operationOutcome(
                                    OperationOutcome.builder()
                                        .status(
                                            ResponseStatus.builder()
                                                .text(Strings.EMPTY)
                                                .code(Status.REJECT)
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build());

    final Mono<ProvincialPatientSearchResponse> actualResponseMono =
        provincialRegistryProcessor
            .searchProvincialPatientInClientRegistry(provincialPatientSearchCriteria)
            .contextWrite(commonRequestContext);
    StepVerifier.create(actualResponseMono)
        .assertNext(
            provincialPatientSearchResponse ->
                assertEquals(
                    Status.REJECT,
                    provincialPatientSearchResponse
                        .getProvincialResponsePayload()
                        .getProvincialResponseAcknowledgement()
                        .getOperationOutcome()
                        .getStatus()
                        .getCode()))
        .verifyComplete();
  }

  @Test
  void whenProvincialLocationSearchRequestIsInvalid() {

    var provincialLocationSearchCriteria =
        ProvincialLocationSearchCriteria.builder()
            .provincialRequestControl(provincialRequestControl)
            .build();
    var domainRequest =
        ProvincialLocationSearchRequest.builder()
            .provincialRequestPayload(provincialLocationSearchCriteria)
            .requestBodyTransmissionWrapper(
                RequestBodyTransmissionWrapper.builder()
                    .senderRoot("$provincialLocationId")
                    .build())
            .requestControlAct(
                RequestControlAct.builder().eventRoot("$provincialLocationId").build())
            .build();

    when(clientRegistryLocationSummaryRequestResponseMapper.convertSearchCriteriaToRequest(any()))
        .thenReturn(domainRequest);

    when(provincialResponseRuleEngineHelper.validateLocationSearchRequest(domainRequest))
        .thenReturn(
            ProvincialLocationSearchResponse.builder()
                .provincialResponsePayload(
                    ProvincialLocationSummaries.builder()
                        .provincialResponseAcknowledgement(
                            ProvincialResponseAcknowledgement.builder()
                                .operationOutcome(
                                    OperationOutcome.builder()
                                        .status(
                                            ResponseStatus.builder()
                                                .text(Strings.EMPTY)
                                                .code(Status.REJECT)
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build());

    final Mono<ProvincialLocationSearchResponse> actualResponseMono =
        provincialRegistryProcessor
            .searchProvincialLocationInClientRegistry(provincialLocationSearchCriteria)
            .contextWrite(commonRequestContext);
    StepVerifier.create(actualResponseMono)
        .assertNext(
            provincialLocationSearchResponse ->
                assertEquals(
                    Status.REJECT,
                    provincialLocationSearchResponse
                        .getProvincialResponsePayload()
                        .getProvincialResponseAcknowledgement()
                        .getOperationOutcome()
                        .getStatus()
                        .getCode()))
        .verifyComplete();
  }

  @Test
  void whenProviderSearchRequestIsInvalid_thenReturnFailureResponse() {

    var domainRequest =
        ProvincialProviderSearchRequest.builder()
            .provincialRequestPayload(
                ProvincialProviderSearchCriteria.builder()
                    .provincialRequestControl(provincialRequestControl)
                    .build())
            .requestBodyTransmissionWrapper(
                RequestBodyTransmissionWrapper.builder()
                    .senderRoot("$provincialLocationId")
                    .build())
            .requestControlAct(
                RequestControlAct.builder().eventRoot("$provincialLocationId").build())
            .build();

    ProvincialProviderSearchCriteria provincialProviderSearchCriteria =
        new ProvincialProviderSearchCriteria();
    final String correlationId = "correlationId";
    provincialProviderSearchCriteria.setProvincialRequestControl(
        ProvincialRequestControl.builder().requestId(correlationId).build());
    ProvincialProviderSearchResponse provincialProviderSearchResponse =
        ProvincialProviderSearchResponse.builder()
            .provincialResponsePayload(
                ProvincialProviderProfiles.builder()
                    .provincialResponseAcknowledgement(
                        ProvincialResponseAcknowledgement.builder()
                            .operationOutcome(
                                OperationOutcome.builder()
                                    .status(
                                        ResponseStatus.builder()
                                            .text(Strings.EMPTY)
                                            .code(Status.REJECT)
                                            .build())
                                    .build())
                            .build())
                    .build())
            .build();

    when(clientRegistryProviderRequestResponseMapper.convertSearchCriteriaToRequest(any()))
        .thenReturn(domainRequest);

    when(provincialResponseRuleEngineHelper.validateProviderSearchRequest(domainRequest))
        .thenReturn((provincialProviderSearchResponse));

    // Test
    final Mono<ProvincialProviderSearchResponse> actualResponseMono =
        provincialRegistryProcessor
            .searchProvincialProviderInClientRegistry(provincialProviderSearchCriteria)
            .contextWrite(commonRequestContext);
    StepVerifier.create(actualResponseMono)
        .assertNext(
            actualResponse ->
                assertEquals(
                    Status.REJECT,
                    actualResponse
                        .getProvincialResponsePayload()
                        .getProvincialResponseAcknowledgement()
                        .getOperationOutcome()
                        .getStatus()
                        .getCode()))
        .verifyComplete();
  }

  @Test
  void whenPatientConsentRequestIsInvalid_thenReturnFailureResponse() {

    var domainRequest =
        ProvincialPatientConsentRequest.builder()
            .provincialRequestPayload(
                ProvincialPatientConsentCriteria.builder()
                    .provincialRequestControl(provincialRequestControl)
                    .build())
            .requestBodyTransmissionWrapper(
                RequestBodyTransmissionWrapper.builder()
                    .senderRoot("$provincialLocationId")
                    .build())
            .requestControlAct(
                RequestControlAct.builder().eventRoot("$provincialLocationId").build())
            .build();

    ProvincialPatientConsentCriteria provincialPatientConsentCriteria =
        new ProvincialPatientConsentCriteria();
    final String correlationId = "correlationId";
    provincialPatientConsentCriteria.setProvincialRequestControl(
        ProvincialRequestControl.builder().requestId(correlationId).build());
    ProvincialPatientConsentResponse provincialPatientConsentResponse =
        ProvincialPatientConsentResponse.builder()
            .provincialResponsePayload(
                ProvincialPatientConsent.builder()
                    .provincialResponseAcknowledgement(
                        ProvincialResponseAcknowledgement.builder()
                            .operationOutcome(
                                OperationOutcome.builder()
                                    .status(
                                        ResponseStatus.builder()
                                            .text(Strings.EMPTY)
                                            .code(Status.REJECT)
                                            .build())
                                    .build())
                            .build())
                    .build())
            .build();

    when(clientRegistryPatientConsentRequestResponseMapper.convertSearchCriteriaToRequest(any()))
        .thenReturn(domainRequest);

    when(provincialResponseRuleEngineHelper.validatePatientConsentRequest(domainRequest))
        .thenReturn((provincialPatientConsentResponse));

    // Test
    final Mono<ProvincialPatientConsentResponse> actualResponseMono =
        provincialRegistryProcessor
            .getProvincialPatientConsentInClientRegistry(provincialPatientConsentCriteria)
            .contextWrite(commonRequestContext);
    StepVerifier.create(actualResponseMono)
        .assertNext(
            actualResponse ->
                assertEquals(
                    Status.REJECT,
                    actualResponse
                        .getProvincialResponsePayload()
                        .getProvincialResponseAcknowledgement()
                        .getOperationOutcome()
                        .getStatus()
                        .getCode()))
        .verifyComplete();
  }

  @Test
  void whenProvincialProviderSearchResponseIsNotNull_ThenGivenFunctionsAreCalledAndVerifiedHere() {

    var domainRequest =
        ProvincialProviderSearchRequest.builder()
            .provincialRequestPayload(
                ProvincialProviderSearchCriteria.builder()
                    .provincialRequestControl(provincialRequestControl)
                    .build())
            .requestBodyTransmissionWrapper(
                RequestBodyTransmissionWrapper.builder()
                    .senderRoot("$provincialLocationId")
                    .build())
            .requestControlAct(
                RequestControlAct.builder().eventRoot("$provincialLocationId").build())
            .build();

    when(clientRegistryProviderRequestResponseMapper.convertSearchCriteriaToRequest(any()))
        .thenReturn(domainRequest);

    when(clientRegistryService.searchProvincialProvider(any()))
        .thenReturn(Mono.just(new ProvincialProviderSearchResponse()));
    when(provincialResponseRuleEngineHelper.validateProviderSearchRequest(domainRequest))
        .thenReturn(
            ProvincialProviderSearchResponse.builder()
                .provincialResponsePayload(
                    ProvincialProviderProfiles.builder()
                        .provincialResponseAcknowledgement(
                            ProvincialResponseAcknowledgement.builder()
                                .operationOutcome(
                                    OperationOutcome.builder()
                                        .status(
                                            ResponseStatus.builder()
                                                .text(Strings.EMPTY)
                                                .code(Status.ACCEPT)
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build());
    ProvincialProviderSearchCriteria provincialProviderSearchCriteria =
        new ProvincialProviderSearchCriteria();
    final String correlationId = "correlationId";
    provincialProviderSearchCriteria.setProvincialRequestControl(
        ProvincialRequestControl.builder().requestId(correlationId).build());

    when(provincialResponseRuleEngineHelper.validateProviderSearchResponse(any()))
        .thenReturn(mock(ProvincialProviderSearchResponse.class));

    // Test
    final Mono<ProvincialProviderSearchResponse> actualResponse =
        provincialRegistryProcessor
            .searchProvincialProviderInClientRegistry(provincialProviderSearchCriteria)
            .contextWrite(commonRequestContext);
    StepVerifier.create(actualResponse)
        .assertNext(
            provincialProviderSearchResponse -> {
              verify(clientRegistryProviderRequestResponseMapper, times(1))
                  .convertSearchCriteriaToRequest(any());
              verify(clientRegistryService, times(1)).searchProvincialProvider(any());
            })
        .verifyComplete();
  }

  @Test
  void whenPatientConsentSearchResponseIsNotNull_ThenGivenFunctionsAreCalledAndVerifiedHere() {

    var domainRequest =
        ProvincialPatientConsentRequest.builder()
            .provincialRequestPayload(
                ProvincialPatientConsentCriteria.builder()
                    .provincialRequestControl(provincialRequestControl)
                    .build())
            .requestBodyTransmissionWrapper(
                RequestBodyTransmissionWrapper.builder()
                    .senderRoot("$provincialLocationId")
                    .build())
            .requestControlAct(
                RequestControlAct.builder().eventRoot("$provincialLocationId").build())
            .build();

    when(clientRegistryPatientConsentRequestResponseMapper.convertSearchCriteriaToRequest(any()))
        .thenReturn(domainRequest);

    when(clientRegistryService.getProvincialPatientConsent(any()))
        .thenReturn(Mono.just(new ProvincialPatientConsentResponse()));

    when(provincialResponseRuleEngineHelper.validatePatientConsentRequest(domainRequest))
        .thenReturn(
            ProvincialPatientConsentResponse.builder()
                .provincialResponsePayload(
                    ProvincialPatientConsent.builder()
                        .provincialResponseAcknowledgement(
                            ProvincialResponseAcknowledgement.builder()
                                .operationOutcome(
                                    OperationOutcome.builder()
                                        .status(
                                            ResponseStatus.builder()
                                                .text(Strings.EMPTY)
                                                .code(Status.ACCEPT)
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build());
    ProvincialPatientConsentCriteria provincialPatientConsentCriteria =
        new ProvincialPatientConsentCriteria();
    final String correlationId = "correlationId";
    provincialPatientConsentCriteria.setProvincialRequestControl(
        ProvincialRequestControl.builder().requestId(correlationId).build());

    when(provincialResponseRuleEngineHelper.validateConsentSearchResponse(any()))
        .thenReturn(mock(ProvincialPatientConsentResponse.class));

    // Test
    final Mono<ProvincialPatientConsentResponse> actualResponse =
        provincialRegistryProcessor
            .getProvincialPatientConsentInClientRegistry(provincialPatientConsentCriteria)
            .contextWrite(commonRequestContext);
    StepVerifier.create(actualResponse)
        .assertNext(
            provincialPatientConsentResponse -> {
              verify(clientRegistryPatientConsentRequestResponseMapper, times(1))
                  .convertSearchCriteriaToRequest(any());
              verify(clientRegistryService, times(1)).getProvincialPatientConsent(any());
            })
        .verifyComplete();
  }

  @Test
  void whenLocationSummarySearchResponseIsNotNull_ThenGivenFunctionsAreCalledAndVerifiedHere() {

    var domainRequest =
        ProvincialLocationSearchRequest.builder()
            .provincialRequestPayload(
                ProvincialLocationSearchCriteria.builder()
                    .provincialRequestControl(provincialRequestControl)
                    .build())
            .requestBodyTransmissionWrapper(
                RequestBodyTransmissionWrapper.builder()
                    .senderRoot("$provincialLocationId")
                    .build())
            .requestControlAct(
                RequestControlAct.builder().eventRoot("$provincialLocationId").build())
            .build();

    when(clientRegistryLocationSummaryRequestResponseMapper.convertSearchCriteriaToRequest(any()))
        .thenReturn(domainRequest);

    when(clientRegistryService.searchProvincialLocationSummary(any()))
        .thenReturn(Mono.just(new ProvincialLocationSearchResponse()));

    when(provincialResponseRuleEngineHelper.validateLocationSearchRequest(domainRequest))
        .thenReturn(
            ProvincialLocationSearchResponse.builder()
                .provincialResponsePayload(
                    ProvincialLocationSummaries.builder()
                        .provincialResponseAcknowledgement(
                            ProvincialResponseAcknowledgement.builder()
                                .operationOutcome(
                                    OperationOutcome.builder()
                                        .status(
                                            ResponseStatus.builder()
                                                .text(Strings.EMPTY)
                                                .code(Status.ACCEPT)
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build());
    ProvincialLocationSearchCriteria provincialLocationSearchCriteria =
        new ProvincialLocationSearchCriteria();
    final String correlationId = "correlationId";
    provincialLocationSearchCriteria.setProvincialRequestControl(
        ProvincialRequestControl.builder().requestId(correlationId).build());

    when(provincialResponseRuleEngineHelper.validateLocationSearchResponse(any()))
        .thenReturn(mock(ProvincialLocationSearchResponse.class));

    // Test
    final Mono<ProvincialLocationSearchResponse> actualResponse =
        provincialRegistryProcessor
            .searchProvincialLocationInClientRegistry(provincialLocationSearchCriteria)
            .contextWrite(commonRequestContext);
    StepVerifier.create(actualResponse)
        .assertNext(
            provincialLocationSearchResponse -> {
              verify(clientRegistryLocationSummaryRequestResponseMapper, times(1))
                  .convertSearchCriteriaToRequest(any());
              verify(clientRegistryService, times(1)).searchProvincialLocationSummary(any());
            })
        .verifyComplete();
  }
}
