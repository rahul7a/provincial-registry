package com.lblw.vphx.phms.rules.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.lblw.vphx.phms.common.coding.services.CodeableConceptRefDataService;
import com.lblw.vphx.phms.common.coding.services.CodeableConceptService;
import com.lblw.vphx.phms.common.config.MongoConfig;
import com.lblw.vphx.phms.domain.coding.Coding;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.rules.config.RulesEngineConfig;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
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
@ActiveProfiles("info")
class CodeableConceptValidationServiceTest {
    @MockBean
    CodeableConceptService codeableConceptService;

    @Autowired
    private CodeableConceptValidationService codeableConceptValidationService;

    @Test
    void should_return_false_when_location_type_is_null() {
        assertThat(codeableConceptValidationService.isSystemLocationTypeValid(Province.QC, null))
                .isFalse();
    }

    @Test
    void should_return_false_when_location_type_is_empty() {
        assertThat(codeableConceptValidationService.isSystemLocationTypeValid(Province.QC, " "))
                .isFalse();
    }

    @Test
    void should_return_false_when_location_type_is_unknown() {
        assertThat(
                codeableConceptValidationService.isSystemLocationTypeValid(Province.QC, "invalidtype"))
                .isFalse();
    }

    @Test
    void should_return_false_when_location_type_is_valid() {
        when(codeableConceptService.findProvincialLocationTypeCodingBySystemCode(any(), any()))
                .thenReturn(Optional.of(Coding.builder().build()));

        assertThat(codeableConceptValidationService.isSystemLocationTypeValid(Province.QC, "LAB"))
                .isTrue();
    }

    @Test
    void should_return_false_when_role_speciality_code_is_unknown() {
        assertThat(
                codeableConceptValidationService.isSystemRoleSpecialityCodeValid(
                        Province.QC, "invalidType"))
                .isFalse();
    }

    @Test
    void should_return_true_when_role_speciality_code_is_valid() {
        when(codeableConceptService.findProvincialSpecialityRoleCodingBySystemSpecialityRoleCode(
                any(), any()))
                .thenReturn(Optional.of(Coding.builder().build()));

        assertThat(
                codeableConceptValidationService.isSystemRoleSpecialityCodeValid(
                        Province.QC, "1990000"))
                .isTrue();
    }

    @Test
    void should_return_false_when_role_code_is_unknown() {
        assertThat(codeableConceptValidationService.isSystemRoleCodeValid(Province.QC, "invalidType"))
                .isFalse();
    }

    @Test
    void should_return_true_when_role_code_is_valid() {
        when(codeableConceptService.findProvincialRoleCodingBySystemRoleCode(any(), any()))
                .thenReturn(Optional.of(Coding.builder().build()));

        assertThat(codeableConceptValidationService.isSystemRoleCodeValid(Province.QC, "2060000"))
                .isTrue();
    }
}
