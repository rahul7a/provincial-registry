package com.lblw.vphx.phms.rules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.lblw.vphx.phms.common.coding.services.CodeableConceptRefDataService;
import com.lblw.vphx.phms.common.coding.services.CodeableConceptService;
import com.lblw.vphx.phms.common.config.MongoConfig;
import com.lblw.vphx.phms.domain.common.Service;
import com.lblw.vphx.phms.domain.common.response.*;
import com.lblw.vphx.phms.domain.common.response.hl7v3.AcknowledgementDetails;
import com.lblw.vphx.phms.domain.common.response.hl7v3.ResponseBodyTransmissionWrapper;
import com.lblw.vphx.phms.domain.provider.response.ProvincialProviderProfiles;
import com.lblw.vphx.phms.domain.provider.response.hl7v3.ProvincialProviderSearchResponse;
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

/**
 * Unit tests for Provincial Provider Search Response
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
class ProvincialProviderSearchResponseTest {
    @MockBean
    CodeableConceptService codeableConceptService;
    @Autowired
    private DroolsEngineService ruleEngineService;

    @Test
    void whenProcessProviderResponseIsNull_ThenResponseAcknowledgementIsFailure() {
        OperationOutcome obsProvincialResponseAcknowledgement =

                ruleEngineService.processProviderResponse(null);

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
        final ProvincialProviderSearchResponse provincialProviderSearchResponse =
                ProvincialProviderSearchResponse.builder()
                        .provincialResponsePayload(ProvincialProviderProfiles.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AA")
                                        .acknowledgementDetails(
                                                List.of(AcknowledgementDetails.builder().code(null).build()))
                                        .build())
                        .build();
        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProviderResponse(provincialProviderSearchResponse);

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
    void WhenMissingOrWrongJurisdictionCode() {
        final ProvincialProviderSearchResponse provincialProviderSearchResponse =
                ProvincialProviderSearchResponse.builder()
                        .provincialResponsePayload(ProvincialProviderProfiles.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AA")
                                        .acknowledgementDetails(
                                                List.of(
                                                        AcknowledgementDetails.builder()
                                                                .typeCode("W")
                                                                .code("PRS.PRV.QRY.PRM.1.0.2201")
                                                                .build()))
                                        .build())
                        .build();

        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProviderResponse(provincialProviderSearchResponse);

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
                                                .details("PR-001")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void WhenCreationDateTimeInvalid() {
        final ProvincialProviderSearchResponse build =
                ProvincialProviderSearchResponse.builder()
                        .provincialResponsePayload(ProvincialProviderProfiles.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AA")
                                        .acknowledgementDetails(
                                                List.of(
                                                        AcknowledgementDetails.builder()
                                                                .typeCode("W")
                                                                .code("GRS.PRV.UNK.UNK.1.0.9998")
                                                                .build()))
                                        .build())
                        .build();

        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProviderResponse(build);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(
                                List.of(
                                        Issue.builder()
                                                .code("EM.009")
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
    void WhenWrongDateOfBirthFormat() {
        final ProvincialProviderSearchResponse provincialProviderSearchResponse =
                ProvincialProviderSearchResponse.builder()
                        .provincialResponsePayload(ProvincialProviderProfiles.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AA")
                                        .acknowledgementDetails(
                                                List.of(
                                                        AcknowledgementDetails.builder()
                                                                .typeCode("E")
                                                                .code("PRS.UNK.UNK.UNK.1.0.2202")
                                                                .build()))
                                        .build())
                        .build();
        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProviderResponse(provincialProviderSearchResponse);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(
                                List.of(
                                        Issue.builder()
                                                .code("EM.005")
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
    void WhenNoProviderRecordsFound() {
        final ProvincialProviderSearchResponse provincialProviderSearchResponse =
                ProvincialProviderSearchResponse.builder()
                        .provincialResponsePayload(ProvincialProviderProfiles.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AA")
                                        .acknowledgementDetails(
                                                List.of(
                                                        AcknowledgementDetails.builder()
                                                                .typeCode("W")
                                                                .code("PRS.PRV.QRY.UNK.1.0.2101")
                                                                .build()))
                                        .build())
                        .build();

        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProviderResponse(provincialProviderSearchResponse);

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
    void WhenTooManyProviderRecordsFound() {
        final ProvincialProviderSearchResponse build =
                ProvincialProviderSearchResponse.builder()
                        .provincialResponsePayload(ProvincialProviderProfiles.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AA")
                                        .acknowledgementDetails(
                                                List.of(
                                                        AcknowledgementDetails.builder()
                                                                .typeCode("W")
                                                                .code("PRS.PRP.UNK.UNK.1.1.2303")
                                                                .build()))
                                        .build())
                        .build();
        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProviderResponse(build);

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
    void WhenInternalErrorInProviderRegistry() {
        final ProvincialProviderSearchResponse provincialProviderSearchResponse =
                ProvincialProviderSearchResponse.builder()
                        .provincialResponsePayload(ProvincialProviderProfiles.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AA")
                                        .acknowledgementDetails(
                                                List.of(
                                                        AcknowledgementDetails.builder()
                                                                .typeCode("E")
                                                                .code("GRS.PRV.UNK.UNK.1.0.9995")
                                                                .build()))
                                        .build())
                        .build();

        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProviderResponse(provincialProviderSearchResponse);

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
                                                .details("PR-002")
                                                .build()))
                        .build();
        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void providerDefaultErrorResponse() {
        final ProvincialProviderSearchResponse provincialProviderSearchResponse =
                ProvincialProviderSearchResponse.builder()
                        .provincialResponsePayload(ProvincialProviderProfiles.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AE")
                                        .acknowledgementDetails(
                                                List.of(
                                                        AcknowledgementDetails.builder().typeCode("E").code("2302").build()))
                                        .build())
                        .build();

        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProviderResponse(provincialProviderSearchResponse);

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
    void providerDefaultErrorResponse2() {
        final ProvincialProviderSearchResponse provincialProviderSearchResponse =
                ProvincialProviderSearchResponse.builder()
                        .provincialResponsePayload(ProvincialProviderProfiles.builder().build())
                        .responseBodyTransmissionWrapper(
                                ResponseBodyTransmissionWrapper.builder()
                                        .acknowledgeTypeCode("AA")
                                        .acknowledgementDetails(
                                                List.of(
                                                        AcknowledgementDetails.builder().typeCode("E").code("2302").build()))
                                        .build())
                        .build();

        OperationOutcome obsProvincialResponseAcknowledgement =
                ruleEngineService.processProviderResponse(provincialProviderSearchResponse);

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
}
