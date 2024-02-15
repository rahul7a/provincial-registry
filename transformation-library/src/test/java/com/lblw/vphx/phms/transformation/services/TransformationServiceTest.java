package com.lblw.vphx.phms.transformation.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.lblw.vphx.iams.securityauditengine.oauth.ApplicationProperties;
import com.lblw.vphx.iams.securityauditengine.oauth.OAuthClientCredentials;
import com.lblw.vphx.phms.common.coding.services.CodeableConceptService;
import com.lblw.vphx.phms.common.config.MongoConfig;
import com.lblw.vphx.phms.common.internal.config.InternalApiConfig;
import com.lblw.vphx.phms.common.internal.objectstorage.client.ObjectStorageClient;
import com.lblw.vphx.phms.common.internal.objectstorage.service.ObjectStorageService;
import com.lblw.vphx.phms.common.security.services.OAuthClientService;
import com.lblw.vphx.phms.common.security.services.SecurityService;
import com.lblw.vphx.phms.common.utils.XmlParsingUtils;
import com.lblw.vphx.phms.configurations.ProvincialRequestProperties;
import com.lblw.vphx.phms.domain.common.MessageProcess;
import com.lblw.vphx.phms.domain.common.SystemIdentifier;
import com.lblw.vphx.phms.domain.common.response.hl7v3.ResponseHeader;
import com.lblw.vphx.phms.domain.patient.response.ProvincialPatientProfile;
import com.lblw.vphx.phms.domain.patient.response.hl7v3.ProvincialPatientSearchResponse;
import com.lblw.vphx.phms.transformation.exceptions.TransformationException;
import com.lblw.vphx.phms.transformation.request.RequestContextFactory;
import com.lblw.vphx.phms.transformation.request.RequestTransformerEngine;
import com.lblw.vphx.phms.transformation.request.preprocessors.BaseTemplateRequestTemplatePreProcessor;
import com.lblw.vphx.phms.transformation.response.ResponseTransformerEngine;
import com.lblw.vphx.phms.transformation.response.helpers.CommonResponseTransformerHelper;
import com.lblw.vphx.phms.transformation.response.helpers.CommonResponseXPathTransformerHelper;
import com.lblw.vphx.phms.transformation.response.transformers.DefaultResponseTransformer;
import com.lblw.vphx.phms.transformation.response.transformers.LocationDetailsResponseTransformer;
import com.lblw.vphx.phms.transformation.response.transformers.locationsummary.LocationSummaryResponseXPathTransformer;
import com.lblw.vphx.phms.transformation.response.transformers.patientconsent.PatientConsentResponseXPathTransformer;
import com.lblw.vphx.phms.transformation.response.transformers.patientsearch.PatientSearchResponseXPathTransformer;
import com.lblw.vphx.phms.transformation.response.transformers.providersearch.ProviderSearchResponseXPathTransformer;
import com.lblw.vphx.phms.transformation.utils.TestUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

@DataMongoTest
@TestPropertySource(
    properties = {
      "phms.quebec.request.security.keystore.location=keystorelocation",
      "phms.quebec.request.security.certificate.password=NOT_NULL",
      "phms.quebec.request.security.certificate.alias=NOT_NULL"
    })
@ContextConfiguration(
    classes = {
      TransformationService.class,
      ObjectStorageClient.class,
      ObjectStorageService.class,
      SecurityService.class,
      ThymeleafAutoConfiguration.class,
      RequestTransformerEngine.class,
      ResponseTransformerEngine.class,
      XmlParsingUtils.class,
      CommonResponseTransformerHelper.class,
      CodeableConceptService.class,
      RequestContextFactory.class,
      MongoConfig.class,
      WebClient.class,
      ApplicationProperties.class,
      OAuthClientCredentials.class,
      OAuthClientService.class,
      PatientSearchResponseXPathTransformer.class,
      ProviderSearchResponseXPathTransformer.class,
      PatientConsentResponseXPathTransformer.class,
      LocationSummaryResponseXPathTransformer.class,
      LocationDetailsResponseTransformer.class,
      DefaultResponseTransformer.class,
      BaseTemplateRequestTemplatePreProcessor.class,
      InternalApiConfig.class,
      ProvincialRequestProperties.class,
      CommonResponseXPathTransformerHelper.class
    })
@Slf4j
@TestPropertySource(
    properties = {
      "phms.qc.request.province.transformation.security.keystore.location=keystore location",
    })
@ActiveProfiles("test")
class TransformationServiceTest {
  @MockBean
  @Qualifier("oauth-web-client")
  WebClient webClient;

  @MockBean ObjectStorageClient objectStorageClient;
  @MockBean OAuthClientService oAuthClientService;
  @MockBean SecurityService securityService;
  @MockBean CodeableConceptService codeableConceptService;

  @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
  ProvincialRequestProperties provincialRequestProperties;

  @Autowired private PatientSearchResponseXPathTransformer patientSearchResponseXPathTransformer;

  @Autowired
  private LocationSummaryResponseXPathTransformer locationSummaryResponseXPathTransformer;

  @Autowired private TransformationService transformationService;
  private Context requestContext;

  @BeforeEach
  void beforeEach() throws Exception {
    requestContext = Context.of(MessageProcess.class, MessageProcess.PATIENT_SEARCH);
    when(securityService.signRequest(anyString(), anyBoolean()))
        .thenAnswer(
            arguments ->
                TestUtils.convertStringToDocument(
                    arguments.<String>getArgument(0).replaceAll(">\\s+<", "><")));
  }

  @Test
  @DisplayName("Expects IllegalArgumentException if response is null")
  void test_nullResponse() {
    StepVerifier.create(
            transformationService
                .transformResponse(null)
                .contextWrite(
                    Context.of(MessageProcess.class, MessageProcess.PATIENT_SEARCH).readOnly()))
        .expectErrorMatches(
            e ->
                e instanceof TransformationException
                    && e.getCause()
                        .getMessage()
                        .equals("Null response or Empty Context is provided."))
        .verify();
  }

  @Test
  @DisplayName("When request and message process is passed  string xml is generated")
  void test_transformRequest() throws Exception {
    var patientSearchRequest = TransformRequestsMockFactory.provincialPatientSearchRequestBuilder();

    Context requestContext = Context.of(MessageProcess.class, MessageProcess.PATIENT_SEARCH);

    when(provincialRequestProperties.getRequest().getSecurity().getCertificate().getLocation())
        .thenReturn("target/certs");

    StepVerifier.create(
            transformationService
                .transformRequest(patientSearchRequest)
                .contextWrite(requestContext))
        .assertNext(
            request -> {
              assertTrue(
                  request
                      .toString()
                      .contains(
                          "<id root=\"2.16.840.1.113883.3.40.1.5\"/>"
                              + "<creationTime value=\"20090905100144\"/>"
                              + "<responseModeCode code=\"I\"/>"
                              + "<versionCode code=\"NE2006\"/>"));
              assertTrue(
                  request
                      .toString()
                      .contains(
                          "<processingCode code=\"D\"/>"
                              + "<processingModeCode code=\"T\"/>"
                              + "<acceptAckCode code=\"AL\"/>"));
              assertTrue(
                  request
                      .toString()
                      .contains(
                          "<id extension=\"564654\" root=\"2.16.840.1.113883.3.40.4.1\" use=\"BUS\"/>"
                              + "<code code=\"PRPA_TE101103CA\" codeSystem=\"2.16.840.1.113883.11.19427\"/>"
                              + "<statusCode code=\"completed\"/>"));
              assertTrue(
                  request.toString().contains("<queryId root=\"2.16.840.1.113883.3.40.1.5\"/>"));
              assertTrue(
                  request
                      .toString()
                      .contains(
                          "<value extension=\"LACM30010115\" root=\"2.16.124.10.101.1.60.100\"/>"));
            })
        .verifyComplete();
  }

  @Test
  @DisplayName("Transform SOAP XML response to ProvincialPatientSearchResponse")
  void test_validResponse() throws Exception {

    Path path =
        Paths.get(
            "src/test/resources",
            "patientprofile/dsq.response/find_candidate_patient_found_response.xml");
    String hl7_response = Files.readString(path);
    MessageProcess messageProcess = MessageProcess.PATIENT_SEARCH;
    StepVerifier.create(
            transformationService
                .transformResponse(hl7_response)
                .contextWrite(
                    Context.of(MessageProcess.class, MessageProcess.PATIENT_SEARCH).readOnly()))
        .assertNext(
            actualResponse -> {
              ProvincialPatientSearchResponse provincialPatientSearchResponse =
                  (ProvincialPatientSearchResponse) actualResponse;
              assertNotNull(provincialPatientSearchResponse, "hl7 response");
              validateResponseHeaders(
                  provincialPatientSearchResponse.getResponseHeader(),
                  "e59b672e-ea03-468a-9d5d-1a2a93d68575",
                  "ac490167a68a43ec819217dff65c8",
                  "67deeeab-2404-4a85-ae31-8517b79856fc");
              var profile = provincialPatientSearchResponse.getProvincialResponsePayload();
              validateProfile(profile);
              var address = profile.getAddress();
              assertAll(
                  "address",
                  () -> assertNotNull(address),
                  () -> assertEquals("4109 avenue Isabella", address.getStreetAddressLine()),
                  () -> assertEquals("Montral", address.getCity()),
                  () -> assertEquals("CA", address.getCountry()),
                  () -> assertEquals("H3T1N5", address.getPostalCode()),
                  () -> assertEquals("Qc", address.getProvince()));
              var responseAck = profile.getProvincialResponseAcknowledgement();

              assertAll(
                  "provincialResponseAcknowledgement",
                  () -> assertNotNull(responseAck),
                  () ->
                      assertEquals(
                          "564654",
                          responseAck.getAuditEvent().getProvincialRequestControl().getRequestId(),
                          "request correlation id"));
            })
        .verifyComplete();
  }

  private void validateResponseHeaders(
      ResponseHeader header,
      String expectedTrackingId,
      String expectedSessionId,
      String expectedTransactionId) {
    assertNotNull(header, "header");
    assertEquals(expectedTrackingId, header.getTrackingId(), "trackingId");
    assertEquals(expectedSessionId, header.getSessionId(), "sessionId");
    assertEquals(expectedTransactionId, header.getTransactionId(), "transactionId");
  }

  private void validateProfile(ProvincialPatientProfile profile) {
    assertAll(
        "profile",
        () -> assertNotNull(profile, "profile"),
        () -> assertEquals("Marina", profile.getFirstName(), "first name"),
        () -> assertEquals("Mayer", profile.getLastName(), "last name)"),
        () -> assertEquals(LocalDate.of(2021, 06, 12), profile.getDateOfBirth(), "date of birth"),
        () -> assertEquals(null, profile.getDeceasedDate(), "deceased date"),
        () -> assertEquals("Fares", profile.getFatherFirstName(), "father's first name"),
        () -> assertEquals("Mayer", profile.getFatherLastName(), "father's last name"),
        () -> assertEquals("Flore", profile.getMotherFirstName(), "mother's first name"),
        () -> assertEquals("Michel", profile.getMotherLastName(), "mother's last name"),
        () ->
            assertEquals(
                "MAYM21561219",
                profile.getProvincialHealthNumber().getValue(),
                "provincialHealthNumber"),
        () ->
            assertEquals(
                "NAM",
                profile.getProvincialHealthNumber().getSystem(),
                "provincialHealthNumberSystem"),
        () ->
            assertEquals(
                "Qc",
                profile.getProvincialHealthNumber().getAssigner(),
                "provincialHealthNumberAssigner"),
        () ->
            assertEquals(
                SystemIdentifier.IDENTIFIER_TYPE.HEALTH_NUMBER,
                profile.getProvincialHealthNumber().getType(),
                "provincialHealthNumberType"),
        () -> assertEquals("8000000318", profile.getIdentifier().getValue(), "NIUU"),
        () -> assertEquals("NIU-U", profile.getIdentifier().getSystem(), "identifierSystem"),
        () -> assertEquals("Qc", profile.getIdentifier().getAssigner(), "identifierAssigner"),
        () ->
            assertEquals(
                SystemIdentifier.IDENTIFIER_TYPE.PATIENT,
                profile.getIdentifier().getType(),
                "identifierType"),
        () -> assertEquals("F", profile.getGender().name(), "gender"),
        () -> assertEquals(59, profile.getMatchingIndex(), "matching index"));
  }
}
