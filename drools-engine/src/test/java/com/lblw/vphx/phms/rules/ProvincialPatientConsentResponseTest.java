package com.lblw.vphx.phms.rules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.lblw.vphx.phms.common.coding.services.CodeableConceptRefDataService;
import com.lblw.vphx.phms.common.coding.services.CodeableConceptService;
import com.lblw.vphx.phms.common.config.MongoConfig;
import com.lblw.vphx.phms.common.utils.DateUtils;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.Service;
import com.lblw.vphx.phms.domain.common.response.*;
import com.lblw.vphx.phms.domain.common.response.hl7v3.AcknowledgementDetails;
import com.lblw.vphx.phms.domain.common.response.hl7v3.DetectedIssue;
import com.lblw.vphx.phms.domain.common.response.hl7v3.QueryAcknowledgement;
import com.lblw.vphx.phms.domain.common.response.hl7v3.ResponseBodyTransmissionWrapper;
import com.lblw.vphx.phms.domain.patient.consent.response.ProvincialPatientConsent;
import com.lblw.vphx.phms.domain.patient.consent.response.hl7v3.ProvincialPatientConsentResponse;
import com.lblw.vphx.phms.rules.config.DebugLoggingAgendaEventListener;
import com.lblw.vphx.phms.rules.config.RulesEngineConfig;
import com.lblw.vphx.phms.rules.service.CodeableConceptValidationService;
import com.lblw.vphx.phms.rules.service.DroolsEngineService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

/**
 * Unit tests for Provincial Patient Consent Search Response
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
                DebugLoggingAgendaEventListener.class
        })
class ProvincialPatientConsentResponseTest {
    @MockBean
    CodeableConceptService codeableConceptService;

    @Autowired
    private DroolsEngineService ruleEngineService;

    @Test
    void whenProvincialPatientSearchResponseIsNull_ThenResponseAcknowledgementIsFailure() {
        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialPatientConsentResponse(null);

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
    void WhenAcknowledgmentCodeIsAA() {
        final ProvincialPatientConsentResponse provincialPatientConsentResponse =
                ProvincialPatientConsentResponse.builder()
                        .detectedIssues(Collections.emptyList())
                        .provincialResponsePayload(ProvincialPatientConsent.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AA")
                                        .acknowledgementDetails(
                                                List.of(AcknowledgementDetails.builder().code(null).build()))
                                        .build())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("OK").build())
                        .build();

        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialPatientConsentResponse(provincialPatientConsentResponse);

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

    @Test
    void WhenPatientHasPastRefusal() {
        final ProvincialPatientConsentResponse provincialPatientConsentResponse =
                ProvincialPatientConsentResponse.builder()
                        .detectedIssues(Collections.emptyList())
                        .provincialResponsePayload(
                                ProvincialPatientConsent.builder()
                                        .consentValidityStartDateTime(
                                                DateUtils.parseProvincialDateTimeFormatToInstantDateTime(
                                                        Province.QC, "20200101050001"))
                                        .build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AA")
                                        .acknowledgementDetails(
                                                List.of(AcknowledgementDetails.builder().code(null).build()))
                                        .build())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("OK").build())
                        .build();

        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialPatientConsentResponse(provincialPatientConsentResponse);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(
                                ResponseStatus.builder().code(Status.ACCEPT_WARNING).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(
                                List.of(
                                        Issue.builder()
                                                .code("EM.105")
                                                .classification(ResponseClassification.SYSTEM)
                                                .priority(
                                                        IssuePriority.builder()
                                                                .code(Priority.WARNING)
                                                                .text(Strings.EMPTY)
                                                                .build())
                                                .severity(
                                                        IssueSeverity.builder()
                                                                .code(Severity.MEDIUM)
                                                                .text(Strings.EMPTY)
                                                                .build())
                                                .source(
                                                        IssueSource.builder().code(Source.INTERNAL).text(Strings.EMPTY).build())
                                                .description("")
                                                .triggeredBy(
                                                        Service.builder()
                                                                .code(ServiceCode.VPHX_PHMS)
                                                                .serviceOutageStatus("")
                                                                .build())
                                                .details("")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void WhenPatientHasPastRefusalAndAcknowledgementNotAA() {
        final ProvincialPatientConsentResponse provincialPatientConsentResponse =
                ProvincialPatientConsentResponse.builder()
                        .detectedIssues(Collections.emptyList())
                        .provincialResponsePayload(
                                ProvincialPatientConsent.builder()
                                        .consentValidityStartDateTime(
                                                DateUtils.parseProvincialDateTimeFormatToInstantDateTime(
                                                        Province.QC, "20200101050001"))
                                        .build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AE")
                                        .acknowledgementDetails(
                                                List.of(AcknowledgementDetails.builder().code(null).build()))
                                        .build())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("OK").build())
                        .build();

        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialPatientConsentResponse(provincialPatientConsentResponse);

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
                                                .description("")
                                                .triggeredBy(
                                                        Service.builder()
                                                                .code(ServiceCode.VPHX_PHMS)
                                                                .serviceOutageStatus("")
                                                                .build())
                                                .details("ERROR")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void WhenPastRefusalAndUnforeseenErrorHappens() {
        final ProvincialPatientConsentResponse provincialPatientConsentResponse =
                ProvincialPatientConsentResponse.builder()
                        .provincialResponsePayload(
                                ProvincialPatientConsent.builder()
                                        .consentValidityStartDateTime(
                                                DateUtils.parseProvincialDateTimeFormatToInstantDateTime(
                                                        Province.QC, "20200101050001"))
                                        .build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AR")
                                        .acknowledgementDetails(
                                                List.of(
                                                        AcknowledgementDetails.builder().typeCode("E").code("INTERR").build()))
                                        .build())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("AE").build())
                        .build();
        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialPatientConsentResponse(provincialPatientConsentResponse);

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

    @Test
    void WhenPatientHasPastRefusalAndWrongOID() {
        final ProvincialPatientConsentResponse provincialPatientConsentResponse =
                ProvincialPatientConsentResponse.builder()
                        .provincialResponsePayload(
                                ProvincialPatientConsent.builder()
                                        .consentValidityStartDateTime(
                                                DateUtils.parseProvincialDateTimeFormatToInstantDateTime(
                                                        Province.QC, "20200101050001"))
                                        .build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AE")
                                        .acknowledgementDetails(
                                                List.of(
                                                        AcknowledgementDetails.builder()
                                                                .typeCode("E")
                                                                .code("NS200")
                                                                .location("4")
                                                                .build()))
                                        .build())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("AE").build())
                        .build();
        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialPatientConsentResponse(provincialPatientConsentResponse);

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
                                                .details("CT-003")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void WhenPatientHasNoPastRefusal() {
        final ProvincialPatientConsentResponse provincialPatientConsentResponse =
                ProvincialPatientConsentResponse.builder()
                        .detectedIssues(Collections.emptyList())
                        .provincialResponsePayload(
                                ProvincialPatientConsent.builder()
                                        .consentValidityStartDateTime(
                                                DateUtils.parseProvincialDateTimeFormatToInstantDateTime(
                                                        Province.QC, "20070101050000"))
                                        .build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AA")
                                        .acknowledgementDetails(
                                                List.of(AcknowledgementDetails.builder().code(null).build()))
                                        .build())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("OK").build())
                        .build();

        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialPatientConsentResponse(provincialPatientConsentResponse);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.ACCEPT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(List.of())
                        .build();
        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void WhenUnforeseenError() {
        final ProvincialPatientConsentResponse provincialPatientConsentResponse =
                ProvincialPatientConsentResponse.builder()
                        .provincialResponsePayload(ProvincialPatientConsent.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AR")
                                        .acknowledgementDetails(
                                                List.of(
                                                        AcknowledgementDetails.builder().typeCode("E").code("INTERR").build()))
                                        .build())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("AE").build())
                        .build();
        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialPatientConsentResponse(provincialPatientConsentResponse);

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

    @Test
    void defaultError() {
        final ProvincialPatientConsentResponse provincialPatientConsentResponse =
                ProvincialPatientConsentResponse.builder()
                        .provincialResponsePayload(ProvincialPatientConsent.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AA")
                                        .acknowledgementDetails(
                                                List.of(
                                                        AcknowledgementDetails.builder().typeCode("E").code("INTERR").build()))
                                        .build())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("AE").build())
                        .build();
        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialPatientConsentResponse(provincialPatientConsentResponse);

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
                                                .details("ERROR")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void creationDateTimeInvalidOrInTheFuture() {
        final ProvincialPatientConsentResponse provincialPatientConsentResponse =
                ProvincialPatientConsentResponse.builder()
                        .provincialResponsePayload(ProvincialPatientConsent.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AE")
                                        .acknowledgementDetails(
                                                List.of(
                                                        AcknowledgementDetails.builder()
                                                                .typeCode("E")
                                                                .code("UNSVAL")
                                                                .location("1")
                                                                .build()))
                                        .build())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("AE").build())
                        .build();
        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialPatientConsentResponse(provincialPatientConsentResponse);

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
                                                .details("CT-014")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void invalidResponseModeCode() {
        List<AcknowledgementDetails> acknowledgementDetailsList =
                List.of(
                        AcknowledgementDetails.builder().typeCode("E").code("UNSVAL").location("2").build());

        // creation of transmission wrapper
        ResponseBodyTransmissionWrapper responseBodyTransmissionWrapper =
                ResponseBodyTransmissionWrapper.builder()
                        .acknowledgeTypeCode("AE")
                        .acknowledgementDetails(acknowledgementDetailsList)
                        .build();
        QueryAcknowledgement queryAcknowledgement =
                QueryAcknowledgement.builder().queryResponseCode("AE").build();

        // Creation of patient response
        ProvincialPatientConsentResponse provincialPatientConsentResponse =
                ProvincialPatientConsentResponse.builder()
                        .provincialResponsePayload(ProvincialPatientConsent.builder().build())
                        .responseBodyTransmissionWrapper(responseBodyTransmissionWrapper)
                        .queryAcknowledgement(queryAcknowledgement)
                        .build();

        // Service call for evaluate rule engine
        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialPatientConsentResponse(provincialPatientConsentResponse);

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
                                                .details("CT-001")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void invalidHL7Version() {
        final ProvincialPatientConsentResponse provincialPatientConsentResponse =
                ProvincialPatientConsentResponse.builder()
                        .provincialResponsePayload(ProvincialPatientConsent.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AE")
                                        .acknowledgementDetails(
                                                List.of(
                                                        AcknowledgementDetails.builder()
                                                                .typeCode("E")
                                                                .code("NS203")
                                                                .location("3")
                                                                .build()))
                                        .build())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("AE").build())
                        .build();
        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialPatientConsentResponse(provincialPatientConsentResponse);

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
                                                .details("CT-002")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void wrongOID() {
        final ProvincialPatientConsentResponse provincialPatientConsentResponse =
                ProvincialPatientConsentResponse.builder()
                        .provincialResponsePayload(ProvincialPatientConsent.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AE")
                                        .acknowledgementDetails(
                                                List.of(
                                                        AcknowledgementDetails.builder()
                                                                .typeCode("E")
                                                                .code("NS200")
                                                                .location("4")
                                                                .build()))
                                        .build())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("AE").build())
                        .build();
        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialPatientConsentResponse(provincialPatientConsentResponse);

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
                                                .details("CT-003")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void wrongProcessingCode() {
        final ProvincialPatientConsentResponse provincialPatientConsentResponse =
                ProvincialPatientConsentResponse.builder()
                        .provincialResponsePayload(ProvincialPatientConsent.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AE")
                                        .acknowledgementDetails(
                                                List.of(
                                                        AcknowledgementDetails.builder()
                                                                .typeCode("E")
                                                                .code("NS202")
                                                                .location("5")
                                                                .build()))
                                        .build())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("AE").build())
                        .build();
        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialPatientConsentResponse(provincialPatientConsentResponse);

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
                                                .details("CT-004")
                                                .build()))
                        .build();
        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void wrongProcessingModeCode() {
        final ProvincialPatientConsentResponse provincialPatientConsentResponse =
                ProvincialPatientConsentResponse.builder()
                        .provincialResponsePayload(ProvincialPatientConsent.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AE")
                                        .acknowledgementDetails(
                                                List.of(
                                                        AcknowledgementDetails.builder()
                                                                .typeCode("E")
                                                                .code("NS250")
                                                                .location("6")
                                                                .build()))
                                        .build())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("AE").build())
                        .build();
        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialPatientConsentResponse(provincialPatientConsentResponse);

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
                                                .details("CT-005")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void wrongAcceptAckCodeInQueryAck() {
        final ProvincialPatientConsentResponse provincialPatientConsentResponse =
                ProvincialPatientConsentResponse.builder()
                        .provincialResponsePayload(ProvincialPatientConsent.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AE")
                                        .acknowledgementDetails(
                                                List.of(
                                                        AcknowledgementDetails.builder()
                                                                .typeCode("E")
                                                                .code("UNSVAL")
                                                                .location("7")
                                                                .build()))
                                        .build())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("AE").build())
                        .build();

        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialPatientConsentResponse(provincialPatientConsentResponse);

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
                                                .details("CT-006")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void receiverDoesNotMatchTheFCORegistry() {
        final ProvincialPatientConsentResponse provincialPatientConsentResponse =
                ProvincialPatientConsentResponse.builder()
                        .provincialResponsePayload(ProvincialPatientConsent.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AE")
                                        .acknowledgementDetails(
                                                List.of(
                                                        AcknowledgementDetails.builder()
                                                                .typeCode("E")
                                                                .code("UNSVAL")
                                                                .location("8")
                                                                .build()))
                                        .build())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("AE").build())
                        .build();
        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialPatientConsentResponse(provincialPatientConsentResponse);

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
                                                .details("CT-007")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void errorInQueryParameter() {
        final ProvincialPatientConsentResponse provincialPatientConsentResponse =
                ProvincialPatientConsentResponse.builder()
                        .provincialResponsePayload(ProvincialPatientConsent.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AE")
                                        .acknowledgementDetails(
                                                List.of(
                                                        AcknowledgementDetails.builder()
                                                                .typeCode("E")
                                                                .code("SYN102")
                                                                .location("9")
                                                                .build()))
                                        .build())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("QE").build())
                        .build();

        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialPatientConsentResponse(provincialPatientConsentResponse);

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
                                                .details("CT-008")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void moreThan1ReferenceDateProvided() {
        ResponseBodyTransmissionWrapper responseBodyTransmissionWrapper =
                ResponseBodyTransmissionWrapper.builder()
                        .acknowledgeTypeCode("AA")
                        .acknowledgementDetails(List.of(AcknowledgementDetails.builder().code(null).build()))
                        .build();
        QueryAcknowledgement queryAcknowledgement =
                QueryAcknowledgement.builder().queryResponseCode("QE").build();
        List<DetectedIssue> detectedIssues =
                List.of(
                        DetectedIssue.builder()
                                .eventCode("VALIDAT")
                                .eventText("Vous devez fournir une seule date de référence")
                                .build());
        ProvincialPatientConsentResponse provincialPatientConsentResponse =
                ProvincialPatientConsentResponse.builder()
                        .responseBodyTransmissionWrapper(responseBodyTransmissionWrapper)
                        .queryAcknowledgement(queryAcknowledgement)
                        .detectedIssues(detectedIssues)
                        .build();

        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialPatientConsentResponse(provincialPatientConsentResponse);

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
                                                .details("CT-010")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void missingMandatoryParameter() {
        ResponseBodyTransmissionWrapper responseBodyTransmissionWrapper =
                ResponseBodyTransmissionWrapper.builder()
                        .acknowledgeTypeCode("AA")
                        .acknowledgementDetails(List.of(AcknowledgementDetails.builder().code(null).build()))
                        .build();
        QueryAcknowledgement queryAcknowledgement =
                QueryAcknowledgement.builder().queryResponseCode("QE").build();
        List<DetectedIssue> detectedIssues =
                List.of(
                        DetectedIssue.builder()
                                .eventCode("MISSMAND")
                                .eventText("Paramètre obligatoire manquant")
                                .build());
        ProvincialPatientConsentResponse provincialPatientConsentResponse =
                ProvincialPatientConsentResponse.builder()
                        .responseBodyTransmissionWrapper(responseBodyTransmissionWrapper)
                        .queryAcknowledgement(queryAcknowledgement)
                        .detectedIssues(detectedIssues)
                        .build();

        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialPatientConsentResponse(provincialPatientConsentResponse);

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
                                                .details("CT-011")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void referenceDateIsInFuture() {
        ResponseBodyTransmissionWrapper responseBodyTransmissionWrapper =
                ResponseBodyTransmissionWrapper.builder()
                        .acknowledgeTypeCode("AA")
                        .acknowledgementDetails(List.of(AcknowledgementDetails.builder().code(null).build()))
                        .build();
        QueryAcknowledgement queryAcknowledgement =
                QueryAcknowledgement.builder().queryResponseCode("QE").build();
        List<DetectedIssue> detectedIssues =
                List.of(
                        DetectedIssue.builder()
                                .eventCode("VALIDAT")
                                .eventText("Date de référence dans le futur.")
                                .build());
        ProvincialPatientConsentResponse provincialPatientConsentResponse =
                ProvincialPatientConsentResponse.builder()
                        .responseBodyTransmissionWrapper(responseBodyTransmissionWrapper)
                        .queryAcknowledgement(queryAcknowledgement)
                        .detectedIssues(detectedIssues)
                        .build();
        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialPatientConsentResponse(provincialPatientConsentResponse);

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
                                                .details("CT-012")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void typeOfConsentNotValid_AllowedValue_DS() {
        ResponseBodyTransmissionWrapper responseBodyTransmissionWrapper =
                ResponseBodyTransmissionWrapper.builder()
                        .acknowledgeTypeCode("AA")
                        .acknowledgementDetails(List.of(AcknowledgementDetails.builder().code(null).build()))
                        .build();
        QueryAcknowledgement queryAcknowledgement =
                QueryAcknowledgement.builder().queryResponseCode("QE").build();
        List<DetectedIssue> detectedIssues =
                List.of(
                        DetectedIssue.builder()
                                .eventCode("VALIDAT")
                                .eventText("Le type de consentement n'est pas valide. La valeur permise est : DS.")
                                .build());
        ProvincialPatientConsentResponse provincialPatientConsentResponse =
                ProvincialPatientConsentResponse.builder()
                        .responseBodyTransmissionWrapper(responseBodyTransmissionWrapper)
                        .queryAcknowledgement(queryAcknowledgement)
                        .detectedIssues(detectedIssues)
                        .build();
        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialPatientConsentResponse(provincialPatientConsentResponse);

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
                                                .details("CT-013")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void useReferenceDateAndTimeLessThan2007_01_01() {
        ResponseBodyTransmissionWrapper responseBodyTransmissionWrapper =
                ResponseBodyTransmissionWrapper.builder()
                        .acknowledgeTypeCode("AA")
                        .acknowledgementDetails(List.of(AcknowledgementDetails.builder().code(null).build()))
                        .build();
        QueryAcknowledgement queryAcknowledgement =
                QueryAcknowledgement.builder().queryResponseCode("QE").build();
        List<DetectedIssue> detectedIssues =
                List.of(
                        DetectedIssue.builder()
                                .eventCode("VALIDAT")
                                .eventText(
                                        "Vous ne pouvez pas utiliser une date de référence inférieure à 2007-01-01.")
                                .build());
        ProvincialPatientConsentResponse provincialPatientConsentResponse =
                ProvincialPatientConsentResponse.builder()
                        .responseBodyTransmissionWrapper(responseBodyTransmissionWrapper)
                        .queryAcknowledgement(queryAcknowledgement)
                        .detectedIssues(detectedIssues)
                        .build();
        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialPatientConsentResponse(provincialPatientConsentResponse);

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
                                                .details("CT-015")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void usersNIUInTheRequestDoesNotMatchThePatientNIUInContext() {
        ResponseBodyTransmissionWrapper responseBodyTransmissionWrapper =
                ResponseBodyTransmissionWrapper.builder()
                        .acknowledgeTypeCode("AA")
                        .acknowledgementDetails(List.of(AcknowledgementDetails.builder().code(null).build()))
                        .build();
        QueryAcknowledgement queryAcknowledgement =
                QueryAcknowledgement.builder().queryResponseCode("QE").build();
        List<DetectedIssue> detectedIssues =
                List.of(
                        DetectedIssue.builder()
                                .eventCode("NOTFND")
                                .eventText(
                                        "Le NIU de l'usager dans la requête ne correspond pas au NIU du patient en contexte.")
                                .build());
        ProvincialPatientConsentResponse provincialPatientConsentResponse =
                ProvincialPatientConsentResponse.builder()
                        .responseBodyTransmissionWrapper(responseBodyTransmissionWrapper)
                        .queryAcknowledgement(queryAcknowledgement)
                        .detectedIssues(detectedIssues)
                        .build();

        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialPatientConsentResponse(provincialPatientConsentResponse);

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
                                                .details("CT-016")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void refusalDirectiveIsFoundOnTheDateOnWhichValidationOfConsentIsRequested() {
        ResponseBodyTransmissionWrapper responseBodyTransmissionWrapper =
                ResponseBodyTransmissionWrapper.builder()
                        .acknowledgeTypeCode("AA")
                        .acknowledgementDetails(List.of(AcknowledgementDetails.builder().code(null).build()))
                        .build();
        QueryAcknowledgement queryAcknowledgement =
                QueryAcknowledgement.builder().queryResponseCode("NF").build();
        List<DetectedIssue> detectedIssues =
                List.of(
                        DetectedIssue.builder()
                                .eventCode("NOTFND")
                                .eventText(
                                        "Une directive de refus a été trouvée à la date à laquelle la validation du consentement est demandée.")
                                .build(),
                        DetectedIssue.builder().eventCode("DISSENT").build());
        ProvincialPatientConsentResponse provincialPatientConsentResponse =
                ProvincialPatientConsentResponse.builder()
                        .responseBodyTransmissionWrapper(responseBodyTransmissionWrapper)
                        .queryAcknowledgement(queryAcknowledgement)
                        .detectedIssues(detectedIssues)
                        .build();
        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialPatientConsentResponse(provincialPatientConsentResponse);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(
                                List.of(
                                        Issue.builder()
                                                .code("EM.104")
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

    @Test
    void noDataFound() {
        ResponseBodyTransmissionWrapper responseBodyTransmissionWrapper =
                ResponseBodyTransmissionWrapper.builder()
                        .acknowledgeTypeCode("AA")
                        .acknowledgementDetails(List.of(AcknowledgementDetails.builder().code(null).build()))
                        .build();
        QueryAcknowledgement queryAcknowledgement =
                QueryAcknowledgement.builder().queryResponseCode("NF").build();
        List<DetectedIssue> detectedIssues =
                List.of(
                        DetectedIssue.builder().eventCode("NOTFND").eventText("Aucune donnée trouvée").build());
        ProvincialPatientConsentResponse provincialPatientConsentResponse =
                ProvincialPatientConsentResponse.builder()
                        .responseBodyTransmissionWrapper(responseBodyTransmissionWrapper)
                        .queryAcknowledgement(queryAcknowledgement)
                        .detectedIssues(detectedIssues)
                        .build();
        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialPatientConsentResponse(provincialPatientConsentResponse);

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
                                                .details("CT-018")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void genericErrorTheInfoSubmittedIsNoLongerValid() {
        ResponseBodyTransmissionWrapper responseBodyTransmissionWrapper =
                ResponseBodyTransmissionWrapper.builder()
                        .acknowledgeTypeCode("AA")
                        .acknowledgementDetails(List.of(AcknowledgementDetails.builder().code(null).build()))
                        .build();
        QueryAcknowledgement queryAcknowledgement =
                QueryAcknowledgement.builder().queryResponseCode("NF").build();
        List<DetectedIssue> detectedIssues =
                List.of(
                        DetectedIssue.builder()
                                .eventCode("VALIDAT")
                                .eventText(
                                        "Les informations soumises ne sont plus valides car l'entité a fait l'objet d'une fusion ou d'une scission.")
                                .build());
        ProvincialPatientConsentResponse provincialPatientConsentResponse =
                ProvincialPatientConsentResponse.builder()
                        .responseBodyTransmissionWrapper(responseBodyTransmissionWrapper)
                        .queryAcknowledgement(queryAcknowledgement)
                        .detectedIssues(detectedIssues)
                        .build();
        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialPatientConsentResponse(provincialPatientConsentResponse);

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
                                                .details("CT-009")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void userRefusesToParticipate() {
        List<DetectedIssue> detectedIssues =
                List.of(DetectedIssue.builder().eventCode("DISSENT").build());
        ProvincialPatientConsentResponse provincialPatientConsentResponse =
                ProvincialPatientConsentResponse.builder().detectedIssues(detectedIssues).build();
        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialPatientConsentResponse(provincialPatientConsentResponse);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(
                                List.of(
                                        Issue.builder()
                                                .code("EM.104")
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
