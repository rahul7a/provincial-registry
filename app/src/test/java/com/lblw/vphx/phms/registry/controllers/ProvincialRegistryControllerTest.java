package com.lblw.vphx.phms.registry.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.lblw.vphx.phms.common.constants.APIConstants;
import com.lblw.vphx.phms.common.constants.HeaderConstants;
import com.lblw.vphx.phms.common.exceptions.ProvincialTimeoutException;
import com.lblw.vphx.phms.common.exceptions.RequestNotAcceptableException;
import com.lblw.vphx.phms.common.utils.ControllerUtils;
import com.lblw.vphx.phms.configurations.ProvincialRequestProperties;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.User;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.common.response.*;
import com.lblw.vphx.phms.domain.patient.request.ProvincialPatientSearchCriteria;
import com.lblw.vphx.phms.domain.patient.response.ProvincialPatientProfile;
import com.lblw.vphx.phms.domain.pharmacy.Pharmacy;
import com.lblw.vphx.phms.domain.pharmacy.ProvincialPharmacyCertificateDetails;
import com.lblw.vphx.phms.registry.processor.processors.ProvincialRegistryProcessor;
import com.lblw.vphx.phms.registry.services.ProvincialRegistryService;
import java.util.List;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

/** Test cases to unit test {@link ProvincialRegistryController} ProvincialRegistryController */
@WebFluxTest(controllers = ProvincialRegistryController.class)
@Import({ControllerUtils.class, ProvincialRequestProperties.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class ProvincialRegistryControllerTest {
  private static final String REQUEST_ID = "request1234";
  private static final String BEARER_TOKEN =
      "Bearer eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2xjbGIyY2Rldi5iMmNsb2dpbi5jb20vYTM0MWU3YTktOTlmNy00OWNmLTgzNDUtNjkxMzljNmY4ZmJlL3YyLjAvIiwiZXhwIjoxNjU1OTIwMjg2LCJuYmYiOjE2NTU5MTY2ODYsImF1ZCI6ImRmZjVjMTVmLWM2MzQtNGFlYi1hMGI0LWM0YTVhMTFlZDQ2ZSIsInN1YiI6IjY3YzRmYjUxLWVkYzMtNGMyMi04ZjM1LTZlYmVjM2YxMzE5MCIsInBoYXJtYWN5SWQiOiI2MTgwMzk0MmY1MDQzZDQ5ZjM1MjFkZjciLCJtYWNoaW5lTmFtZSI6IiIsInJvbGVzIjpbIk5VUlNFIl0sImxhbmd1YWdlQ29kZSI6IkZSRSIsInByb3ZpbmNlQ29kZSI6IlFDIiwidGlkIjoiYTM0MWU3YTktOTlmNy00OWNmLTgzNDUtNjkxMzljNmY4ZmJlIiwiYXpwIjoiZGZmNWMxNWYtYzYzNC00YWViLWEwYjQtYzRhNWExMWVkNDZlIiwidmVyIjoiMS4wIiwiaWF0IjoxNjU1OTE2Njg2fQ.yNPwe_dXjC0hfOzOO3ylIQnu8LxuGL3zq3EJV0P-llA";
  private static final String BEARER_TOKEN_FOR_INVALID_PROVINCE =
      "Bearer eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2xjbGIyY2Rldi5iMmNsb2dpbi5jb20vYTM0MWU3YTktOTlmNy00OWNmLTgzNDUtNjkxMzljNmY4ZmJlL3YyLjAvIiwiZXhwIjoxNjU1OTIwMjg2LCJuYmYiOjE2NTU5MTY2ODYsImF1ZCI6ImRmZjVjMTVmLWM2MzQtNGFlYi1hMGI0LWM0YTVhMTFlZDQ2ZSIsInN1YiI6IjY3YzRmYjUxLWVkYzMtNGMyMi04ZjM1LTZlYmVjM2YxMzE5MCIsInBoYXJtYWN5SWQiOiI2MTgwMzk0MmY1MDQzZDQ5ZjM1MjFkZjciLCJtYWNoaW5lTmFtZSI6IiIsInJvbGVzIjpbIk5VUlNFIl0sImxhbmd1YWdlQ29kZSI6IkZSRSIsInByb3ZpbmNlQ29kZSI6IkFCIiwidGlkIjoiYTM0MWU3YTktOTlmNy00OWNmLTgzNDUtNjkxMzljNmY4ZmJlIiwiYXpwIjoiZGZmNWMxNWYtYzYzNC00YWViLWEwYjQtYzRhNWExMWVkNDZlIiwidmVyIjoiMS4wIiwiaWF0IjoxNjU1OTE2Njg2fQ.yNPwe_dXjC0hfOzOO3ylIQnu8LxuGL3zq3EJV0P-llA";
  @MockBean ProvincialRegistryProcessor provincialRegistryProcessor;
  @SpyBean WebFluxProperties webFluxProperties;
  private ProvincialPatientSearchCriteria provincialPatientSearchCriteria;
  private ProvincialPatientProfile expectedProvincialPatientProfileResp;
  @Autowired private WebTestClient webTestClient;
  @MockBean private ProvincialRegistryService provincialRegistryService;
  @MockBean private ControllerUtils controllerUtils;
  @MockBean private Tracer tracer;

  @Nested
  class PatientSearchTest {
    @BeforeEach
    public void beforeEach() {
      provincialPatientSearchCriteria =
          ProvincialPatientSearchCriteria.builder().provincialHealthNumber(REQUEST_ID).build();
      when(webFluxProperties.getBasePath()).thenReturn("");
    }

    @Test
    void whenAuthorizationHeaderIsNull() {
      webTestClient
          .post()
          .uri(APIConstants.PATIENT_SEARCH_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .equals(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void whenProvincialHealthNumber() throws Exception {
      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);
      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(expectedProvincialRequestControl)
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));

      webTestClient
          .post()
          .uri(APIConstants.PATIENT_SEARCH_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .is2xxSuccessful();
    }

    @Test
    void whenProvincialHealthNumberNull() throws Exception {

      provincialPatientSearchCriteria.setProvincialHealthNumber(null);
      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);

      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(expectedProvincialRequestControl)
                              .build())
                      .operationOutcome(
                          OperationOutcome.builder()
                              .status(
                                  ResponseStatus.builder()
                                      .code(Status.ACCEPT)
                                      .text(Strings.EMPTY)
                                      .build())
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));

      webTestClient
          .post()
          .uri(APIConstants.PATIENT_SEARCH_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .is2xxSuccessful();
    }

    @Test
    void whenProvincialHealthNumberEmptyOrBlank() throws Exception {
      provincialPatientSearchCriteria.setProvincialHealthNumber("");
      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);
      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(expectedProvincialRequestControl)
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));

      webTestClient
          .post()
          .uri(APIConstants.PATIENT_SEARCH_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .is2xxSuccessful();
    }

    @Test
    void whenProvincialPatientSearchCriteriaNull() throws Exception {

      webTestClient
          .post()
          .uri(APIConstants.PATIENT_SEARCH_URI)
          .body(null)
          .exchange()
          .expectStatus()
          .is4xxClientError();
    }

    @Test
    void whenResponseAcknowledgementIsFailure() throws Exception {

      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);
      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(expectedProvincialRequestControl)
                              .build())
                      .operationOutcome(
                          OperationOutcome.builder()
                              .status(
                                  ResponseStatus.builder()
                                      .code(Status.REJECT)
                                      .text(Strings.EMPTY)
                                      .build())
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));

      webTestClient
          .post()
          .uri(APIConstants.PATIENT_SEARCH_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .is2xxSuccessful();
    }

    @Test
    void whenProvincialResponseAcknowledgementIsEmpty() throws Exception {

      String provincialHealthNumber = "1234";
      provincialPatientSearchCriteria.setProvincialHealthNumber(provincialHealthNumber);
      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);

      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(
                                  com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl
                                      .builder()
                                      .requestId(REQUEST_ID)
                                      .province(Province.QC)
                                      .build())
                              .build())
                      .operationOutcome(
                          OperationOutcome.builder()
                              .status(
                                  ResponseStatus.builder()
                                      .code(Status.REJECT)
                                      .text(Strings.EMPTY)
                                      .build())
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));

      webTestClient
          .post()
          .uri(APIConstants.PATIENT_SEARCH_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .header(HeaderConstants.X_PROVINCE_CODE_HEADER, Province.QC.name())
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .is2xxSuccessful();
    }

    @Test
    void should_return_400_status_when_search_criteria_not_provided() {
      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);

      webTestClient
          .post()
          .uri(APIConstants.PATIENT_SEARCH_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .contentType(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus()
          .isBadRequest();
    }

    @Test
    void should_return_400_status_when_xRequestId_header_not_provided() {
      expectedProvincialPatientProfileResp = ProvincialPatientProfile.builder().build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));
      WebTestClient.bindToController(new ProvincialRegistryController(provincialRegistryService))
          .build()
          .post()
          .uri(APIConstants.PATIENT_SEARCH_URI)
          .contentType(MediaType.APPLICATION_JSON)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .headers((httpHeaders) -> httpHeaders.remove(HeaderConstants.X_REQUEST_ID_HEADER))
          .exchange()
          .expectStatus()
          .isBadRequest();
    }

    @Test
    void should_have_xResponseId_header_in_response() {

      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);

      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(expectedProvincialRequestControl)
                              .build())
                      .operationOutcome(
                          OperationOutcome.builder()
                              .status(
                                  ResponseStatus.builder()
                                      .code(Status.ACCEPT)
                                      .text(Strings.EMPTY)
                                      .build())
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(
              Mono.just(
                  ProvincialPatientProfile.builder()
                      .provincialResponseAcknowledgement(
                          ProvincialResponseAcknowledgement.builder()
                              .auditEvent(
                                  AuditEvent.builder()
                                      .provincialRequestControl(expectedProvincialRequestControl)
                                      .build())
                              .operationOutcome(
                                  OperationOutcome.builder()
                                      .status(
                                          ResponseStatus.builder()
                                              .code(Status.ACCEPT)
                                              .text(Strings.EMPTY)
                                              .build())
                                      .build())
                              .build())
                      .build()));

      webTestClient
          .post()
          .uri(APIConstants.PATIENT_SEARCH_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .isOk()
          .expectHeader()
          .valueEquals(HeaderConstants.X_RESPONSE_ID_HEADER, REQUEST_ID)
          .expectStatus()
          .is2xxSuccessful();
    }

    @Test
    void testForProvince_notSupported() {
      ProvincialRequestControl provincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.AB)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();

      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(provincialRequestControl);
      Mockito.doThrow(new RequestNotAcceptableException("Error"))
          .when(controllerUtils)
          .checkSupportedProvince(provincialRequestControl.getProvince());

      webTestClient
          .post()
          .uri(APIConstants.PATIENT_SEARCH_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_FOR_INVALID_PROVINCE)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .isEqualTo(HttpStatus.NOT_ACCEPTABLE);
    }

    @Test
    void whenResponseAcknowledgementWithRunTimeException() throws Exception {

      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(Pharmacy.Identifier.builder().build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      when(provincialRegistryProcessor.searchProvincialProviderInClientRegistry(any()))
          .thenThrow(new ProvincialTimeoutException("Exception"));

      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);
      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(expectedProvincialRequestControl)
                              .build())
                      .operationOutcome(
                          OperationOutcome.builder()
                              .status(
                                  ResponseStatus.builder()
                                      .code(Status.REJECT)
                                      .text(Strings.EMPTY)
                                      .build())
                              .issues(
                                  List.of(
                                      Issue.builder()
                                          .classification(ResponseClassification.SYSTEM)
                                          .code("EM.093")
                                          .priority(
                                              IssuePriority.builder().code(Priority.ERROR).build())
                                          .build()))
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));

      webTestClient
          .post()
          .uri(APIConstants.PATIENT_SEARCH_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .is2xxSuccessful();
    }
  }

  @Nested
  class ProviderSearchTest {
    @BeforeEach
    public void beforeEach() {
      provincialPatientSearchCriteria =
          ProvincialPatientSearchCriteria.builder().provincialHealthNumber(REQUEST_ID).build();
      when(webFluxProperties.getBasePath()).thenReturn("");
    }

    @Test
    void whenAuthorizationHeaderIsNull() {
      webTestClient
          .post()
          .uri(APIConstants.PROVIDER_SEARCH_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .equals(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void whenProvincialHealthNumber() throws Exception {
      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);
      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(expectedProvincialRequestControl)
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));

      webTestClient
          .post()
          .uri(APIConstants.PROVIDER_SEARCH_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .is2xxSuccessful();
    }

    @Test
    void whenProvincialHealthNumberNull() throws Exception {

      provincialPatientSearchCriteria.setProvincialHealthNumber(null);
      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);

      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(expectedProvincialRequestControl)
                              .build())
                      .operationOutcome(
                          OperationOutcome.builder()
                              .status(
                                  ResponseStatus.builder()
                                      .code(Status.ACCEPT)
                                      .text(Strings.EMPTY)
                                      .build())
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));

      webTestClient
          .post()
          .uri(APIConstants.PROVIDER_SEARCH_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .is2xxSuccessful();
    }

    @Test
    void whenProvincialHealthNumberEmptyOrBlank() throws Exception {
      provincialPatientSearchCriteria.setProvincialHealthNumber("");
      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);
      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(expectedProvincialRequestControl)
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));

      webTestClient
          .post()
          .uri(APIConstants.PROVIDER_SEARCH_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .is2xxSuccessful();
    }

    @Test
    void whenProvincialPatientSearchCriteriaNull() throws Exception {

      webTestClient
          .post()
          .uri(APIConstants.PROVIDER_SEARCH_URI)
          .body(null)
          .exchange()
          .expectStatus()
          .is4xxClientError();
    }

    @Test
    void whenResponseAcknowledgementIsFailure() throws Exception {

      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);
      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(expectedProvincialRequestControl)
                              .build())
                      .operationOutcome(
                          OperationOutcome.builder()
                              .status(
                                  ResponseStatus.builder()
                                      .code(Status.REJECT)
                                      .text(Strings.EMPTY)
                                      .build())
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));

      webTestClient
          .post()
          .uri(APIConstants.PROVIDER_SEARCH_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .is2xxSuccessful();
    }

    @Test
    void whenProvincialResponseAcknowledgementIsEmpty() throws Exception {

      String provincialHealthNumber = "1234";
      provincialPatientSearchCriteria.setProvincialHealthNumber(provincialHealthNumber);
      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);

      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(
                                  com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl
                                      .builder()
                                      .requestId(REQUEST_ID)
                                      .province(Province.QC)
                                      .build())
                              .build())
                      .operationOutcome(
                          OperationOutcome.builder()
                              .status(
                                  ResponseStatus.builder()
                                      .code(Status.REJECT)
                                      .text(Strings.EMPTY)
                                      .build())
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));

      webTestClient
          .post()
          .uri(APIConstants.PROVIDER_SEARCH_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .header(HeaderConstants.X_PROVINCE_CODE_HEADER, Province.QC.name())
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .is2xxSuccessful();
    }

    @Test
    void should_return_400_status_when_search_criteria_not_provided() {
      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);

      webTestClient
          .post()
          .uri(APIConstants.PROVIDER_SEARCH_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .contentType(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus()
          .isBadRequest();
    }

    @Test
    void should_return_400_status_when_xRequestId_header_not_provided() {

      expectedProvincialPatientProfileResp = ProvincialPatientProfile.builder().build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));
      WebTestClient.bindToController(new ProvincialRegistryController(provincialRegistryService))
          .build()
          .post()
          .uri(APIConstants.PROVIDER_SEARCH_URI)
          .contentType(MediaType.APPLICATION_JSON)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .headers((httpHeaders) -> httpHeaders.remove(HeaderConstants.X_REQUEST_ID_HEADER))
          .exchange()
          .expectStatus()
          .isBadRequest();
    }

    @Test
    void should_have_xResponseId_header_in_response() {

      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);

      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(expectedProvincialRequestControl)
                              .build())
                      .operationOutcome(
                          OperationOutcome.builder()
                              .status(
                                  ResponseStatus.builder()
                                      .code(Status.ACCEPT)
                                      .text(Strings.EMPTY)
                                      .build())
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(
              Mono.just(
                  ProvincialPatientProfile.builder()
                      .provincialResponseAcknowledgement(
                          ProvincialResponseAcknowledgement.builder()
                              .auditEvent(
                                  AuditEvent.builder()
                                      .provincialRequestControl(expectedProvincialRequestControl)
                                      .build())
                              .operationOutcome(
                                  OperationOutcome.builder()
                                      .status(
                                          ResponseStatus.builder()
                                              .code(Status.ACCEPT)
                                              .text(Strings.EMPTY)
                                              .build())
                                      .build())
                              .build())
                      .build()));

      webTestClient
          .post()
          .uri(APIConstants.PROVIDER_SEARCH_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .isOk()
          .expectHeader()
          .valueEquals(HeaderConstants.X_RESPONSE_ID_HEADER, REQUEST_ID)
          .expectStatus()
          .is2xxSuccessful();
    }

    @Test
    void testForProvince_notSupported() {
      ProvincialRequestControl provincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.AB)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();

      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(provincialRequestControl);
      Mockito.doThrow(new RequestNotAcceptableException("Error"))
          .when(controllerUtils)
          .checkSupportedProvince(provincialRequestControl.getProvince());

      webTestClient
          .post()
          .uri(APIConstants.PROVIDER_SEARCH_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_FOR_INVALID_PROVINCE)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .isEqualTo(HttpStatus.NOT_ACCEPTABLE);
    }

    @Test
    void whenResponseAcknowledgementWithRunTimeException() throws Exception {

      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(Pharmacy.Identifier.builder().build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      when(provincialRegistryProcessor.searchProvincialProviderInClientRegistry(any()))
          .thenThrow(new ProvincialTimeoutException("Exception"));

      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);
      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(expectedProvincialRequestControl)
                              .build())
                      .operationOutcome(
                          OperationOutcome.builder()
                              .status(
                                  ResponseStatus.builder()
                                      .code(Status.REJECT)
                                      .text(Strings.EMPTY)
                                      .build())
                              .issues(
                                  List.of(
                                      Issue.builder()
                                          .classification(ResponseClassification.SYSTEM)
                                          .code("EM.093")
                                          .priority(
                                              IssuePriority.builder().code(Priority.ERROR).build())
                                          .build()))
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));

      webTestClient
          .post()
          .uri(APIConstants.PROVIDER_SEARCH_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .is2xxSuccessful();
    }
  }

  @Nested
  class PatientConsentTest {
    @BeforeEach
    public void beforeEach() {
      provincialPatientSearchCriteria =
          ProvincialPatientSearchCriteria.builder().provincialHealthNumber(REQUEST_ID).build();
      when(webFluxProperties.getBasePath()).thenReturn("");
    }

    @Test
    void whenAuthorizationHeaderIsNull() {
      webTestClient
          .post()
          .uri(APIConstants.PATIENT_CONSENT_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .equals(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void whenProvincialHealthNumber() throws Exception {
      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);
      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(expectedProvincialRequestControl)
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));

      webTestClient
          .post()
          .uri(APIConstants.PATIENT_CONSENT_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .is2xxSuccessful();
    }

    @Test
    void whenProvincialHealthNumberNull() throws Exception {

      provincialPatientSearchCriteria.setProvincialHealthNumber(null);
      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);

      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(expectedProvincialRequestControl)
                              .build())
                      .operationOutcome(
                          OperationOutcome.builder()
                              .status(
                                  ResponseStatus.builder()
                                      .code(Status.ACCEPT)
                                      .text(Strings.EMPTY)
                                      .build())
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));

      webTestClient
          .post()
          .uri(APIConstants.PATIENT_CONSENT_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .is2xxSuccessful();
    }

    @Test
    void whenProvincialHealthNumberEmptyOrBlank() throws Exception {
      provincialPatientSearchCriteria.setProvincialHealthNumber("");
      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);
      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(expectedProvincialRequestControl)
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));

      webTestClient
          .post()
          .uri(APIConstants.PATIENT_CONSENT_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .is2xxSuccessful();
    }

    @Test
    void whenProvincialPatientSearchCriteriaNull() throws Exception {

      webTestClient
          .post()
          .uri(APIConstants.PATIENT_CONSENT_URI)
          .body(null)
          .exchange()
          .expectStatus()
          .is4xxClientError();
    }

    @Test
    void whenResponseAcknowledgementIsFailure() throws Exception {

      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);
      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(expectedProvincialRequestControl)
                              .build())
                      .operationOutcome(
                          OperationOutcome.builder()
                              .status(
                                  ResponseStatus.builder()
                                      .code(Status.REJECT)
                                      .text(Strings.EMPTY)
                                      .build())
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));

      webTestClient
          .post()
          .uri(APIConstants.PATIENT_CONSENT_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .is2xxSuccessful();
    }

    @Test
    void whenProvincialResponseAcknowledgementIsEmpty() throws Exception {

      String provincialHealthNumber = "1234";
      provincialPatientSearchCriteria.setProvincialHealthNumber(provincialHealthNumber);
      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);

      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(
                                  com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl
                                      .builder()
                                      .requestId(REQUEST_ID)
                                      .province(Province.QC)
                                      .build())
                              .build())
                      .operationOutcome(
                          OperationOutcome.builder()
                              .status(
                                  ResponseStatus.builder()
                                      .code(Status.REJECT)
                                      .text(Strings.EMPTY)
                                      .build())
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));

      webTestClient
          .post()
          .uri(APIConstants.PATIENT_CONSENT_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .header(HeaderConstants.X_PROVINCE_CODE_HEADER, Province.QC.name())
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .is2xxSuccessful();
    }

    @Test
    void should_return_400_status_when_search_criteria_not_provided() {
      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);

      webTestClient
          .post()
          .uri(APIConstants.PATIENT_CONSENT_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .contentType(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus()
          .isBadRequest();
    }

    @Test
    void should_return_400_status_when_xRequestId_header_not_provided() {

      expectedProvincialPatientProfileResp = ProvincialPatientProfile.builder().build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));
      WebTestClient.bindToController(new ProvincialRegistryController(provincialRegistryService))
          .build()
          .post()
          .uri(APIConstants.PATIENT_CONSENT_URI)
          .contentType(MediaType.APPLICATION_JSON)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .headers((httpHeaders) -> httpHeaders.remove(HeaderConstants.X_REQUEST_ID_HEADER))
          .exchange()
          .expectStatus()
          .isBadRequest();
    }

    @Test
    void should_have_xResponseId_header_in_response() {

      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);

      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(expectedProvincialRequestControl)
                              .build())
                      .operationOutcome(
                          OperationOutcome.builder()
                              .status(
                                  ResponseStatus.builder()
                                      .code(Status.ACCEPT)
                                      .text(Strings.EMPTY)
                                      .build())
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(
              Mono.just(
                  ProvincialPatientProfile.builder()
                      .provincialResponseAcknowledgement(
                          ProvincialResponseAcknowledgement.builder()
                              .auditEvent(
                                  AuditEvent.builder()
                                      .provincialRequestControl(expectedProvincialRequestControl)
                                      .build())
                              .operationOutcome(
                                  OperationOutcome.builder()
                                      .status(
                                          ResponseStatus.builder()
                                              .code(Status.ACCEPT)
                                              .text(Strings.EMPTY)
                                              .build())
                                      .build())
                              .build())
                      .build()));

      webTestClient
          .post()
          .uri(APIConstants.PATIENT_CONSENT_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .isOk()
          .expectHeader()
          .valueEquals(HeaderConstants.X_RESPONSE_ID_HEADER, REQUEST_ID)
          .expectStatus()
          .is2xxSuccessful();
    }

    @Test
    void testForProvince_notSupported() {
      ProvincialRequestControl provincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.AB)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();

      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(provincialRequestControl);
      Mockito.doThrow(new RequestNotAcceptableException("Error"))
          .when(controllerUtils)
          .checkSupportedProvince(provincialRequestControl.getProvince());

      webTestClient
          .post()
          .uri(APIConstants.PATIENT_CONSENT_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_FOR_INVALID_PROVINCE)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .isEqualTo(HttpStatus.NOT_ACCEPTABLE);
    }

    @Test
    void whenResponseAcknowledgementWithRunTimeException() throws Exception {

      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(Pharmacy.Identifier.builder().build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      when(provincialRegistryProcessor.searchProvincialProviderInClientRegistry(any()))
          .thenThrow(new ProvincialTimeoutException("Exception"));

      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);
      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(expectedProvincialRequestControl)
                              .build())
                      .operationOutcome(
                          OperationOutcome.builder()
                              .status(
                                  ResponseStatus.builder()
                                      .code(Status.REJECT)
                                      .text(Strings.EMPTY)
                                      .build())
                              .issues(
                                  List.of(
                                      Issue.builder()
                                          .classification(ResponseClassification.SYSTEM)
                                          .code("EM.093")
                                          .priority(
                                              IssuePriority.builder().code(Priority.ERROR).build())
                                          .build()))
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));

      webTestClient
          .post()
          .uri(APIConstants.PATIENT_CONSENT_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .is2xxSuccessful();
    }
  }

  @Nested
  class LocationSearchTest {
    @BeforeEach
    public void beforeEach() {
      provincialPatientSearchCriteria =
          ProvincialPatientSearchCriteria.builder().provincialHealthNumber(REQUEST_ID).build();
      when(webFluxProperties.getBasePath()).thenReturn("");
    }

    @Test
    void whenAuthorizationHeaderIsNull() {
      webTestClient
          .post()
          .uri(APIConstants.LOCATION_SEARCH_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .equals(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void whenProvincialHealthNumber() throws Exception {
      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);
      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(expectedProvincialRequestControl)
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));

      webTestClient
          .post()
          .uri(APIConstants.LOCATION_SEARCH_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .is2xxSuccessful();
    }

    @Test
    void whenProvincialHealthNumberNull() throws Exception {

      provincialPatientSearchCriteria.setProvincialHealthNumber(null);
      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);

      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(expectedProvincialRequestControl)
                              .build())
                      .operationOutcome(
                          OperationOutcome.builder()
                              .status(
                                  ResponseStatus.builder()
                                      .code(Status.ACCEPT)
                                      .text(Strings.EMPTY)
                                      .build())
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));

      webTestClient
          .post()
          .uri(APIConstants.LOCATION_SEARCH_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .is2xxSuccessful();
    }

    @Test
    void whenProvincialHealthNumberEmptyOrBlank() throws Exception {
      provincialPatientSearchCriteria.setProvincialHealthNumber("");
      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);
      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(expectedProvincialRequestControl)
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));

      webTestClient
          .post()
          .uri(APIConstants.LOCATION_SEARCH_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .is2xxSuccessful();
    }

    @Test
    void whenProvincialPatientSearchCriteriaNull() throws Exception {

      webTestClient
          .post()
          .uri(APIConstants.LOCATION_SEARCH_URI)
          .body(null)
          .exchange()
          .expectStatus()
          .is4xxClientError();
    }

    @Test
    void whenResponseAcknowledgementIsFailure() throws Exception {

      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);
      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(expectedProvincialRequestControl)
                              .build())
                      .operationOutcome(
                          OperationOutcome.builder()
                              .status(
                                  ResponseStatus.builder()
                                      .code(Status.REJECT)
                                      .text(Strings.EMPTY)
                                      .build())
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));

      webTestClient
          .post()
          .uri(APIConstants.LOCATION_SEARCH_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .is2xxSuccessful();
    }

    @Test
    void whenProvincialResponseAcknowledgementIsEmpty() throws Exception {

      String provincialHealthNumber = "1234";
      provincialPatientSearchCriteria.setProvincialHealthNumber(provincialHealthNumber);
      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);

      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(
                                  com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl
                                      .builder()
                                      .requestId(REQUEST_ID)
                                      .province(Province.QC)
                                      .build())
                              .build())
                      .operationOutcome(
                          OperationOutcome.builder()
                              .status(
                                  ResponseStatus.builder()
                                      .code(Status.REJECT)
                                      .text(Strings.EMPTY)
                                      .build())
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));

      webTestClient
          .post()
          .uri(APIConstants.LOCATION_SEARCH_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .header(HeaderConstants.X_PROVINCE_CODE_HEADER, Province.QC.name())
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .is2xxSuccessful();
    }

    @Test
    void should_return_400_status_when_search_criteria_not_provided() {
      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);

      webTestClient
          .post()
          .uri(APIConstants.LOCATION_SEARCH_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .contentType(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus()
          .isBadRequest();
    }

    @Test
    void should_return_400_status_when_xRequestId_header_not_provided() {

      expectedProvincialPatientProfileResp = ProvincialPatientProfile.builder().build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));
      WebTestClient.bindToController(new ProvincialRegistryController(provincialRegistryService))
          .build()
          .post()
          .uri(APIConstants.LOCATION_SEARCH_URI)
          .contentType(MediaType.APPLICATION_JSON)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .headers((httpHeaders) -> httpHeaders.remove(HeaderConstants.X_REQUEST_ID_HEADER))
          .exchange()
          .expectStatus()
          .isBadRequest();
    }

    @Test
    void should_have_xResponseId_header_in_response() {

      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);

      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(expectedProvincialRequestControl)
                              .build())
                      .operationOutcome(
                          OperationOutcome.builder()
                              .status(
                                  ResponseStatus.builder()
                                      .code(Status.ACCEPT)
                                      .text(Strings.EMPTY)
                                      .build())
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(
              Mono.just(
                  ProvincialPatientProfile.builder()
                      .provincialResponseAcknowledgement(
                          ProvincialResponseAcknowledgement.builder()
                              .auditEvent(
                                  AuditEvent.builder()
                                      .provincialRequestControl(expectedProvincialRequestControl)
                                      .build())
                              .operationOutcome(
                                  OperationOutcome.builder()
                                      .status(
                                          ResponseStatus.builder()
                                              .code(Status.ACCEPT)
                                              .text(Strings.EMPTY)
                                              .build())
                                      .build())
                              .build())
                      .build()));

      webTestClient
          .post()
          .uri(APIConstants.LOCATION_SEARCH_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .isOk()
          .expectHeader()
          .valueEquals(HeaderConstants.X_RESPONSE_ID_HEADER, REQUEST_ID)
          .expectStatus()
          .is2xxSuccessful();
    }

    @Test
    void testForProvince_notSupported() {
      ProvincialRequestControl provincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.AB)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();

      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(provincialRequestControl);
      Mockito.doThrow(new RequestNotAcceptableException("Error"))
          .when(controllerUtils)
          .checkSupportedProvince(provincialRequestControl.getProvince());

      webTestClient
          .post()
          .uri(APIConstants.LOCATION_SEARCH_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_FOR_INVALID_PROVINCE)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .isEqualTo(HttpStatus.NOT_ACCEPTABLE);
    }

    @Test
    void whenResponseAcknowledgementWithRunTimeException() throws Exception {

      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(Pharmacy.Identifier.builder().build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      when(provincialRegistryProcessor.searchProvincialProviderInClientRegistry(any()))
          .thenThrow(new ProvincialTimeoutException("Exception"));

      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);
      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(expectedProvincialRequestControl)
                              .build())
                      .operationOutcome(
                          OperationOutcome.builder()
                              .status(
                                  ResponseStatus.builder()
                                      .code(Status.REJECT)
                                      .text(Strings.EMPTY)
                                      .build())
                              .issues(
                                  List.of(
                                      Issue.builder()
                                          .classification(ResponseClassification.SYSTEM)
                                          .code("EM.093")
                                          .priority(
                                              IssuePriority.builder().code(Priority.ERROR).build())
                                          .build()))
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));

      webTestClient
          .post()
          .uri(APIConstants.LOCATION_SEARCH_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .is2xxSuccessful();
    }
  }

  @Nested
  class LocationDetailsTest {
    @BeforeEach
    public void beforeEach() {
      provincialPatientSearchCriteria =
          ProvincialPatientSearchCriteria.builder().provincialHealthNumber(REQUEST_ID).build();
      when(webFluxProperties.getBasePath()).thenReturn("");
    }

    @Test
    void whenAuthorizationHeaderIsNull() {
      webTestClient
          .post()
          .uri(APIConstants.LOCATION_DETAILS_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .equals(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void whenProvincialHealthNumber() throws Exception {
      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);
      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(expectedProvincialRequestControl)
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));

      webTestClient
          .post()
          .uri(APIConstants.LOCATION_DETAILS_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .is2xxSuccessful();
    }

    @Test
    void whenProvincialHealthNumberNull() throws Exception {

      provincialPatientSearchCriteria.setProvincialHealthNumber(null);
      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);

      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(expectedProvincialRequestControl)
                              .build())
                      .operationOutcome(
                          OperationOutcome.builder()
                              .status(
                                  ResponseStatus.builder()
                                      .code(Status.ACCEPT)
                                      .text(Strings.EMPTY)
                                      .build())
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));

      webTestClient
          .post()
          .uri(APIConstants.LOCATION_DETAILS_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .is2xxSuccessful();
    }

    @Test
    void whenProvincialHealthNumberEmptyOrBlank() throws Exception {
      provincialPatientSearchCriteria.setProvincialHealthNumber("");
      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);
      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(expectedProvincialRequestControl)
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));

      webTestClient
          .post()
          .uri(APIConstants.LOCATION_DETAILS_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .is2xxSuccessful();
    }

    @Test
    void whenProvincialPatientSearchCriteriaNull() throws Exception {

      webTestClient
          .post()
          .uri(APIConstants.LOCATION_DETAILS_URI)
          .body(null)
          .exchange()
          .expectStatus()
          .is4xxClientError();
    }

    @Test
    void whenResponseAcknowledgementIsFailure() throws Exception {

      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);
      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(expectedProvincialRequestControl)
                              .build())
                      .operationOutcome(
                          OperationOutcome.builder()
                              .status(
                                  ResponseStatus.builder()
                                      .code(Status.REJECT)
                                      .text(Strings.EMPTY)
                                      .build())
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));

      webTestClient
          .post()
          .uri(APIConstants.LOCATION_DETAILS_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .is2xxSuccessful();
    }

    @Test
    void whenProvincialResponseAcknowledgementIsEmpty() throws Exception {

      String provincialHealthNumber = "1234";
      provincialPatientSearchCriteria.setProvincialHealthNumber(provincialHealthNumber);
      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);

      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(
                                  com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl
                                      .builder()
                                      .requestId(REQUEST_ID)
                                      .province(Province.QC)
                                      .build())
                              .build())
                      .operationOutcome(
                          OperationOutcome.builder()
                              .status(
                                  ResponseStatus.builder()
                                      .code(Status.REJECT)
                                      .text(Strings.EMPTY)
                                      .build())
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));

      webTestClient
          .post()
          .uri(APIConstants.LOCATION_DETAILS_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .header(HeaderConstants.X_PROVINCE_CODE_HEADER, Province.QC.name())
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .is2xxSuccessful();
    }

    @Test
    void should_return_400_status_when_search_criteria_not_provided() {
      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);

      webTestClient
          .post()
          .uri(APIConstants.LOCATION_DETAILS_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .contentType(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus()
          .isBadRequest();
    }

    @Test
    void should_return_400_status_when_xRequestId_header_not_provided() {

      expectedProvincialPatientProfileResp = ProvincialPatientProfile.builder().build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));
      WebTestClient.bindToController(new ProvincialRegistryController(provincialRegistryService))
          .build()
          .post()
          .uri(APIConstants.LOCATION_DETAILS_URI)
          .contentType(MediaType.APPLICATION_JSON)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .headers((httpHeaders) -> httpHeaders.remove(HeaderConstants.X_REQUEST_ID_HEADER))
          .exchange()
          .expectStatus()
          .isBadRequest();
    }

    @Test
    void should_have_xResponseId_header_in_response() {

      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);

      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(expectedProvincialRequestControl)
                              .build())
                      .operationOutcome(
                          OperationOutcome.builder()
                              .status(
                                  ResponseStatus.builder()
                                      .code(Status.ACCEPT)
                                      .text(Strings.EMPTY)
                                      .build())
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(
              Mono.just(
                  ProvincialPatientProfile.builder()
                      .provincialResponseAcknowledgement(
                          ProvincialResponseAcknowledgement.builder()
                              .auditEvent(
                                  AuditEvent.builder()
                                      .provincialRequestControl(expectedProvincialRequestControl)
                                      .build())
                              .operationOutcome(
                                  OperationOutcome.builder()
                                      .status(
                                          ResponseStatus.builder()
                                              .code(Status.ACCEPT)
                                              .text(Strings.EMPTY)
                                              .build())
                                      .build())
                              .build())
                      .build()));

      webTestClient
          .post()
          .uri(APIConstants.LOCATION_DETAILS_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .isOk()
          .expectHeader()
          .valueEquals(HeaderConstants.X_RESPONSE_ID_HEADER, REQUEST_ID)
          .expectStatus()
          .is2xxSuccessful();
    }

    @Test
    void testForProvince_notSupported() {
      ProvincialRequestControl provincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.AB)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();

      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(provincialRequestControl);
      Mockito.doThrow(new RequestNotAcceptableException("Error"))
          .when(controllerUtils)
          .checkSupportedProvince(provincialRequestControl.getProvince());

      webTestClient
          .post()
          .uri(APIConstants.LOCATION_DETAILS_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_FOR_INVALID_PROVINCE)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .isEqualTo(HttpStatus.NOT_ACCEPTABLE);
    }

    @Test
    void whenResponseAcknowledgementWithRunTimeException() throws Exception {

      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(Pharmacy.Identifier.builder().build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      when(provincialRegistryProcessor.searchProvincialProviderInClientRegistry(any()))
          .thenThrow(new ProvincialTimeoutException("Exception"));

      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);
      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(expectedProvincialRequestControl)
                              .build())
                      .operationOutcome(
                          OperationOutcome.builder()
                              .status(
                                  ResponseStatus.builder()
                                      .code(Status.REJECT)
                                      .text(Strings.EMPTY)
                                      .build())
                              .issues(
                                  List.of(
                                      Issue.builder()
                                          .classification(ResponseClassification.SYSTEM)
                                          .code("EM.093")
                                          .priority(
                                              IssuePriority.builder().code(Priority.ERROR).build())
                                          .build()))
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(Mono.just(expectedProvincialPatientProfileResp));

      webTestClient
          .post()
          .uri(APIConstants.LOCATION_DETAILS_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .is2xxSuccessful();
    }

    @Test
    void WhenHttpHeadersRefererNotNull_ThenSetSourceTarget() {

      ProvincialRequestControl expectedProvincialRequestControl =
          ProvincialRequestControl.builder()
              .requestId(REQUEST_ID)
              .province(Province.QC)
              .pharmacy(
                  Pharmacy.builder()
                      .id("pharmacyId")
                      .certificate(
                          ProvincialPharmacyCertificateDetails.builder()
                              .certificateReferenceId("certId")
                              .build())
                      .name("name")
                      .provincialLocation(
                          Pharmacy.ProvincialLocation.builder()
                              .identifier(
                                  Pharmacy.Identifier.builder().value("locationIdentifier").build())
                              .build())
                      .build())
              .user(User.builder().idpUserId("ID").build())
              .build();
      Mockito.when(controllerUtils.buildProvincialRequestControl(any(ServerHttpRequest.class)))
          .thenReturn(expectedProvincialRequestControl);

      expectedProvincialPatientProfileResp =
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(expectedProvincialRequestControl)
                              .build())
                      .operationOutcome(
                          OperationOutcome.builder()
                              .status(
                                  ResponseStatus.builder()
                                      .code(Status.ACCEPT)
                                      .text(Strings.EMPTY)
                                      .build())
                              .build())
                      .build())
              .build();

      Mockito.when(
              provincialRegistryService.searchProvincialPatient(
                  any(ProvincialPatientSearchCriteria.class)))
          .thenReturn(
              Mono.just(
                  ProvincialPatientProfile.builder()
                      .provincialResponseAcknowledgement(
                          ProvincialResponseAcknowledgement.builder()
                              .auditEvent(
                                  AuditEvent.builder()
                                      .provincialRequestControl(expectedProvincialRequestControl)
                                      .build())
                              .operationOutcome(
                                  OperationOutcome.builder()
                                      .status(
                                          ResponseStatus.builder()
                                              .code(Status.ACCEPT)
                                              .text(Strings.EMPTY)
                                              .build())
                                      .build())
                              .build())
                      .build()));

      webTestClient
          .post()
          .uri(APIConstants.PROVIDER_SEARCH_URI)
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .header(HttpHeaders.REFERER, "https://www.xyz.com")
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .isOk()
          .expectHeader()
          .valueEquals(HeaderConstants.X_RESPONSE_ID_HEADER, REQUEST_ID)
          .expectStatus()
          .is2xxSuccessful();
    }

    @Test
    void WhenUriIsIncorrect_ThenHttpStatus404() {

      webTestClient
          .post()
          .uri("/provincial-registry/provider/searches")
          .header(HeaderConstants.X_REQUEST_ID_HEADER, REQUEST_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .body(BodyInserters.fromValue(provincialPatientSearchCriteria))
          .exchange()
          .expectStatus()
          .isEqualTo(HttpStatus.NOT_FOUND);
    }
  }

  @Nested
  class RedirectToRootContextTest {

    @Test
    void WhenAccessingRootUrl_ThenRedirectingToSwaggerUrl() {

      webTestClient
          .get()
          .uri("/")
          .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
          .exchange()
          .expectStatus()
          .isEqualTo(HttpStatus.MOVED_PERMANENTLY)
          .expectHeader()
          .valueEquals("Location", "/swagger-ui.html");
    }
  }
}
