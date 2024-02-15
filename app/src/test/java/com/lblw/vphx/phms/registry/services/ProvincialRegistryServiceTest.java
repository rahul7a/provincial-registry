package com.lblw.vphx.phms.registry.services;

import static org.assertj.core.api.Assertions.assertThat;

import com.lblw.vphx.phms.domain.common.Gender;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.common.response.*;
import com.lblw.vphx.phms.domain.location.details.request.ProvincialLocationDetailsCriteria;
import com.lblw.vphx.phms.domain.location.details.response.ProvincialLocationDetails;
import com.lblw.vphx.phms.domain.location.details.response.hl7v3.ProvincialLocationDetailsResponse;
import com.lblw.vphx.phms.domain.location.request.ProvincialLocationSearchCriteria;
import com.lblw.vphx.phms.domain.location.response.ProvincialLocationSummaries;
import com.lblw.vphx.phms.domain.location.response.hl7v3.ProvincialLocationSearchResponse;
import com.lblw.vphx.phms.domain.patient.consent.request.ProvincialPatientConsentCriteria;
import com.lblw.vphx.phms.domain.patient.consent.response.ProvincialPatientConsent;
import com.lblw.vphx.phms.domain.patient.consent.response.hl7v3.ProvincialPatientConsentResponse;
import com.lblw.vphx.phms.domain.patient.request.ProvincialPatientSearchCriteria;
import com.lblw.vphx.phms.domain.patient.response.ProvincialPatientProfile;
import com.lblw.vphx.phms.domain.patient.response.hl7v3.ProvincialPatientSearchResponse;
import com.lblw.vphx.phms.domain.pharmacy.Pharmacy;
import com.lblw.vphx.phms.domain.provider.request.ProvincialProviderSearchCriteria;
import com.lblw.vphx.phms.domain.provider.response.ProvincialProviderProfiles;
import com.lblw.vphx.phms.domain.provider.response.hl7v3.ProvincialProviderSearchResponse;
import com.lblw.vphx.phms.registry.processor.processors.ProvincialRegistryProcessor;
import java.time.LocalDate;
import java.util.UUID;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/** Test cases to unit test {@link ProvincialRegistryService} ProvincialRegistryService */
@ExtendWith(SpringExtension.class)
@Import({ProvincialRegistryService.class, ProvincialRegistryProcessor.class})
class ProvincialRegistryServiceTest {

  @MockBean ProvincialRegistryProcessor provincialRegistryProcessor;
  @Autowired ProvincialRegistryService provincialRegistryService;

  @Test
  void searchProvincialPatient() {
    ProvincialRequestControl provincialRequestControl = getProvincialRequestControl();

    ProvincialPatientSearchCriteria provincialPatientSearchCriteria =
        ProvincialPatientSearchCriteria.builder()
            .firstName("FirstName")
            .lastName("LastName")
            .gender(Gender.M)
            .dateOfBirth(LocalDate.of(2010, 11, 01))
            .provincialRequestControl(provincialRequestControl)
            .build();

    String bearerToken = "sampleToken";

    ProvincialPatientProfile expectedProvincialPatientProfile =
        ProvincialPatientProfile.builder()
            .provincialResponseAcknowledgement(
                ProvincialResponseAcknowledgement.builder()
                    .operationOutcome(
                        OperationOutcome.builder()
                            .status(
                                ResponseStatus.builder()
                                    .code(Status.ACCEPT)
                                    .text(Strings.EMPTY)
                                    .build())
                            .build())
                    .auditEvent(
                        AuditEvent.builder()
                            .provincialRequestControl(
                                ProvincialRequestControl.builder()
                                    .requestId(UUID.randomUUID().toString())
                                    .build())
                            .build())
                    .build())
            .build();

    Mono<ProvincialPatientSearchResponse> expectedProvincialPatientSearchResponse =
        Mono.just(
            ProvincialPatientSearchResponse.builder()
                .provincialResponsePayload(expectedProvincialPatientProfile)
                .build());

    Mockito.when(
            provincialRegistryProcessor.searchProvincialPatientInClientRegistry(
                provincialPatientSearchCriteria))
        .thenReturn(expectedProvincialPatientSearchResponse);

    Mono<ProvincialPatientProfile> ProvincialPatientProfile =
        provincialRegistryService.searchProvincialPatient(provincialPatientSearchCriteria);

    StepVerifier.create(
            ProvincialPatientProfile.contextWrite(
                ctx ->
                    ctx.put(ProvincialRequestControl.class, provincialRequestControl)
                        .put(HttpHeaders.AUTHORIZATION, "mock")))
        .assertNext(
            actualResponse ->
                assertThat(actualResponse).isEqualTo(expectedProvincialPatientProfile))
        .verifyComplete();
  }

  @Test
  void searchProvincialProvider() {

    ProvincialRequestControl provincialRequestControl = getProvincialRequestControl();

    ProvincialProviderSearchCriteria provincialProviderSearchCriteria =
        ProvincialProviderSearchCriteria.builder()
            .firstName("Karim")
            .lastName("Jotun")
            .gender(Gender.M)
            .dateOfBirth("20211212")
            .build();

    String bearerToken = "sampleToken";

    ProvincialProviderProfiles expectedProvincialProviderProfiles =
        ProvincialProviderProfiles.builder()
            .provincialResponseAcknowledgement(
                ProvincialResponseAcknowledgement.builder()
                    .operationOutcome(
                        OperationOutcome.builder()
                            .status(
                                ResponseStatus.builder()
                                    .code(Status.ACCEPT)
                                    .text(Strings.EMPTY)
                                    .build())
                            .build())
                    .auditEvent(
                        AuditEvent.builder()
                            .provincialRequestControl(
                                ProvincialRequestControl.builder()
                                    .requestId(UUID.randomUUID().toString())
                                    .build())
                            .build())
                    .build())
            .build();

    Mono<ProvincialProviderSearchResponse> expectedProvincialProviderSearchResponse =
        Mono.just(
            ProvincialProviderSearchResponse.builder()
                .provincialResponsePayload(expectedProvincialProviderProfiles)
                .build());

    Mockito.when(
            provincialRegistryProcessor.searchProvincialProviderInClientRegistry(
                provincialProviderSearchCriteria))
        .thenReturn(expectedProvincialProviderSearchResponse);

    Mono<ProvincialProviderProfiles> ProvincialPatientProfile =
        provincialRegistryService.searchProvincialProvider(provincialProviderSearchCriteria);

    StepVerifier.create(
            ProvincialPatientProfile.contextWrite(
                ctx -> ctx.put(ProvincialRequestControl.class, provincialRequestControl)))
        .assertNext(
            actualResponse ->
                assertThat(actualResponse).isEqualTo(expectedProvincialProviderProfiles))
        .verifyComplete();
  }

  @Test
  void getProvincialPatientConsent() {

    ProvincialRequestControl provincialRequestControl = getProvincialRequestControl();

    ProvincialPatientConsentCriteria provincialPatientConsentCriteria =
        ProvincialPatientConsentCriteria.builder()
            .firstName("First_Name")
            .lastName("last_Name")
            .patientIdentifier("Patient_Identifier")
            .effectiveDateTime("20220110")
            .build();

    String bearerToken = "sampleToken";

    ProvincialPatientConsent expectedProvincialPatientConsent =
        ProvincialPatientConsent.builder()
            .provincialResponseAcknowledgement(
                ProvincialResponseAcknowledgement.builder()
                    .operationOutcome(
                        OperationOutcome.builder()
                            .status(
                                ResponseStatus.builder()
                                    .code(Status.ACCEPT)
                                    .text(Strings.EMPTY)
                                    .build())
                            .build())
                    .auditEvent(
                        AuditEvent.builder()
                            .provincialRequestControl(
                                ProvincialRequestControl.builder()
                                    .requestId(UUID.randomUUID().toString())
                                    .build())
                            .build())
                    .build())
            .build();

    Mono<ProvincialPatientConsentResponse> expectedProvincialPatientSearchResponse =
        Mono.just(
            ProvincialPatientConsentResponse.builder()
                .provincialResponsePayload(expectedProvincialPatientConsent)
                .build());

    Mockito.when(
            provincialRegistryProcessor.getProvincialPatientConsentInClientRegistry(
                provincialPatientConsentCriteria))
        .thenReturn(expectedProvincialPatientSearchResponse);

    Mono<ProvincialPatientConsent> provincialPatientConsent =
        provincialRegistryService.getProvincialPatientConsent(provincialPatientConsentCriteria);

    StepVerifier.create(
            provincialPatientConsent.contextWrite(
                ctx -> ctx.put(ProvincialRequestControl.class, provincialRequestControl)))
        .assertNext(
            actualResponse ->
                assertThat(actualResponse).isEqualTo(expectedProvincialPatientConsent))
        .verifyComplete();
  }

  @Test
  void retrieveLocation() {

    ProvincialRequestControl provincialRequestControl = getProvincialRequestControl();

    ProvincialLocationSearchCriteria provincialLocationSearchCriteria =
        ProvincialLocationSearchCriteria.builder()
            .locationName("general")
            .locationType("hosp")
            .provincialRequestControl(provincialRequestControl)
            .build();

    String bearerToken = "sampleToken";

    ProvincialLocationSummaries expectedProvincialLocationSummaries =
        ProvincialLocationSummaries.builder()
            .provincialResponseAcknowledgement(
                ProvincialResponseAcknowledgement.builder()
                    .operationOutcome(
                        OperationOutcome.builder()
                            .status(
                                ResponseStatus.builder()
                                    .code(Status.ACCEPT)
                                    .text(Strings.EMPTY)
                                    .build())
                            .build())
                    .auditEvent(
                        AuditEvent.builder()
                            .provincialRequestControl(
                                ProvincialRequestControl.builder()
                                    .requestId(UUID.randomUUID().toString())
                                    .build())
                            .build())
                    .build())
            .build();

    Mono<ProvincialLocationSearchResponse> expectedProvincialPatientSearchResponse =
        Mono.just(
            ProvincialLocationSearchResponse.builder()
                .provincialResponsePayload(expectedProvincialLocationSummaries)
                .build());

    Mockito.when(
            provincialRegistryProcessor.searchProvincialLocationInClientRegistry(
                provincialLocationSearchCriteria))
        .thenReturn(expectedProvincialPatientSearchResponse);

    StepVerifier.create(
            provincialRegistryService
                .retrieveLocation(provincialLocationSearchCriteria)
                .contextWrite(
                    ctx ->
                        ctx.put(ProvincialRequestControl.class, provincialRequestControl)
                            .put(HttpHeaders.AUTHORIZATION, "mock")))
        .assertNext(
            actualResponse ->
                assertThat(actualResponse).isEqualTo(expectedProvincialLocationSummaries))
        .verifyComplete();
  }

  @Test
  void retrieveLocationDetails() {

    ProvincialRequestControl provincialRequestControl = getProvincialRequestControl();
    ProvincialLocationDetailsCriteria provincialLocationDetailsCriteria =
        ProvincialLocationDetailsCriteria.builder().identifier("sampleIdentifier").build();

    String bearerToken = "sampleToken";

    ProvincialLocationDetails expectedProvincialLocationDetails =
        ProvincialLocationDetails.builder()
            .provincialResponseAcknowledgement(
                ProvincialResponseAcknowledgement.builder()
                    .operationOutcome(
                        OperationOutcome.builder()
                            .status(
                                ResponseStatus.builder()
                                    .code(Status.ACCEPT)
                                    .text(Strings.EMPTY)
                                    .build())
                            .build())
                    .auditEvent(
                        AuditEvent.builder()
                            .provincialRequestControl(
                                ProvincialRequestControl.builder()
                                    .requestId(UUID.randomUUID().toString())
                                    .build())
                            .build())
                    .build())
            .build();

    Mono<ProvincialLocationDetailsResponse> expectedProvincialLocationDetailsResponse =
        Mono.just(
            ProvincialLocationDetailsResponse.builder()
                .provincialResponsePayload(expectedProvincialLocationDetails)
                .build());

    Mockito.when(
            provincialRegistryProcessor.searchProvincialLocationDetailsInClientRegistry(
                provincialLocationDetailsCriteria))
        .thenReturn(expectedProvincialLocationDetailsResponse);

    StepVerifier.create(
            provincialRegistryService
                .retrieveLocationDetails(provincialLocationDetailsCriteria)
                .contextWrite(
                    ctx ->
                        ctx.put(ProvincialRequestControl.class, provincialRequestControl)
                            .put(HttpHeaders.AUTHORIZATION, bearerToken)))
        .assertNext(
            actualResponse ->
                assertThat(actualResponse).isEqualTo(expectedProvincialLocationDetails))
        .verifyComplete();
  }

  private ProvincialRequestControl getProvincialRequestControl() {
    return ProvincialRequestControl.builder()
        .requestId("Correlation_Id")
        .pharmacy(Pharmacy.builder().name("Pharmacy_Id").build())
        .province(Province.QC)
        .build();
  }
}
