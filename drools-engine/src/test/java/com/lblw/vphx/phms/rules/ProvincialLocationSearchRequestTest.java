package com.lblw.vphx.phms.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.lblw.vphx.phms.common.coding.services.CodeableConceptRefDataService;
import com.lblw.vphx.phms.common.coding.services.CodeableConceptService;
import com.lblw.vphx.phms.common.config.MongoConfig;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.Service;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.common.response.*;
import com.lblw.vphx.phms.domain.location.request.ProvincialLocationSearchCriteria;
import com.lblw.vphx.phms.domain.location.request.hl7v3.ProvincialLocationSearchRequest;
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
class ProvincialLocationSearchRequestTest {
    @MockBean
    CodeableConceptService codeableConceptService;
    @Autowired
    private DroolsEngineService droolsEngineService;

    @Test
    void whenLocationDetailsIsNull_ThenResponseAcknowledgementIsFailure() {
        ProvincialLocationSearchRequest provincialLocationSearchRequest =
                ProvincialLocationSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialLocationSearchCriteria.builder().locationName(null).build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialLocationSearchRequest);
        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .classification(ResponseClassification.SYSTEM)
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
                        .issues(
                                List.of(
                                        Issue.builder()
                                                .code("EM.112")
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
                                                .classification(ResponseClassification.SYSTEM)
                                                .details("Location Details")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenLocationDetailsIsEmpty_ThenResponseAcknowledgementIsFailure() {
        ProvincialLocationSearchRequest provincialLocationSearchRequest =
                ProvincialLocationSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialLocationSearchCriteria.builder().locationName(" ").build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialLocationSearchRequest);
        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .classification(ResponseClassification.SYSTEM)
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
                                                .description("")
                                                .triggeredBy(
                                                        Service.builder()
                                                                .code(ServiceCode.VPHX_PHMS)
                                                                .serviceOutageStatus("")
                                                                .build())
                                                .details("Location Details")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenLocationNameIsReceived_ThenResponseAcknowledgementIsSuccess() {
        ProvincialLocationSearchRequest provincialLocationSearchRequest =
                ProvincialLocationSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialLocationSearchCriteria.builder().locationName("locationName").build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialLocationSearchRequest);
        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .classification(ResponseClassification.SYSTEM)
                        .status(ResponseStatus.builder().code(Status.ACCEPT).text(Strings.EMPTY).build())
                        .issues(new ArrayList<>())
                        .build();

        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenInvalidLocationTypeIsReceived_ThenResponseAcknowledgementIsError() {
        ProvincialLocationSearchRequest provincialLocationSearchRequest =
                ProvincialLocationSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialLocationSearchCriteria.builder()
                                        .provincialRequestControl(
                                                ProvincialRequestControl.builder().province(Province.QC).build())
                                        .locationType("LAB-dummy")
                                        .locationName("locationName")
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialLocationSearchRequest);
        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .classification(ResponseClassification.SYSTEM)
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
                        .issues(
                                List.of(
                                        Issue.builder()
                                                .code("EM.173")
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
                                                .details(
                                                        "The code used is not a valid code: LAB-dummy. Please use a valid code and try again.")
                                                .build()))
                        .build();
        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenValidLocationTypeIsReceived_ThenResponseAcknowledgementIsSuccess() {
        ProvincialLocationSearchRequest provincialLocationSearchRequest =
                ProvincialLocationSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialLocationSearchCriteria.builder()
                                        .locationType("LAB")
                                        .locationName("locationName")
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialLocationSearchRequest);
        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .classification(ResponseClassification.SYSTEM)
                        .status(ResponseStatus.builder().code(Status.ACCEPT).text(Strings.EMPTY).build())
                        .issues(new ArrayList<>())
                        .build();
        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void
    whenInvalidLocationTypeAndLocationNameIsEmptyIsReceived_ThenResponseAcknowledgementIsError() {
        ProvincialLocationSearchRequest provincialLocationSearchRequest =
                ProvincialLocationSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialLocationSearchCriteria.builder()
                                        .provincialRequestControl(
                                                ProvincialRequestControl.builder().province(Province.QC).build())
                                        .locationType("LAB-dummy")
                                        .locationName(" ")
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialLocationSearchRequest);
        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .classification(ResponseClassification.SYSTEM)
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
                                                .description("")
                                                .triggeredBy(
                                                        Service.builder()
                                                                .code(ServiceCode.VPHX_PHMS)
                                                                .serviceOutageStatus("")
                                                                .build())
                                                .details("Location Details")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }
}
