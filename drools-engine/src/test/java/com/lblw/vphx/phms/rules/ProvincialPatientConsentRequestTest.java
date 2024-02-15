package com.lblw.vphx.phms.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.lblw.vphx.phms.common.coding.services.CodeableConceptRefDataService;
import com.lblw.vphx.phms.common.coding.services.CodeableConceptService;
import com.lblw.vphx.phms.common.config.MongoConfig;
import com.lblw.vphx.phms.domain.common.Service;
import com.lblw.vphx.phms.domain.common.response.*;
import com.lblw.vphx.phms.domain.patient.consent.request.ProvincialPatientConsentCriteria;
import com.lblw.vphx.phms.domain.patient.consent.request.hl7v3.ProvincialPatientConsentRequest;
import com.lblw.vphx.phms.rules.config.RulesEngineConfig;
import com.lblw.vphx.phms.rules.service.CodeableConceptValidationService;
import com.lblw.vphx.phms.rules.service.DroolsEngineService;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

@DataMongoTest
@ContextConfiguration(
        classes = {
                RulesEngineConfig.class,
                DroolsEngineService.class,
                MongoConfig.class,
                CodeableConceptValidationService.class,
                CodeableConceptService.class,
                CodeableConceptRefDataService.class
        })
class ProvincialPatientConsentRequestTest {
    @MockBean
    CodeableConceptService codeableConceptService;
    @Autowired
    private DroolsEngineService droolsEngineService;

    @Test
    void whenSearchCriteriaIsBlank_ThenResponseAcknowledgementIsFailure() {
        ProvincialPatientConsentRequest provincialPatientConsentRequest =
                ProvincialPatientConsentRequest.builder()
                        .provincialRequestPayload(ProvincialPatientConsentCriteria.builder().build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialPatientConsentRequest);
        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
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
                                                .details("Patient Details")
                                                .build()))
                        .build();
        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenFirstNameIsNullOrBlank_ThenResponseAcknowledgmentIsFailure() {
        ProvincialPatientConsentRequest provincialPatientConsentRequest =
                ProvincialPatientConsentRequest.builder()
                        .provincialRequestPayload(
                                ProvincialPatientConsentCriteria.builder()
                                        .firstName("")
                                        .lastName("lastName")
                                        .effectiveDateTime("effectiveDateTime")
                                        .patientIdentifier("patientIdentifier")
                                        .build())
                        .build();
        ProvincialPatientConsentRequest patientConsentRequest =
                ProvincialPatientConsentRequest.builder()
                        .provincialRequestPayload(
                                ProvincialPatientConsentCriteria.builder()
                                        .firstName(null)
                                        .lastName("lastName")
                                        .effectiveDateTime("effectiveDateTime")
                                        .patientIdentifier("patientIdentifier")
                                        .build())
                        .build();

        final OperationOutcome provincialResponseAcknowledgementBlank =
                droolsEngineService.processRequest(provincialPatientConsentRequest);
        final OperationOutcome provincialResponseAcknowledgementNull =
                droolsEngineService.processRequest(patientConsentRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
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
                                                .details("Patient Details")
                                                .build()))
                        .build();
        assertThat(provincialResponseAcknowledgementBlank).isEqualTo(expectedAcknowledgement);
        assertThat(provincialResponseAcknowledgementNull).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenLastNameIsNullOrBlank_ThenResponseAcknowledgementIsFailure() {
        ProvincialPatientConsentRequest provincialPatientConsentRequest =
                ProvincialPatientConsentRequest.builder()
                        .provincialRequestPayload(
                                ProvincialPatientConsentCriteria.builder()
                                        .firstName("firstName")
                                        .lastName("")
                                        .effectiveDateTime("effectiveDateTime")
                                        .patientIdentifier("patientIdentifier")
                                        .build())
                        .build();
        ProvincialPatientConsentRequest patientConsentRequest =
                ProvincialPatientConsentRequest.builder()
                        .provincialRequestPayload(
                                ProvincialPatientConsentCriteria.builder()
                                        .firstName("firstName")
                                        .lastName(null)
                                        .effectiveDateTime("effectiveDateTime")
                                        .patientIdentifier("patientIdentifier")
                                        .build())
                        .build();
        final OperationOutcome provincialResponseAcknowledgementBlank =
                droolsEngineService.processRequest(provincialPatientConsentRequest);
        final OperationOutcome provincialResponseAcknowledgementNull =
                droolsEngineService.processRequest(patientConsentRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
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
                                                .details("Patient Details")
                                                .build()))
                        .build();
        assertThat(provincialResponseAcknowledgementBlank).isEqualTo(expectedAcknowledgement);
        assertThat(provincialResponseAcknowledgementNull).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenEffectiveIsNullOrBlank_ThenResponseAcknowledgementIsFailure() {
        ProvincialPatientConsentRequest provincialPatientConsentRequest =
                ProvincialPatientConsentRequest.builder()
                        .provincialRequestPayload(
                                ProvincialPatientConsentCriteria.builder()
                                        .firstName("firstName")
                                        .lastName("lastName")
                                        .effectiveDateTime("")
                                        .patientIdentifier("patientIdentifier")
                                        .build())
                        .build();
        ProvincialPatientConsentRequest patientConsentRequest =
                ProvincialPatientConsentRequest.builder()
                        .provincialRequestPayload(
                                ProvincialPatientConsentCriteria.builder()
                                        .firstName("firstName")
                                        .lastName("lastName")
                                        .effectiveDateTime(null)
                                        .patientIdentifier("patientIdentifier")
                                        .build())
                        .build();
        final OperationOutcome provincialResponseAcknowledgementBlank =
                droolsEngineService.processRequest(provincialPatientConsentRequest);
        final OperationOutcome provincialResponseAcknowledgementNull =
                droolsEngineService.processRequest(patientConsentRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
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
                                                .details("Patient Details")
                                                .build()))
                        .build();
        assertThat(provincialResponseAcknowledgementBlank).isEqualTo(expectedAcknowledgement);
        assertThat(provincialResponseAcknowledgementNull).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenPatientIdentifierIsNullOrBlank_ThenResponseAcknowledgementIsFailure() {
        ProvincialPatientConsentRequest provincialPatientConsentRequest =
                ProvincialPatientConsentRequest.builder()
                        .provincialRequestPayload(
                                ProvincialPatientConsentCriteria.builder()
                                        .firstName("firstName")
                                        .lastName("lastName")
                                        .effectiveDateTime("effectiveDateTime")
                                        .patientIdentifier("")
                                        .build())
                        .build();
        ProvincialPatientConsentRequest patientConsentRequest =
                ProvincialPatientConsentRequest.builder()
                        .provincialRequestPayload(
                                ProvincialPatientConsentCriteria.builder()
                                        .firstName("firstName")
                                        .lastName("lastName")
                                        .effectiveDateTime("effectiveDateTime")
                                        .patientIdentifier(null)
                                        .build())
                        .build();
        final OperationOutcome provincialResponseAcknowledgementBlank =
                droolsEngineService.processRequest(provincialPatientConsentRequest);
        final OperationOutcome provincialResponseAcknowledgementNull =
                droolsEngineService.processRequest(patientConsentRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
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
                                                .details("Patient Details")
                                                .build()))
                        .build();
        assertThat(provincialResponseAcknowledgementBlank).isEqualTo(expectedAcknowledgement);
        assertThat(provincialResponseAcknowledgementNull).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenFirstNameAndLastNameIsNullOrBlank_ThenResponseAcknowledgementIsFailure() {
        ProvincialPatientConsentRequest provincialPatientConsentRequest =
                ProvincialPatientConsentRequest.builder()
                        .provincialRequestPayload(
                                ProvincialPatientConsentCriteria.builder()
                                        .firstName("")
                                        .lastName("")
                                        .effectiveDateTime("effectiveDateTime")
                                        .patientIdentifier("patientIdentifier")
                                        .build())
                        .build();
        ProvincialPatientConsentRequest patientConsentRequest =
                ProvincialPatientConsentRequest.builder()
                        .provincialRequestPayload(
                                ProvincialPatientConsentCriteria.builder()
                                        .firstName(null)
                                        .lastName(null)
                                        .effectiveDateTime("effectiveDateTime")
                                        .patientIdentifier("patientIdentifier")
                                        .build())
                        .build();
        final OperationOutcome provincialResponseAcknowledgementBlank =
                droolsEngineService.processRequest(provincialPatientConsentRequest);
        final OperationOutcome provincialResponseAcknowledgementNull =
                droolsEngineService.processRequest(patientConsentRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
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
                                                .details("Patient Details")
                                                .build()))
                        .build();
        assertThat(provincialResponseAcknowledgementBlank).isEqualTo(expectedAcknowledgement);
        assertThat(provincialResponseAcknowledgementNull).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void
    whenFirstNameAndEffectiveDateTimeIsNullOrBlank_ThenResponseAcknowledgementIsFailure() {
        ProvincialPatientConsentRequest provincialPatientConsentRequest =
                ProvincialPatientConsentRequest.builder()
                        .provincialRequestPayload(
                                ProvincialPatientConsentCriteria.builder()
                                        .firstName("")
                                        .lastName("lastName")
                                        .effectiveDateTime("")
                                        .patientIdentifier("patientIdentifier")
                                        .build())
                        .build();
        ProvincialPatientConsentRequest patientConsentRequest =
                ProvincialPatientConsentRequest.builder()
                        .provincialRequestPayload(
                                ProvincialPatientConsentCriteria.builder()
                                        .firstName(null)
                                        .lastName("lastName")
                                        .effectiveDateTime(null)
                                        .patientIdentifier("patientIdentifier")
                                        .build())
                        .build();
        final OperationOutcome provincialResponseAcknowledgementBlank =
                droolsEngineService.processRequest(provincialPatientConsentRequest);
        final OperationOutcome provincialResponseAcknowledgementNull =
                droolsEngineService.processRequest(patientConsentRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
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
                                                .details("Patient Details")
                                                .build()))
                        .build();
        assertThat(provincialResponseAcknowledgementBlank).isEqualTo(expectedAcknowledgement);
        assertThat(provincialResponseAcknowledgementNull).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void
    whenFirstNameAndPatientIdentifierIsNullOrBlank_ThenResponseAcknowledgementIsFailure() {
        ProvincialPatientConsentRequest provincialPatientConsentRequest =
                ProvincialPatientConsentRequest.builder()
                        .provincialRequestPayload(
                                ProvincialPatientConsentCriteria.builder()
                                        .firstName("")
                                        .lastName("lastName")
                                        .effectiveDateTime("effectiveDateTime")
                                        .patientIdentifier("")
                                        .build())
                        .build();
        ProvincialPatientConsentRequest patientConsentRequest =
                ProvincialPatientConsentRequest.builder()
                        .provincialRequestPayload(
                                ProvincialPatientConsentCriteria.builder()
                                        .firstName(null)
                                        .lastName("lastName")
                                        .effectiveDateTime("effectiveDateTime")
                                        .patientIdentifier(null)
                                        .build())
                        .build();
        final OperationOutcome provincialResponseAcknowledgementBlank =
                droolsEngineService.processRequest(provincialPatientConsentRequest);
        final OperationOutcome provincialResponseAcknowledgementNull =
                droolsEngineService.processRequest(patientConsentRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
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
                                                .details("Patient Details")
                                                .build()))
                        .build();
        assertThat(provincialResponseAcknowledgementBlank).isEqualTo(expectedAcknowledgement);
        assertThat(provincialResponseAcknowledgementNull).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenLastNameAndEffectiveDateTimeIsNullOrBlank_ThenResponseAcknowledgementIsFailure() {
        ProvincialPatientConsentRequest provincialPatientConsentRequest =
                ProvincialPatientConsentRequest.builder()
                        .provincialRequestPayload(
                                ProvincialPatientConsentCriteria.builder()
                                        .firstName("firstName")
                                        .lastName("")
                                        .effectiveDateTime("")
                                        .patientIdentifier("patientIdentifier")
                                        .build())
                        .build();
        ProvincialPatientConsentRequest patientConsentRequest =
                ProvincialPatientConsentRequest.builder()
                        .provincialRequestPayload(
                                ProvincialPatientConsentCriteria.builder()
                                        .firstName("firstName")
                                        .lastName(null)
                                        .effectiveDateTime(null)
                                        .patientIdentifier("patientIdentifier")
                                        .build())
                        .build();
        final OperationOutcome provincialResponseAcknowledgementBlank =
                droolsEngineService.processRequest(provincialPatientConsentRequest);
        final OperationOutcome provincialResponseAcknowledgementNull =
                droolsEngineService.processRequest(patientConsentRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
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
                                                .details("Patient Details")
                                                .build()))
                        .build();
        assertThat(provincialResponseAcknowledgementBlank).isEqualTo(expectedAcknowledgement);
        assertThat(provincialResponseAcknowledgementNull).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenLastNameAndPatientIdentifierIsNullOrBlank_ThenResponseAcknowledgementIsFailure() {
        ProvincialPatientConsentRequest provincialPatientConsentRequest =
                ProvincialPatientConsentRequest.builder()
                        .provincialRequestPayload(
                                ProvincialPatientConsentCriteria.builder()
                                        .firstName("firstName")
                                        .lastName("")
                                        .effectiveDateTime("effectiveDateTime")
                                        .patientIdentifier("")
                                        .build())
                        .build();
        ProvincialPatientConsentRequest patientConsentRequest =
                ProvincialPatientConsentRequest.builder()
                        .provincialRequestPayload(
                                ProvincialPatientConsentCriteria.builder()
                                        .firstName("firstName")
                                        .lastName(null)
                                        .effectiveDateTime("effectiveDateTime")
                                        .patientIdentifier(null)
                                        .build())
                        .build();
        final OperationOutcome provincialResponseAcknowledgementBlank =
                droolsEngineService.processRequest(provincialPatientConsentRequest);
        final OperationOutcome provincialResponseAcknowledgementNull =
                droolsEngineService.processRequest(patientConsentRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
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
                                                .details("Patient Details")
                                                .build()))
                        .build();
        assertThat(provincialResponseAcknowledgementBlank).isEqualTo(expectedAcknowledgement);
        assertThat(provincialResponseAcknowledgementNull).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void
    whenEffectiveDateTimeAndPatientIdentifierIsNullOrBlank_ThenResponseAcknowledgementIsFailure() {
        ProvincialPatientConsentRequest provincialPatientConsentRequest =
                ProvincialPatientConsentRequest.builder()
                        .provincialRequestPayload(
                                ProvincialPatientConsentCriteria.builder()
                                        .firstName("firstName")
                                        .lastName("lastName")
                                        .effectiveDateTime("")
                                        .patientIdentifier("")
                                        .build())
                        .build();
        ProvincialPatientConsentRequest patientConsentRequest =
                ProvincialPatientConsentRequest.builder()
                        .provincialRequestPayload(
                                ProvincialPatientConsentCriteria.builder()
                                        .firstName("firstName")
                                        .lastName("lastName")
                                        .effectiveDateTime(null)
                                        .patientIdentifier(null)
                                        .build())
                        .build();
        final OperationOutcome provincialResponseAcknowledgementBlank =
                droolsEngineService.processRequest(provincialPatientConsentRequest);
        final OperationOutcome provincialResponseAcknowledgementNull =
                droolsEngineService.processRequest(patientConsentRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
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
                                                .details("Patient Details")
                                                .build()))
                        .build();
        assertThat(provincialResponseAcknowledgementBlank).isEqualTo(expectedAcknowledgement);
        assertThat(provincialResponseAcknowledgementNull).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenPatientConsentCriteriaIsPresent_ThenResponseAcknowledgementIsSuccess() {
        ProvincialPatientConsentRequest provincialPatientConsentRequest =
                ProvincialPatientConsentRequest.builder()
                        .provincialRequestPayload(
                                ProvincialPatientConsentCriteria.builder()
                                        .firstName("firstName")
                                        .lastName("lastName")
                                        .effectiveDateTime("effectiveDateTime")
                                        .patientIdentifier("patientIdentifier")
                                        .build())
                        .build();

        final OperationOutcome provincialResponseAcknowledgementBlank =
                droolsEngineService.processRequest(provincialPatientConsentRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.ACCEPT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(new ArrayList<>())
                        .build();
        assertThat(provincialResponseAcknowledgementBlank)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }
}
