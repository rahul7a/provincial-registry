package com.lblw.vphx.phms.rules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.lblw.vphx.phms.common.coding.services.CodeableConceptRefDataService;
import com.lblw.vphx.phms.common.coding.services.CodeableConceptService;
import com.lblw.vphx.phms.common.config.MongoConfig;
import com.lblw.vphx.phms.domain.common.Service;
import com.lblw.vphx.phms.domain.common.response.*;
import com.lblw.vphx.phms.domain.common.response.hl7v3.AcknowledgementDetails;
import com.lblw.vphx.phms.domain.common.response.hl7v3.DetectedIssue;
import com.lblw.vphx.phms.domain.common.response.hl7v3.QueryAcknowledgement;
import com.lblw.vphx.phms.domain.common.response.hl7v3.ResponseBodyTransmissionWrapper;
import com.lblw.vphx.phms.domain.patient.response.ProvincialPatientProfile;
import com.lblw.vphx.phms.domain.patient.response.hl7v3.ProvincialPatientSearchResponse;
import com.lblw.vphx.phms.rules.config.RulesEngineConfig;
import com.lblw.vphx.phms.rules.service.CodeableConceptValidationService;
import com.lblw.vphx.phms.rules.service.DroolsEngineService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

/**
 * Unit tests for Provincial Patient Search Response
 */
@DataMongoTest
@ContextConfiguration(
        classes = {
                RulesEngineConfig.class,
                DroolsEngineService.class,
                MongoConfig.class,
                CodeableConceptValidationService.class,
                CodeableConceptService.class,
                CodeableConceptRefDataService.class,
        })
class ProvincialPatientSearchResponseTest {
    @MockBean
    CodeableConceptService codeableConceptService;

    @Autowired
    private DroolsEngineService droolsEngineService;

    private static Stream<Arguments> provideTestArguments() {
        return Stream.of(
                Arguments.of(
                        "AA",
                        "VALIDAT",
                        "La date de naissance doit être plus petite ou égale à la date du jour",
                        "EM.009",
                        ""),
                Arguments.of("AA", "VALIDAT", "Un paramètre d'entrée obligatoire est absent", "EM.003", ""),
                Arguments.of(
                        "AA",
                        "VALIDAT",
                        "Le nom et le prénom du père doivent être renseignés si l'un ou l'autre est saisi",
                        "EM.003",
                        ""),
                Arguments.of(
                        "AA",
                        "VALIDAT",
                        "Le champ {0} est invalide ou ne respecte pas le format requis",
                        "EM.075",
                        "CR-010"),
                Arguments.of("AA", null, null, "EM.075", "ERROR"),
                Arguments.of(
                        "AA",
                        "VALIDAT",
                        "Le code de pays {0} n'est pas présent dans l'entité \" Pays \"",
                        "EM.003",
                        ""),
                Arguments.of(
                        "AA",
                        "VALIDAT",
                        "Le code de subdivision de pays {0} n'est pas présent dans l'entité \" Subdivision de pays \"",
                        "EM.003",
                        ""),
                Arguments.of(
                        "AA",
                        "VALIDAT",
                        "Le code de subdivision de pays ne correspond pas au code de pays associé",
                        "EM.003",
                        ""),
                Arguments.of(
                        "AA",
                        "VALIDAT",
                        "Le code de subdivision de pays et/ou le code postal ne devraient pas être présents car le code de pays correspondant n'est ni celui du Canada ni celui des États-Unis",
                        "EM.003",
                        ""),
                Arguments.of(
                        "AA",
                        "VALIDAT",
                        "Le code de subdivision de pays et/ou le code postal ne devraient pas être présents car le code de pays est absent",
                        "EM.003",
                        ""),
                Arguments.of(
                        "AA",
                        "VALIDAT",
                        "Les valeurs présentes du domaine de valeur {0} ne sont pas conformes",
                        "EM.075",
                        "CR-011"));
    }

    private static Stream<Arguments> providePatientTestArguments() {
        return Stream.of(
                Arguments.of(90, "AA", null, "OK", Status.ACCEPT, "ECARTMIN"),
                Arguments.of(90, "AA", null, "OK", Status.ACCEPT, "ECARTMAJ"));
    }

    private static Stream<Arguments> providePatientWrongTestArguments() {
        return Stream.of(
                Arguments.of("AA", "MAXOCCURS", "AE", "EM.002", ""),
                Arguments.of("AA", "VALIDAT", "NF", "EM.009", ""),
                Arguments.of("AA", "NOTFND", "NF", "EM.001", ""),
                Arguments.of("AA", "MISSMAND", "QE", "EM.003", ""));
    }

    private static Stream<Arguments> providePatientDataTestArguments() {
        return Stream.of(
                Arguments.of(
                        "AE", "UNSVAL", "PRPA_IN101103CA/acceptAckCode[@code]", "AE", "EM.075", "CR-007"),
                Arguments.of(
                        "AE", "UNSVAL", "PRPA_IN101103CA/creationTime[@value]", "AE", "EM.075", "CR-001"),
                Arguments.of(
                        "AE", "UNSVAL", "PRPA_IN101103CA/responseModeCode[@code]", "AE", "EM.075", "CR-002"),
                Arguments.of(
                        "AE", "UNSVAL", "PRPA_IN101103CA/receiver/device/id", "AE", "EM.075", "CR-008"));
    }

    @Test
    void when_provincialPatientSearchResponseIsNull_ThenResponseAcknowledgementIsFailure() {
        OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processPatientResponse(null);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(new ArrayList<>())
                        .build();
        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void WhenQueryResponseCodeIsOkWithMatchingIndexGreaterThan10() {
        final ProvincialPatientSearchResponse provincialPatientSearchResponse =
                ProvincialPatientSearchResponse.builder()
                        .provincialResponsePayload(ProvincialPatientProfile.builder().matchingIndex(90).build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AA")
                                        .acknowledgementDetails(
                                                List.of(AcknowledgementDetails.builder().code(null).build()))
                                        .build())
                        .detectedIssues(Collections.emptyList())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("OK").build())
                        .build();
        OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processPatientResponse(provincialPatientSearchResponse);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.ACCEPT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .build();
        assertEquals(
                obsProvincialResponseAcknowledgement.getStatus(), expectedAcknowledgement.getStatus());
        assertEquals(
                obsProvincialResponseAcknowledgement.getClassification(),
                expectedAcknowledgement.getClassification());
    }

    @ParameterizedTest
    @MethodSource("providePatientTestArguments")
    void WhenDetectedIssueCodeIsDifferent(
            int matchingIndex,
            String acknowledgeTypeCode,
            String code,
            String queryResponseCode,
            Status status,
            String eventCode) {
        final ProvincialPatientSearchResponse build =
                ProvincialPatientSearchResponse.builder()
                        .provincialResponsePayload(
                                ProvincialPatientProfile.builder().matchingIndex(matchingIndex).build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode(acknowledgeTypeCode)
                                        .acknowledgementDetails(
                                                List.of(AcknowledgementDetails.builder().code(code).build()))
                                        .build())
                        .detectedIssues(List.of(DetectedIssue.builder().eventCode(eventCode).build()))
                        .queryAcknowledgement(
                                QueryAcknowledgement.builder().queryResponseCode(queryResponseCode).build())
                        .build();

        OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processPatientResponse(build);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(status).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .build();
        assertEquals(
                obsProvincialResponseAcknowledgement.getStatus(), expectedAcknowledgement.getStatus());
        assertEquals(
                obsProvincialResponseAcknowledgement.getClassification(),
                expectedAcknowledgement.getClassification());
    }

    @ParameterizedTest
    @MethodSource("provideTestArguments")
    void WhenMissingMandatorySearchParameter(
            String acknowledgeTypeCode, String eventCode, String eventText, String code, String details) {

        final ProvincialPatientSearchResponse build =
                ProvincialPatientSearchResponse.builder()
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode(acknowledgeTypeCode)
                                        .acknowledgementDetails(
                                                List.of(AcknowledgementDetails.builder().code(null).build()))
                                        .build())
                        .detectedIssues(
                                List.of(DetectedIssue.builder().eventCode(eventCode).eventText(eventText).build()))
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode(null).build())
                        .build();

        OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processPatientResponse(build);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(
                                List.of(
                                        Issue.builder()
                                                .code(code)
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
                                                .details(details)
                                                .build()))
                        .build();
        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @ParameterizedTest
    @MethodSource("providePatientWrongTestArguments")
    void WhenMultipleRecordsFound(
            String acknowledgeTypeCode,
            String eventCode,
            String queryResponseCode,
            String code,
            String details) {
        final ProvincialPatientSearchResponse provincialPatientSearchResponse =
                ProvincialPatientSearchResponse.builder()
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode(acknowledgeTypeCode)
                                        .acknowledgementDetails(
                                                List.of(AcknowledgementDetails.builder().code(null).build()))
                                        .build())
                        .detectedIssues(List.of(DetectedIssue.builder().eventCode(eventCode).build()))
                        .queryAcknowledgement(
                                QueryAcknowledgement.builder().queryResponseCode(queryResponseCode).build())
                        .build();

        OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processPatientResponse(provincialPatientSearchResponse);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(
                                List.of(
                                        Issue.builder()
                                                .code(code)
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
                                                .details(details)
                                                .build()))
                        .build();
        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @ParameterizedTest
    @MethodSource("providePatientDataTestArguments")
    void WhenWrongAcceptAckCode(
            String acknowledgeTypeCode,
            String code,
            String location,
            String queryResponseCode,
            String issueCode,
            String details) {
        final ProvincialPatientSearchResponse provincialPatientSearchResponse =
                ProvincialPatientSearchResponse.builder()
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode(acknowledgeTypeCode)
                                        .acknowledgementDetails(
                                                List.of(
                                                        AcknowledgementDetails.builder().code(code).location(location).build()))
                                        .build())
                        .detectedIssues(Collections.emptyList())
                        .queryAcknowledgement(
                                QueryAcknowledgement.builder().queryResponseCode(queryResponseCode).build())
                        .build();

        OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processPatientResponse(provincialPatientSearchResponse);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(
                                List.of(
                                        Issue.builder()
                                                .code(issueCode)
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
                                                .details(details)
                                                .build()))
                        .build();
        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void WhenWrongHL7Version() {
        final ProvincialPatientSearchResponse provincialPatientSearchResponse =
                ProvincialPatientSearchResponse.builder()
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AE")
                                        .acknowledgementDetails(
                                                List.of(AcknowledgementDetails.builder().code("NS203").build()))
                                        .build())
                        .detectedIssues(Collections.emptyList())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("AE").build())
                        .build();

        OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processPatientResponse(provincialPatientSearchResponse);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(
                                List.of(
                                        Issue.builder()
                                                .code("EM.075")
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
                                                .details("CR-003")
                                                .build()))
                        .build();
        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void WhenWrongOID() {
        final ProvincialPatientSearchResponse provincialPatientSearchResponse =
                ProvincialPatientSearchResponse.builder()
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AE")
                                        .acknowledgementDetails(
                                                List.of(AcknowledgementDetails.builder().code("NS200").build()))
                                        .build())
                        .detectedIssues(Collections.emptyList())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("AE").build())
                        .build();

        OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processPatientResponse(provincialPatientSearchResponse);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(
                                List.of(
                                        Issue.builder()
                                                .code("EM.075")
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
                                                .details("CR-004")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void WhenWrongProcessingCodeNS202() {
        final ProvincialPatientSearchResponse provincialPatientSearchResponse =
                ProvincialPatientSearchResponse.builder()
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AE")
                                        .acknowledgementDetails(
                                                List.of(AcknowledgementDetails.builder().code("NS202").build()))
                                        .build())
                        .detectedIssues(Collections.emptyList())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("AE").build())
                        .build();

        OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processPatientResponse(provincialPatientSearchResponse);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(
                                List.of(
                                        Issue.builder()
                                                .code("EM.075")
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
                                                .details("CR-005")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void WhenWrongProcessingCodeNS250() {
        final ProvincialPatientSearchResponse provincialPatientSearchResponse =
                ProvincialPatientSearchResponse.builder()
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AE")
                                        .acknowledgementDetails(
                                                List.of(AcknowledgementDetails.builder().code("NS250").build()))
                                        .build())
                        .detectedIssues(Collections.emptyList())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("AE").build())
                        .build();

        OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processPatientResponse(provincialPatientSearchResponse);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(
                                List.of(
                                        Issue.builder()
                                                .code("EM.075")
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
                                                .details("CR-006")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void WhenFormatError() {
        final ProvincialPatientSearchResponse provincialPatientSearchResponse =
                ProvincialPatientSearchResponse.builder()
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AE")
                                        .acknowledgementDetails(
                                                List.of(AcknowledgementDetails.builder().code("SYN102").build()))
                                        .build())
                        .detectedIssues(Collections.emptyList())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("QE").build())
                        .build();

        OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processPatientResponse(provincialPatientSearchResponse);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(
                                List.of(
                                        Issue.builder()
                                                .code("EM.075")
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
                                                .details("CR-009")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void WhenInternalSystemError() {
        final ProvincialPatientSearchResponse provincialPatientSearchResponse =
                ProvincialPatientSearchResponse.builder()
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AR")
                                        .acknowledgementDetails(
                                                List.of(AcknowledgementDetails.builder().code("INTERR").build()))
                                        .build())
                        .detectedIssues(Collections.emptyList())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("AE").build())
                        .build();

        OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processPatientResponse(provincialPatientSearchResponse);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(
                                List.of(
                                        Issue.builder()
                                                .code("EM.046")
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

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }
}
