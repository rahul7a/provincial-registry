package com.lblw.vphx.phms.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.lblw.vphx.phms.common.coding.services.CodeableConceptRefDataService;
import com.lblw.vphx.phms.common.coding.services.CodeableConceptService;
import com.lblw.vphx.phms.common.config.MongoConfig;
import com.lblw.vphx.phms.domain.common.Service;
import com.lblw.vphx.phms.domain.common.response.*;
import com.lblw.vphx.phms.domain.common.response.hl7v3.AcknowledgementDetails;
import com.lblw.vphx.phms.domain.common.response.hl7v3.DetectedIssue;
import com.lblw.vphx.phms.domain.common.response.hl7v3.QueryAcknowledgement;
import com.lblw.vphx.phms.domain.common.response.hl7v3.ResponseBodyTransmissionWrapper;
import com.lblw.vphx.phms.domain.location.response.ProvincialLocationSummaries;
import com.lblw.vphx.phms.domain.location.response.hl7v3.ProvincialLocationSearchResponse;
import com.lblw.vphx.phms.rules.config.RulesEngineConfig;
import com.lblw.vphx.phms.rules.service.CodeableConceptValidationService;
import com.lblw.vphx.phms.rules.service.DroolsEngineService;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

/**
 * Unit tests for Provincial Location Search Response
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
class ProvincialLocationSearchResponseTest {
    @MockBean
    CodeableConceptService codeableConceptService;

    @Autowired
    private DroolsEngineService ruleEngineService;

    @Test
    void when_provincialPatientSearchResponseIsNull_ThenResponseAcknowledgementIsFailure() {
        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialLocationSummariesResponse(null);
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
        final ProvincialLocationSearchResponse provincialLocationSearchResponse =
                ProvincialLocationSearchResponse.builder()
                        .provincialResponsePayload(ProvincialLocationSummaries.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AA")
                                        .acknowledgementDetails(
                                                List.of(AcknowledgementDetails.builder().code(null).build()))
                                        .build())
                        .build();

        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialLocationSummariesResponse(
                        provincialLocationSearchResponse);

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
    void WhenInvalidFormatOfCityNamePostalCodeOrFirstLineOfAddress() {
        final ProvincialLocationSearchResponse provincialLocationSearchResponse =
                buildProvincialLocationSearchResponse(
                        "AE",
                        "E",
                        "PRLO_IN202010CAQC_V01/controlActEvent/queryByParameter/parameterList/address/value/postalCode");

        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialLocationSummariesResponse(
                        provincialLocationSearchResponse);

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
                                                .details("LS-008")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    private ProvincialLocationSearchResponse buildProvincialLocationSearchResponse(
            String acknowledgementTypeCode, String typeCode, String location) {
        return ProvincialLocationSearchResponse.builder()
                .provincialResponsePayload(ProvincialLocationSummaries.builder().build())
                .responseBodyTransmissionWrapper(
                        ResponseBodyTransmissionWrapper.builder()
                                .acknowledgeTypeCode(acknowledgementTypeCode)
                                .acknowledgementDetails(
                                        List.of(
                                                AcknowledgementDetails.builder()
                                                        .code("SYN102")
                                                        .typeCode(typeCode)
                                                        .location(location)
                                                        .build()))
                                .build())
                .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("QE").build())
                .build();
    }

    @Test
    void WhenInvalidFormatOfLocationName() {
        final ProvincialLocationSearchResponse provincialLocationSearchResponse =
                ProvincialLocationSearchResponse.builder()
                        .provincialResponsePayload(ProvincialLocationSummaries.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AE")
                                        .acknowledgementDetails(
                                                List.of(
                                                        AcknowledgementDetails.builder()
                                                                .code("SYN102")
                                                                .typeCode("E")
                                                                .location(
                                                                        "PRLO_IN202010CAQC_V01/controlActEvent/queryByParameter/parameterList/nameContains/value")
                                                                .build()))
                                        .build())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("QE").build())
                        .build();

        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialLocationSummariesResponse(
                        provincialLocationSearchResponse);

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
                                                .details("LS-009")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void WhenInvalidFormatTypeOrSecondaryIdentifierOfTheLocationInTheQuery() {
        final ProvincialLocationSearchResponse provincialLocationSearchResponse =
                buildProvincialLocationSearchResponse(
                        "PRLO_IN202010CAQC_V01/controlActEvent/queryByParameter/parameterList/OtherIDs");
        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialLocationSummariesResponse(
                        provincialLocationSearchResponse);

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
                                                .details("LS-010")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void WhenInvalidFormatOfLocationType() {
        final ProvincialLocationSearchResponse build =
                buildProvincialLocationSearchResponse(
                        "PRLO_IN202010CAQC_V01/controlActEvent/queryByParameter/parameterList/recordType/value[@code]");

        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialLocationSummariesResponse(build);

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
                                                .details("LS-011")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void WhenUnknownScenario() {
        final ProvincialLocationSearchResponse provincialLocationSearchResponse =
                ProvincialLocationSearchResponse.builder()
                        .provincialResponsePayload(ProvincialLocationSummaries.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode(null)
                                        .acknowledgementDetails(List.of(AcknowledgementDetails.builder().build()))
                                        .build())
                        .build();

        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialLocationSummariesResponse(
                        provincialLocationSearchResponse);

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

    private ProvincialLocationSearchResponse buildProvincialLocationSearchResponse(String s) {
        return ProvincialLocationSearchResponse.builder()
                .provincialResponsePayload(ProvincialLocationSummaries.builder().build())
                .responseBodyTransmissionWrapper(
                        ResponseBodyTransmissionWrapper.builder()
                                .acknowledgeTypeCode("AE")
                                .acknowledgementDetails(
                                        List.of(
                                                AcknowledgementDetails.builder()
                                                        .code("SYN102")
                                                        .typeCode("E")
                                                        .location(s)
                                                        .build()))
                                .build())
                .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("QE").build())
                .build();
    }

    @Test
    void WhenInvalidFormatOfCreationDate() {
        final ProvincialLocationSearchResponse provincialLocationSearchResponse =
                ProvincialLocationSearchResponse.builder()
                        .provincialResponsePayload(ProvincialLocationSummaries.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AE")
                                        .acknowledgementDetails(
                                                List.of(
                                                        AcknowledgementDetails.builder()
                                                                .code("UNSVAL")
                                                                .typeCode("E")
                                                                .location("PRLO_IN202010CAQC_V01/creationTime[@value]")
                                                                .build()))
                                        .build())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("AE").build())
                        .build();
        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialLocationSummariesResponse(
                        provincialLocationSearchResponse);

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
                                                .details("LS-014")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void WhenInvalidFormatOfResponseModeCode() {
        final ProvincialLocationSearchResponse provincialLocationSearchResponse =
                ProvincialLocationSearchResponse.builder()
                        .provincialResponsePayload(ProvincialLocationSummaries.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AE")
                                        .acknowledgementDetails(
                                                List.of(
                                                        AcknowledgementDetails.builder()
                                                                .code("UNSVAL")
                                                                .typeCode("E")
                                                                .location("PRLO_IN202010CAQC_V01/responseModeCode[@code]")
                                                                .build()))
                                        .build())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("AE").build())
                        .build();
        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialLocationSummariesResponse(
                        provincialLocationSearchResponse);

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
                                                .details("LS-001")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void WhenInvalidFormatOfOID() {
        final ProvincialLocationSearchResponse provincialLocationSearchResponse =
                ProvincialLocationSearchResponse.builder()
                        .provincialResponsePayload(ProvincialLocationSummaries.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AE")
                                        .acknowledgementDetails(
                                                List.of(
                                                        AcknowledgementDetails.builder()
                                                                .code("NS200")
                                                                .typeCode("E")
                                                                .location("PRLO_IN202010CAQC_V01/interactionId")
                                                                .build()))
                                        .build())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("AE").build())
                        .build();
        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialLocationSummariesResponse(
                        provincialLocationSearchResponse);

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
                                                .details("LS-003")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void WhenInvalidFormatOfProcessingModeCode() {
        final ProvincialLocationSearchResponse provincialLocationSearchResponse =
                ProvincialLocationSearchResponse.builder()
                        .provincialResponsePayload(ProvincialLocationSummaries.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AE")
                                        .acknowledgementDetails(
                                                List.of(
                                                        AcknowledgementDetails.builder()
                                                                .code("NS250")
                                                                .typeCode("E")
                                                                .location("PRLO_IN202010CAQC_V01/processingModeCode[@code]")
                                                                .build()))
                                        .build())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("AE").build())
                        .build();
        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialLocationSummariesResponse(
                        provincialLocationSearchResponse);

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
                                                .details("LS-005")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void WhenInvalidFormatOfAcceptAckCode() {
        final ProvincialLocationSearchResponse provincialLocationSearchResponse =
                ProvincialLocationSearchResponse.builder()
                        .provincialResponsePayload(ProvincialLocationSummaries.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AE")
                                        .acknowledgementDetails(
                                                List.of(
                                                        AcknowledgementDetails.builder()
                                                                .code("UNSVAL")
                                                                .typeCode("E")
                                                                .location("PRLO_IN202010CAQC_V01/acceptAckCode[@code]")
                                                                .build()))
                                        .build())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("AE").build())
                        .build();
        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialLocationSummariesResponse(
                        provincialLocationSearchResponse);

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
                                                .details("LS-006")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void WhenReceiverDoesNotMatchTheLocationRegistry() {
        final ProvincialLocationSearchResponse provincialLocationSearchResponse =
                ProvincialLocationSearchResponse.builder()
                        .provincialResponsePayload(ProvincialLocationSummaries.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AE")
                                        .acknowledgementDetails(
                                                List.of(
                                                        AcknowledgementDetails.builder()
                                                                .code("UNSVAL")
                                                                .typeCode("E")
                                                                .location("PRLO_IN202010CAQC_V01/receiver/device/id")
                                                                .build()))
                                        .build())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("AE").build())
                        .build();

        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialLocationSummariesResponse(
                        provincialLocationSearchResponse);

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
                                                .details("LS-007")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void WhenNoDataFound() {
        final ProvincialLocationSearchResponse provincialLocationSearchResponse =
                ProvincialLocationSearchResponse.builder()
                        .provincialResponsePayload(ProvincialLocationSummaries.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder().acknowledgeTypeCode("AA").build())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("NF").build())
                        .detectedIssues(List.of(DetectedIssue.builder().eventCode("NOTFND").build()))
                        .build();

        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialLocationSummariesResponse(
                        provincialLocationSearchResponse);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(
                                List.of(
                                        Issue.builder()
                                                .code("EM.001")
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
                                                .details("")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void WhenNoParametersFound() {
        final ProvincialLocationSearchResponse provincialLocationSearchResponse =
                ProvincialLocationSearchResponse.builder()
                        .provincialResponsePayload(ProvincialLocationSummaries.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder().acknowledgeTypeCode("AA").build())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("QE").build())
                        .detectedIssues(
                                List.of(
                                        DetectedIssue.builder()
                                                .eventCode("VALIDAT")
                                                .eventText("Au moins un paramètre doit être présent.")
                                                .build()))
                        .build();
        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialLocationSummariesResponse(
                        provincialLocationSearchResponse);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(
                                List.of(
                                        Issue.builder()
                                                .code("EM.013")
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
                                                .details("")
                                                .build()))
                        .build();

        Assertions.assertEquals(
                expectedAcknowledgement.getStatus().getCode(),
                obsProvincialResponseAcknowledgement.getStatus().getCode());
        Assertions.assertEquals(
                expectedAcknowledgement.getClassification(),
                obsProvincialResponseAcknowledgement.getClassification());
    }

    @Test
    void WhenTooManyResultsReturned() {
        final ProvincialLocationSearchResponse provincialLocationSearchResponse =
                ProvincialLocationSearchResponse.builder()
                        .provincialResponsePayload(ProvincialLocationSummaries.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder().acknowledgeTypeCode("AA").build())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("AE").build())
                        .detectedIssues(
                                List.of(DetectedIssue.builder()
                                        .eventCode("MAXOCCURS")
                                        .eventText(
                                                "Le nombre d'occurrences permis par HL7 est dépassé. Veuillez raffiner les critères de recherche pour obtenir moins de résultats.")
                                        .build()))
                        .build();
        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialLocationSummariesResponse(
                        provincialLocationSearchResponse);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(
                                List.of(
                                        Issue.builder()
                                                .code("EM.002")
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
                                                .details("")
                                                .build()))
                        .build();
        Assertions.assertEquals(
                expectedAcknowledgement.getStatus().getCode(),
                obsProvincialResponseAcknowledgement.getStatus().getCode());
        Assertions.assertEquals(
                expectedAcknowledgement.getClassification(),
                obsProvincialResponseAcknowledgement.getClassification());
    }

    @Test
    void unforeseenErrorOrApplicationError() {
        final ProvincialLocationSearchResponse provincialLocationSearchResponse =
                ProvincialLocationSearchResponse.builder()
                        .provincialResponsePayload(ProvincialLocationSummaries.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AR")
                                        .acknowledgementDetails(
                                                List.of(
                                                        AcknowledgementDetails.builder().typeCode("E").code("INTERR").build()))
                                        .build())
                        .queryAcknowledgement(QueryAcknowledgement.builder().queryResponseCode("AE").build())
                        .detectedIssues(
                                List.of(DetectedIssue.builder()
                                        .eventText("Erreur d'application, la requête n'a pas été executée")
                                        .build()))
                        .build();
        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProvincialLocationSummariesResponse(
                        provincialLocationSearchResponse);

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
                                                .description("")
                                                .triggeredBy(
                                                        Service.builder()
                                                                .code(ServiceCode.VPHX_PHMS)
                                                                .serviceOutageStatus("")
                                                                .build())
                                                .details("")
                                                .build()))
                        .build();
        Assertions.assertEquals(
                expectedAcknowledgement.getStatus().getCode(),
                obsProvincialResponseAcknowledgement.getStatus().getCode());
        Assertions.assertEquals(
                expectedAcknowledgement.getClassification(),
                obsProvincialResponseAcknowledgement.getClassification());
    }
}
