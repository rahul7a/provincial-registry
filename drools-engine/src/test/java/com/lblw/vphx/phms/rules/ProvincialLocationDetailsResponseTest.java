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
import com.lblw.vphx.phms.domain.location.details.response.ProvincialLocationDetails;
import com.lblw.vphx.phms.domain.location.details.response.hl7v3.ProvincialLocationDetailsResponse;
import com.lblw.vphx.phms.rules.config.RulesEngineConfig;
import com.lblw.vphx.phms.rules.service.CodeableConceptValidationService;
import com.lblw.vphx.phms.rules.service.DroolsEngineService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
                CodeableConceptRefDataService.class,
        })
class ProvincialLocationDetailsResponseTest {
    @MockBean
    CodeableConceptService codeableConceptService;
    @Autowired
    private DroolsEngineService droolsEngineService;

    @Test
    void whenLocationDetailsConsentIsNull_ThenResponseAcknowledgementIsFailure() {
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processProvincialLocationDetailsResponse(null);

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
    void
    whenResponseIsMissingMandatoryParameter_ThenResponseAcknowledgementIsUpdateAccordingly() {
        ProvincialLocationDetailsResponse provincialLocationDetailsResponse =
                buildProvincialLocationDetailsResponse(
                        "AA",
                        "QE",
                        null,
                        null,
                        List.of(
                                DetectedIssue.builder()
                                        .eventCode("MISSMAND")
                                        .eventText("Paramètre obligatoire manquant.")
                                        .build()));

        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processProvincialLocationDetailsResponse(
                        provincialLocationDetailsResponse);

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
                                                .details("LD-013")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenResponseIsDataNotFound_ThenResponseAcknowledgementIsUpdateAccordingly() {
        ProvincialLocationDetailsResponse provincialLocationDetailsResponse =
                buildProvincialLocationDetailsResponse(
                        "AA",
                        "NF",
                        null,
                        null,
                        List.of(
                                DetectedIssue.builder()
                                        .eventCode("KEY204")
                                        .eventText("Aucun LDS ne correspond à ce NIU.")
                                        .build()));

        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processProvincialLocationDetailsResponse(
                        provincialLocationDetailsResponse);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(
                                List.of(
                                        Issue.builder()
                                                .code("EM.108")
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
    void whenInternalErrorInConsentRegistry_ThenResponseAcknowledgementIsUpdateAccordingly() {
        ProvincialLocationDetailsResponse provincialLocationDetailsResponse =
                buildProvincialLocationDetailsResponse(
                        "AR",
                        "AE",
                        "E",
                        "INTERR",
                        List.of(
                                DetectedIssue.builder()
                                        .eventCode(UUID.randomUUID().toString())
                                        .eventText("Erreur d'application, la requête n'a pas été executée")
                                        .build()));

        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processProvincialLocationDetailsResponse(
                        provincialLocationDetailsResponse);

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
                                                .details("LD-013")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement)
                .usingRecursiveComparison()
                .isEqualTo(expectedAcknowledgement);
    }

    /**
     * Builds an instance of {@link ProvincialLocationDetailsResponse} from the given inputs
     *
     * @param acknowledgementTypeCode        acknowledgement Type Code
     * @param queryAckCode                   query Acknowledgement Code
     * @param acknowledgementDetailsTypeCode query acknowledgementDetailsType Code
     * @param acknowledgementDetailsCode     query acknowledgementDetails Code
     * @param detectedIssues                 a list of detectedIssues {@link DetectedIssue}
     * @return Prepared instance of {@link ProvincialLocationDetailsResponse}
     */
    private ProvincialLocationDetailsResponse buildProvincialLocationDetailsResponse(
            String acknowledgementTypeCode,
            String queryAckCode,
            String acknowledgementDetailsTypeCode,
            String acknowledgementDetailsCode,
            List<DetectedIssue> detectedIssues) {

        return ProvincialLocationDetailsResponse.builder()
                .provincialResponsePayload(ProvincialLocationDetails.builder().build())
                .responseBodyTransmissionWrapper(
                        ResponseBodyTransmissionWrapper.builder()
                                .acknowledgeTypeCode(acknowledgementTypeCode)
                                .acknowledgementDetails(
                                        List.of(
                                                AcknowledgementDetails.builder()
                                                        .typeCode(acknowledgementDetailsTypeCode)
                                                        .code(acknowledgementDetailsCode)
                                                        .build()))
                                .build())
                .detectedIssues(detectedIssues)
                .queryAcknowledgement(
                        QueryAcknowledgement.builder().queryResponseCode(queryAckCode).build())
                .build();
    }
}
