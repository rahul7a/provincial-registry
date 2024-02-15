package com.lblw.vphx.phms.rules.service;

import com.lblw.vphx.phms.common.coding.services.CodeableConceptService;
import com.lblw.vphx.phms.domain.common.Province;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Provides specific methods to validate {@link com.lblw.vphx.phms.domain.coding.Coding} used in rule drl files.
 */
@Service
public class CodeableConceptValidationService {
    private final CodeableConceptService codeableConceptService;

    /**
     * @param codeableConceptService {@link CodeableConceptService}
     */
    public CodeableConceptValidationService(CodeableConceptService codeableConceptService) {
        this.codeableConceptService = codeableConceptService;
    }

    /**
     * Validates location type system code by checking if a VocabMapping is present in the lookup
     *
     * @param province   {@link Province}
     * @param systemCode of location type
     * @return true if present in the lookup otherwise false
     */
    public boolean isSystemLocationTypeValid(Province province, String systemCode) {
        return this.codeableConceptService
                .findProvincialLocationTypeCodingBySystemCode(province, StringUtils.trimToNull(systemCode))
                .isPresent();
    }

    /**
     * Validates roleSpecialityCode system code by checking if a VocabMapping is present in the lookup
     *
     * @param province   {@link Province}
     * @param systemCode of roleSpecialityCode
     * @return true if present in the lookup otherwise false
     */
    public boolean isSystemRoleSpecialityCodeValid(Province province, String systemCode) {
        return this.codeableConceptService
                .findProvincialSpecialityRoleCodingBySystemSpecialityRoleCode(
                        province, StringUtils.trimToNull(systemCode))
                .isPresent();
    }

    /**
     * Validates roleCode system code by checking if a VocabMapping is present in the lookup
     *
     * @param province   {@link Province}
     * @param systemCode of roleCode
     * @return true if present in the lookup otherwise false
     */
    public boolean isSystemRoleCodeValid(Province province, String systemCode) {
        return this.codeableConceptService
                .findProvincialRoleCodingBySystemRoleCode(province, StringUtils.trimToNull(systemCode))
                .isPresent();
    }
}
