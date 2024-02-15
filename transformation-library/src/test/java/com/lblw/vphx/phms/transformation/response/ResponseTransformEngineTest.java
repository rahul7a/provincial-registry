package com.lblw.vphx.phms.transformation.response;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.lblw.vphx.phms.common.coding.services.CodeableConceptService;
import com.lblw.vphx.phms.domain.coding.Coding;
import com.lblw.vphx.phms.domain.coding.LanguageCode;
import com.lblw.vphx.phms.domain.coding.LocalizedText;
import com.lblw.vphx.phms.domain.common.MessageProcess;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.response.hl7v3.Response;
import com.lblw.vphx.phms.domain.location.details.response.hl7v3.ProvincialLocationDetailsResponse;
import com.lblw.vphx.phms.domain.location.response.hl7v3.ProvincialLocationSearchResponse;
import com.lblw.vphx.phms.domain.patient.consent.response.hl7v3.ProvincialPatientConsentResponse;
import com.lblw.vphx.phms.domain.patient.response.hl7v3.ProvincialPatientSearchResponse;
import com.lblw.vphx.phms.domain.provider.response.hl7v3.ProvincialProviderSearchResponse;
import com.lblw.vphx.phms.transformation.exceptions.TransformationException;
import com.lblw.vphx.phms.transformation.response.helpers.CommonResponseTransformerHelper;
import com.lblw.vphx.phms.transformation.response.helpers.CommonResponseXPathTransformerHelper;
import com.lblw.vphx.phms.transformation.response.transformers.DefaultResponseTransformer;
import com.lblw.vphx.phms.transformation.response.transformers.LocationDetailsResponseTransformer;
import com.lblw.vphx.phms.transformation.response.transformers.locationsummary.LocationSummaryResponseXPathTransformer;
import com.lblw.vphx.phms.transformation.response.transformers.patientconsent.PatientConsentResponseXPathTransformer;
import com.lblw.vphx.phms.transformation.response.transformers.patientsearch.PatientSearchResponseXPathTransformer;
import com.lblw.vphx.phms.transformation.response.transformers.providersearch.ProviderSearchResponseXPathTransformer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {
      CommonResponseTransformerHelper.class,
      CodeableConceptService.class,
      PatientSearchResponseXPathTransformer.class,
      ProviderSearchResponseXPathTransformer.class,
      PatientConsentResponseXPathTransformer.class,
      LocationSummaryResponseXPathTransformer.class,
      LocationDetailsResponseTransformer.class,
      DefaultResponseTransformer.class,
      ResponseTransformerEngine.class,
      CommonResponseXPathTransformerHelper.class
    })
class ResponseTransformEngineTest {
  private String response;
  private String providerSearchResponse;
  private String patientConsentResponse;
  private String locationSummaryResponse;
  private String locationDetailsResponse;

  @MockBean private CommonResponseTransformerHelper commonResponseTransformerHelper;
  @MockBean private CodeableConceptService codeableConceptService;
  @Autowired private ResponseTransformerEngine responseTransformerEngine;

  @BeforeEach
  public void beforeEach() throws IOException {
    Path path =
        Paths.get(
            "src/test/resources",
            "patientprofile/dsq.response/find_candidate_patient_found_response.xml");
    response = Files.readString(path);
    providerSearchResponse =
        Files.readString(
            Paths.get("src/test/resources", "providerprofile/dsq.response/success.xml"));
    patientConsentResponse =
        Files.readString(
            Paths.get("src/test/resources", "patientconsent/dsq.response/success.xml"));
    locationSummaryResponse =
        Files.readString(
            Paths.get("src/test/resources", "locationsummary/dsq.response/success.xml"));
    locationDetailsResponse =
        Files.readString(
            Paths.get("src/test/resources", "locationdetails/dsq.response/success.xml"));
  }

  @Test
  void whenPatientSearchResponseIsNull_ThenTransformationException() {
    response = null;
    StepVerifier.create(
            responseTransformerEngine
                .transform(response)
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
  void whenPatientSearchResponseIsBlank_ThenTransformationException() {
    response = "";
    StepVerifier.create(
            responseTransformerEngine
                .transform(response)
                .contextWrite(
                    Context.of(MessageProcess.class, MessageProcess.PATIENT_SEARCH).readOnly()))
        .expectErrorMatches(
            e ->
                e instanceof TransformationException
                    && e.getCause().getMessage().equals("Response body is required"))
        .verify();
  }

  @Test
  void whenContextIsEmpty_ThenTransformationException() {
    StepVerifier.create(responseTransformerEngine.transform(response))
        .expectErrorMatches(
            e ->
                e instanceof TransformationException
                    && e.getCause()
                        .getMessage()
                        .equals("Null response or Empty Context is provided."))
        .verify();
  }

  @Test
  void whenResponseIsNullAndContextIsEmpty_ThenTransformationException() {
    response = null;
    StepVerifier.create(responseTransformerEngine.transform(response))
        .expectErrorMatches(
            e ->
                e instanceof TransformationException
                    && e.getCause()
                        .getMessage()
                        .equals("Null response or Empty Context is provided."))
        .verify();
  }

  @Test
  void whenTransactionTypeisFindCandidateQuery_thenPatientSearchResponseReceived() {
    StepVerifier.create(
            responseTransformerEngine
                .transform(response)
                .contextWrite(
                    Context.of(MessageProcess.class, MessageProcess.PATIENT_SEARCH).readOnly()))
        .assertNext(
            actualResponse -> {
              Response hl7Response = (Response) actualResponse;
              assertTrue(hl7Response instanceof ProvincialPatientSearchResponse);
            })
        .verifyComplete();
  }

  @Test
  void whenProviderSearchResponseIsNull_ThenTransformationException() {
    providerSearchResponse = null;
    StepVerifier.create(
            responseTransformerEngine
                .transform(providerSearchResponse)
                .contextWrite(
                    Context.of(MessageProcess.class, MessageProcess.PROVIDER_SEARCH).readOnly()))
        .expectErrorMatches(
            e ->
                e instanceof TransformationException
                    && e.getCause()
                        .getMessage()
                        .equals("Null response or Empty Context is provided."))
        .verify();
  }

  @Test
  void whenProviderSearchResponseIsBlank_ThenTransformationException() {
    providerSearchResponse = "";
    StepVerifier.create(
            responseTransformerEngine
                .transform(providerSearchResponse)
                .contextWrite(
                    Context.of(MessageProcess.class, MessageProcess.PROVIDER_SEARCH).readOnly()))
        .expectErrorMatches(
            e ->
                e instanceof TransformationException
                    && e.getCause().getMessage().equals("Response body is required"))
        .verify();
  }

  @Test
  void whenTransactionTypeisFindProviderQuery_thenProviderSearchResponseReceived() {

    when(codeableConceptService.findProvincialRoleCodingBySystemRoleCode(Province.QC, "2060000"))
        .thenReturn(
            Optional.of(
                Coding.builder()
                    .province(Province.QC)
                    .code("DEN")
                    .display(
                        new LocalizedText[] {
                          LocalizedText.builder()
                              .language(LanguageCode.ENG)
                              .text("Dentist")
                              .build(),
                          LocalizedText.builder()
                              .language(LanguageCode.FRA)
                              .text("Dentiste")
                              .build()
                        })
                    .build()));

    when(codeableConceptService.findProvincialRoleCodingBySystemRoleCode(Province.QC, "2430000"))
        .thenReturn(
            Optional.of(
                Coding.builder()
                    .province(Province.QC)
                    .code("PHARM")
                    .display(
                        new LocalizedText[] {
                          LocalizedText.builder()
                              .language(LanguageCode.ENG)
                              .text("Pharmacist")
                              .build(),
                          LocalizedText.builder()
                              .language(LanguageCode.FRA)
                              .text("Pharmacien")
                              .build()
                        })
                    .build()));

    when(codeableConceptService.findSystemSpecialityRoleCodingByProvincialSpecialityRoleCode(
            Province.QC, "227"))
        .thenReturn(
            Optional.of(
                Coding.builder()
                    .province(Province.QC)
                    .code("1890000")
                    .display(
                        new LocalizedText[] {
                          LocalizedText.builder()
                              .language(LanguageCode.ENG)
                              .text("MEDICAL MICROBIOLOGY AND INFECTIOUS DISEASES")
                              .build(),
                          LocalizedText.builder()
                              .language(LanguageCode.FRA)
                              .text("MICROBIOLOGIE MÉDICALE ET INFECTIOLOGIE")
                              .build()
                        })
                    .build()));

    StepVerifier.create(
            responseTransformerEngine
                .transform(providerSearchResponse)
                .contextWrite(
                    Context.of(MessageProcess.class, MessageProcess.PROVIDER_SEARCH).readOnly()))
        .assertNext(
            actualResponse -> {
              Response hl7Response = (Response) actualResponse;
              assertTrue(hl7Response instanceof ProvincialProviderSearchResponse);
            })
        .verifyComplete();
  }

  @Test
  void whenPatientConsentResponseIsNull_ThenTransformationException() {
    patientConsentResponse = null;
    StepVerifier.create(
            responseTransformerEngine
                .transform(patientConsentResponse)
                .contextWrite(
                    Context.of(MessageProcess.class, MessageProcess.PATIENT_CONSENT).readOnly()))
        .expectErrorMatches(
            e ->
                e instanceof TransformationException
                    && e.getCause()
                        .getMessage()
                        .equals("Null response or Empty Context is provided."))
        .verify();
  }

  @Test
  void whenPatientConsentResponseIsBlank_ThenTransformationException() {
    patientConsentResponse = "";
    StepVerifier.create(
            responseTransformerEngine
                .transform(patientConsentResponse)
                .contextWrite(
                    Context.of(MessageProcess.class, MessageProcess.PATIENT_CONSENT).readOnly()))
        .expectErrorMatches(
            e ->
                e instanceof TransformationException
                    && e.getCause().getMessage().equals("Response body is required"))
        .verify();
  }

  @Test
  void whenTransactionTypeIsPatientConsentQuery_thenPatientConsentResponseReceived() {
    StepVerifier.create(
            responseTransformerEngine
                .transform(patientConsentResponse)
                .contextWrite(
                    Context.of(MessageProcess.class, MessageProcess.PATIENT_CONSENT).readOnly()))
        .assertNext(
            actualResponse -> {
              Response hl7Response = (Response) actualResponse;
              assertTrue(hl7Response instanceof ProvincialPatientConsentResponse);
            })
        .verifyComplete();
  }

  @Test
  void whenLocationSummaryResponseIsNull_ThenTransformationException() {
    locationSummaryResponse = null;
    StepVerifier.create(
            responseTransformerEngine
                .transform(locationSummaryResponse)
                .contextWrite(
                    Context.of(MessageProcess.class, MessageProcess.LOCATION_SEARCH).readOnly()))
        .expectErrorMatches(
            e ->
                e instanceof TransformationException
                    && e.getCause()
                        .getMessage()
                        .equals("Null response or Empty Context is provided."))
        .verify();
  }

  @Test
  void whenTransactionTypeIsLocationSummaryQuery_thenLocationSummaryResponseReceived() {
    StepVerifier.create(
            responseTransformerEngine
                .transform(locationSummaryResponse)
                .contextWrite(
                    Context.of(MessageProcess.class, MessageProcess.LOCATION_SEARCH).readOnly()))
        .assertNext(
            actualResponse -> {
              Response hl7Response = (Response) actualResponse;
              assertTrue(hl7Response instanceof ProvincialLocationSearchResponse);
            })
        .verifyComplete();
  }

  @Test
  void whenLocationDetailsResponseIsNull_ThenTransformationException() {
    locationDetailsResponse = null;
    StepVerifier.create(
            responseTransformerEngine
                .transform(locationDetailsResponse)
                .contextWrite(
                    Context.of(MessageProcess.class, MessageProcess.LOCATION_DETAILS).readOnly()))
        .expectErrorMatches(
            e ->
                e instanceof TransformationException
                    && e.getCause()
                        .getMessage()
                        .equals("Null response or Empty Context is provided."))
        .verify();
  }

  @Test
  void whenLocationDetailsResponseIsBlank_ThenTransformationException() {
    locationDetailsResponse = "";
    StepVerifier.create(
            responseTransformerEngine
                .transform(locationDetailsResponse)
                .contextWrite(
                    Context.of(MessageProcess.class, MessageProcess.LOCATION_DETAILS).readOnly()))
        .expectErrorMatches(
            e ->
                e instanceof TransformationException
                    && e.getCause()
                        .getMessage()
                        .equals("Response body is required to transform location details response"))
        .verify();
  }

  @Test
  void whenTransactionTypeIsLocationDetailsQuery_thenLocationDetailsResponseReceived() {
    StepVerifier.create(
            responseTransformerEngine
                .transform(locationDetailsResponse)
                .contextWrite(
                    Context.of(MessageProcess.class, MessageProcess.LOCATION_DETAILS).readOnly()))
        .assertNext(
            actualResponse -> {
              Response hl7Response = (Response) actualResponse;
              assertTrue(hl7Response instanceof ProvincialLocationDetailsResponse);
            })
        .verifyComplete();
  }
}
