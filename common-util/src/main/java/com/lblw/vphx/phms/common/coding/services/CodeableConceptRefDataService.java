package com.lblw.vphx.phms.common.coding.services;

import static com.lblw.vphx.phms.common.constants.HL7Constants.PROVINCIAL_PROVIDER_IDENTIFIER_OUTSIDE_PROVINCE;
import static com.lblw.vphx.phms.common.constants.HL7Constants.PROVINCIAL_PROVIDER_IDENTIFIER_UNRESOLVED;
import static com.lblw.vphx.phms.common.internal.vocab.constants.VocabConstants.*;

import com.lblw.vphx.phms.common.constants.CommonConstants;
import com.lblw.vphx.phms.common.internal.vocab.RdsUtil;
import com.lblw.vphx.phms.common.internal.vocab.VocabService;
import com.lblw.vphx.phms.common.internal.vocab.constants.VocabConstants;
import com.lblw.vphx.phms.domain.coding.*;
import com.lblw.vphx.phms.domain.common.Province;
import java.util.*;
import java.util.function.UnaryOperator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * {@link CodeableConceptRefDataService} is a implementation of CodeableConceptService.
 *
 * <p>It utilizes {@link VocabService} to retrieve relevant coding from Reference Data Service.
 *
 * @since <a href="https://jira.lblw.cloud/browse/LTPHVP-20285">LTPHVP-20285</a>
 */
@Service
public class CodeableConceptRefDataService implements CodeableConceptService {
  private final Map<String, Coding> nullFlavorCodings;
  private final VocabService vocabService;
  private final RdsUtil rdsUtil;

  /**
   * Public Constructor. Initializes nullFlavorCodings map of known nullFlavor codes and respective
   * Coding {@link Coding}
   *
   * @param vocabService {@link VocabService}
   * @param rdsUtil {@link RdsUtil}
   */
  public CodeableConceptRefDataService(VocabService vocabService, RdsUtil rdsUtil) {
    this.vocabService = vocabService;
    this.rdsUtil = rdsUtil;

    final Coding nullFlavorUNKCoding =
        Coding.builder()
            .code(VocabConstants.CODE_UNKNOWN)
            .province(Province.ALL)
            .system(VocabConstants.SYSTEM)
            .display(
                new LocalizedText[] {
                  LocalizedText.builder()
                      .text(VocabConstants.CODE_UNKNOWN_ENG)
                      .language(LanguageCode.ENG)
                      .build(),
                  LocalizedText.builder()
                      .text(VocabConstants.CODE_UNKNOWN_FRE)
                      .language(LanguageCode.FRA)
                      .build()
                })
            .build();
    final Coding nullFlavorNANICoding =
        Coding.builder()
            .code("")
            .province(Province.ALL)
            .system(VocabConstants.SYSTEM)
            .display(
                new LocalizedText[] {
                  LocalizedText.builder().text("").language(LanguageCode.ENG).build(),
                  LocalizedText.builder().text("").language(LanguageCode.FRA).build()
                })
            .build();

    nullFlavorCodings =
        Map.ofEntries(
            Map.entry(VocabConstants.CODE_UNKNOWN, nullFlavorUNKCoding),
            Map.entry(VocabConstants.CODE_NOT_AVALIABLE, nullFlavorNANICoding),
            Map.entry(VocabConstants.CODE_NULL_FLAVOUR, nullFlavorNANICoding));
  }

  @Override
  public Optional<Coding> findProvincialRoleCodingBySystemRoleCode(
      Province province, String systemRoleCode) {
    if (StringUtils.isBlank(systemRoleCode)) {
      return Optional.empty();
    }
    return rdsUtil
        .getVocabByHwCode(
            CodeableConceptType.PROVIDER_ROLE.getRdsVocabKey(), province, systemRoleCode)
        .filter(rdsCoding -> StringUtils.isNotBlank(rdsCoding.getCerxCode()))
        .map(
            rdsCoding ->
                Coding.builder()
                    .code(rdsCoding.getCerxCode())
                    .province(province)
                    .display(
                        new LocalizedText[] {
                          LocalizedText.builder()
                              .text(rdsCoding.getHwDescription())
                              .language(LanguageCode.ENG)
                              .build(),
                          LocalizedText.builder()
                              .text(rdsCoding.getHwDescriptionFrench())
                              .language(LanguageCode.FRA)
                              .build()
                        })
                    .system(VocabConstants.SYSTEM)
                    .build())
        .map(
            roleCoding -> {
              rdsUtil
                  .getVocabByHwCode(
                      CodeableConceptType.PROVIDER_ROLE_OID.getRdsVocabKey(),
                      province,
                      roleCoding.getCode())
                  .ifPresent(
                      roleOIDCoding -> roleCoding.setCodingIdentifier(roleOIDCoding.getCerxCode()));
              return roleCoding;
            });
  }

  @Override
  public Optional<Coding> findSystemRoleCodingByProvincialRoleCode(
      Province province, String provincialRoleCode) {
    if (provincialRoleCode == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(nullFlavorCodings.get(provincialRoleCode))
        .or(
            () ->
                this.vocabService.getSystemCodingByProvincialCode(
                    CodeableConceptType.PROVIDER_ROLE, province, provincialRoleCode))
        .or(
            () ->
                Optional.of(
                    Coding.builder()
                        .code(VocabConstants.UNKNOWN_CODE_ENG_PREFIX + provincialRoleCode)
                        .province(province)
                        .display(mapToUnknownLocalizedText(provincialRoleCode))
                        .build()));
  }

  @Override
  public Optional<Coding> findProvincialSpecialityRoleCodingBySystemSpecialityRoleCode(
      Province province, String systemSpecialityCode) {
    if (systemSpecialityCode == null) {
      return Optional.empty();
    }
    return vocabService.getProvincialCodingBySystemCode(
        CodeableConceptType.PROVIDER_SPECIALITY, province, systemSpecialityCode);
  }

  @Override
  public Optional<Coding> findSystemSpecialityRoleCodingByProvincialSpecialityRoleCode(
      Province province, String provincialSpecialityCode) {
    if (provincialSpecialityCode == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(nullFlavorCodings.get(provincialSpecialityCode))
        .or(
            () ->
                this.vocabService.getSystemCodingByProvincialCode(
                    CodeableConceptType.PROVIDER_SPECIALITY, province, provincialSpecialityCode))
        .or(
            () ->
                Optional.of(
                    Coding.builder()
                        .code(VocabConstants.UNKNOWN_CODE_ENG_PREFIX + provincialSpecialityCode)
                        .province(province)
                        .display(mapToUnknownLocalizedText(provincialSpecialityCode))
                        .build()));
  }

  @Override
  public Optional<Coding> findProvincialLocationTypeCodingBySystemCode(
      Province province, String systemLocationCode) {
    if (systemLocationCode == null) {
      return Optional.empty();
    }
    return vocabService.getProvincialCodingBySystemCode(
        CodeableConceptType.LOCATION, province, systemLocationCode);
  }

  @Override
  public Optional<Coding> findSystemLocationTypeCodingByProvincialCode(
      Province province, String provincialLocationCode) {
    if (provincialLocationCode == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(nullFlavorCodings.get(provincialLocationCode))
        .or(
            () ->
                this.vocabService.getSystemCodingByProvincialCode(
                    CodeableConceptType.LOCATION, province, provincialLocationCode));
  }

  @Override
  public Optional<Coding> findProvincialPrescriptionAdministrationSiteCodingByProvincialCode(
      Province province, String provincialAdministrationSiteCode) {
    if (StringUtils.isBlank(provincialAdministrationSiteCode)) {
      return Optional.empty();
    }

    return Optional.ofNullable(nullFlavorCodings.get(provincialAdministrationSiteCode))
        .or(
            () ->
                this.vocabService.getProvincialCodingByProvincialCode(
                    CodeableConceptType.ADMINISTRATION_SITE,
                    province,
                    provincialAdministrationSiteCode))
        .or(
            () ->
                Optional.of(
                    Coding.builder()
                        .code(provincialAdministrationSiteCode)
                        .province(province)
                        .display(mapToUnknownLocalizedText(provincialAdministrationSiteCode))
                        .build()));
  }

  @Override
  public Optional<Coding> findProvincialPrescriptionRouteOfAdminCodingByProvincialCode(
      Province province, String provincialRouteOfAdminCode) {
    if (StringUtils.isBlank(provincialRouteOfAdminCode)) {
      return Optional.empty();
    }

    return Optional.ofNullable(nullFlavorCodings.get(provincialRouteOfAdminCode))
        .or(
            () ->
                this.vocabService.getProvincialCodingByProvincialCode(
                    CodeableConceptType.ROUTE_OF_ADMINISTRATION,
                    province,
                    provincialRouteOfAdminCode))
        .or(
            () ->
                Optional.of(
                    Coding.builder()
                        .code(provincialRouteOfAdminCode)
                        .province(province)
                        .display(mapToUnknownLocalizedText(provincialRouteOfAdminCode))
                        .build()));
  }

  @Override
  public Optional<Coding> findProvincialPrescriptionIssueSeverityCodingByProvincialCode(
      Province province, String provincialIssueSeverityCode) {
    if (StringUtils.isBlank(provincialIssueSeverityCode)) {
      return Optional.empty();
    }

    return Optional.ofNullable(nullFlavorCodings.get(provincialIssueSeverityCode))
        .or(
            () ->
                this.vocabService.getProvincialCodingByProvincialCode(
                    CodeableConceptType.MANAGED_ISSUE_SEVERITY_CODE,
                    province,
                    provincialIssueSeverityCode))
        .or(
            () ->
                Optional.of(
                    Coding.builder()
                        .code(provincialIssueSeverityCode)
                        .province(province)
                        .display(mapToUnknownLocalizedText(provincialIssueSeverityCode))
                        .build()));
  }

  @Override
  public Optional<Coding> findProvincialPrescriptionIssuePriorityCodingByProvincialCode(
      Province province, String provincialIssuePriorityCode) {
    if (StringUtils.isBlank(provincialIssuePriorityCode)) {
      return Optional.empty();
    }

    return Optional.ofNullable(nullFlavorCodings.get(provincialIssuePriorityCode))
        .or(
            () ->
                this.vocabService.getProvincialCodingByProvincialCode(
                    CodeableConceptType.MANAGED_ISSUE_PRIORITY_CODE,
                    province,
                    provincialIssuePriorityCode))
        .or(
            () ->
                Optional.of(
                    Coding.builder()
                        .code(provincialIssuePriorityCode)
                        .province(province)
                        .display(mapToUnknownLocalizedText(provincialIssuePriorityCode))
                        .build()));
  }

  @Override
  public Optional<Coding> findProvincialPrescriptionPackSizeUoMCodingByProvincialCode(
      Province province, String provincialPackSizeUoMCode) {
    if (StringUtils.isBlank(provincialPackSizeUoMCode)) {
      return Optional.empty();
    }

    return Optional.ofNullable(nullFlavorCodings.get(provincialPackSizeUoMCode))
        .or(
            () ->
                this.vocabService.getProvincialCodingByProvincialCode(
                    CodeableConceptType.PACK_SIZE_UOM, province, provincialPackSizeUoMCode))
        .or(
            () ->
                Optional.of(
                    Coding.builder()
                        .code(provincialPackSizeUoMCode)
                        .province(province)
                        .display(mapToUnknownLocalizedText(provincialPackSizeUoMCode))
                        .build()));
  }

  @Override
  public Optional<Coding> findProvincialPrescriptionDrugFormCodingByProvincialCode(
      Province province, String provincialDrugFormCode) {
    if (StringUtils.isBlank(provincialDrugFormCode)) {
      return Optional.empty();
    }

    return Optional.ofNullable(nullFlavorCodings.get(provincialDrugFormCode))
        .or(
            () ->
                this.vocabService.getProvincialCodingByProvincialCode(
                    CodeableConceptType.DRUG_FORM, province, provincialDrugFormCode))
        .or(
            () ->
                Optional.of(
                    Coding.builder()
                        .code(VocabConstants.UNKNOWN_CODE_ENG_PREFIX.concat(provincialDrugFormCode))
                        .province(province)
                        .display(mapToUnknownLocalizedText(provincialDrugFormCode))
                        .build()));
  }

  @Override
  public Optional<Coding> findProvincialPrescriptionLocationTypeCodingByProvincialCode(
      Province province, String provincialLocationTypeCode, String provincialId) {
    if (StringUtils.isBlank(provincialLocationTypeCode)) {
      return Optional.empty();
    }

    var engCodingDescriptionPrefix = new StringBuilder();
    var freCodingDescriptionPrefix = new StringBuilder();

    if (PROVINCIAL_PROVIDER_IDENTIFIER_UNRESOLVED.equals(provincialId)) {
      engCodingDescriptionPrefix
          .append(UNRESOLVED_LOCATION_ENG)
          .append(CommonConstants.COLON)
          .append(StringUtils.SPACE);
      freCodingDescriptionPrefix
          .append(UNRESOLVED_LOCATION_FRE)
          .append(CommonConstants.COLON)
          .append(StringUtils.SPACE);
    } else if (PROVINCIAL_PROVIDER_IDENTIFIER_OUTSIDE_PROVINCE.equals(provincialId)) {
      engCodingDescriptionPrefix
          .append(String.format(OUT_OF_PROVINCE_LOCATION_ENG, province.getEn()))
          .append(CommonConstants.COLON)
          .append(StringUtils.SPACE);
      freCodingDescriptionPrefix
          .append(String.format(OUT_OF_PROVINCE_LOCATION_FRE, province.getFr()))
          .append(CommonConstants.COLON)
          .append(StringUtils.SPACE);
    }

    UnaryOperator<Coding> mapCodingWithDescriptionPrefix =
        (Coding coding) ->
            coding.toBuilder()
                .display(
                    Arrays.stream(coding.getDisplay())
                        .map(
                            localizedText -> {
                              switch (localizedText.getLanguage()) {
                                case ENG:
                                  return localizedText.toBuilder()
                                      .text(
                                          engCodingDescriptionPrefix
                                              .append(localizedText.getText())
                                              .toString())
                                      .build();
                                case FRA:
                                  return localizedText.toBuilder()
                                      .text(
                                          freCodingDescriptionPrefix
                                              .append(localizedText.getText())
                                              .toString())
                                      .build();
                                default:
                                  return localizedText;
                              }
                            })
                        .toArray(LocalizedText[]::new))
                .build();

    return Optional.ofNullable(nullFlavorCodings.get(provincialLocationTypeCode))
        .or(
            () ->
                this.vocabService.getProvincialCodingByProvincialCode(
                    CodeableConceptType.LOCATION, province, provincialLocationTypeCode))
        .or(
            () ->
                Optional.of(
                    Coding.builder()
                        .code(provincialLocationTypeCode)
                        .province(province)
                        .display(mapToUnknownLocalizedText(provincialLocationTypeCode))
                        .build()))
        .map(mapCodingWithDescriptionPrefix);
  }

  @Override
  public Optional<Coding> findProvincialPrescriptionActCareEventTypeCodingByProvincialCode(
      Province province, String provincialActCareEventType) {
    if (StringUtils.isBlank(provincialActCareEventType)) {
      return Optional.empty();
    }

    return Optional.ofNullable(nullFlavorCodings.get(provincialActCareEventType))
        .or(
            () ->
                this.vocabService.getProvincialCodingByProvincialCode(
                    CodeableConceptType.ACT_CARE_EVENT_TYPE, province, provincialActCareEventType))
        .or(
            () ->
                Optional.of(
                    Coding.builder()
                        .code(provincialActCareEventType)
                        .province(province)
                        .display(mapToUnknownLocalizedText(provincialActCareEventType))
                        .build()));
  }

  @Override
  public Optional<Coding>
      findProvincialPrescriptionServiceDeliveryLocationRoleTypeCodingByProvincialCode(
          Province province, String provincialServiceLocationRoleTypeCode) {
    if (StringUtils.isBlank(provincialServiceLocationRoleTypeCode)) {
      return Optional.empty();
    }
    return Optional.ofNullable(nullFlavorCodings.get(provincialServiceLocationRoleTypeCode))
        .or(
            () ->
                this.vocabService.getProvincialCodingByProvincialCode(
                    CodeableConceptType.SERVICE_DELIVERY_LOCATION_ROLE_TYPE,
                    province,
                    provincialServiceLocationRoleTypeCode))
        .or(
            () ->
                Optional.of(
                    Coding.builder()
                        .code(provincialServiceLocationRoleTypeCode)
                        .province(province)
                        .display(mapToUnknownLocalizedText(provincialServiceLocationRoleTypeCode))
                        .build()));
  }

  @Override
  public Optional<Coding> findProvincialPrescriptionActDetectedIssueCodingByProvincialCode(
      Province province, String provincialActDetectedIssueCode) {
    if (StringUtils.isBlank(provincialActDetectedIssueCode)) {
      return Optional.empty();
    }

    return Optional.ofNullable(nullFlavorCodings.get(provincialActDetectedIssueCode))
        .or(
            () ->
                this.vocabService.getProvincialCodingByProvincialCode(
                    CodeableConceptType.MANAGED_ISSUE_CODE,
                    province,
                    provincialActDetectedIssueCode))
        .or(
            () ->
                Optional.of(
                    Coding.builder()
                        .code(provincialActDetectedIssueCode)
                        .province(province)
                        .display(mapToUnknownLocalizedText(provincialActDetectedIssueCode))
                        .build()));
  }

  @Override
  public Optional<Coding> findProvincialPrescriptionDurationLengthUnitCodingByProvincialCode(
      Province province, String provincialDurationLengthUnitCode) {
    if (StringUtils.isBlank(provincialDurationLengthUnitCode)) {
      return Optional.empty();
    }

    return Optional.ofNullable(nullFlavorCodings.get(provincialDurationLengthUnitCode))
        .or(
            () ->
                this.vocabService.getProvincialCodingByProvincialCode(
                    CodeableConceptType.DURATION_LENGTH_UNIT,
                    province,
                    provincialDurationLengthUnitCode))
        .or(
            () ->
                Optional.of(
                    Coding.builder()
                        .code(provincialDurationLengthUnitCode)
                        .province(province)
                        .display(mapToUnknownLocalizedText(provincialDurationLengthUnitCode))
                        .build()));
  }

  /**
   * Maps a Coding {@link Coding} to an 'Unknown' equivalent by prefixing code and display
   * descriptions with "Unknown Code:". Returns a new mapped Coding {@link Coding} given an existing
   * Coding {@link Coding}V
   *
   * @param coding {@link Coding}
   * @return Coding
   */
  private Coding mapUnknownCoding(Coding coding) {
    return Coding.builder()
        .system(VocabConstants.SYSTEM)
        .province(Province.ALL)
        .code(VocabConstants.UNKNOWN_CODE_ENG_PREFIX + coding.getCode())
        .display(
            Arrays.stream(coding.getDisplay())
                .map(
                    localizedText ->
                        LocalizedText.builder()
                            .language(localizedText.getLanguage())
                            .text(
                                localizedText.getLanguage() == LanguageCode.FRA
                                    ? VocabConstants.UNKNOWN_CODE_FRE_PREFIX
                                        + localizedText.getText()
                                    : VocabConstants.UNKNOWN_CODE_ENG_PREFIX
                                        + localizedText.getText())
                            .build())
                .toArray(LocalizedText[]::new))
        .build();
  }

  /**
   * Maps a Coding {@link Coding} to an 'Unknown' equivalent by prefixing code and display code with
   * "Unknown Code:". Returns a new mapped LocalizedText array {@link String} given an existing
   * Coding {@link Coding}
   *
   * @param code {@link String}
   * @return LocalizedText[]
   */
  private LocalizedText[] mapToUnknownLocalizedText(String code) {
    return new LocalizedText[] {
      LocalizedText.builder()
          .text(VocabConstants.UNKNOWN_CODE_ENG_PREFIX + code)
          .language(LanguageCode.ENG)
          .build(),
      LocalizedText.builder()
          .text(VocabConstants.UNKNOWN_CODE_FRE_PREFIX + code)
          .language(LanguageCode.FRA)
          .build()
    };
  }
}
