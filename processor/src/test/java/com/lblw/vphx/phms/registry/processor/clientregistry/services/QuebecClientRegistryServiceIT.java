package com.lblw.vphx.phms.registry.processor.clientregistry.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.lblw.vphx.iams.securityauditengine.oauth.ApplicationProperties;
import com.lblw.vphx.iams.securityauditengine.oauth.OAuthClientCredentials;
import com.lblw.vphx.phms.common.coding.services.CodeableConceptRefDataService;
import com.lblw.vphx.phms.common.coding.services.CodeableConceptService;
import com.lblw.vphx.phms.common.config.MongoConfig;
import com.lblw.vphx.phms.common.constants.HL7Constants;
import com.lblw.vphx.phms.common.exceptions.ProvincialTimeoutException;
import com.lblw.vphx.phms.common.internal.config.InternalApiConfig;
import com.lblw.vphx.phms.common.internal.objectstorage.client.ObjectStorageClient;
import com.lblw.vphx.phms.common.internal.objectstorage.client.ObjectStorageClientConfig;
import com.lblw.vphx.phms.common.internal.objectstorage.service.ObjectStorageService;
import com.lblw.vphx.phms.common.internal.services.InternalLookupService;
import com.lblw.vphx.phms.common.internal.services.ProvincialRequestControlEnrichService;
import com.lblw.vphx.phms.common.logs.MessageLogger;
import com.lblw.vphx.phms.common.province.client.ProvincialWebClient;
import com.lblw.vphx.phms.common.province.config.ProvincialWebClientConfiguration;
import com.lblw.vphx.phms.common.security.services.OAuthClientService;
import com.lblw.vphx.phms.common.security.services.SecurityService;
import com.lblw.vphx.phms.common.utils.UUIDGenerator;
import com.lblw.vphx.phms.common.utils.XmlParsingUtils;
import com.lblw.vphx.phms.configurations.ProvincialRequestProperties;
import com.lblw.vphx.phms.domain.coding.Coding;
import com.lblw.vphx.phms.domain.coding.LanguageCode;
import com.lblw.vphx.phms.domain.coding.LocalizedText;
import com.lblw.vphx.phms.domain.common.*;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequest;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.common.request.hl7v3.Request;
import com.lblw.vphx.phms.domain.common.request.hl7v3.RequestBodyTransmissionWrapper;
import com.lblw.vphx.phms.domain.common.request.hl7v3.RequestControlAct;
import com.lblw.vphx.phms.domain.common.request.hl7v3.RequestHeader;
import com.lblw.vphx.phms.domain.common.response.*;
import com.lblw.vphx.phms.domain.common.response.hl7v3.*;
import com.lblw.vphx.phms.domain.location.details.request.ProvincialLocationDetailsCriteria;
import com.lblw.vphx.phms.domain.location.details.request.hl7v3.ProvincialLocationDetailsRequest;
import com.lblw.vphx.phms.domain.location.request.ProvincialLocationSearchCriteria;
import com.lblw.vphx.phms.domain.location.request.hl7v3.ProvincialLocationSearchRequest;
import com.lblw.vphx.phms.domain.patient.consent.request.ProvincialPatientConsentCriteria;
import com.lblw.vphx.phms.domain.patient.consent.request.hl7v3.ProvincialPatientConsentRequest;
import com.lblw.vphx.phms.domain.patient.request.ProvincialPatientSearchCriteria;
import com.lblw.vphx.phms.domain.patient.request.hl7v3.ProvincialPatientSearchRequest;
import com.lblw.vphx.phms.domain.patient.response.ProvincialPatientProfile;
import com.lblw.vphx.phms.domain.patient.response.hl7v3.ProvincialPatientSearchResponse;
import com.lblw.vphx.phms.domain.patient.response.hl7v3.ProvincialPatientSearchResponseControlAct;
import com.lblw.vphx.phms.domain.pharmacy.Pharmacy;
import com.lblw.vphx.phms.domain.provider.request.ProvincialProviderSearchCriteria;
import com.lblw.vphx.phms.domain.provider.request.hl7v3.ProvincialProviderSearchRequest;
import com.lblw.vphx.phms.registry.processor.helpers.ProvincialResponseRuleEngineHelper;
import com.lblw.vphx.phms.registry.processor.processors.BaseRequestPreProcessor;
import com.lblw.vphx.phms.transformation.request.RequestContextFactory;
import com.lblw.vphx.phms.transformation.request.RequestTransformerEngine;
import com.lblw.vphx.phms.transformation.request.preprocessors.BaseTemplateRequestTemplatePreProcessor;
import com.lblw.vphx.phms.transformation.response.ResponseTransformerEngine;
import com.lblw.vphx.phms.transformation.response.helpers.CommonResponseTransformerHelper;
import com.lblw.vphx.phms.transformation.response.transformers.DefaultResponseTransformer;
import com.lblw.vphx.phms.transformation.response.transformers.LocationDetailsResponseTransformer;
import com.lblw.vphx.phms.transformation.response.transformers.locationsummary.LocationSummaryResponseXPathTransformer;
import com.lblw.vphx.phms.transformation.response.transformers.patientconsent.PatientConsentResponseXPathTransformer;
import com.lblw.vphx.phms.transformation.response.transformers.patientsearch.PatientSearchResponseXPathTransformer;
import com.lblw.vphx.phms.transformation.response.transformers.providersearch.ProviderSearchResponseXPathTransformer;
import com.lblw.vphx.phms.transformation.services.TransformationService;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.reactive.function.client.WebClient;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

/** Integration tests for {@link QuebecClientRegistryService} */
@DataMongoTest
@ActiveProfiles("test")
@ContextConfiguration(
    classes = {
      QuebecClientRegistryService.class,
      WebClient.class,
      ApplicationProperties.class,
      OAuthClientCredentials.class,
      OAuthClientService.class,
      ObjectStorageClientConfig.class,
      ObjectStorageClient.class,
      ObjectStorageService.class,
      InternalApiConfig.class,
      SecurityService.class,
      TransformationService.class,
      BaseTemplateRequestTemplatePreProcessor.class,
      ThymeleafAutoConfiguration.class,
      UUIDGenerator.class,
      RequestTransformerEngine.class,
      ResponseTransformerEngine.class,
      XmlParsingUtils.class,
      CommonResponseTransformerHelper.class,
      ProvincialRequestProperties.class,
      RequestContextFactory.class,
      CodeableConceptRefDataService.class,
      CodeableConceptService.class,
      MongoConfig.class,
      PatientSearchResponseXPathTransformer.class,
      ProviderSearchResponseXPathTransformer.class,
      PatientConsentResponseXPathTransformer.class,
      LocationSummaryResponseXPathTransformer.class,
      LocationDetailsResponseTransformer.class,
      DefaultResponseTransformer.class,
      BaseRequestPreProcessor.class,
      InternalLookupService.class,
      BaseTemplateRequestTemplatePreProcessor.class,
      ProvincialWebClientConfiguration.class,
      ProvincialWebClient.class,
      ProvincialRequestControlEnrichService.class
    })
class QuebecClientRegistryServiceIT {
  private static final String SOAP_ACTION = "SOAPAction";

  @MockBean
  @Qualifier("objectStorageWebClient")
  WebClient objectStorageWebClient;

  @MockBean ObjectStorageClient objectStorageClient;
  @MockBean CodeableConceptService codeableConceptService;
  @MockBean MessageLogger messageLogger;
  @MockBean InternalLookupService internalLookupService;
  @MockBean ProvincialResponseRuleEngineHelper provincialResponseRuleEngineHelper;

  @MockBean
  @Qualifier("oauth-web-client")
  WebClient webClient;

  @MockBean OAuthClientService oAuthClientService;
  @Autowired private SecurityService securityService;
  @Autowired private BaseRequestPreProcessor baseRequestPreProcessor;
  @Autowired private ProvincialRequestControlEnrichService provincialRequestControlEnrichService;
  @Autowired private QuebecClientRegistryService quebecClientRegistryService;
  @Autowired private TransformationService<?> transformationService;
  @Autowired private UUIDGenerator uuidGenerator;
  @Autowired private ProvincialRequestProperties provincialRequestProperties;

  @Autowired private ProvincialWebClientConfiguration provincialWebClientConfiguration;

  @Autowired
  @Qualifier("provincial")
  private WebClient provincialWebclient;

  private ProvincialWebClient provincialWebClient;

  private void configureTimeOut(int timeoutValue) {
    provincialRequestProperties.getRequest().getWebclient().setReadTimeOut(timeoutValue);
    provincialRequestProperties.getRequest().getWebclient().setConnectionTimeOut(timeoutValue);
    provincialRequestProperties.getRequest().getWebclient().setResponseTimeOut(timeoutValue);
    provincialRequestProperties.getRequest().getWebclient().setWriteTimeOut(timeoutValue);
    provincialWebClient =
        new ProvincialWebClient(
            new ProvincialWebClientConfiguration()
                .webClient(securityService, provincialRequestProperties));
  }

  @BeforeEach
  void beforeEach() {
    Answer<Response<? extends ResponseControlAct, ? extends ProvincialResponse>> identityAnswer =
        answer -> {
          var response = (Response) answer.getArgument(0);
          response
              .getProvincialResponsePayload()
              .setProvincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .operationOutcome(OperationOutcome.builder().build())
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(
                                  ProvincialRequestControl.builder()
                                      .requestId("Patient-generated-id")
                                      .build())
                              .build())
                      .build());
          return response;
        };
    when(provincialResponseRuleEngineHelper.validatePatientSearchResponse(any()))
        .thenAnswer(identityAnswer);
    when(provincialResponseRuleEngineHelper.validateConsentSearchResponse(any()))
        .thenAnswer(identityAnswer);
    when(provincialResponseRuleEngineHelper.validateLocationDetailsResponse(any()))
        .thenAnswer(identityAnswer);
    when(provincialResponseRuleEngineHelper.validateLocationSearchResponse(any()))
        .thenAnswer(identityAnswer);
    when(provincialResponseRuleEngineHelper.validateProviderSearchResponse(any()))
        .thenAnswer(identityAnswer);
  }

  @Test
  void verify_webclient_configuration_properties_DSQ() {
    assertThat(provincialRequestProperties.getRequest().getWebclient().getResponseTimeOut())
        .isEqualTo(1000);
    assertThat(provincialRequestProperties.getRequest().getWebclient().getConnectionTimeOut())
        .isEqualTo(1000);
    assertThat(provincialRequestProperties.getRequest().getWebclient().getReadTimeOut())
        .isEqualTo(500);
    assertThat(provincialRequestProperties.getRequest().getWebclient().getWriteTimeOut())
        .isEqualTo(500);
    assertThat(
            provincialRequestProperties.getRequest().getWebclient().getResponseMemoryLimitInBytes())
        .isEqualTo(1000000);
  }

  @Nested
  @DisplayName("Test When province calls timeout values are low")
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class TestWhenProvinceCallsTimeoutLow {
    ProvincialPatientConsentRequest provincialPatientConsentRequest;
    ProvincialLocationSearchRequest provincialLocationSearchRequest;
    ProvincialPatientSearchRequest provincialPatientSearchRequest;
    ProvincialProviderSearchRequest provincialProviderSearchRequest;
    ProvincialLocationDetailsRequest provincialLocationDetailsRequest;

    @BeforeAll
    void setup() {
      configureTimeOut(50);
      provincialPatientConsentRequest = buildTestProvincialPatientConsentRequest();
      provincialLocationSearchRequest = buildTestProvincialLocationSearchRequest();
      provincialPatientSearchRequest = buildTestProvincialPatientSearchRequest();
      provincialProviderSearchRequest = buildTestProvincialProviderSearchRequest();
      provincialLocationDetailsRequest = buildTestProvincialLocationDetailsRequest();
    }

    public Stream<Arguments> buildRequestArgs() {
      return Stream.of(
          Arguments.of(provincialPatientConsentRequest, MessageProcess.PATIENT_CONSENT),
          Arguments.of(provincialLocationSearchRequest, MessageProcess.LOCATION_SEARCH),
          Arguments.of(provincialPatientSearchRequest, MessageProcess.PATIENT_SEARCH),
          Arguments.of(provincialProviderSearchRequest, MessageProcess.PROVIDER_SEARCH),
          Arguments.of(provincialLocationDetailsRequest, MessageProcess.LOCATION_DETAILS));
    }

    @ParameterizedTest
    @MethodSource("buildRequestArgs")
    void givenLowTimeOut_CallClientRegistry_for_DSQ_Transactions_then_ProvincialTimeoutException(
        Request<? extends ProvincialRequest> request, MessageProcess messageProcess)
        throws Exception {
      when(internalLookupService.fetchPharmacy(any()))
          .thenReturn(Mono.just(Pharmacy.builder().build()));

      HttpHeaders headers = new HttpHeaders();
      headers.set(
          provincialRequestProperties
              .getRequest()
              .getTransaction()
              .get(messageProcess.getName())
              .getHeader()
              .getSoapAction(),
          provincialRequestProperties
              .getRequest()
              .getTransaction()
              .get(messageProcess.getName())
              .getHeader()
              .getValue());

      StepVerifier.create(
              transformationService
                  .transformRequest(request)
                  .flatMap(
                      signedSoapRequest -> {
                        return provincialWebClient.callClientRegistry(
                            provincialRequestProperties
                                .getRequest()
                                .getTransaction()
                                .get(messageProcess.getName())
                                .getUri(),
                            headers,
                            signedSoapRequest);
                      })
                  .contextWrite(Context.of(MessageProcess.class, messageProcess)))
          .expectError(ProvincialTimeoutException.class)
          .verify();
    }

    public ProvincialPatientConsentRequest buildTestProvincialPatientConsentRequest() {
      return ProvincialPatientConsentRequest.builder()
          .requestHeader(RequestHeader.builder().bearerToken("bearerToken").build())
          .requestControlAct(
              RequestControlAct.builder()
                  .eventCorrelationId("eventCorrelationId")
                  .eventRoot("eventRoot")
                  .build())
          .requestBodyTransmissionWrapper(
              RequestBodyTransmissionWrapper.builder()
                  .transmissionUniqueIdentifier(uuidGenerator.generateUUID())
                  .transmissionCreationDateTime("20211110182928")
                  .processingCode("D")
                  .senderRoot("VPHX")
                  .senderApplicationName("ApplicationName")
                  .senderApplicationId("ApplicationID")
                  .build())
          .provincialRequestPayload(
              ProvincialPatientConsentCriteria.builder()
                  .patientIdentifier("8000000300")
                  .firstName("Fares")
                  .lastName("Mayer")
                  .effectiveDateTime("20211012131212")
                  .provincialRequestControl(
                      ProvincialRequestControl.builder()
                          .requestId("UUID")
                          .pharmacy(Pharmacy.builder().id("test").build())
                          .province(Province.QC)
                          .build())
                  .build())
          .build();
    }

    public ProvincialLocationSearchRequest buildTestProvincialLocationSearchRequest() {
      return ProvincialLocationSearchRequest.builder()
          .requestHeader(RequestHeader.builder().bearerToken("bearerToken").build())
          .requestControlAct(
              RequestControlAct.builder()
                  .eventCorrelationId("eventCorrelationId")
                  .eventRoot("eventRoot")
                  .build())
          .requestBodyTransmissionWrapper(
              RequestBodyTransmissionWrapper.builder()
                  .transmissionUniqueIdentifier(uuidGenerator.generateUUID())
                  .transmissionCreationDateTime("20211110182928")
                  .processingCode("D")
                  .senderRoot("VPHX")
                  .senderApplicationName("ApplicationName")
                  .senderApplicationId("ApplicationID")
                  .build())
          .provincialRequestPayload(
              ProvincialLocationSearchCriteria.builder()
                  .provincialRequestControl(
                      ProvincialRequestControl.builder()
                          .pharmacy(Pharmacy.builder().id("test").build())
                          .province(Province.QC)
                          .build())
                  .locationType("LAB")
                  .build())
          .build();
    }

    public ProvincialPatientSearchRequest buildTestProvincialPatientSearchRequest() {
      return ProvincialPatientSearchRequest.builder()
          .requestHeader(RequestHeader.builder().bearerToken("bearerToken").build())
          .requestControlAct(
              RequestControlAct.builder()
                  .eventRoot("eventRoot")
                  .eventCorrelationId("correlationId")
                  .build())
          .requestBodyTransmissionWrapper(
              RequestBodyTransmissionWrapper.builder()
                  .transmissionUniqueIdentifier("tUniqueId")
                  .transmissionCreationDateTime("20090905100144")
                  .processingCode("D")
                  .senderRoot("VPHX")
                  .senderApplicationId("ApplicationID")
                  .senderApplicationName("ApplicationName")
                  .build())
          .provincialRequestPayload(
              ProvincialPatientSearchCriteria.builder()
                  .provincialHealthNumber("MAYM21561219")
                  .provincialRequestControl(
                      ProvincialRequestControl.builder()
                          .requestId("UUID")
                          .pharmacy(Pharmacy.builder().id("test").build())
                          .province(Province.QC)
                          .build())
                  .build())
          .build();
    }

    public ProvincialProviderSearchRequest buildTestProvincialProviderSearchRequest() {
      var transmissionUniqueIdentifier = uuidGenerator.generateUUID();
      var eventCorrelationID = uuidGenerator.generateUUID();
      return ProvincialProviderSearchRequest.builder()
          .requestHeader(RequestHeader.builder().bearerToken("bearerToken").build())
          .requestControlAct(
              RequestControlAct.builder()
                  .eventRoot("eventRoot")
                  .eventCorrelationId(eventCorrelationID)
                  .build())
          .requestBodyTransmissionWrapper(
              RequestBodyTransmissionWrapper.builder()
                  .transmissionUniqueIdentifier(transmissionUniqueIdentifier)
                  .transmissionCreationDateTime("20211110182928")
                  .processingCode("D")
                  .senderRoot("VPHX")
                  .senderApplicationId("ApplicationID")
                  .senderApplicationName("ApplicationName")
                  .build())
          .provincialRequestPayload(
              ProvincialProviderSearchCriteria.builder()
                  .firstName("Ivan")
                  .lastName("Helios")
                  .gender(Gender.M)
                  .provincialRequestControl(
                      ProvincialRequestControl.builder()
                          .pharmacy(Pharmacy.builder().id("test").build())
                          .province(Province.QC)
                          .requestId(eventCorrelationID)
                          .build())
                  .build())
          .build();
    }

    public ProvincialLocationDetailsRequest buildTestProvincialLocationDetailsRequest() {
      return ProvincialLocationDetailsRequest.builder()
          .requestHeader(RequestHeader.builder().bearerToken("bearerToken").build())
          .requestBodyTransmissionWrapper(
              RequestBodyTransmissionWrapper.builder()
                  .senderApplicationId("senderApplicationId")
                  .senderApplicationName("senderApplicationName")
                  .senderRoot("senderRoot")
                  .processingCode("D")
                  .transmissionCreationDateTime("20211110182928")
                  .transmissionUniqueIdentifier(uuidGenerator.generateUUID())
                  .build())
          .requestControlAct(
              RequestControlAct.builder()
                  .eventRoot("eventRoot")
                  .eventCorrelationId("eventCorrelationId")
                  .build())
          .provincialRequestPayload(
              ProvincialLocationDetailsCriteria.builder()
                  .provincialRequestControl(
                      ProvincialRequestControl.builder()
                          .pharmacy(Pharmacy.builder().id("test").build())
                          .province(Province.QC)
                          .build())
                  .identifier("1000022499")
                  .build())
          .build();
    }

    // TODO Exception handling is covering in Service Side, Need to write this test in Service side
    public void should_return_a_ProvincialProviderSearch_hl7_response_with_error_for_timeout()
        throws UnsupportedEncodingException, NoSuchAlgorithmException {
      var transmissionUniqueIdentifier = uuidGenerator.generateUUID();
      var eventCorrelationID = uuidGenerator.generateUUID();

      var provincialProviderSearchRequest =
          ProvincialProviderSearchRequest.builder()
              .requestHeader(RequestHeader.builder().bearerToken("bearerToken").build())
              .requestControlAct(
                  RequestControlAct.builder()
                      .eventRoot("eventRoot")
                      .eventCorrelationId(eventCorrelationID)
                      .build())
              .requestBodyTransmissionWrapper(
                  RequestBodyTransmissionWrapper.builder()
                      .transmissionUniqueIdentifier(transmissionUniqueIdentifier)
                      .transmissionCreationDateTime("20211110182928")
                      .processingCode("D")
                      .senderRoot("VPHX")
                      .senderApplicationId("ApplicationID")
                      .senderApplicationName("ApplicationName")
                      .build())
              .provincialRequestPayload(
                  ProvincialProviderSearchCriteria.builder()
                      .firstName("Ivan")
                      .lastName("Helios")
                      .gender(Gender.M)
                      .provincialRequestControl(
                          ProvincialRequestControl.builder()
                              .province(Province.QC)
                              .requestId(eventCorrelationID)
                              .build())
                      .build())
              .build();
      var provincialProviderSearchResponseMono =
          quebecClientRegistryService.searchProvincialProvider(provincialProviderSearchRequest);

      assertNotNull(
          provincialProviderSearchResponseMono, "provincialProviderSearchResponseMono is Null");
      OperationOutcome expectedOperationOutcome =
          OperationOutcome.buildOperationOutcomeForSystemTimeoutError(
              Source.EXTERNAL, ServiceCode.QC_REGISTRIES, null, OperationOutcome.TIMEOUT_ERROR);

      StepVerifier.create(provincialProviderSearchResponseMono)
          .assertNext(
              provincialProviderSearchResponse ->
                  assertEquals(
                      provincialProviderSearchResponse
                          .getProvincialResponsePayload()
                          .getProvincialResponseAcknowledgement()
                          .getOperationOutcome(),
                      expectedOperationOutcome))
          .verifyComplete();
    }

    // TODO Exception handling is covering in Service Side, Need to write this test in Service side
    public void should_return_a_ProvincialPatientConsent_hl7_response_with_error_for_timeout()
        throws UnsupportedEncodingException, NoSuchAlgorithmException {
      var transmissionUniqueIdentifier = uuidGenerator.generateUUID();
      var eventCorrelationID = uuidGenerator.generateUUID();
      var provincialPatientConsentRequest =
          ProvincialPatientConsentRequest.builder()
              .requestHeader(RequestHeader.builder().bearerToken("bearerToken").build())
              .requestControlAct(
                  RequestControlAct.builder()
                      .eventRoot("eventRoot")
                      .eventCorrelationId(eventCorrelationID)
                      .build())
              .requestBodyTransmissionWrapper(
                  RequestBodyTransmissionWrapper.builder()
                      .transmissionUniqueIdentifier(transmissionUniqueIdentifier)
                      .transmissionCreationDateTime("20090905100144")
                      .processingCode("D")
                      .senderRoot("2.16.840.1.113883.3.40.5.2")
                      .senderApplicationName("senderApplicationName")
                      .senderApplicationId("senderApplicationId")
                      .build())
              .provincialRequestPayload(
                  ProvincialPatientConsentCriteria.builder()
                      .patientIdentifier("8000000300")
                      .firstName("Fares")
                      .lastName("Mayer")
                      .effectiveDateTime("20211012131212")
                      .provincialRequestControl(
                          ProvincialRequestControl.builder()
                              .province(Province.QC)
                              .requestId(eventCorrelationID)
                              .pharmacy(Pharmacy.builder().name("12345678").build())
                              .build())
                      .build())
              .build();
      var provincialRequestControl =
          ProvincialRequestControl.builder().province(Province.QC).build();
      var provincialPatientSearchResponseMono =
          quebecClientRegistryService
              .getProvincialPatientConsent(provincialPatientConsentRequest)
              .contextWrite(ctx -> ctx.put(MessageProcess.class, MessageProcess.PATIENT_CONSENT));
      assertNotNull(
          provincialPatientSearchResponseMono, "provincialPatientSearchResponseMono is Null");

      OperationOutcome expectedOperationOutcome =
          OperationOutcome.buildOperationOutcomeForSystemTimeoutError(
              Source.EXTERNAL, ServiceCode.QC_REGISTRIES, null, OperationOutcome.TIMEOUT_ERROR);

      StepVerifier.create(provincialPatientSearchResponseMono)
          .assertNext(
              provincialPatientSearchResponse ->
                  assertEquals(
                      provincialPatientSearchResponse
                          .getProvincialResponsePayload()
                          .getProvincialResponseAcknowledgement()
                          .getOperationOutcome(),
                      expectedOperationOutcome))
          .verifyComplete();
    }

    // TODO Exception handling is covering in Service Side, Need to write this test in Service side
    void should_return_a_ProvincialPatientSearch_hl7_response_with_error_for_timeout()
        throws UnsupportedEncodingException, NoSuchAlgorithmException {
      var transmissionUniqueIdentifier = uuidGenerator.generateUUID();
      var eventCorrelationID = uuidGenerator.generateUUID();
      when(internalLookupService.fetchPharmacy(any()))
          .thenReturn(Mono.just(Pharmacy.builder().build()));
      var provincialPatientSearchRequest =
          ProvincialPatientSearchRequest.builder()
              .requestHeader(RequestHeader.builder().bearerToken("bearerToken").build())
              .requestControlAct(
                  RequestControlAct.builder()
                      .eventRoot("eventRoot")
                      .eventCorrelationId(eventCorrelationID)
                      .build())
              .requestBodyTransmissionWrapper(
                  RequestBodyTransmissionWrapper.builder()
                      .transmissionUniqueIdentifier(transmissionUniqueIdentifier)
                      .transmissionCreationDateTime("20090905100144")
                      .processingCode("D")
                      .senderRoot("2.16.840.1.113883.3.40.5.2")
                      .senderApplicationName("senderApplicationName")
                      .senderApplicationId("senderApplicationId")
                      .build())
              .provincialRequestPayload(
                  ProvincialPatientSearchCriteria.builder()
                      .provincialHealthNumber("MAYM21561219")
                      .provincialRequestControl(
                          ProvincialRequestControl.builder()
                              .pharmacy(Pharmacy.builder().id("test").build())
                              .province(Province.QC)
                              .requestId("Patient-generated-id")
                              .build())
                      .build())
              .build();
      var provincialPatientSearchResponseMono =
          quebecClientRegistryService
              .searchProvincialPatient(provincialPatientSearchRequest)
              .contextWrite(ctx -> ctx.put(MessageProcess.class, MessageProcess.PATIENT_SEARCH));

      assertNotNull(
          provincialPatientSearchResponseMono, "provincialPatientSearchResponseMono is Null");

      OperationOutcome expectedOperationOutcome =
          OperationOutcome.buildOperationOutcomeForSystemTimeoutError(
              Source.EXTERNAL, ServiceCode.QC_REGISTRIES, null, OperationOutcome.TIMEOUT_ERROR);

      StepVerifier.create(provincialPatientSearchResponseMono)
          .assertNext(
              provincialPatientSearchResponse ->
                  assertEquals(
                      provincialPatientSearchResponse
                          .getProvincialResponsePayload()
                          .getProvincialResponseAcknowledgement()
                          .getOperationOutcome(),
                      expectedOperationOutcome))
          .verifyComplete();
    }

    // TODO Exception handling is covering in Service Side, Need to write this test in Service side
    public void should_return_a_ProvincialLocationSearch_hl7_response_with_error_for_timeout()
        throws UnsupportedEncodingException, NoSuchAlgorithmException {
      var transmissionUniqueIdentifier = uuidGenerator.generateUUID();
      var eventCorrelationID = uuidGenerator.generateUUID();

      ProvincialLocationSearchRequest provincialLocationSearchRequest =
          ProvincialLocationSearchRequest.builder()
              .requestControlAct(
                  RequestControlAct.builder()
                      .eventRoot("eventRoot")
                      .eventCorrelationId(eventCorrelationID)
                      .build())
              .requestBodyTransmissionWrapper(
                  RequestBodyTransmissionWrapper.builder()
                      .senderApplicationId("senderApplicationId")
                      .senderApplicationName("senderApplicationName")
                      .senderRoot("senderRoot")
                      .processingCode("D")
                      .transmissionCreationDateTime("20211110182928")
                      .transmissionUniqueIdentifier(transmissionUniqueIdentifier)
                      .build())
              .requestControlAct(
                  RequestControlAct.builder()
                      .eventRoot("eventRoot")
                      .eventCorrelationId("eventCorrelationId")
                      .build())
              .provincialRequestPayload(
                  ProvincialLocationSearchCriteria.builder()
                      .provincialRequestControl(
                          ProvincialRequestControl.builder().province(Province.QC).build())
                      .locationType("LAB")
                      .build())
              .build();
      var provincialRequestControl =
          ProvincialRequestControl.builder().province(Province.QC).build();
      var provincialLocationSearchResponseMono =
          quebecClientRegistryService
              .searchProvincialLocationSummary(provincialLocationSearchRequest)
              .contextWrite(ctx -> ctx.put(MessageProcess.class, MessageProcess.LOCATION_SEARCH));
      assertNotNull(
          provincialLocationSearchResponseMono, "provincialLocationSearchResponseMono is Null");

      OperationOutcome expectedOperationOutcome =
          OperationOutcome.buildOperationOutcomeForSystemTimeoutError(
              Source.EXTERNAL, ServiceCode.QC_REGISTRIES, null, OperationOutcome.TIMEOUT_ERROR);
      StepVerifier.create(provincialLocationSearchResponseMono)
          .assertNext(
              provincialLocationSearchResponse ->
                  assertEquals(
                      provincialLocationSearchResponse
                          .getProvincialResponsePayload()
                          .getProvincialResponseAcknowledgement()
                          .getOperationOutcome(),
                      expectedOperationOutcome))
          .verifyComplete();
    }

    // TODO Exception handling is covering in Service Side, Need to write this test in Service side
    public void should_return_a_ProvincialLocationDetails_hl7_response_with_error_for_timeout()
        throws UnsupportedEncodingException, NoSuchAlgorithmException {
      var transmissionUniqueIdentifier = uuidGenerator.generateUUID();
      var eventCorrelationID = uuidGenerator.generateUUID();
      ProvincialLocationDetailsRequest provincialLocationDetailsRequest =
          ProvincialLocationDetailsRequest.builder()
              .requestControlAct(
                  RequestControlAct.builder()
                      .eventRoot("eventRoot")
                      .eventCorrelationId(eventCorrelationID)
                      .build())
              .requestBodyTransmissionWrapper(
                  RequestBodyTransmissionWrapper.builder()
                      .senderApplicationId("senderApplicationId")
                      .senderApplicationName("senderApplicationName")
                      .senderRoot("senderRoot")
                      .processingCode("D")
                      .transmissionCreationDateTime("20211110182928")
                      .transmissionUniqueIdentifier(transmissionUniqueIdentifier)
                      .build())
              .requestControlAct(
                  RequestControlAct.builder()
                      .eventRoot("eventRoot")
                      .eventCorrelationId("eventCorrelationId")
                      .build())
              .provincialRequestPayload(
                  ProvincialLocationDetailsCriteria.builder()
                      .provincialRequestControl(
                          ProvincialRequestControl.builder().province(Province.QC).build())
                      .identifier("1000022499")
                      .build())
              .build();

      var provincialLocationDetailsResponseMono =
          quebecClientRegistryService.searchProvincialLocationDetails(
              provincialLocationDetailsRequest);
      assertNotNull(
          provincialLocationDetailsResponseMono, "provincialLocationDetailsResponseMono is Null");

      OperationOutcome expectedOperationOutcome =
          OperationOutcome.buildOperationOutcomeForSystemTimeoutError(
              Source.EXTERNAL, ServiceCode.QC_REGISTRIES, null, OperationOutcome.TIMEOUT_ERROR);
      StepVerifier.create(provincialLocationDetailsResponseMono)
          .assertNext(
              provincialLocationDetailsResponse ->
                  assertEquals(
                      provincialLocationDetailsResponse
                          .getProvincialResponsePayload()
                          .getProvincialResponseAcknowledgement()
                          .getOperationOutcome(),
                      expectedOperationOutcome))
          .verifyComplete();
    }
  }

  @Nested
  @DisplayName("Test When province calls timeout values are high")
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class TestWhenProvinceCallTimeoutHigh {
    @BeforeAll
    void setup() {
      configureTimeOut(100000);
    }

    @Test
    @DisplayName("should return FindPatientQuery hl7 success response from DSQ")
    void should_return_hl7_success_response_to_FindPatientQuery_from_DSQ() throws Exception {
      when(internalLookupService.fetchPharmacy(any()))
          .thenReturn(Mono.just(Pharmacy.builder().build()));
      var provincialPatientSearchRequest =
          ProvincialPatientSearchRequest.builder()
              .requestHeader(RequestHeader.builder().bearerToken("bearerToken").build())
              .requestControlAct(
                  RequestControlAct.builder()
                      .eventRoot("eventRoot")
                      .eventCorrelationId("correlationId")
                      .build())
              .requestBodyTransmissionWrapper(
                  RequestBodyTransmissionWrapper.builder()
                      .transmissionUniqueIdentifier("tUniqueId")
                      .transmissionCreationDateTime("20090905100144")
                      .processingCode("D")
                      .senderRoot("VPHX")
                      .senderApplicationId("ApplicationID")
                      .senderApplicationName("ApplicationName")
                      .build())
              .provincialRequestPayload(
                  ProvincialPatientSearchCriteria.builder()
                      .provincialHealthNumber("MAYM21561219")
                      .provincialRequestControl(
                          ProvincialRequestControl.builder()
                              .pharmacy(Pharmacy.builder().id("test").build())
                              .province(Province.QC)
                              .requestId("UUID")
                              .build())
                      .build())
              .build();

      var messageProcess = MessageProcess.PATIENT_SEARCH;

      HttpHeaders headers = new HttpHeaders();
      headers.set(
          provincialRequestProperties
              .getRequest()
              .getTransaction()
              .get(messageProcess.getName())
              .getHeader()
              .getSoapAction(),
          provincialRequestProperties
              .getRequest()
              .getTransaction()
              .get(messageProcess.getName())
              .getHeader()
              .getValue());

      ResponseEntity<String> response =
          transformationService
              .transformRequest(provincialPatientSearchRequest)
              .flatMap(
                  signedSoapRequest -> {
                    return provincialWebClient.callClientRegistry(
                        provincialRequestProperties
                            .getRequest()
                            .getTransaction()
                            .get(messageProcess.getName())
                            .getUri(),
                        headers,
                        signedSoapRequest);
                  })
              .contextWrite(Context.of(MessageProcess.class, MessageProcess.PATIENT_SEARCH))
              .block();

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
      builderFactory.setNamespaceAware(false);
      DocumentBuilder builder = builderFactory.newDocumentBuilder();
      Document xmlDocument =
          builder.parse(new InputSource(new StringReader(response.getBody().replace("\n", ""))));
      var xPathFactory = XPathFactory.newInstance();

      var acknowledgementTypeCode =
          xPathFactory
              .newXPath()
              .compile("//Envelope/Body/PRPA_IN101104CA/acknowledgement/typeCode/@code")
              .evaluate(xmlDocument);
      assertThat(acknowledgementTypeCode).isEqualTo("AA");
      var acknowledgementDetailsTypeCode =
          xPathFactory
              .newXPath()
              .compile(
                  "//Envelope/Body/PRPA_IN101104CA/acknowledgement/acknowledgementDetail/@typeCode")
              .evaluate(xmlDocument);
      // there should not be any acknowledgementDetail typeCode when query finds and returns
      // provider
      // details
      assertThat(acknowledgementDetailsTypeCode).isEmpty();
    }

    @Test
    @DisplayName(
        value = "should return successful provincialPatientSearchResponse with ackType code is AA")
    void testSuccessResponseWithAckTypeAA() {
      when(internalLookupService.fetchPharmacy(any()))
          .thenReturn(Mono.just(Pharmacy.builder().build()));
      var provincialPatientSearchRequest =
          ProvincialPatientSearchRequest.builder()
              .requestHeader(RequestHeader.builder().bearerToken("bearerToken").build())
              .requestControlAct(
                  RequestControlAct.builder()
                      .eventRoot("eventRoot")
                      .eventCorrelationId("Patient-generated-id")
                      .build())
              .requestBodyTransmissionWrapper(
                  RequestBodyTransmissionWrapper.builder()
                      .transmissionUniqueIdentifier("2.16.840.1.113883.3.40.1.5")
                      .transmissionCreationDateTime("20090905100144")
                      .processingCode("D")
                      .senderRoot("2.16.840.1.113883.3.40.5.2")
                      .senderApplicationName("senderApplicationName")
                      .senderApplicationId("senderApplicationId")
                      .build())
              .provincialRequestPayload(
                  ProvincialPatientSearchCriteria.builder()
                      .provincialHealthNumber("MAYM21561219")
                      .provincialRequestControl(
                          ProvincialRequestControl.builder()
                              .pharmacy(Pharmacy.builder().id("test").build())
                              .province(Province.QC)
                              .requestId("Patient-generated-id")
                              .build())
                      .build())
              .build();

      var expectedResponse =
          ProvincialPatientSearchResponse.builder()
              .responseHeader(ResponseHeader.builder().build())
              .provincialResponsePayload(
                  ProvincialPatientProfile.builder()
                      .firstName("Marina")
                      .lastName("Mayer")
                      .fatherFirstName("Fares")
                      .fatherLastName("Mayer")
                      .motherFirstName("Flore")
                      .motherLastName("Michel")
                      .identifier(
                          SystemIdentifier.builder()
                              .type(SystemIdentifier.IDENTIFIER_TYPE.PATIENT)
                              .value("8000000318")
                              .assigner(HL7Constants.QC)
                              .system(HL7Constants.NIU_U)
                              .build())
                      .provincialHealthNumber(
                          SystemIdentifier.builder()
                              .type(SystemIdentifier.IDENTIFIER_TYPE.HEALTH_NUMBER)
                              .value("MAYM21561219")
                              .assigner(HL7Constants.QC)
                              .system(HL7Constants.NAM)
                              .build())
                      .gender(Gender.F)
                      .matchingIndex(59)
                      .address(
                          Address.builder()
                              .streetAddressLine("4109 avenue Isabella")
                              .city("Montréal")
                              .country("CA")
                              .postalCode("H3T1N5")
                              .province("Qc")
                              .build())
                      .dateOfBirth(LocalDate.of(2021, 06, 12))
                      .provincialResponseAcknowledgement(
                          ProvincialResponseAcknowledgement.builder()
                              .operationOutcome(OperationOutcome.builder().build())
                              .auditEvent(
                                  AuditEvent.builder()
                                      .provincialRequestControl(
                                          ProvincialRequestControl.builder()
                                              .requestId("Patient-generated-id")
                                              .build())
                                      .build())
                              .build())
                      .build())
              .responseControlAct(
                  ProvincialPatientSearchResponseControlAct.builder()
                      .eventCorrelationId("Patient-generated-id")
                      .eventRoot("eventRoot")
                      .targetPatient(null)
                      .provincialPatientSearchCriteria(
                          ProvincialPatientSearchCriteria.builder()
                              .patientIdentifier(null)
                              .provincialHealthNumber(null)
                              .provincialRequestControl(
                                  ProvincialRequestControl.builder()
                                      .pharmacy(Pharmacy.builder().id("test").build())
                                      .province(Province.QC)
                                      .requestId("Patient-generated-id")
                                      .build())
                              .address(
                                  Address.builder()
                                      .streetAddressLine(null)
                                      .city(null)
                                      .province(null)
                                      .postalCode(null)
                                      .build())
                              .build())
                      .build())
              .queryAcknowledgement(
                  QueryAcknowledgement.builder()
                      .queryResponseCode("OK")
                      .resultCurrentQuantity("1")
                      .resultTotalQuantity("1")
                      .resultRemainingQuantity("0")
                      .build())
              .build();

      var provincialPatientSearchResponseMono =
          quebecClientRegistryService
              .searchProvincialPatient(provincialPatientSearchRequest)
              .contextWrite(ctx -> ctx.put(MessageProcess.class, MessageProcess.PATIENT_SEARCH));

      assertNotNull(
          provincialPatientSearchResponseMono, "provincialPatientSearchResponseMono is Null");
      StepVerifier.create(provincialPatientSearchResponseMono)
          .assertNext(
              provincialPatientSearchResponse -> {
                var header = provincialPatientSearchResponse.getResponseHeader();
                assertAll(
                    "header",
                    () -> assertNotNull(header),
                    () -> assertNotNull(StringUtils.trim(header.getTrackingId()), "trackingId"),
                    () -> assertNotNull(StringUtils.trim(header.getSessionId()), "sessionId"),
                    () -> assertNotNull(StringUtils.trim(header.getTrackingId()), "transactionId"));

                var profile = provincialPatientSearchResponse.getProvincialResponsePayload();

                assertNotNull(profile, "profile");
                assertEquals(
                    expectedResponse.getProvincialResponsePayload(),
                    provincialPatientSearchResponse.getProvincialResponsePayload());

                var responseControlAct = expectedResponse.getResponseControlAct();
                assertNotNull(responseControlAct);
                assertEquals(expectedResponse.getResponseControlAct(), responseControlAct);

                var queryAck = provincialPatientSearchResponse.getQueryAcknowledgement();

                assertNotNull(queryAck);
                assertEquals(
                    expectedResponse.getQueryAcknowledgement(),
                    provincialPatientSearchResponse.getQueryAcknowledgement());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        value =
            "should return successful provincialPatientSearchResponse with detected issue ECARTMAJ")
    void testSuccessResponseWithDetectedIssuesCodeECARTMAJ() {
      when(internalLookupService.fetchPharmacy(any()))
          .thenReturn(Mono.just(Pharmacy.builder().build()));
      var provincialPatientSearchRequest =
          ProvincialPatientSearchRequest.builder()
              .requestHeader(RequestHeader.builder().bearerToken("bearerToken").build())
              .requestControlAct(
                  RequestControlAct.builder()
                      .eventRoot("2.16.840.1.113883.3.40.4.1")
                      .eventCorrelationId("564654")
                      .build())
              .requestBodyTransmissionWrapper(
                  RequestBodyTransmissionWrapper.builder()
                      .transmissionUniqueIdentifier("2.16.840.1.113883.3.40.1.5")
                      .transmissionCreationDateTime("20090905100144")
                      .processingCode("D")
                      .senderRoot("2.16.840.1.113883.3.40.5.2")
                      .senderApplicationName("senderApplicationName")
                      .senderApplicationId("TMPT-RI")
                      .build())
              .provincialRequestPayload(
                  ProvincialPatientSearchCriteria.builder()
                      .provincialHealthNumber("MAYM21561219")
                      .provincialRequestControl(
                          ProvincialRequestControl.builder()
                              .pharmacy(Pharmacy.builder().id("test").build())
                              .province(Province.QC)
                              .requestId("564654")
                              .build())
                      .firstName("Marina")
                      .lastName("Mayer")
                      .dateOfBirth(LocalDate.of(2021, 06, 12))
                      .gender(Gender.M)
                      .build())
              .build();
      var provincialPatientSearchResponseMono =
          quebecClientRegistryService
              .searchProvincialPatient(provincialPatientSearchRequest)
              .contextWrite(ctx -> ctx.put(MessageProcess.class, MessageProcess.PATIENT_SEARCH));

      assertNotNull(
          provincialPatientSearchResponseMono, "provincialPatientSearchResponseMono is Null ");
      StepVerifier.create(provincialPatientSearchResponseMono)
          .assertNext(
              provincialPatientSearchResponse -> {
                var ackTypeCode =
                    provincialPatientSearchResponse
                        .getResponseBodyTransmissionWrapper()
                        .getAcknowledgeTypeCode();
                assertEquals("AA", ackTypeCode, "Acknowledgment Type Code");

                var ackDetails =
                    provincialPatientSearchResponse
                        .getResponseBodyTransmissionWrapper()
                        .getAcknowledgementDetails();
                assertTrue(ackDetails.isEmpty(), "Acknowledgment Details");

                var queryAck = provincialPatientSearchResponse.getQueryAcknowledgement();
                assertAll(
                    "queryAck",
                    () ->
                        assertEquals("OK", queryAck.getQueryResponseCode(), "Query response code"));

                provincialPatientSearchResponse.getDetectedIssues().stream()
                    .findFirst()
                    .map(DetectedIssue::getEventCode)
                    .ifPresent(
                        detectedIssuesCode ->
                            assertEquals("ECARTMAJ", detectedIssuesCode, "Detected Issues Code"));
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        value =
            "should return successful provincialPatientSearchResponse with detected issue code ECARTMIN")
    void testSuccessResponseWithDetectedIssuesCodeECARTMIN() {
      when(internalLookupService.fetchPharmacy(any()))
          .thenReturn(Mono.just(Pharmacy.builder().build()));
      var provincialPatientSearchRequest =
          ProvincialPatientSearchRequest.builder()
              .requestHeader(RequestHeader.builder().bearerToken("bearerToken").build())
              .requestControlAct(
                  RequestControlAct.builder()
                      .eventRoot("2.16.840.1.113883.3.40.4.1")
                      .eventCorrelationId("564654")
                      .build())
              .requestBodyTransmissionWrapper(
                  RequestBodyTransmissionWrapper.builder()
                      .transmissionUniqueIdentifier("2.16.840.1.113883.3.40.1.5")
                      .transmissionCreationDateTime("20090905100144")
                      .processingCode("D")
                      .senderRoot("2.16.840.1.113883.3.40.5.2")
                      .senderApplicationName("senderApplicationName")
                      .senderApplicationId("TMPT-RI")
                      .build())
              .provincialRequestPayload(
                  ProvincialPatientSearchCriteria.builder()
                      .provincialHealthNumber("MICF93560415")
                      .provincialRequestControl(
                          ProvincialRequestControl.builder()
                              .pharmacy(Pharmacy.builder().id("test").build())
                              .province(Province.QC)
                              .requestId("564654")
                              .build())
                      .firstName("Michel")
                      .lastName("Flore")
                      .dateOfBirth(LocalDate.of(1993, 06, 04))
                      .build())
              .build();
      var provincialPatientSearchResponseMono =
          quebecClientRegistryService
              .searchProvincialPatient(provincialPatientSearchRequest)
              .contextWrite(ctx -> ctx.put(MessageProcess.class, MessageProcess.PATIENT_SEARCH));

      assertNotNull(
          provincialPatientSearchResponseMono, "provincialPatientSearchResponseMono is Null");
      StepVerifier.create(provincialPatientSearchResponseMono)
          .assertNext(
              provincialPatientSearchResponse -> {
                var ackTypeCode =
                    provincialPatientSearchResponse
                        .getResponseBodyTransmissionWrapper()
                        .getAcknowledgeTypeCode();
                assertEquals("AA", ackTypeCode, "Acknowledgment Type Code");

                var ackDetails =
                    provincialPatientSearchResponse
                        .getResponseBodyTransmissionWrapper()
                        .getAcknowledgementDetails();
                assertTrue(ackDetails.isEmpty(), "Acknowledgment Details");

                var queryAck = provincialPatientSearchResponse.getQueryAcknowledgement();
                assertAll(
                    "queryAck",
                    () ->
                        assertEquals("OK", queryAck.getQueryResponseCode(), "Query response code"));

                provincialPatientSearchResponse.getDetectedIssues().stream()
                    .findFirst()
                    .map(DetectedIssue::getEventCode)
                    .ifPresent(
                        detectedIssuesCode ->
                            assertEquals("ECARTMIN", detectedIssuesCode, "Detected Issues Code"));
              })
          .verifyComplete();
    }

    /** Marking disabled as the test data is corrected at province. */
    @DisplayName(
        value =
            "should return Error Response when ackDetails code is UNSVAL and ackType code is AE")
    @Disabled("As the data at provice has been changed.")
    public void testErrorResponseEC001() {
      when(internalLookupService.fetchPharmacy(any()))
          .thenReturn(Mono.just(Pharmacy.builder().build()));
      var provincialPatientSearchRequest =
          ProvincialPatientSearchRequest.builder()
              .requestHeader(RequestHeader.builder().bearerToken("bearerToken").build())
              .requestControlAct(
                  RequestControlAct.builder()
                      .eventRoot("2.16.840.1.113883.3.40.4.1")
                      .eventCorrelationId("564654")
                      .build())
              .requestBodyTransmissionWrapper(
                  RequestBodyTransmissionWrapper.builder()
                      .transmissionUniqueIdentifier("2.16.840.1.113883.3.40.1.5")
                      .transmissionCreationDateTime("20220905100144")
                      .processingCode("D")
                      .senderRoot("2.16.840.1.113883.3.40.5.2")
                      .senderApplicationName("senderApplicationName")
                      .senderApplicationId("TMPT-RI")
                      .build())
              .provincialRequestPayload(
                  ProvincialPatientSearchCriteria.builder()
                      .provincialHealthNumber("MAYM21561219")
                      .provincialRequestControl(
                          ProvincialRequestControl.builder()
                              .pharmacy(Pharmacy.builder().id("test").build())
                              .province(Province.QC)
                              .requestId("564654")
                              .build())
                      .firstName("Marina")
                      .lastName("Mayer")
                      .dateOfBirth(LocalDate.of(2021, 06, 12))
                      .gender(Gender.F)
                      .build())
              .build();
      var provincialPatientSearchResponseMono =
          quebecClientRegistryService.searchProvincialPatient(provincialPatientSearchRequest);

      assertNotNull(
          provincialPatientSearchResponseMono, "provincialPatientSearchResponseMono is Null");
      StepVerifier.create(provincialPatientSearchResponseMono)
          .assertNext(
              provincialPatientSearchResponse -> {
                var ackTypeCode =
                    provincialPatientSearchResponse
                        .getResponseBodyTransmissionWrapper()
                        .getAcknowledgeTypeCode();
                assertEquals("AE", ackTypeCode, "Acknowledgment Type Code");

                var ackDetails =
                    provincialPatientSearchResponse
                        .getResponseBodyTransmissionWrapper()
                        .getAcknowledgementDetails()
                        .get(0);
                assertEquals("E", ackDetails.getTypeCode(), "Acknowledgment Details type code");
                assertEquals("UNSVAL", ackDetails.getCode(), "Acknowledgment Details code");

                var queryAck = provincialPatientSearchResponse.getQueryAcknowledgement();
                assertAll(
                    "queryAck",
                    () ->
                        assertEquals("AE", queryAck.getQueryResponseCode(), "Query response code"));

                provincialPatientSearchResponse.getDetectedIssues().stream()
                    .findFirst()
                    .map(DetectedIssue::getEventCode)
                    .ifPresent(
                        detectedIssuesCode ->
                            assertNull(detectedIssuesCode, "Detected Issues Code"));
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(value = "should return error response when ackDetails code is NS202")
    void testErrorResponseEC005() {
      when(internalLookupService.fetchPharmacy(any()))
          .thenReturn(Mono.just(Pharmacy.builder().build()));

      var provincialPatientSearchRequest =
          ProvincialPatientSearchRequest.builder()
              .requestHeader(RequestHeader.builder().bearerToken("bearerToken").build())
              .requestControlAct(
                  RequestControlAct.builder()
                      .eventRoot("2.16.840.1.113883.3.40.4.1")
                      .eventCorrelationId("564654")
                      .build())
              .requestBodyTransmissionWrapper(
                  RequestBodyTransmissionWrapper.builder()
                      .transmissionUniqueIdentifier("2.16.840.1.113883.3.40.1.5")
                      .transmissionCreationDateTime("20090905100144")
                      .processingCode("R")
                      .senderRoot("2.16.840.1.113883.3.40.5.2")
                      .senderApplicationId("TMPT-RI")
                      .build())
              .provincialRequestPayload(
                  ProvincialPatientSearchCriteria.builder()
                      .provincialHealthNumber("MAYM21561219")
                      .provincialRequestControl(
                          ProvincialRequestControl.builder()
                              .province(Province.QC)
                              .pharmacy(Pharmacy.builder().id("test").build())
                              .requestId("564654")
                              .build())
                      .firstName("Marina")
                      .lastName("Mayer")
                      .dateOfBirth(LocalDate.of(2021, 06, 12))
                      .gender(Gender.F)
                      .build())
              .build();
      var provincialPatientSearchResponseMono =
          quebecClientRegistryService
              .searchProvincialPatient(provincialPatientSearchRequest)
              .contextWrite(ctx -> ctx.put(MessageProcess.class, MessageProcess.PATIENT_SEARCH));

      assertNotNull(
          provincialPatientSearchResponseMono, "provincialPatientSearchResponseMono is Null");
      StepVerifier.create(provincialPatientSearchResponseMono)
          .assertNext(
              provincialPatientSearchResponse -> {
                var ackTypeCode =
                    provincialPatientSearchResponse
                        .getResponseBodyTransmissionWrapper()
                        .getAcknowledgeTypeCode();
                assertEquals("AE", ackTypeCode, "Acknowledgment Type Code");

                var ackDetails =
                    provincialPatientSearchResponse
                        .getResponseBodyTransmissionWrapper()
                        .getAcknowledgementDetails()
                        .get(0);
                assertEquals("E", ackDetails.getTypeCode(), "Acknowledgment Details type code");
                assertEquals("NS202", ackDetails.getCode(), "Acknowledgment Details code");

                var queryAcknowledgement =
                    provincialPatientSearchResponse.getQueryAcknowledgement();
                assertAll(
                    "queryAck",
                    () ->
                        assertEquals(
                            "AE",
                            queryAcknowledgement.getQueryResponseCode(),
                            "Query response code"));

                provincialPatientSearchResponse.getDetectedIssues().stream()
                    .findFirst()
                    .map(DetectedIssue::getEventCode)
                    .ifPresent(
                        detectedIssuesCode ->
                            assertNull(detectedIssuesCode, "Detected Issues Code"));
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        value = "should return error response When ackDetails code is SYN102 and queryAck is QE ")
    void testErrorResponseEF001() {
      when(internalLookupService.fetchPharmacy(any()))
          .thenReturn(Mono.just(Pharmacy.builder().build()));
      var provincialPatientSearchRequest =
          ProvincialPatientSearchRequest.builder()
              .requestHeader(RequestHeader.builder().bearerToken("bearerToken").build())
              .requestControlAct(
                  RequestControlAct.builder()
                      .eventRoot("2.16.840.1.113883.3.40.4.1")
                      .eventCorrelationId("564654")
                      .build())
              .requestBodyTransmissionWrapper(
                  RequestBodyTransmissionWrapper.builder()
                      .transmissionUniqueIdentifier("2.16.840.1.113883.3.40.1.5")
                      .transmissionCreationDateTime("20090905100144")
                      .processingCode("D")
                      .senderRoot("2.16.840.1.113883.3.40.5.2")
                      .senderApplicationId("TMPT-RI")
                      .build())
              .provincialRequestPayload(
                  ProvincialPatientSearchCriteria.builder()
                      .provincialRequestControl(
                          ProvincialRequestControl.builder()
                              .province(Province.QC)
                              .pharmacy(Pharmacy.builder().id("test").build())
                              .requestId("564654")
                              .build())
                      .firstName("PrénomvingtcaractèreXXXXZAAAAA")
                      .lastName("NomavectrentecaractèresmaximumXXXXXXXXXZAAAAAAAAAA")
                      .dateOfBirth(LocalDate.of(1978, 07, 13))
                      .gender(Gender.F)
                      .build())
              .build();
      var provincialPatientSearchResponseMono =
          quebecClientRegistryService
              .searchProvincialPatient(provincialPatientSearchRequest)
              .contextWrite(ctx -> ctx.put(MessageProcess.class, MessageProcess.PATIENT_SEARCH));

      assertNotNull(
          provincialPatientSearchResponseMono, "provincialPatientSearchResponseMono is null ");
      StepVerifier.create(provincialPatientSearchResponseMono)
          .assertNext(
              provincialPatientSearchResponse -> {
                var ackTypeCode =
                    provincialPatientSearchResponse
                        .getResponseBodyTransmissionWrapper()
                        .getAcknowledgeTypeCode();
                assertEquals("AE", ackTypeCode, "Acknowledgment Type Code");

                var ackDetails =
                    provincialPatientSearchResponse
                        .getResponseBodyTransmissionWrapper()
                        .getAcknowledgementDetails()
                        .get(0);
                assertEquals("E", ackDetails.getTypeCode(), "Acknowledgment Details");
                assertEquals("SYN102", ackDetails.getCode(), "Acknowledgment Details code");

                var queryAck = provincialPatientSearchResponse.getQueryAcknowledgement();
                assertAll(
                    "queryAck",
                    () ->
                        assertEquals("QE", queryAck.getQueryResponseCode(), "Query response code"));

                provincialPatientSearchResponse.getDetectedIssues().stream()
                    .findFirst()
                    .map(DetectedIssue::getEventCode)
                    .ifPresent(Assertions::assertNull);
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        value = "should return error response when DetectedIssues is VALIDAT and queryAck is NF")
    void testErrorResponse1410() {
      when(internalLookupService.fetchPharmacy(any()))
          .thenReturn(Mono.just(Pharmacy.builder().build()));
      var provincialPatientSearchRequest =
          ProvincialPatientSearchRequest.builder()
              .requestHeader(RequestHeader.builder().bearerToken("bearerToken").build())
              .requestControlAct(
                  RequestControlAct.builder()
                      .eventRoot("2.16.840.1.113883.3.40.4.1")
                      .eventCorrelationId("564654")
                      .build())
              .requestBodyTransmissionWrapper(
                  RequestBodyTransmissionWrapper.builder()
                      .transmissionUniqueIdentifier("2.16.840.1.113883.3.40.1.5")
                      .transmissionCreationDateTime("20090905100144")
                      .processingCode("D")
                      .senderRoot("2.16.840.1.113883.3.40.5.2")
                      .senderApplicationId("TMPT-RI")
                      .build())
              .provincialRequestPayload(
                  ProvincialPatientSearchCriteria.builder()
                      .provincialHealthNumber("MICF93560415")
                      .provincialRequestControl(
                          ProvincialRequestControl.builder()
                              .province(Province.QC)
                              .pharmacy(Pharmacy.builder().id("test").build())
                              .requestId("564654")
                              .build())
                      .firstName("Michel")
                      .lastName("Flore")
                      // invalid date for date of birth
                      .dateOfBirth(LocalDate.of(3025, 06, 04))
                      .build())
              .build();
      var provincialPatientSearchResponseMono =
          quebecClientRegistryService
              .searchProvincialPatient(provincialPatientSearchRequest)
              .contextWrite(ctx -> ctx.put(MessageProcess.class, MessageProcess.PATIENT_SEARCH));

      assertNotNull(
          provincialPatientSearchResponseMono, "provincialPatientSearchResponseMono is null");
      StepVerifier.create(provincialPatientSearchResponseMono)
          .assertNext(
              provincialPatientSearchResponse -> {
                var ackTypeCode =
                    provincialPatientSearchResponse
                        .getResponseBodyTransmissionWrapper()
                        .getAcknowledgeTypeCode();
                assertEquals("AA", ackTypeCode, "Acknowledgment Type Code");

                var ackDetails =
                    provincialPatientSearchResponse
                        .getResponseBodyTransmissionWrapper()
                        .getAcknowledgementDetails();
                assertTrue(ackDetails.isEmpty(), "Acknowledgement Details");

                var queryAck = provincialPatientSearchResponse.getQueryAcknowledgement();
                assertAll(
                    "queryAck",
                    () ->
                        assertEquals("NF", queryAck.getQueryResponseCode(), "Query response code"));

                provincialPatientSearchResponse.getDetectedIssues().stream()
                    .findFirst()
                    .map(DetectedIssue::getEventCode)
                    .ifPresent(
                        detectedIssuesCode ->
                            assertEquals("VALIDAT", detectedIssuesCode, "Detected Issues Code"));
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(value = "should return error response MISSMAND ")
    void testErrorResponseWithMISSMAND() {
      when(internalLookupService.fetchPharmacy(any()))
          .thenReturn(Mono.just(Pharmacy.builder().build()));
      var provincialPatientSearchRequest =
          ProvincialPatientSearchRequest.builder()
              .requestHeader(RequestHeader.builder().bearerToken("bearerToken").build())
              .requestControlAct(
                  RequestControlAct.builder()
                      .eventRoot("2.16.840.1.113883.3.40.4.1")
                      .eventCorrelationId("564654")
                      .build())
              .requestBodyTransmissionWrapper(
                  RequestBodyTransmissionWrapper.builder()
                      .transmissionUniqueIdentifier("2.16.840.1.113883.3.40.1.5")
                      .transmissionCreationDateTime("20090905100144")
                      .processingCode("D")
                      .senderRoot("2.16.840.1.113883.3.40.5.2")
                      .senderApplicationId("TMPT-RI")
                      .build())
              .provincialRequestPayload(
                  ProvincialPatientSearchCriteria.builder()
                      .provincialRequestControl(
                          ProvincialRequestControl.builder()
                              .pharmacy(Pharmacy.builder().id("test").build())
                              .province(Province.QC)
                              .build())
                      .build())
              .build();
      var provincialPatientSearchResponseMono =
          quebecClientRegistryService
              .searchProvincialPatient(provincialPatientSearchRequest)
              .contextWrite(ctx -> ctx.put(MessageProcess.class, MessageProcess.PATIENT_SEARCH));

      assertNotNull(
          provincialPatientSearchResponseMono, "provincialPatientSearchResponseMono is null");
      StepVerifier.create(provincialPatientSearchResponseMono)
          .assertNext(
              provincialPatientSearchResponse -> {
                provincialPatientSearchResponse.getDetectedIssues().stream()
                    .findFirst()
                    .map(DetectedIssue::getEventCode)
                    .ifPresent(
                        detectedIssuesCode ->
                            assertEquals("MISSMAND", detectedIssuesCode, "Detected Issues Code"));
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(value = "should return error response NOTFND ")
    void testErrorResponseWithNOTFND() {
      when(internalLookupService.fetchPharmacy(any()))
          .thenReturn(Mono.just(Pharmacy.builder().build()));
      var provincialPatientSearchRequest =
          ProvincialPatientSearchRequest.builder()
              .requestHeader(RequestHeader.builder().bearerToken("bearerToken").build())
              .requestControlAct(
                  RequestControlAct.builder()
                      .eventRoot("2.16.840.1.113883.3.40.4.1")
                      .eventCorrelationId("564654")
                      .build())
              .requestBodyTransmissionWrapper(
                  RequestBodyTransmissionWrapper.builder()
                      .transmissionUniqueIdentifier("2.16.840.1.113883.3.40.1.5")
                      .transmissionCreationDateTime("20090905100144")
                      .processingCode("D")
                      .senderRoot("2.16.840.1.113883.3.40.5.2")
                      .senderApplicationId("TMPT-RI")
                      .build())
              .provincialRequestPayload(
                  ProvincialPatientSearchCriteria.builder()
                      .provincialHealthNumber("MICF93560415")
                      .provincialRequestControl(
                          ProvincialRequestControl.builder()
                              .province(Province.QC)
                              .pharmacy(Pharmacy.builder().id("test").build())
                              .requestId("564654")
                              .build())
                      .firstName("Prénomvingtcaractère")
                      .lastName("Nomavectrentecaractèresmaximum")
                      .gender(Gender.F)
                      .dateOfBirth(LocalDate.of(1978, 07, 13))
                      .build())
              .build();
      var provincialPatientSearchResponseMono =
          quebecClientRegistryService
              .searchProvincialPatient(provincialPatientSearchRequest)
              .contextWrite(ctx -> ctx.put(MessageProcess.class, MessageProcess.PATIENT_SEARCH));

      assertNotNull(
          provincialPatientSearchResponseMono, "provincialPatientSearchResponseMono is null");

      StepVerifier.create(provincialPatientSearchResponseMono)
          .assertNext(
              provincialPatientSearchResponse -> {
                provincialPatientSearchResponse.getDetectedIssues().stream()
                    .findFirst()
                    .map(DetectedIssue::getEventCode)
                    .ifPresent(
                        detectedIssuesCode ->
                            assertEquals("NOTFND", detectedIssuesCode, "Detected Issues Code"));
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(value = "Shouldn Return Nam of new Born ")
    void testNewbornWithNAM() {
      when(internalLookupService.fetchPharmacy(any()))
          .thenReturn(Mono.just(Pharmacy.builder().build()));
      var provincialPatientSearchRequest =
          ProvincialPatientSearchRequest.builder()
              .requestHeader(RequestHeader.builder().bearerToken("bearerToken").build())
              .requestControlAct(
                  RequestControlAct.builder()
                      .eventRoot("2.16.840.1.113883.3.40.4.1")
                      .eventCorrelationId("564654")
                      .build())
              .requestBodyTransmissionWrapper(
                  RequestBodyTransmissionWrapper.builder()
                      .transmissionUniqueIdentifier("2.16.840.1.113883.3.40.1.5")
                      .transmissionCreationDateTime("20090905100144")
                      .processingCode("D")
                      .senderRoot("2.16.840.1.113883.3.40.5.2")
                      .senderApplicationId("TMPT-RI")
                      .build())
              .provincialRequestPayload(
                  ProvincialPatientSearchCriteria.builder()
                      .provincialRequestControl(
                          ProvincialRequestControl.builder()
                              .province(Province.QC)
                              .pharmacy(Pharmacy.builder().id("test").build())
                              .requestId("564654")
                              .build())
                      .firstName("Marina")
                      .lastName("Mayer")
                      .gender(Gender.F)
                      .dateOfBirth(LocalDate.of(2021, 06, 12))
                      .build())
              .build();
      var provincialPatientSearchResponseMono =
          quebecClientRegistryService
              .searchProvincialPatient(provincialPatientSearchRequest)
              .contextWrite(ctx -> ctx.put(MessageProcess.class, MessageProcess.PATIENT_SEARCH));

      assertNotNull(
          provincialPatientSearchResponseMono, "provincialPatientSearchResponseMono is null");
      StepVerifier.create(provincialPatientSearchResponseMono)
          .assertNext(
              provincialPatientSearchResponse -> {
                var profile = provincialPatientSearchResponse.getProvincialResponsePayload();
                assertEquals(
                    "MAYM21561219", profile.getProvincialHealthNumber().getValue(), "HealthNumber");
                assertEquals("8000000318", profile.getIdentifier().getValue(), "NIUU");
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(value = "Shouldn't Return Nam of new Born ")
    void testNewbornWithoutNAM() {
      when(internalLookupService.fetchPharmacy(any()))
          .thenReturn(Mono.just(Pharmacy.builder().build()));
      var provincialPatientSearchRequest =
          ProvincialPatientSearchRequest.builder()
              .requestHeader(RequestHeader.builder().bearerToken("bearerToken").build())
              .requestControlAct(
                  RequestControlAct.builder()
                      .eventRoot("2.16.840.1.113883.3.40.4.1")
                      .eventCorrelationId("564654")
                      .build())
              .requestBodyTransmissionWrapper(
                  RequestBodyTransmissionWrapper.builder()
                      .transmissionUniqueIdentifier("2.16.840.1.113883.3.40.1.5")
                      .transmissionCreationDateTime("20090905100144")
                      .processingCode("D")
                      .senderRoot("2.16.840.1.113883.3.40.5.2")
                      .senderApplicationId("TMPT-RI")
                      .build())
              .provincialRequestPayload(
                  ProvincialPatientSearchCriteria.builder()
                      .provincialRequestControl(
                          ProvincialRequestControl.builder()
                              .province(Province.QC)
                              .pharmacy(Pharmacy.builder().id("test").build())
                              .requestId("564654")
                              .build())
                      .firstName("BB De Héloïse")
                      .lastName("Braun")
                      .gender(Gender.F)
                      .dateOfBirth(LocalDate.of(2021, 06, 10))
                      .build())
              .build();
      var provincialPatientSearchResponseMono =
          quebecClientRegistryService
              .searchProvincialPatient(provincialPatientSearchRequest)
              .contextWrite(ctx -> ctx.put(MessageProcess.class, MessageProcess.PATIENT_SEARCH));

      assertNotNull(provincialPatientSearchResponseMono, "provincialPatientSearchResponseMono");
      StepVerifier.create(provincialPatientSearchResponseMono)
          .assertNext(
              provincialPatientSearchResponse -> {
                var profile = provincialPatientSearchResponse.getProvincialResponsePayload();
                assertNull(profile.getProvincialHealthNumber(), "HealthNumber");
                assertEquals("8000000441", profile.getIdentifier().getValue(), "NIUU");
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(value = "Deceased patient")
    void testDeceasedPatient() {
      when(internalLookupService.fetchPharmacy(any()))
          .thenReturn(Mono.just(Pharmacy.builder().build()));
      var provincialPatientSearchRequest =
          ProvincialPatientSearchRequest.builder()
              .requestHeader(RequestHeader.builder().bearerToken("bearerToken").build())
              .requestControlAct(
                  RequestControlAct.builder()
                      .eventRoot("2.16.840.1.113883.3.40.4.1")
                      .eventCorrelationId("564654")
                      .build())
              .requestBodyTransmissionWrapper(
                  RequestBodyTransmissionWrapper.builder()
                      .transmissionUniqueIdentifier("2.16.840.1.113883.3.40.1.5")
                      .transmissionCreationDateTime("20090905100144")
                      .processingCode("D")
                      .senderRoot("2.16.840.1.113883.3.40.5.2")
                      .senderApplicationId("TMPT-RI")
                      .build())
              .provincialRequestPayload(
                  ProvincialPatientSearchCriteria.builder()
                      .provincialHealthNumber("LACM30010115")
                      .provincialRequestControl(
                          ProvincialRequestControl.builder()
                              .province(Province.QC)
                              .pharmacy(Pharmacy.builder().id("test").build())
                              .requestId("564654")
                              .build())
                      .build())
              .build();
      var provincialPatientSearchResponseMono =
          quebecClientRegistryService
              .searchProvincialPatient(provincialPatientSearchRequest)
              .contextWrite(ctx -> ctx.put(MessageProcess.class, MessageProcess.PATIENT_SEARCH));

      assertNotNull(
          provincialPatientSearchResponseMono, "provincialPatienSearchResponseMono is null");
      StepVerifier.create(provincialPatientSearchResponseMono)
          .assertNext(
              provincialPatientSearchResponse -> {
                var profile = provincialPatientSearchResponse.getProvincialResponsePayload();
                assertNotNull(provincialPatientSearchResponse, "provincialPatientSearchResponse");

                assertEquals(
                    "LACM30010115",
                    profile.getProvincialHealthNumber().getValue(),
                    "Health Number");
                assertEquals("8000000110", profile.getIdentifier().getValue(), "NIUU");
                assertEquals(
                    LocalDate.of(2021, 01, 01), profile.getDeceasedDate(), "Deceased Date");
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        value = "When Excessively Long Names Then Multiple Acknowledgement Details Are Observed")
    void whenExcessiveLongNames_ThenMultipleAcknowledgementDetailsAreObserved() {
      when(internalLookupService.fetchPharmacy(any()))
          .thenReturn(Mono.just(Pharmacy.builder().build()));
      var provincialPatientSearchRequest =
          ProvincialPatientSearchRequest.builder()
              .requestHeader(RequestHeader.builder().bearerToken("bearerToken").build())
              .requestControlAct(
                  RequestControlAct.builder()
                      .eventRoot("2.16.840.1.113883.3.40.4.1")
                      .eventCorrelationId("564654")
                      .build())
              .requestBodyTransmissionWrapper(
                  RequestBodyTransmissionWrapper.builder()
                      .transmissionUniqueIdentifier("2.16.840.1.113883.3.40.1.5")
                      .transmissionCreationDateTime("20090905100144")
                      .processingCode("D")
                      .senderRoot("2.16.840.1.113883.3.40.5.2")
                      .senderApplicationId("TMPT-RI")
                      .build())
              .provincialRequestPayload(
                  ProvincialPatientSearchCriteria.builder()
                      .firstName("BB De Héloïse")
                      .lastName("Braun")
                      .dateOfBirth(LocalDate.of(2020, 10, 23))
                      .gender(Gender.F)
                      .motherLastName("Poulainnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn")
                      .motherFirstName("Héloïseeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee")
                      .fatherFirstName("Michonnnnnnnnnnnnnnnnnnnnnnnnnnnnnn")
                      .fatherLastName("Braun")
                      .provincialRequestControl(
                          ProvincialRequestControl.builder()
                              .province(Province.QC)
                              .pharmacy(Pharmacy.builder().id("test").build())
                              .requestId("564654")
                              .build())
                      .build())
              .build();
      var provincialPatientSearchResponseMono =
          quebecClientRegistryService
              .searchProvincialPatient(provincialPatientSearchRequest)
              .contextWrite(ctx -> ctx.put(MessageProcess.class, MessageProcess.PATIENT_SEARCH));

      assertNotNull(provincialPatientSearchResponseMono, "Response should not be null");
      StepVerifier.create(provincialPatientSearchResponseMono)
          .assertNext(
              provincialPatientSearchResponse -> {
                List<AcknowledgementDetails> observedAcknowledgementDetails =
                    provincialPatientSearchResponse
                        .getResponseBodyTransmissionWrapper()
                        .getAcknowledgementDetails();
                List<AcknowledgementDetails> expectedAcknowledgementDetails = new ArrayList<>();
                expectedAcknowledgementDetails.add(
                    getAcknowledgementDetails(
                        "PRPA_IN101103CA/controlActEvent/queryByParameter/parameterList/mothersMaidenName/value/given"));
                expectedAcknowledgementDetails.add(
                    getAcknowledgementDetails(
                        "PRPA_IN101103CA/controlActEvent/queryByParameter/parameterList/fathersName/value/given"));

                assertOnAcknowledgementDetails(
                    expectedAcknowledgementDetails, observedAcknowledgementDetails);
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(value = "should return  successful hl7 response toFindProviderQuery from DSQ")
    void should_return_hl7_success_response_to_FindProviderQuery_from_DSQ()
        throws UnsupportedEncodingException, NoSuchAlgorithmException {
      var transmissionUniqueIdentifier = uuidGenerator.generateUUID();
      var eventCorrelationID = uuidGenerator.generateUUID();
      when(codeableConceptService.findSystemRoleCodingByProvincialRoleCode(any(), any()))
          .thenReturn(
              Optional.of(
                  Coding.builder()
                      .province(Province.ALL)
                      .system("SYSTEM")
                      .code("3310000")
                      .display(
                          new LocalizedText[] {
                            LocalizedText.builder()
                                .language(LanguageCode.ENG)
                                .text("3310000")
                                .build(),
                            LocalizedText.builder()
                                .language(LanguageCode.FRA)
                                .text("OPTOMÉTRISTE")
                                .build()
                          })
                      .build()));
      when(internalLookupService.fetchPharmacy(any()))
          .thenReturn(Mono.just(Pharmacy.builder().build()));

      var request =
          ProvincialProviderSearchRequest.builder()
              .requestHeader(RequestHeader.builder().bearerToken("bearerToken").build())
              .requestControlAct(
                  RequestControlAct.builder()
                      .eventRoot("eventRoot")
                      .eventCorrelationId(eventCorrelationID)
                      .build())
              .requestBodyTransmissionWrapper(
                  RequestBodyTransmissionWrapper.builder()
                      .transmissionUniqueIdentifier(transmissionUniqueIdentifier)
                      .transmissionCreationDateTime("20211110182928")
                      .processingCode("D")
                      .senderRoot("VPHX")
                      .senderApplicationId("ApplicationID")
                      .senderApplicationName("ApplicationName")
                      .build())
              .provincialRequestPayload(
                  ProvincialProviderSearchCriteria.builder()
                      .firstName("Ivan")
                      .lastName("Helios")
                      .gender(Gender.M)
                      .provincialRequestControl(
                          ProvincialRequestControl.builder()
                              .province(Province.QC)
                              .pharmacy(Pharmacy.builder().id("test").build())
                              .requestId(eventCorrelationID)
                              .build())
                      .build())
              .build();
      var provincialProviderSearchResponseMono =
          quebecClientRegistryService
              .searchProvincialProvider(request)
              .contextWrite(ctx -> ctx.put(MessageProcess.class, MessageProcess.PROVIDER_SEARCH));

      assertNotNull(provincialProviderSearchResponseMono, "provincialPatientSearchResponse");
      StepVerifier.create(provincialProviderSearchResponseMono)
          .assertNext(
              provincialProviderSearchResponse -> {
                var profiles = provincialProviderSearchResponse.getProvincialResponsePayload();
                assertThat(profiles.getProvincialProviderProfiles()).hasSize(1);
                var providerProfile = profiles.getProvincialProviderProfiles().get(0);
                assertEquals(
                    "CPN.01001976.QC.PRS", providerProfile.getIdentifier().getValue(), "NIUI");
                assertThat(providerProfile.getProviderRole())
                    .isEqualTo(
                        Coding.builder()
                            .code("3310000")
                            .system("SYSTEM")
                            .province(Province.ALL)
                            .display(
                                new LocalizedText[] {
                                  LocalizedText.builder()
                                      .language(LanguageCode.ENG)
                                      .text("OPTOMETRIST")
                                      .build(),
                                  LocalizedText.builder()
                                      .language(LanguageCode.FRA)
                                      .text("OPTOMÉTRISTE")
                                      .build()
                                })
                            .build());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName("should return FindProviderQuery hl7 success response from DSQ")
    void should_return_hl7_success_response_to_FindProviderQueryfrom_DSQ() throws Exception {
      var uuid = uuidGenerator.generateUUID();
      var uuidCorrelation = uuidGenerator.generateUUID();
      when(internalLookupService.fetchPharmacy(any()))
          .thenReturn(Mono.just(Pharmacy.builder().build()));
      var request =
          ProvincialProviderSearchRequest.builder()
              .requestHeader(RequestHeader.builder().bearerToken("bearerToken").build())
              .requestControlAct(
                  RequestControlAct.builder()
                      .eventRoot("eventRoot")
                      .eventCorrelationId(uuid)
                      .build())
              .requestBodyTransmissionWrapper(
                  RequestBodyTransmissionWrapper.builder()
                      .transmissionUniqueIdentifier(uuid)
                      .transmissionCreationDateTime("20211110182928")
                      .processingCode("D")
                      .senderRoot("VPHX")
                      .senderApplicationId("ApplicationID")
                      .senderApplicationName("ApplicationName")
                      .build())
              .provincialRequestPayload(
                  ProvincialProviderSearchCriteria.builder()
                      .firstName("Ivan")
                      .lastName("Helios")
                      .gender(Gender.M)
                      .provincialRequestControl(
                          ProvincialRequestControl.builder()
                              .requestId(uuidCorrelation)
                              .pharmacy(Pharmacy.builder().id("test").name("12345678").build())
                              .province(Province.QC)
                              .build())
                      .build())
              .build();
      var messageProcess = MessageProcess.PROVIDER_SEARCH;
      HttpHeaders headers = new HttpHeaders();
      headers.set(
          provincialRequestProperties
              .getRequest()
              .getTransaction()
              .get(messageProcess.getName())
              .getHeader()
              .getSoapAction(),
          provincialRequestProperties
              .getRequest()
              .getTransaction()
              .get(messageProcess.getName())
              .getHeader()
              .getValue());

      ResponseEntity<String> response =
          transformationService
              .transformRequest(request)
              .flatMap(
                  signedSoapRequest -> {
                    return provincialWebClient.callClientRegistry(
                        provincialRequestProperties
                            .getRequest()
                            .getTransaction()
                            .get(messageProcess.getName())
                            .getUri(),
                        headers,
                        signedSoapRequest);
                  })
              .contextWrite(Context.of(MessageProcess.class, MessageProcess.PROVIDER_SEARCH))
              .block();

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
      builderFactory.setNamespaceAware(false);
      DocumentBuilder builder = builderFactory.newDocumentBuilder();
      Document xmlDocument =
          builder.parse(new InputSource(new StringReader(response.getBody().replace("\n", ""))));
      var xPathFactory = XPathFactory.newInstance();

      var acknowledgementTypeCode =
          xPathFactory
              .newXPath()
              .compile("//Envelope/Body/PRPM_IN306011CA/acknowledgement/typeCode/@code")
              .evaluate(xmlDocument);
      assertThat(acknowledgementTypeCode).isEqualTo("AA");
      var acknowledgementDetailsTypeCode =
          xPathFactory
              .newXPath()
              .compile(
                  "//Envelope/Body/PRPM_IN306011CA/acknowledgement/acknowledgementDetail/@typeCode")
              .evaluate(xmlDocument);
      // there should not be any acknowledgementDetail typeCode when query finds and returns
      // provider
      // details
      assertThat(acknowledgementDetailsTypeCode).isEmpty();
    }

    @Test
    @DisplayName("should return GetPatientConsent hl7 success response from DSQ")
    void should_return_hl7_success_response_to_GetPatientConsentfrom_DSQ() throws Exception {
      var uuid = uuidGenerator.generateUUID();
      var uuidCorrelation = uuidGenerator.generateUUID();
      when(internalLookupService.fetchPharmacy(any()))
          .thenReturn(Mono.just(Pharmacy.builder().build()));
      var provincialPatientConsentRequest =
          ProvincialPatientConsentRequest.builder()
              .requestHeader(RequestHeader.builder().bearerToken("bearerToken").build())
              .requestControlAct(
                  RequestControlAct.builder()
                      .eventCorrelationId("eventCorrelationId")
                      .eventRoot("eventRoot")
                      .build())
              .requestBodyTransmissionWrapper(
                  RequestBodyTransmissionWrapper.builder()
                      .transmissionUniqueIdentifier(uuid)
                      .transmissionCreationDateTime("20211110182928")
                      .processingCode("D")
                      .senderRoot("VPHX")
                      .senderApplicationName("ApplicationName")
                      .senderApplicationId("ApplicationID")
                      .build())
              .provincialRequestPayload(
                  ProvincialPatientConsentCriteria.builder()
                      .patientIdentifier("8000000300")
                      .firstName("Fares")
                      .lastName("Mayer")
                      .effectiveDateTime("20211012131212")
                      .provincialRequestControl(
                          ProvincialRequestControl.builder()
                              .requestId(uuidCorrelation)
                              .pharmacy(Pharmacy.builder().id("test").name("12345678").build())
                              .province(Province.QC)
                              .build())
                      .build())
              .build();
      var messageProcess = MessageProcess.PATIENT_CONSENT;
      HttpHeaders headers = new HttpHeaders();
      headers.set(
          provincialRequestProperties
              .getRequest()
              .getTransaction()
              .get(messageProcess.getName())
              .getHeader()
              .getSoapAction(),
          provincialRequestProperties
              .getRequest()
              .getTransaction()
              .get(messageProcess.getName())
              .getHeader()
              .getValue());

      ResponseEntity<String> response =
          transformationService
              .transformRequest(provincialPatientConsentRequest)
              .flatMap(
                  signedSoapRequest -> {
                    return provincialWebClient.callClientRegistry(
                        provincialRequestProperties
                            .getRequest()
                            .getTransaction()
                            .get(messageProcess.getName())
                            .getUri(),
                        headers,
                        signedSoapRequest);
                  })
              .contextWrite(Context.of(MessageProcess.class, MessageProcess.PATIENT_CONSENT))
              .block();

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
      builderFactory.setNamespaceAware(false);
      DocumentBuilder builder = builderFactory.newDocumentBuilder();
      Document xmlDocument =
          builder.parse(new InputSource(new StringReader(response.getBody().replace("\n", ""))));
      var xPathFactory = XPathFactory.newInstance();
      var acknowledgementTypeCode =
          xPathFactory
              .newXPath()
              .compile("//Envelope/Body/RCMR_IN010997CAQC_V01/acknowledgement/typeCode/@code")
              .evaluate(xmlDocument);
      assertThat(acknowledgementTypeCode).isEqualTo("AA");

      var queryResponseCode =
          xPathFactory
              .newXPath()
              .compile(
                  "//Envelope/Body/RCMR_IN010997CAQC_V01/controlActEvent/queryAck/queryResponseCode/@code")
              .evaluate(xmlDocument);
      assertThat(queryResponseCode).isEqualTo("OK");
    }

    @Test
    @DisplayName("should return Find Location Summary hl7 success response from DSQ")
    void should_return_hl7_success_response_to_FindLocationSummaryfrom_DSQ() throws Exception {
      var uuid = uuidGenerator.generateUUID();
      var uuidCorrelation = uuidGenerator.generateUUID();
      when(internalLookupService.fetchPharmacy(any()))
          .thenReturn(Mono.just(Pharmacy.builder().build()));
      ProvincialLocationSearchRequest provincialLocationSearchRequest =
          ProvincialLocationSearchRequest.builder()
              .requestHeader(RequestHeader.builder().bearerToken("bearerToken").build())
              .requestBodyTransmissionWrapper(
                  RequestBodyTransmissionWrapper.builder()
                      .senderApplicationId("senderApplicationId")
                      .senderApplicationName("senderApplicationName")
                      .senderRoot("senderRoot")
                      .processingCode("D")
                      .transmissionCreationDateTime("20211110182928")
                      .transmissionUniqueIdentifier(uuid)
                      .build())
              .requestControlAct(
                  RequestControlAct.builder()
                      .eventRoot("eventRoot")
                      .eventCorrelationId("eventCorrelationId")
                      .build())
              .provincialRequestPayload(
                  ProvincialLocationSearchCriteria.builder()
                      .provincialRequestControl(
                          ProvincialRequestControl.builder()
                              .province(Province.QC)
                              .pharmacy(Pharmacy.builder().id("test").build())
                              .build())
                      .locationName("general")
                      .locationType("HOSP")
                      .build())
              .build();
      var messageProcess = MessageProcess.LOCATION_SEARCH;

      HttpHeaders headers = new HttpHeaders();
      headers.set(
          provincialRequestProperties
              .getRequest()
              .getTransaction()
              .get(messageProcess.getName())
              .getHeader()
              .getSoapAction(),
          provincialRequestProperties
              .getRequest()
              .getTransaction()
              .get(messageProcess.getName())
              .getHeader()
              .getValue());

      ResponseEntity<String> response =
          transformationService
              .transformRequest(provincialLocationSearchRequest)
              .flatMap(
                  signedSoapRequest -> {
                    return provincialWebClient.callClientRegistry(
                        provincialRequestProperties
                            .getRequest()
                            .getTransaction()
                            .get(messageProcess.getName())
                            .getUri(),
                        headers,
                        signedSoapRequest);
                  })
              .contextWrite(Context.of(MessageProcess.class, MessageProcess.LOCATION_SEARCH))
              .block();

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
      builderFactory.setNamespaceAware(false);
      DocumentBuilder builder = builderFactory.newDocumentBuilder();
      Document xmlDocument =
          builder.parse(new InputSource(new StringReader(response.getBody().replace("\n", ""))));
      var xPathFactory = XPathFactory.newInstance();
      var acknowledgementTypeCode =
          xPathFactory
              .newXPath()
              .compile("//Envelope/Body/PRLO_IN202011CAQC_V01/acknowledgement/typeCode/@code")
              .evaluate(xmlDocument);
      assertThat(acknowledgementTypeCode).isEqualTo("AA");

      var queryResponseCode =
          xPathFactory
              .newXPath()
              .compile(
                  "//Envelope/Body/PRLO_IN202011CAQC_V01/controlActEvent/queryAck/queryResponseCode/@code")
              .evaluate(xmlDocument);
      assertThat(queryResponseCode).isEqualTo("OK");
    }

    @Test
    @DisplayName("should return Find Location Details hl7 success response from DSQ")
    void should_return_hl7_success_response_to_FindLocationDetailsfrom_DSQ() throws Exception {
      var uuid = uuidGenerator.generateUUID();
      when(internalLookupService.fetchPharmacy(any()))
          .thenReturn(Mono.just(Pharmacy.builder().build()));
      ProvincialLocationDetailsRequest provincialLocationDetailsRequest =
          ProvincialLocationDetailsRequest.builder()
              .requestHeader(RequestHeader.builder().bearerToken("bearerToken").build())
              .requestBodyTransmissionWrapper(
                  RequestBodyTransmissionWrapper.builder()
                      .senderApplicationId("senderApplicationId")
                      .senderApplicationName("senderApplicationName")
                      .senderRoot("senderRoot")
                      .processingCode("D")
                      .transmissionCreationDateTime("20211110182928")
                      .transmissionUniqueIdentifier(uuid)
                      .build())
              .requestControlAct(
                  RequestControlAct.builder()
                      .eventRoot("eventRoot")
                      .eventCorrelationId("eventCorrelationId")
                      .build())
              .provincialRequestPayload(
                  ProvincialLocationDetailsCriteria.builder()
                      .provincialRequestControl(
                          ProvincialRequestControl.builder()
                              .pharmacy(Pharmacy.builder().id("test").build())
                              .province(Province.QC)
                              .build())
                      .identifier("1000017085")
                      .build())
              .build();
      var messageProcess = MessageProcess.LOCATION_DETAILS;

      HttpHeaders headers = new HttpHeaders();
      headers.set(
          provincialRequestProperties
              .getRequest()
              .getTransaction()
              .get(messageProcess.getName())
              .getHeader()
              .getSoapAction(),
          provincialRequestProperties
              .getRequest()
              .getTransaction()
              .get(messageProcess.getName())
              .getHeader()
              .getValue());

      ResponseEntity<String> response =
          transformationService
              .transformRequest(provincialLocationDetailsRequest)
              .flatMap(
                  signedSoapRequest -> {
                    return provincialWebClient.callClientRegistry(
                        provincialRequestProperties
                            .getRequest()
                            .getTransaction()
                            .get(messageProcess.getName())
                            .getUri(),
                        headers,
                        signedSoapRequest);
                  })
              .contextWrite(Context.of(MessageProcess.class, MessageProcess.LOCATION_DETAILS))
              .block();

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
      builderFactory.setNamespaceAware(false);
      DocumentBuilder builder = builderFactory.newDocumentBuilder();
      Document xmlDocument =
          builder.parse(new InputSource(new StringReader(response.getBody().replace("\n", ""))));
      var xPathFactory = XPathFactory.newInstance();
      var acknowledgementTypeCode =
          xPathFactory
              .newXPath()
              .compile("//Envelope/Body/PRLO_IN202013CAQC_V01/acknowledgement/typeCode/@code")
              .evaluate(xmlDocument);
      assertThat(acknowledgementTypeCode).isEqualTo("AA");

      var queryResponseCode =
          xPathFactory
              .newXPath()
              .compile(
                  "//Envelope/Body/PRLO_IN202013CAQC_V01/controlActEvent/queryAck/queryResponseCode/@code")
              .evaluate(xmlDocument);
      assertThat(queryResponseCode).isEqualTo("OK");
    }

    private void assertOnAcknowledgementDetails(
        List<AcknowledgementDetails> expectedAcknowledgementDetails,
        List<AcknowledgementDetails> observedAcknowledgementDetails) {
      assertIterableEquals(expectedAcknowledgementDetails, observedAcknowledgementDetails);
    }

    private AcknowledgementDetails getAcknowledgementDetails(String location) {
      AcknowledgementDetails expectedAcknowledgementDetail1 =
          AcknowledgementDetails.builder()
              .typeCode("E")
              .code("SYN102")
              .text("Data type error")
              .location(location)
              .build();
      return expectedAcknowledgementDetail1;
    }
  }
}
