package com.lblw.vphx.phms.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.lblw.vphx.phms.common.coding.services.CodeableConceptRefDataService;
import com.lblw.vphx.phms.common.coding.services.CodeableConceptService;
import com.lblw.vphx.phms.common.config.MongoConfig;
import com.lblw.vphx.phms.domain.common.Gender;
import com.lblw.vphx.phms.domain.common.Service;
import com.lblw.vphx.phms.domain.common.response.*;
import com.lblw.vphx.phms.domain.patient.request.ProvincialPatientSearchCriteria;
import com.lblw.vphx.phms.domain.patient.request.hl7v3.ProvincialPatientSearchRequest;
import com.lblw.vphx.phms.rules.config.RulesEngineConfig;
import com.lblw.vphx.phms.rules.service.CodeableConceptValidationService;
import com.lblw.vphx.phms.rules.service.DroolsEngineService;
import java.time.LocalDate;
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
                CodeableConceptRefDataService.class,
                CodeableConceptService.class
        })
class ProvincialPatientSearchRequestTest {
    @MockBean
    CodeableConceptService codeableConceptService;
    @Autowired
    private DroolsEngineService droolsEngineService;

    @Test
    void whenPHNIsNull_ThenResponseAcknowledgementIsFailure() {
        ProvincialPatientSearchRequest provincialPatientSearchRequest =
                ProvincialPatientSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialPatientSearchCriteria.builder().provincialHealthNumber(null).build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialPatientSearchRequest);

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
    void whenFirstNameIsNull_ThenResponseAcknowledgementIsFailure() {
        ProvincialPatientSearchRequest provincialPatientSearchRequest =
                ProvincialPatientSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialPatientSearchCriteria.builder().firstName(null).build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialPatientSearchRequest);

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
    void whenLastNameIsNull_ThenResponseAcknowledgementIsFailure() {
        ProvincialPatientSearchRequest provincialPatientSearchRequest =
                ProvincialPatientSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialPatientSearchCriteria.builder().lastName(null).build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialPatientSearchRequest);

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
    void whenFirstName_LastName_PHN_IsNull_ThenResponseAcknowledgementIsFailure() {
        ProvincialPatientSearchRequest provincialPatientSearchRequest =
                ProvincialPatientSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialPatientSearchCriteria.builder()
                                        .firstName(null)
                                        .lastName(null)
                                        .provincialHealthNumber(null)
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialPatientSearchRequest);

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
    void whenFirstName_LastName_PHN_IsEmpty_ThenResponseAcknowledgementIsFailure() {
        ProvincialPatientSearchRequest provincialPatientSearchRequest =
                ProvincialPatientSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialPatientSearchCriteria.builder()
                                        .firstName("")
                                        .lastName("")
                                        .provincialHealthNumber("")
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialPatientSearchRequest);

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
    void whenPatientSearchIsNotEmptyOrNotNull_ThenResponseAcknowledgementIsSuccess() {
        ProvincialPatientSearchRequest provincialPatientSearchRequest =
                ProvincialPatientSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialPatientSearchCriteria.builder()
                                        .firstName("firstName")
                                        .lastName("lastName")
                                        .provincialHealthNumber("provincialHealthNumber")
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialPatientSearchRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.ACCEPT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(new ArrayList<>())
                        .build();
        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenOtherPatientDetailsAreNullOrEmpty_ThenResponseAcknowledgementIsFailure() {
        ProvincialPatientSearchRequest provincialPatientSearchRequest =
                ProvincialPatientSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialPatientSearchCriteria.builder()
                                        .firstName("firstName")
                                        .lastName("")
                                        .dateOfBirth(null)
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialPatientSearchRequest);

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
    void whenOtherPatientDetailsAreNull_ThenResponseAcknowledgementIsFailure() {
        ProvincialPatientSearchRequest provincialPatientSearchRequest =
                ProvincialPatientSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialPatientSearchCriteria.builder()
                                        .firstName("firstName")
                                        .lastName("lastName")
                                        .dateOfBirth(null)
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialPatientSearchRequest);

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
    void whenOtherPatientDetailsAreEmpty_ThenResponseAcknowledgementIsFailure() {
        ProvincialPatientSearchRequest provincialPatientSearchRequest =
                ProvincialPatientSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialPatientSearchCriteria.builder()
                                        .firstName("firstName")
                                        .lastName("lastName")
                                        .dateOfBirth(null)
                                        .gender(Gender.F)
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialPatientSearchRequest);

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
    void whenOtherPatientDetailsAreReceived_ThenResponseAcknowledgementIsSuccess() {
        ProvincialPatientSearchRequest provincialPatientSearchRequest =
                ProvincialPatientSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialPatientSearchCriteria.builder()
                                        .firstName("firstName")
                                        .lastName("lastName")
                                        .dateOfBirth(LocalDate.of(1940, 1, 1))
                                        .gender(Gender.F)
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialPatientSearchRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.ACCEPT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(new ArrayList<>())
                        .build();
        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }
}
