package com.lblw.vphx.phms.rules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;

import com.lblw.vphx.phms.common.coding.services.CodeableConceptRefDataService;
import com.lblw.vphx.phms.common.coding.services.CodeableConceptService;
import com.lblw.vphx.phms.common.config.MongoConfig;
import com.lblw.vphx.phms.domain.coding.Coding;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.Service;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.common.response.*;
import com.lblw.vphx.phms.domain.provider.request.ProviderIdentifierType;
import com.lblw.vphx.phms.domain.provider.request.ProvincialProviderSearchCriteria;
import com.lblw.vphx.phms.domain.provider.request.hl7v3.ProvincialProviderSearchRequest;
import com.lblw.vphx.phms.rules.config.RulesEngineConfig;
import com.lblw.vphx.phms.rules.service.CodeableConceptValidationService;
import com.lblw.vphx.phms.rules.service.DroolsEngineService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
class ProvincialProviderSearchRequestTest {
    @MockBean
    CodeableConceptService codeableConceptService;
    @Autowired
    private DroolsEngineService droolsEngineService;


    private static Stream<Arguments> provideTestArguments() {
        return Stream.of(
                Arguments.of(
                        "firstName",
                        "lastName",
                        "roleSpecialityCode",
                        "EM.173",
                        "The code used is not a valid code roleSpecialityCode. Please use a valid code and try again.",
                        ""),
                Arguments.of(
                        "  ",
                        "lastName",
                        "",
                        "EM.173",
                        "The code used is not a valid code roleSpecialityCode. Please use a valid code and try again.",
                        ""),
                Arguments.of(
                        "  ",
                        " ",
                        "",
                        "EM.173",
                        "The code used is not a valid code roleSpecialityCode. Please use a valid code and try again.",
                        ""));
    }

    @Test
    void whenRequestIsNull_ThenResponseAcknowledgementIsFailure() {
        OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(null);

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
    void whenProviderSearchIsNull_ThenResponseAcknowledgementIsFailure() {
        ProvincialProviderSearchRequest provincialProviderSearchRequest =
                ProvincialProviderSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialProviderSearchCriteria.builder()
                                        .providerIdentifierValue(null)
                                        .licensingProvince(Province.QC.name())
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialProviderSearchRequest);

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
                                                .details("Please enter mandatory field: ProviderDetails.")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenFirstNameIsNull_ThenResponseAcknowledgementIsFailure() {
        ProvincialProviderSearchRequest provincialProviderSearchRequest =
                ProvincialProviderSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialProviderSearchCriteria.builder()
                                        .firstName(null)
                                        .licensingProvince(Province.QC.toString())
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialProviderSearchRequest);

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
                                                .details("Please enter mandatory field: ProviderDetails.")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenLastNameIsNull_ThenResponseAcknowledgementIsFailure() {
        ProvincialProviderSearchRequest provincialProviderSearchRequest =
                ProvincialProviderSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialProviderSearchCriteria.builder().lastName(null).build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialProviderSearchRequest);

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
                                                .details("Please enter mandatory field: ProviderDetails.")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenFirstName_LastName_PPN_IsNull_ThenResponseAcknowledgementIsFailure() {
        ProvincialProviderSearchRequest provincialProviderSearchRequest =
                ProvincialProviderSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialProviderSearchCriteria.builder()
                                        .firstName(null)
                                        .lastName(null)
                                        .providerIdentifierValue(null)
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialProviderSearchRequest);

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
                                                .details("Please enter mandatory field: ProviderDetails.")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenPPNisEmptyWithFirstNameAndLastName_ThenResponseAcknowledgementIsSuccess() {
        ProvincialProviderSearchRequest provincialProviderSearchRequest =
                ProvincialProviderSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialProviderSearchCriteria.builder()
                                        .firstName("firstName")
                                        .lastName("lastName")
                                        .licensingProvince(Province.QC.toString())
                                        .providerIdentifierType(ProviderIdentifierType.LICENSE)
                                        .providerIdentifierValue("providerIdentifierValue")
                                        .roleCode("2060000")
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialProviderSearchRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.ACCEPT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(List.of())
                        .build();
        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    // when Firstname is empty
    @Test
    void whenFirstnameIsEmptyWithIdentifierAndIdentifierType_ThenResponseAcknowledgementIsSuccess() {
        ProvincialProviderSearchRequest provincialProviderSearchRequest =
                ProvincialProviderSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialProviderSearchCriteria.builder()
                                        .firstName(" ")
                                        .lastName("lastName")
                                        .licensingProvince(Province.QC.toString())
                                        .providerIdentifierValue("providerIdentifierValue")
                                        .providerIdentifierType(ProviderIdentifierType.LICENSE)
                                        .roleCode("2060000")
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialProviderSearchRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.ACCEPT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(List.of())
                        .build();
        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    // when lastname is empty
    @Test
    void whenLastnameIsEmptyWithIdentifierAndIdentifierType_ThenResponseAcknowledgementIsSuccess() {
        ProvincialProviderSearchRequest provincialProviderSearchRequest =
                ProvincialProviderSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialProviderSearchCriteria.builder()
                                        .firstName("firstName")
                                        .lastName(" ")
                                        .licensingProvince(Province.QC.toString())
                                        .providerIdentifierValue("providerIdentifierValue")
                                        .providerIdentifierType(ProviderIdentifierType.LICENSE)
                                        .roleCode("2060000")
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialProviderSearchRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.ACCEPT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(List.of())
                        .build();
        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenFirstName_LastName_PPN_IsEmpty_ThenResponseAcknowledgementIsFailure() {
        ProvincialProviderSearchRequest provincialProviderSearchRequest =
                ProvincialProviderSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialProviderSearchCriteria.builder()
                                        .firstName("firstName")
                                        .licensingProvince(Province.QC.toString())
                                        .providerIdentifierValue("providerIdentifierValue")
                                        .roleCode("2060000")
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialProviderSearchRequest);

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
                                                .details(
                                                        "Please enter mandatory field: ProviderIdentifierType or ProviderIdentifierValue.")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenFirstNameAndLastNameIsReceived_ThenResponseAcknowledgementIsSuccess() {
        ProvincialProviderSearchRequest provincialProviderSearchRequest =
                ProvincialProviderSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialProviderSearchCriteria.builder()
                                        .firstName("firstName")
                                        .lastName("lastName")
                                        .licensingProvince(Province.QC.toString())
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialProviderSearchRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.ACCEPT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(List.of())
                        .build();
        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenFirstNameIsEmpty_ThenResponseAcknowledgementIsFailure() {
        ProvincialProviderSearchRequest provincialProviderSearchRequest =
                ProvincialProviderSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialProviderSearchCriteria.builder()
                                        .firstName(" ")
                                        .lastName("lastName")
                                        .licensingProvince(Province.QC.toString())
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialProviderSearchRequest);

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
                                                .details("Please enter mandatory field: FirstName or LastName.")
                                                .description("")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenIdentifierTypeIsNotReceived_ThenResponseAcknowledgementIsFailure() {
        ProvincialProviderSearchRequest provincialProviderSearchRequest =
                ProvincialProviderSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialProviderSearchCriteria.builder()
                                        .licensingProvince(Province.QC.toString())
                                        .providerIdentifierValue("providerIdentifierValue")
                                        .roleCode("2060000")
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialProviderSearchRequest);

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
                                                .details(
                                                        "Please enter mandatory field: ProviderIdentifierType or ProviderIdentifierValue.")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    @ParameterizedTest
    @MethodSource("provideTestArguments")
    void whenInvalidRoleSpecialityCodeIsReceived_ThenResponseAcknowledgementIsError(
            String firstName,
            String lastName,
            String roleSpecialityCode,
            String code,
            String details,
            String description) {
        ProvincialProviderSearchRequest provincialProviderSearchRequest =
                ProvincialProviderSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialProviderSearchCriteria.builder()
                                        .provincialRequestControl(
                                                ProvincialRequestControl.builder().province(Province.QC).build())
                                        .firstName(firstName)
                                        .lastName(lastName)
                                        .licensingProvince(Province.QC.toString())
                                        .roleSpecialityCode(roleSpecialityCode)
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialProviderSearchRequest);

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
                                                .details(details)
                                                .description(description)
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenInvalidRoleCodeIsReceived_ThenResponseAcknowledgementIsError() {
        ProvincialProviderSearchRequest provincialProviderSearchRequest =
                ProvincialProviderSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialProviderSearchCriteria.builder()
                                        .provincialRequestControl(
                                                ProvincialRequestControl.builder().province(Province.QC).build())
                                        .firstName("firstName")
                                        .lastName("lastName")
                                        .licensingProvince(Province.QC.toString())
                                        .roleCode("roleCode")
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialProviderSearchRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
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
                                                .triggeredBy(
                                                        Service.builder()
                                                                .code(ServiceCode.VPHX_PHMS)
                                                                .serviceOutageStatus("")
                                                                .build())
                                                .description("")
                                                .details(
                                                        "The code used is not a valid code roleCode. Please use a valid code and try again.")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenValidRoleCodeIsReceived_ThenResponseAcknowledgementIsSuccess() {
        ProvincialProviderSearchRequest provincialProviderSearchRequest =
                ProvincialProviderSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialProviderSearchCriteria.builder()
                                        .provincialRequestControl(
                                                ProvincialRequestControl.builder().province(Province.QC).build())
                                        .firstName("firstName")
                                        .lastName("lastName")
                                        .licensingProvince(Province.QC.toString())
                                        .providerIdentifierType(ProviderIdentifierType.LICENSE)
                                        .providerIdentifierValue("1234")
                                        .roleCode("2060000")
                                        .build())
                        .build();
        when(codeableConceptService.findProvincialRoleCodingBySystemRoleCode(any(), any()))
                .thenReturn(Optional.of(Coding.builder().build()));
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialProviderSearchRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.ACCEPT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(List.of())
                        .build();

        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenFirstNameLastNameAndRoleCodeIsReceived_ThenResponseAcknowledgementIsSuccess() {
        ProvincialProviderSearchRequest provincialProviderSearchRequest =
                ProvincialProviderSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialProviderSearchCriteria.builder()
                                        .firstName("firstName")
                                        .lastName("lastName")
                                        .licensingProvince(Province.QC.toString())
                                        .roleCode("roleCode")
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialProviderSearchRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.ACCEPT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(List.of())
                        .build();
        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenIdentifierValueAndRoleCodeIsReceived_ThenResponseAcknowledgementIsSuccess() {
        ProvincialProviderSearchRequest provincialProviderSearchRequest =
                ProvincialProviderSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialProviderSearchCriteria.builder()
                                        .licensingProvince(Province.QC.toString())
                                        .providerIdentifierType(ProviderIdentifierType.LICENSE)
                                        .providerIdentifierValue("value")
                                        .roleCode("roleCode")
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialProviderSearchRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.ACCEPT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(List.of())
                        .build();
        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void
    whenFirstNameLastNameIdentifierValueAndRoleCodeIsReceived_ThenResponseAcknowledgementIsSuccess() {
        ProvincialProviderSearchRequest provincialProviderSearchRequest =
                ProvincialProviderSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialProviderSearchCriteria.builder()
                                        .firstName("firstName")
                                        .lastName("lastName")
                                        .licensingProvince(Province.QC.toString())
                                        .providerIdentifierType(ProviderIdentifierType.LICENSE)
                                        .providerIdentifierValue("value")
                                        .roleCode("roleCode")
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialProviderSearchRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.ACCEPT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(List.of())
                        .build();
        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void
    whenFirstNameLastNameIdentifierValueAndRoleCodeWithBillingIsReceived_ThenResponseAcknowledgementIsSuccess() {
        ProvincialProviderSearchRequest provincialProviderSearchRequest =
                ProvincialProviderSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialProviderSearchCriteria.builder()
                                        .firstName("firstName")
                                        .lastName("lastName")
                                        .licensingProvince(Province.QC.toString())
                                        .providerIdentifierType(ProviderIdentifierType.BILLING)
                                        .providerIdentifierValue("value")
                                        .roleCode("roleCode")
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialProviderSearchRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.ACCEPT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(List.of())
                        .build();
        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenIdentifierValueAndRoleCodeWithBillingIsReceived_ThenResponseAcknowledgementIsSuccess() {
        ProvincialProviderSearchRequest provincialProviderSearchRequest =
                ProvincialProviderSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialProviderSearchCriteria.builder()
                                        .licensingProvince(Province.QC.toString())
                                        .providerIdentifierType(ProviderIdentifierType.BILLING)
                                        .providerIdentifierValue("value")
                                        .roleCode("roleCode")
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialProviderSearchRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.ACCEPT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(List.of())
                        .build();
        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenEmptyRoleCodeIsReceived_ThenResponseAcknowledgementIsFailure() {
        ProvincialProviderSearchRequest provincialProviderSearchRequest =
                ProvincialProviderSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialProviderSearchCriteria.builder()
                                        .provincialRequestControl(
                                                ProvincialRequestControl.builder().province(Province.QC).build())
                                        .firstName("firstName")
                                        .lastName("lastName")
                                        .licensingProvince(Province.QC.toString())
                                        .providerIdentifierType(ProviderIdentifierType.LICENSE)
                                        .providerIdentifierValue("1234")
                                        .roleCode("")
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialProviderSearchRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(
                                List.of(
                                        Issue.builder()
                                                .code("EM.227")
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
                                                        "Please select the valid Prescriber Type associated with the License Number and try again.")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenEmptyRoleSpecialityCodeIsReceived_ThenResponseAcknowledgementIsFailure() {
        ProvincialProviderSearchRequest provincialProviderSearchRequest =
                ProvincialProviderSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialProviderSearchCriteria.builder()
                                        .provincialRequestControl(
                                                ProvincialRequestControl.builder().province(Province.QC).build())
                                        .firstName("firstName")
                                        .lastName("lastName")
                                        .licensingProvince(Province.QC.toString())
                                        .providerIdentifierType(ProviderIdentifierType.LICENSE)
                                        .providerIdentifierValue("1234")
                                        .roleSpecialityCode("")
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialProviderSearchRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(
                                List.of(
                                        Issue.builder()
                                                .code("EM.227")
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
                                                        "Please select the valid Prescriber Type associated with the License Number and try again.")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenEmptyproviderIdentifierValueIsReceived_ThenResponseAcknowledgementIsFailure() {
        ProvincialProviderSearchRequest provincialProviderSearchRequest =
                ProvincialProviderSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialProviderSearchCriteria.builder()
                                        .provincialRequestControl(
                                                ProvincialRequestControl.builder().province(Province.QC).build())
                                        .firstName("firstName")
                                        .lastName("lastName")
                                        .licensingProvince(Province.QC.toString())
                                        .providerIdentifierType(ProviderIdentifierType.LICENSE)
                                        .providerIdentifierValue("")
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialProviderSearchRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(
                                List.of(
                                        Issue.builder()
                                                .code("EM.227")
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
                                                        "Please select the valid Prescriber Type associated with the License Number and try again.")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenProviderIdentifierValueIsProvidedButNotRoleCode_ThenResponseAcknowledgementIsFailure() {
        ProvincialProviderSearchRequest provincialProviderSearchRequest =
                ProvincialProviderSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialProviderSearchCriteria.builder()
                                        .provincialRequestControl(
                                                ProvincialRequestControl.builder().province(Province.QC).build())
                                        .firstName("firstName")
                                        .lastName("lastName")
                                        .licensingProvince(Province.QC.toString())
                                        .providerIdentifierType(ProviderIdentifierType.LICENSE)
                                        .providerIdentifierValue("providerIdentifierValue")
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialProviderSearchRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(
                                List.of(
                                        Issue.builder()
                                                .code("EM.227")
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
                                                        "Please select the valid Prescriber Type associated with the License Number and try again.")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenProviderIdentifierTypeIsLicenseProviderIdentifierValueRoleCodeIsMissing() {
        ProvincialProviderSearchRequest provincialProviderSearchRequest =
                ProvincialProviderSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialProviderSearchCriteria.builder()
                                        .provincialRequestControl(
                                                ProvincialRequestControl.builder().province(Province.QC).build())
                                        .licensingProvince(Province.QC.toString())
                                        .providerIdentifierType(ProviderIdentifierType.LICENSE)
                                        .roleCode("2060000")
                                        .build())
                        .build();
        when(codeableConceptService.findProvincialRoleCodingBySystemRoleCode(any(), any()))
                .thenReturn(Optional.of(Coding.builder().build()));

        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialProviderSearchRequest);

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
                                                .details(
                                                        "Please enter mandatory field: ProviderIdentifierType or ProviderIdentifierValue.")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    // 22
    @Test
    void whenLicensingProvinceIsNotQC_ThenResponseAcknowledgementIsError() {
        ProvincialProviderSearchRequest provincialProviderSearchRequest =
                ProvincialProviderSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialProviderSearchCriteria.builder()
                                        .provincialRequestControl(
                                                ProvincialRequestControl.builder().province(Province.QC).build())
                                        .firstName("Guy")
                                        .lastName("Dumais")
                                        .licensingProvince(Province.AB.toString())
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialProviderSearchRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(
                                List.of(
                                        Issue.builder()
                                                .code("EM.229")
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
                                                .details("Please select Licensing Province as 'QC' for provincial search.")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    // 21,23
    @Test
    void whenLicensingProvinceIsEmpty_ThenResponseAcknowledgementIsError() {
        ProvincialProviderSearchRequest provincialProviderSearchRequest =
                ProvincialProviderSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialProviderSearchCriteria.builder()
                                        .provincialRequestControl(
                                                ProvincialRequestControl.builder().province(Province.QC).build())
                                        .firstName("Guy")
                                        .lastName("Dumais")
                                        .licensingProvince(" ")
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialProviderSearchRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(
                                List.of(
                                        Issue.builder()
                                                .code("EM.229")
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
                                                .details("Please select Licensing Province as 'QC' for provincial search.")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void
    LastNameAndLicensingProvinceIsProvided_AndFirstNameIsEmpty_ThenResponseAcknowledgementIsError() {
        ProvincialProviderSearchRequest provincialProviderSearchRequest =
                ProvincialProviderSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialProviderSearchCriteria.builder()
                                        .provincialRequestControl(
                                                ProvincialRequestControl.builder().province(Province.QC).build())
                                        .firstName(" ")
                                        .lastName("Dumais")
                                        .licensingProvince(Province.QC.toString())
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialProviderSearchRequest);

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
                                                .details("Please enter mandatory field: FirstName or LastName.")
                                                .build()))
                        .build();

        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenFirstNameLastNameLiscensingProvince_ThenResponseAcknowledgementIsSuccess() {
        ProvincialProviderSearchRequest provincialProviderSearchRequest =
                ProvincialProviderSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialProviderSearchCriteria.builder()
                                        .provincialRequestControl(
                                                ProvincialRequestControl.builder().province(Province.QC).build())
                                        .firstName("Guy")
                                        .lastName("Dumais")
                                        .licensingProvince(Province.QC.toString())
                                        .providerIdentifierType(ProviderIdentifierType.LICENSE)
                                        .providerIdentifierValue("89100")
                                        .roleCode("1940000")
                                        .build())
                        .build();
        when(codeableConceptService.findProvincialLocationTypeCodingBySystemCode(any(), any()))
                .thenReturn(Optional.of(Coding.builder().build()));

        when(codeableConceptService.findProvincialSpecialityRoleCodingBySystemSpecialityRoleCode(
                any(), any()))
                .thenReturn(Optional.of(Coding.builder().build()));
        when(codeableConceptService.findProvincialRoleCodingBySystemRoleCode(any(), any()))
                .thenReturn(Optional.of(Coding.builder().build()));

        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialProviderSearchRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.ACCEPT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(List.of())
                        .build();
        assertThat(obsProvincialResponseAcknowledgement).isEqualTo(expectedAcknowledgement);
    }

    @Test
    void whenLicensingProvinceAndRoleCodeIsNull_ThenResponseAcknowledgementIsError() {
        ProvincialProviderSearchRequest provincialProviderSearchRequest =
                ProvincialProviderSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialProviderSearchCriteria.builder()
                                        .provincialRequestControl(
                                                ProvincialRequestControl.builder().province(Province.QC).build())
                                        .firstName("Guy")
                                        .lastName("Dumais")
                                        .providerIdentifierType(ProviderIdentifierType.LICENSE)
                                        .providerIdentifierValue("321134")
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialProviderSearchRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(
                                List.of(
                                        Issue.builder()
                                                .code("EM.229")
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
                                                .details("Please select Licensing Province as 'QC' for provincial search.")
                                                .build()))
                        .build();

        assertEquals("Licensing Province is not 'QC'", expectedAcknowledgement, obsProvincialResponseAcknowledgement);
    }

    @Test
    void
    whenLicensingProvinceAndRoleCodeAndProviderIdentifierValueIsNull_ThenResponseAcknowledgementIsError() {
        ProvincialProviderSearchRequest provincialProviderSearchRequest =
                ProvincialProviderSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialProviderSearchCriteria.builder()
                                        .provincialRequestControl(
                                                ProvincialRequestControl.builder().province(Province.QC).build())
                                        .firstName("Guy")
                                        .lastName("Dumais")
                                        .providerIdentifierType(ProviderIdentifierType.LICENSE)
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialProviderSearchRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(
                                List.of(
                                        Issue.builder()
                                                .code("EM.229")
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
                                                .details("Please select Licensing Province as 'QC' for provincial search.")
                                                .build()))
                        .build();

        assertEquals("Licensing Province is not 'QC'", expectedAcknowledgement, obsProvincialResponseAcknowledgement);
    }

    @Test
    void
    whenLicensingProvinceAndRoleCodeAndProviderIdentifierTypeIsNull_ThenResponseAcknowledgementIsError() {
        ProvincialProviderSearchRequest provincialProviderSearchRequest =
                ProvincialProviderSearchRequest.builder()
                        .provincialRequestPayload(
                                ProvincialProviderSearchCriteria.builder()
                                        .provincialRequestControl(
                                                ProvincialRequestControl.builder().province(Province.QC).build())
                                        .firstName("Guy")
                                        .lastName("Dumais")
                                        .providerIdentifierValue("321134")
                                        .build())
                        .build();
        final OperationOutcome obsProvincialResponseAcknowledgement =
                droolsEngineService.processRequest(provincialProviderSearchRequest);

        OperationOutcome expectedAcknowledgement =
                OperationOutcome.builder()
                        .status(ResponseStatus.builder().code(Status.REJECT).text(Strings.EMPTY).build())
                        .classification(ResponseClassification.SYSTEM)
                        .issues(
                                List.of(
                                        Issue.builder()
                                                .code("EM.229")
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
                                                .details("Please select Licensing Province as 'QC' for provincial search.")
                                                .build()))
                        .build();

        assertEquals("Licensing Province is not 'QC'", expectedAcknowledgement, obsProvincialResponseAcknowledgement);
    }
}
