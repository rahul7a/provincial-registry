package com.lblw.vphx.phms.registry.processor.helpers;

import static com.mongodb.assertions.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import com.lblw.vphx.phms.domain.common.Service;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.common.response.*;
import com.lblw.vphx.phms.domain.location.ProvincialLocation;
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
import com.lblw.vphx.phms.domain.provider.request.ProvincialProviderSearchCriteria;
import com.lblw.vphx.phms.domain.provider.request.hl7v3.ProvincialProviderSearchRequest;
import com.lblw.vphx.phms.domain.provider.response.ProvincialProviderProfiles;
import com.lblw.vphx.phms.domain.provider.response.hl7v3.ProvincialProviderSearchResponse;
import com.lblw.vphx.phms.rules.service.DroolsEngineService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProvincialResponseRuleEngineHelperTest {

  private ProvincialPatientSearchResponse responseToValidate;

  private ProvincialResponseRuleEngineHelper provincialResponseRuleEngineHelper;

  @Mock private DroolsEngineService phmsRulesEngineService;

  @BeforeEach
  public void beforeEach() {
    responseToValidate =
        ProvincialPatientSearchResponse.builder()
            .provincialResponsePayload(
                ProvincialPatientProfile.builder()
                    .firstName("test")
                    .lastName("person")
                    .provincialResponseAcknowledgement(
                        ProvincialResponseAcknowledgement.builder()
                            .auditEvent(
                                AuditEvent.builder()
                                    .provincialRequestControl(
                                        ProvincialRequestControl.builder()
                                            .requestId("correlation-id")
                                            .build())
                                    .build())
                            .build())
                    .build())
            .build();

    provincialResponseRuleEngineHelper =
        new ProvincialResponseRuleEngineHelper(phmsRulesEngineService);
  }

  @Test
  void validateSuccessfulResponse() {

    OperationOutcome ackWithValidationMsg =
        OperationOutcome.builder()
            .classification(ResponseClassification.SYSTEM)
            .status(ResponseStatus.builder().text(Strings.EMPTY).code(Status.ACCEPT).build())
            .build();

    Mockito.when(phmsRulesEngineService.processPatientResponse(responseToValidate))
        .thenReturn(ackWithValidationMsg);

    ProvincialPatientSearchResponse validatedResponse =
        provincialResponseRuleEngineHelper.validatePatientSearchResponse(responseToValidate);

    ProvincialPatientProfile validatedProfile = validatedResponse.getProvincialResponsePayload();
    ProvincialResponseAcknowledgement validatedAcknowledgement =
        validatedProfile.getProvincialResponseAcknowledgement();

    assertEquals("test", validatedProfile.getFirstName());
    assertEquals("person", validatedProfile.getLastName());

    assertEquals(
        "correlation-id",
        validatedAcknowledgement.getAuditEvent().getProvincialRequestControl().getRequestId());
    assertEquals(
        Status.ACCEPT, validatedAcknowledgement.getOperationOutcome().getStatus().getCode());

    assertNotNull(validatedAcknowledgement.getOperationOutcome().getClassification());
    assertNotNull(validatedAcknowledgement.getOperationOutcome().getStatus());
  }

  @Test
  void validateResponseWithJustErrorMessage() {

    OperationOutcome ackWithValidationMsg =
        OperationOutcome.builder()
            .status(ResponseStatus.builder().text(Strings.EMPTY).code(Status.ACCEPT).build())
            .classification(ResponseClassification.SYSTEM)
            .build();

    Mockito.when(phmsRulesEngineService.processPatientResponse(responseToValidate))
        .thenReturn(ackWithValidationMsg);

    ProvincialPatientSearchResponse validatedResponse =
        provincialResponseRuleEngineHelper.validatePatientSearchResponse(responseToValidate);

    ProvincialPatientProfile validatedProfile = validatedResponse.getProvincialResponsePayload();
    ProvincialResponseAcknowledgement validatedAcknowledgement =
        validatedProfile.getProvincialResponseAcknowledgement();

    assertEquals("test", validatedProfile.getFirstName());
    assertEquals("person", validatedProfile.getLastName());

    assertEquals(
        "correlation-id",
        validatedAcknowledgement.getAuditEvent().getProvincialRequestControl().getRequestId());
    assertEquals(
        Status.ACCEPT, validatedAcknowledgement.getOperationOutcome().getStatus().getCode());
    assertEquals(
        ResponseClassification.SYSTEM,
        validatedAcknowledgement.getOperationOutcome().getClassification());
    assertNotNull(validatedAcknowledgement.getOperationOutcome().getStatus());
  }

  @Test
  void validateResponseWithErrorMessageAndCode() {

    OperationOutcome ackWithValidationMsg =
        OperationOutcome.builder()
            .classification(ResponseClassification.SYSTEM)
            .issues(
                List.of(
                    Issue.builder()
                        .code("EM.112")
                        .classification(ResponseClassification.SYSTEM)
                        .priority(
                            IssuePriority.builder()
                                .code(Priority.ERROR)
                                .text(Strings.EMPTY)
                                .build())
                        .severity(
                            IssueSeverity.builder().code(Severity.HIGH).text(Strings.EMPTY).build())
                        .source(
                            IssueSource.builder().code(Source.INTERNAL).text(Strings.EMPTY).build())
                        .triggeredBy(
                            Service.builder()
                                .code(ServiceCode.VPHX_PHMS)
                                .serviceOutageStatus("")
                                .build())
                        .description("")
                        .details(
                            "Please enter mandatory field: ProviderIdentifierType or ProviderIdentifierValue.")
                        .build()))
            .status(ResponseStatus.builder().text(Strings.EMPTY).code(Status.REJECT).build())
            .build();

    Mockito.when(phmsRulesEngineService.processPatientResponse(responseToValidate))
        .thenReturn(ackWithValidationMsg);

    ProvincialPatientSearchResponse validatedResponse =
        provincialResponseRuleEngineHelper.validatePatientSearchResponse(responseToValidate);

    ProvincialPatientProfile validatedProfile = validatedResponse.getProvincialResponsePayload();
    ProvincialResponseAcknowledgement validatedAcknowledgement =
        validatedProfile.getProvincialResponseAcknowledgement();

    assertEquals("test", validatedProfile.getFirstName());
    assertEquals("person", validatedProfile.getLastName());

    assertEquals(
        "correlation-id",
        validatedAcknowledgement.getAuditEvent().getProvincialRequestControl().getRequestId());
    assertEquals(
        Status.REJECT, validatedAcknowledgement.getOperationOutcome().getStatus().getCode());
    assertEquals(
        ResponseClassification.SYSTEM,
        validatedAcknowledgement.getOperationOutcome().getClassification());
  }

  @Test
  void validateRecordsFoundInDsqAreNotFound() {

    responseToValidate =
        ProvincialPatientSearchResponse.builder()
            .provincialResponsePayload(
                ProvincialPatientProfile.builder()
                    .provincialResponseAcknowledgement(
                        ProvincialResponseAcknowledgement.builder()
                            .auditEvent(
                                AuditEvent.builder()
                                    .provincialRequestControl(
                                        ProvincialRequestControl.builder()
                                            .requestId(UUID.randomUUID().toString())
                                            .build())
                                    .build())
                            .build())
                    .build())
            .build();

    OperationOutcome ackWithValidationMsg =
        OperationOutcome.builder()
            .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
            .issues(
                List.of(
                    Issue.builder()
                        .code("EM.112")
                        .classification(ResponseClassification.SYSTEM)
                        .priority(
                            IssuePriority.builder()
                                .code(Priority.ERROR)
                                .text(Strings.EMPTY)
                                .build())
                        .severity(
                            IssueSeverity.builder().code(Severity.HIGH).text(Strings.EMPTY).build())
                        .source(
                            IssueSource.builder().code(Source.INTERNAL).text(Strings.EMPTY).build())
                        .triggeredBy(
                            Service.builder()
                                .code(ServiceCode.VPHX_PHMS)
                                .serviceOutageStatus("")
                                .build())
                        .description("")
                        .details(
                            "Please enter mandatory field: ProviderIdentifierType or ProviderIdentifierValue.")
                        .build()))
            .classification(ResponseClassification.SYSTEM)
            .build();

    Mockito.when(phmsRulesEngineService.processPatientResponse(responseToValidate))
        .thenReturn(ackWithValidationMsg);

    ProvincialPatientSearchResponse validatedResponse =
        provincialResponseRuleEngineHelper.validatePatientSearchResponse(responseToValidate);

    ProvincialPatientProfile validatedProfile = validatedResponse.getProvincialResponsePayload();
    ProvincialResponseAcknowledgement validatedAcknowledgement =
        validatedProfile.getProvincialResponseAcknowledgement();

    assertEquals(
        Status.REJECT, validatedAcknowledgement.getOperationOutcome().getStatus().getCode());
    assertEquals(
        ResponseClassification.SYSTEM,
        validatedAcknowledgement.getOperationOutcome().getClassification());
  }

  @Test
  void whenLocationRequestIsInValid_ThenReturnFailureResponseAcknowledgement() {
    ProvincialLocationDetailsRequest provincialLocationDetailsRequest =
        ProvincialLocationDetailsRequest.builder()
            .provincialRequestPayload(ProvincialLocationDetailsCriteria.builder().build())
            .build();

    OperationOutcome provincialResponseAcknowledgement =
        OperationOutcome.builder()
            .status(ResponseStatus.builder().text(Strings.EMPTY).code(Status.REJECT).build())
            .issues(
                List.of(
                    Issue.builder()
                        .code("EM.112")
                        .classification(ResponseClassification.SYSTEM)
                        .priority(
                            IssuePriority.builder()
                                .code(Priority.ERROR)
                                .text(Strings.EMPTY)
                                .build())
                        .severity(
                            IssueSeverity.builder().code(Severity.HIGH).text(Strings.EMPTY).build())
                        .source(
                            IssueSource.builder().code(Source.INTERNAL).text(Strings.EMPTY).build())
                        .triggeredBy(
                            Service.builder()
                                .code(ServiceCode.VPHX_PHMS)
                                .serviceOutageStatus("")
                                .build())
                        .description("")
                        .details("Location Details")
                        .build()))
            .build();

    Mockito.when(phmsRulesEngineService.processRequest(any(ProvincialLocationDetailsRequest.class)))
        .thenReturn(provincialResponseAcknowledgement);
    ProvincialLocationDetailsResponse provincialLocationDetailsResponse =
        provincialResponseRuleEngineHelper.validateLocationDetailsRequest(
            (provincialLocationDetailsRequest));
    assertEquals(
        Status.REJECT,
        provincialLocationDetailsResponse
            .getProvincialResponsePayload()
            .getProvincialResponseAcknowledgement()
            .getOperationOutcome()
            .getStatus()
            .getCode());
    assertEquals(
        "Location Details",
        provincialLocationDetailsResponse
            .getProvincialResponsePayload()
            .getProvincialResponseAcknowledgement()
            .getOperationOutcome()
            .getIssues()
            .get(0)
            .getDetails());
    assertEquals(
        "EM.112",
        provincialLocationDetailsResponse
            .getProvincialResponsePayload()
            .getProvincialResponseAcknowledgement()
            .getOperationOutcome()
            .getIssues()
            .get(0)
            .getCode());
  }

  @Test
  void whenLocationRequestIsNull_ThenProvincialRequestControlIsNull() {

    // Null request
    ProvincialLocationDetailsRequest provincialLocationDetailsRequest = null;

    // Expected Audit Event
    AuditEvent expectedAuditEvent = AuditEvent.builder().provincialRequestControl(null).build();

    // Stubbed Outcome
    OperationOutcome expectedOperationOutcome =
        OperationOutcome.builder()
            .status(ResponseStatus.builder().code(Status.ACCEPT).text(Strings.EMPTY).build())
            .classification(ResponseClassification.SYSTEM)
            .issues(new ArrayList<>())
            .build();
    Mockito.when(phmsRulesEngineService.processRequest(any())).thenReturn(expectedOperationOutcome);

    ProvincialLocationDetailsResponse provincialLocationDetailsResponseActual =
        provincialResponseRuleEngineHelper.validateLocationDetailsRequest(
            (provincialLocationDetailsRequest));

    assertEquals(
        expectedAuditEvent,
        provincialLocationDetailsResponseActual
            .getProvincialResponsePayload()
            .getProvincialResponseAcknowledgement()
            .getAuditEvent());
  }

  @Test
  void whenProviderRequestIsInValid_ThenReturnFailureResponseAcknowledgement() {
    ProvincialProviderSearchRequest provincialProviderSearchRequest =
        ProvincialProviderSearchRequest.builder()
            .provincialRequestPayload(ProvincialProviderSearchCriteria.builder().build())
            .build();

    OperationOutcome provincialResponseAcknowledgement =
        OperationOutcome.builder()
            .status(ResponseStatus.builder().text(Strings.EMPTY).code(Status.REJECT).build())
            .classification(ResponseClassification.SYSTEM)
            .issues(
                List.of(
                    Issue.builder()
                        .code("EM.112")
                        .classification(ResponseClassification.SYSTEM)
                        .priority(
                            IssuePriority.builder()
                                .code(Priority.ERROR)
                                .text(Strings.EMPTY)
                                .build())
                        .severity(
                            IssueSeverity.builder().code(Severity.HIGH).text(Strings.EMPTY).build())
                        .source(
                            IssueSource.builder().code(Source.INTERNAL).text(Strings.EMPTY).build())
                        .triggeredBy(
                            Service.builder()
                                .code(ServiceCode.VPHX_PHMS)
                                .serviceOutageStatus("")
                                .build())
                        .description("")
                        .details("")
                        .build()))
            .build();
    Mockito.when(phmsRulesEngineService.processRequest(any(ProvincialProviderSearchRequest.class)))
        .thenReturn(provincialResponseAcknowledgement);
    ProvincialProviderSearchResponse provincialProviderSearchResponse =
        provincialResponseRuleEngineHelper.validateProviderSearchRequest(
            (provincialProviderSearchRequest));
    assertEquals(
        Status.REJECT,
        provincialProviderSearchResponse
            .getProvincialResponsePayload()
            .getProvincialResponseAcknowledgement()
            .getOperationOutcome()
            .getStatus()
            .getCode());
    assertNotNull(
        provincialProviderSearchResponse
            .getProvincialResponsePayload()
            .getProvincialResponseAcknowledgement()
            .getOperationOutcome()
            .getClassification());
    assertEquals(
        "EM.112",
        provincialProviderSearchResponse
            .getProvincialResponsePayload()
            .getProvincialResponseAcknowledgement()
            .getOperationOutcome()
            .getIssues()
            .get(0)
            .getCode());
  }

  @Test
  void whenProviderSearchRequestIsNull_ThenProvincialRequestControlIsNull() {

    // Null request
    ProvincialProviderSearchRequest provincialProviderSearchRequest = null;

    // Expected Audit Event
    AuditEvent expectedAuditEvent = AuditEvent.builder().provincialRequestControl(null).build();

    // Stubbed Outcome
    OperationOutcome expectedOperationOutcome =
        OperationOutcome.builder()
            .status(ResponseStatus.builder().code(Status.ACCEPT).text(Strings.EMPTY).build())
            .classification(ResponseClassification.SYSTEM)
            .issues(new ArrayList<>())
            .build();
    Mockito.when(phmsRulesEngineService.processRequest(any())).thenReturn(expectedOperationOutcome);

    ProvincialProviderSearchResponse provincialProviderSearchResponseActual =
        provincialResponseRuleEngineHelper.validateProviderSearchRequest(
            (provincialProviderSearchRequest));

    assertEquals(
        expectedAuditEvent,
        provincialProviderSearchResponseActual
            .getProvincialResponsePayload()
            .getProvincialResponseAcknowledgement()
            .getAuditEvent());
  }

  @Test
  void whenPatientConsentRequestIsInValid_ThenReturnFailureResponseAcknowledgement() {

    // Invalid Request
    ProvincialPatientConsentRequest provincialPatientConsentRequest =
        ProvincialPatientConsentRequest.builder()
            .provincialRequestPayload(ProvincialPatientConsentCriteria.builder().build())
            .build();

    // Expected Outcome
    OperationOutcome expectedProvincialResponseAcknowledgement =
        OperationOutcome.builder()
            .classification(ResponseClassification.SYSTEM)
            .status(ResponseStatus.builder().text(Strings.EMPTY).code(Status.REJECT).build())
            .issues(
                List.of(
                    Issue.builder()
                        .code("EM.112")
                        .classification(ResponseClassification.SYSTEM)
                        .priority(
                            IssuePriority.builder()
                                .code(Priority.ERROR)
                                .text(Strings.EMPTY)
                                .build())
                        .severity(
                            IssueSeverity.builder().code(Severity.HIGH).text(Strings.EMPTY).build())
                        .source(
                            IssueSource.builder().code(Source.INTERNAL).text(Strings.EMPTY).build())
                        .triggeredBy(
                            Service.builder()
                                .code(ServiceCode.VPHX_PHMS)
                                .serviceOutageStatus("")
                                .build())
                        .description("")
                        .details("")
                        .build()))
            .build();

    Mockito.when(phmsRulesEngineService.processRequest(any()))
        .thenReturn(expectedProvincialResponseAcknowledgement);

    ProvincialPatientConsentResponse actualProvincialPatientConsentResponse =
        provincialResponseRuleEngineHelper.validatePatientConsentRequest(
            (provincialPatientConsentRequest));
    assertEquals(
        expectedProvincialResponseAcknowledgement,
        actualProvincialPatientConsentResponse
            .getProvincialResponsePayload()
            .getProvincialResponseAcknowledgement()
            .getOperationOutcome());
  }

  @Test
  void whenPatientConsentRequestIsNull_ThenProvincialRequestControlIsNull() {

    // Null request
    ProvincialPatientConsentRequest provincialPatientConsentRequest = null;

    // Expected Audit Event
    AuditEvent expectedAuditEvent = AuditEvent.builder().provincialRequestControl(null).build();

    // Stubbed Outcome
    OperationOutcome expectedOperationOutcome =
        OperationOutcome.builder()
            .status(ResponseStatus.builder().code(Status.ACCEPT).text(Strings.EMPTY).build())
            .classification(ResponseClassification.SYSTEM)
            .issues(new ArrayList<>())
            .build();
    Mockito.when(phmsRulesEngineService.processRequest(any())).thenReturn(expectedOperationOutcome);

    ProvincialPatientConsentResponse actualProvincialPatientConsentResponse =
        provincialResponseRuleEngineHelper.validatePatientConsentRequest(
            (provincialPatientConsentRequest));

    assertEquals(
        expectedAuditEvent,
        actualProvincialPatientConsentResponse
            .getProvincialResponsePayload()
            .getProvincialResponseAcknowledgement()
            .getAuditEvent());
  }

  @Test
  void whenPatientSearchRequestIsInValid_ThenReturnFailureResponseAcknowledgement() {

    // Invalid Request
    ProvincialPatientSearchRequest provincialPatientSearchRequest =
        ProvincialPatientSearchRequest.builder()
            .provincialRequestPayload(ProvincialPatientSearchCriteria.builder().build())
            .build();

    // Expected Outcome
    OperationOutcome expectedProvincialResponseAcknowledgement =
        OperationOutcome.builder()
            .status(ResponseStatus.builder().text(Strings.EMPTY).code(Status.REJECT).build())
            .classification(ResponseClassification.SYSTEM)
            .issues(
                List.of(
                    Issue.builder()
                        .code("EM.112")
                        .classification(ResponseClassification.SYSTEM)
                        .priority(
                            IssuePriority.builder()
                                .code(Priority.ERROR)
                                .text(Strings.EMPTY)
                                .build())
                        .severity(
                            IssueSeverity.builder().code(Severity.HIGH).text(Strings.EMPTY).build())
                        .source(
                            IssueSource.builder().code(Source.INTERNAL).text(Strings.EMPTY).build())
                        .triggeredBy(
                            Service.builder()
                                .code(ServiceCode.VPHX_PHMS)
                                .serviceOutageStatus("")
                                .build())
                        .description("")
                        .details("")
                        .build()))
            .build();

    Mockito.when(phmsRulesEngineService.processRequest(any()))
        .thenReturn(expectedProvincialResponseAcknowledgement);

    ProvincialPatientSearchResponse actualProvincialPatientSearchResponse =
        provincialResponseRuleEngineHelper.validatePatientSearchRequest(
            (provincialPatientSearchRequest));
    assertEquals(
        expectedProvincialResponseAcknowledgement,
        actualProvincialPatientSearchResponse
            .getProvincialResponsePayload()
            .getProvincialResponseAcknowledgement()
            .getOperationOutcome());
  }

  @Test
  void whenPatientSearchRequestIsNull_ThenProvincialRequestControlIsNull() {

    // Null request
    ProvincialPatientSearchRequest provincialPatientSearchRequest = null;

    // Expected Audit Event
    AuditEvent expectedAuditEvent = AuditEvent.builder().provincialRequestControl(null).build();

    // Stubbed Outcome
    OperationOutcome expectedOperationOutcome =
        OperationOutcome.builder()
            .status(ResponseStatus.builder().code(Status.ACCEPT).text(Strings.EMPTY).build())
            .classification(ResponseClassification.SYSTEM)
            .issues(new ArrayList<>())
            .build();
    Mockito.when(phmsRulesEngineService.processRequest(any())).thenReturn(expectedOperationOutcome);

    ProvincialPatientSearchResponse actualProvincialPatientSearchResponse =
        provincialResponseRuleEngineHelper.validatePatientSearchRequest(
            (provincialPatientSearchRequest));

    assertEquals(
        expectedAuditEvent,
        actualProvincialPatientSearchResponse
            .getProvincialResponsePayload()
            .getProvincialResponseAcknowledgement()
            .getAuditEvent());
  }

  @Test
  void whenLocationSearchRequestIsInValid_ThenReturnFailureResponseAcknowledgement() {

    // Invalid Request
    ProvincialLocationSearchRequest provincialLocationSearchRequest =
        ProvincialLocationSearchRequest.builder()
            .provincialRequestPayload(ProvincialLocationSearchCriteria.builder().build())
            .build();

    // Expected Outcome
    OperationOutcome expectedProvincialResponseAcknowledgement =
        OperationOutcome.builder()
            .status(ResponseStatus.builder().text(Strings.EMPTY).code(Status.REJECT).build())
            .classification(ResponseClassification.SYSTEM)
            .issues(
                List.of(
                    Issue.builder()
                        .code("EM.112")
                        .classification(ResponseClassification.SYSTEM)
                        .priority(
                            IssuePriority.builder()
                                .code(Priority.ERROR)
                                .text(Strings.EMPTY)
                                .build())
                        .severity(
                            IssueSeverity.builder().code(Severity.HIGH).text(Strings.EMPTY).build())
                        .source(
                            IssueSource.builder().code(Source.INTERNAL).text(Strings.EMPTY).build())
                        .triggeredBy(
                            Service.builder()
                                .code(ServiceCode.VPHX_PHMS)
                                .serviceOutageStatus("")
                                .build())
                        .description("")
                        .details("")
                        .build()))
            .build();

    Mockito.when(phmsRulesEngineService.processRequest(any()))
        .thenReturn(expectedProvincialResponseAcknowledgement);

    ProvincialLocationSearchResponse actualProvincialLocationSearchResponse =
        provincialResponseRuleEngineHelper.validateLocationSearchRequest(
            (provincialLocationSearchRequest));

    assertEquals(
        expectedProvincialResponseAcknowledgement,
        actualProvincialLocationSearchResponse
            .getProvincialResponsePayload()
            .getProvincialResponseAcknowledgement()
            .getOperationOutcome());
  }

  @Test
  void whenLocationSearchRequestIsInValid_ThenProvincialRequestControlIsNull() {

    // Null request
    ProvincialLocationSearchRequest provincialLocationSearchRequest = null;

    // Expected Audit Event
    AuditEvent expectedAuditEvent = AuditEvent.builder().provincialRequestControl(null).build();

    // Stubbed Outcome
    OperationOutcome expectedOperationOutcome =
        OperationOutcome.builder()
            .status(ResponseStatus.builder().code(Status.ACCEPT).text(Strings.EMPTY).build())
            .classification(ResponseClassification.SYSTEM)
            .issues(new ArrayList<>())
            .build();
    Mockito.when(phmsRulesEngineService.processRequest(any())).thenReturn(expectedOperationOutcome);

    ProvincialLocationSearchResponse actualProvincialLocationSearchResponse =
        provincialResponseRuleEngineHelper.validateLocationSearchRequest(
            (provincialLocationSearchRequest));

    assertEquals(
        expectedAuditEvent,
        actualProvincialLocationSearchResponse
            .getProvincialResponsePayload()
            .getProvincialResponseAcknowledgement()
            .getAuditEvent());
  }

  @Test
  void testValidateProviderSearchResponse_ForSuccessScenario() {

    ProvincialProviderSearchResponse provincialProviderSearchResponse =
        ProvincialProviderSearchResponse.builder()
            .provincialResponsePayload(
                ProvincialProviderProfiles.builder()
                    .provincialProviderProfiles(Arrays.asList())
                    .provincialResponseAcknowledgement(
                        ProvincialResponseAcknowledgement.builder()
                            .auditEvent(
                                AuditEvent.builder()
                                    .provincialRequestControl(
                                        ProvincialRequestControl.builder()
                                            .requestId("correlation-id")
                                            .build())
                                    .build())
                            .build())
                    .build())
            .build();

    OperationOutcome operationOutcome =
        OperationOutcome.builder()
            .classification(ResponseClassification.SYSTEM)
            .status(ResponseStatus.builder().text(Strings.EMPTY).code(Status.ACCEPT).build())
            .build();

    Mockito.when(phmsRulesEngineService.processProviderResponse(provincialProviderSearchResponse))
        .thenReturn(operationOutcome);

    ProvincialProviderSearchResponse validatedResponse =
        provincialResponseRuleEngineHelper.validateProviderSearchResponse(
            provincialProviderSearchResponse);

    ProvincialProviderProfiles validatedProfile = validatedResponse.getProvincialResponsePayload();
    ProvincialResponseAcknowledgement validatedAcknowledgement =
        validatedProfile.getProvincialResponseAcknowledgement();

    assertEquals(
        "correlation-id",
        validatedAcknowledgement.getAuditEvent().getProvincialRequestControl().getRequestId());
    assertEquals(
        Status.ACCEPT, validatedAcknowledgement.getOperationOutcome().getStatus().getCode());
    assertNotNull(validatedAcknowledgement.getOperationOutcome().getClassification());
    assertNotNull(validatedAcknowledgement.getOperationOutcome().getStatus());
  }

  @Test
  void testValidateConsentSearchResponse_ForSuccessScenario() {

    ProvincialPatientConsentResponse provincialPatientConsentResponse =
        ProvincialPatientConsentResponse.builder()
            .provincialResponsePayload(
                ProvincialPatientConsent.builder()
                    .consentToken("consentToken")
                    .consentValidityEndDateTime(Instant.now())
                    .consentValidityStartDateTime(Instant.now())
                    .provincialResponseAcknowledgement(
                        ProvincialResponseAcknowledgement.builder()
                            .auditEvent(
                                AuditEvent.builder()
                                    .provincialRequestControl(
                                        ProvincialRequestControl.builder()
                                            .requestId("correlation-id")
                                            .build())
                                    .build())
                            .build())
                    .build())
            .build();

    OperationOutcome operationOutcome =
        OperationOutcome.builder()
            .classification(ResponseClassification.SYSTEM)
            .status(ResponseStatus.builder().text(Strings.EMPTY).code(Status.ACCEPT).build())
            .build();

    Mockito.when(
            phmsRulesEngineService.processProvincialPatientConsentResponse(
                provincialPatientConsentResponse))
        .thenReturn(operationOutcome);

    ProvincialPatientConsentResponse validatedResponse =
        provincialResponseRuleEngineHelper.validateConsentSearchResponse(
            provincialPatientConsentResponse);

    ProvincialPatientConsent validatedProfile = validatedResponse.getProvincialResponsePayload();
    ProvincialResponseAcknowledgement validatedAcknowledgement =
        validatedProfile.getProvincialResponseAcknowledgement();

    assertEquals(
        "correlation-id",
        validatedAcknowledgement.getAuditEvent().getProvincialRequestControl().getRequestId());
    assertEquals(
        Status.ACCEPT, validatedAcknowledgement.getOperationOutcome().getStatus().getCode());
    assertNotNull(validatedAcknowledgement.getOperationOutcome().getClassification());
    assertNotNull(validatedAcknowledgement.getOperationOutcome().getStatus());
  }

  @Test
  void testValidateLocationSearchResponse_ForSuccessScenario() {

    ProvincialLocationSearchResponse provincialLocationSearchResponse =
        ProvincialLocationSearchResponse.builder()
            .provincialResponsePayload(
                ProvincialLocationSummaries.builder()
                    .provincialLocations(Arrays.asList())
                    .provincialResponseAcknowledgement(
                        ProvincialResponseAcknowledgement.builder()
                            .auditEvent(
                                AuditEvent.builder()
                                    .provincialRequestControl(
                                        ProvincialRequestControl.builder()
                                            .requestId("correlation-id")
                                            .build())
                                    .build())
                            .build())
                    .build())
            .build();

    OperationOutcome operationOutcome =
        OperationOutcome.builder()
            .classification(ResponseClassification.SYSTEM)
            .status(ResponseStatus.builder().text(Strings.EMPTY).code(Status.ACCEPT).build())
            .build();

    Mockito.when(
            phmsRulesEngineService.processProvincialLocationSummariesResponse(
                provincialLocationSearchResponse))
        .thenReturn(operationOutcome);

    ProvincialLocationSearchResponse validatedResponse =
        provincialResponseRuleEngineHelper.validateLocationSearchResponse(
            provincialLocationSearchResponse);

    ProvincialLocationSummaries validatedProfile = validatedResponse.getProvincialResponsePayload();
    ProvincialResponseAcknowledgement validatedAcknowledgement =
        validatedProfile.getProvincialResponseAcknowledgement();

    assertEquals(
        "correlation-id",
        validatedAcknowledgement.getAuditEvent().getProvincialRequestControl().getRequestId());
    assertEquals(
        Status.ACCEPT, validatedAcknowledgement.getOperationOutcome().getStatus().getCode());
    assertNotNull(validatedAcknowledgement.getOperationOutcome().getClassification());
    assertNotNull(validatedAcknowledgement.getOperationOutcome().getStatus());
  }

  @Test
  void testValidateLocationDetailsResponse_ForSuccessScenario() {

    ProvincialLocationDetailsResponse provincialLocationDetailsResponse =
        ProvincialLocationDetailsResponse.builder()
            .provincialResponsePayload(
                ProvincialLocationDetails.builder()
                    .provincialLocation(ProvincialLocation.builder().build())
                    .provincialResponseAcknowledgement(
                        ProvincialResponseAcknowledgement.builder()
                            .auditEvent(
                                AuditEvent.builder()
                                    .provincialRequestControl(
                                        ProvincialRequestControl.builder()
                                            .requestId("correlation-id")
                                            .build())
                                    .build())
                            .build())
                    .build())
            .build();

    OperationOutcome operationOutcome =
        OperationOutcome.builder()
            .classification(ResponseClassification.SYSTEM)
            .status(ResponseStatus.builder().text(Strings.EMPTY).code(Status.ACCEPT).build())
            .build();

    Mockito.when(
            phmsRulesEngineService.processProvincialLocationDetailsResponse(
                provincialLocationDetailsResponse))
        .thenReturn(operationOutcome);

    ProvincialLocationDetailsResponse validatedResponse =
        provincialResponseRuleEngineHelper.validateLocationDetailsResponse(
            provincialLocationDetailsResponse);

    ProvincialLocationDetails validatedProfile = validatedResponse.getProvincialResponsePayload();
    ProvincialResponseAcknowledgement validatedAcknowledgement =
        validatedProfile.getProvincialResponseAcknowledgement();

    assertEquals(
        "correlation-id",
        validatedAcknowledgement.getAuditEvent().getProvincialRequestControl().getRequestId());
    assertEquals(
        Status.ACCEPT, validatedAcknowledgement.getOperationOutcome().getStatus().getCode());
    assertNotNull(validatedAcknowledgement.getOperationOutcome().getClassification());
    assertNotNull(validatedAcknowledgement.getOperationOutcome().getStatus());
  }
}
