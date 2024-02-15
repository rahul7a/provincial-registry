package com.lblw.vphx.phms.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.lblw.vphx.phms.common.coding.services.CodeableConceptRefDataService;
import com.lblw.vphx.phms.common.coding.services.CodeableConceptService;
import com.lblw.vphx.phms.common.config.MongoConfig;
import com.lblw.vphx.phms.domain.common.Service;
import com.lblw.vphx.phms.domain.common.response.*;
import com.lblw.vphx.phms.domain.location.details.request.ProvincialLocationDetailsCriteria;
import com.lblw.vphx.phms.domain.location.details.request.hl7v3.ProvincialLocationDetailsRequest;
import com.lblw.vphx.phms.rules.config.RulesEngineConfig;
import com.lblw.vphx.phms.rules.service.CodeableConceptValidationService;
import com.lblw.vphx.phms.rules.service.DroolsEngineService;
import java.util.List;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Assertions;
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
class ProvincialLocationDetailsRequestTest {
    @MockBean
    CodeableConceptService codeableConceptService;
    @Autowired
    private DroolsEngineService droolsEngineService;

    @Test
    void whenLocationDetailsIsNull_ThenResponseAcknowledgementIsFailure() {
        ProvincialLocationDetailsRequest provincialLocationDetailsRequest =
                ProvincialLocationDetailsRequest.builder()
                        .provincialRequestPayload(
                                ProvincialLocationDetailsCriteria.builder().identifier(null).build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialLocationDetailsRequest);
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
    void whenLocationDetailsIsEmpty_ThenResponseAcknowledgementIsFailure() {
        ProvincialLocationDetailsRequest provincialLocationDetailsRequest =
                ProvincialLocationDetailsRequest.builder()
                        .provincialRequestPayload(
                                ProvincialLocationDetailsCriteria.builder().identifier("").build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialLocationDetailsRequest);
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
    void whenLocationDetailsIsNotEmptyOrNotNull_ThenResponseAcknowledgementIsSuccess() {
        ProvincialLocationDetailsRequest provincialLocationDetailsRequest =
                ProvincialLocationDetailsRequest.builder()
                        .provincialRequestPayload(
                                ProvincialLocationDetailsCriteria.builder().identifier("abcd").build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialLocationDetailsRequest);
        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.ACCEPT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .build();
        Assertions.assertEquals(
                expectedAcknowledgement.getStatus().getCode(),
                obsProvincialResponseAcknowledgement.getStatus().getCode());
    }
}
