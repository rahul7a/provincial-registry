package com.lblw.vphx.phms.transformation.response.transformers.providersearch.mappers;

import com.lblw.vphx.phms.common.coding.services.CodeableConceptService;
import com.lblw.vphx.phms.common.databind.BindMany;
import com.lblw.vphx.phms.common.databind.XPathTarget;
import com.lblw.vphx.phms.common.utils.DateUtils;
import com.lblw.vphx.phms.domain.common.*;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public class ProvincialProviderProfileMapper extends ProvincialProviderProfile {

  public static final String LICENSE = "License";
  public static final String NIU_I = "NIU-I";
  public static final String BILLING = "Billing";
  private static final String QC = "Qc";
  private final Province province;
  private final CodeableConceptService codeableConceptService;

  public ProvincialProviderProfileMapper(
      Province province, CodeableConceptService codeableConceptService) {
    this.province = province;
    this.codeableConceptService = codeableConceptService;
  }

  @XPathTarget
  public void bindIdentifier(
      @XPathTarget.Binding(
              xPath =
                  "registrationRequest/subject/healthCareProvider/id[@root=\"2.16.840.1.113883.4.277\"]/@extension")
          String identifier) {
    if (StringUtils.isBlank(identifier)) {
      return;
    }

    super.setIdentifier(
        SystemIdentifier.builder()
            .type(SystemIdentifier.IDENTIFIER_TYPE.PROVIDER)
            .value(identifier)
            .assigner(QC)
            .system(NIU_I)
            .build());
  }

  @XPathTarget
  public void bindBillingIdentifier(
      @XPathTarget.Binding(
              xPath =
                  "registrationRequest/subject/healthCareProvider/id[@root=\"2.16.124.10.101.1.60.105\"]/@extension")
          String billingIdentifier) {
    if (StringUtils.isBlank(billingIdentifier)) {
      return;
    }

    super.setBilling(
        SystemIdentifier.builder()
            .type(SystemIdentifier.IDENTIFIER_TYPE.PROVIDER_BILLING_NUMBER)
            .value(billingIdentifier)
            .system(BILLING)
            .assigner(QC)
            .build());
  }

  @XPathTarget
  public void bindLicenseIdentifier(
      @XPathTarget.Binding(
              xPath =
                  "registrationRequest/subject/healthCareProvider/id[@root=\"2.16.840.1.113883.4.299\"]/@extension")
          String licenseIdentifier) {
    if (StringUtils.isBlank(licenseIdentifier)) {
      return;
    }

    super.setLicense(
        SystemIdentifier.builder()
            .type(SystemIdentifier.IDENTIFIER_TYPE.PROVIDER_LICENSE_NUMBER)
            .value(licenseIdentifier)
            .system(LICENSE)
            .assigner(QC)
            .build());
  }

  @XPathTarget
  public void bindFirstName(
      @XPathTarget.Binding(
              xPath =
                  "registrationRequest/subject/healthCareProvider/healthCarePrincipalPerson/name/given/text()")
          String firstName) {
    super.setFirstName(firstName);
  }

  @XPathTarget
  public void bindLastName(
      @XPathTarget.Binding(
              xPath =
                  "registrationRequest/subject/healthCareProvider/healthCarePrincipalPerson/name/family/text()")
          String lastName) {
    super.setLastName(lastName);
  }

  @XPathTarget
  public void bindGender(
      @XPathTarget.Binding(
              xPath =
                  "registrationRequest/subject/healthCareProvider/healthCarePrincipalPerson/administrativeGenderCode/@code")
          String gender) {
    if (StringUtils.isBlank(gender)) {
      return;
    }
    super.setGender(Gender.valueOf(gender));
  }

  @XPathTarget
  public void bindStatusCode(
      @XPathTarget.Binding(
              xPath = "registrationRequest/subject/healthCareProvider/statusCode/@code")
          String statusCode) {
    super.setStatus(statusCode);
  }

  /** Builds a date range object from given <effectiveTime> element. */
  @XPathTarget
  public void buildEffectiveDateRange(
      @XPathTarget.Binding(
              xPath = "registrationRequest/subject/healthCareProvider/effectiveTime/low/@value")
          String low,
      @XPathTarget.Binding(
              xPath = "registrationRequest/subject/healthCareProvider/effectiveTime/high/@value")
          String high) {
    EffectiveDateRange effectiveDateRange = null;

    if (!StringUtils.isAllBlank(low, high)) {
      effectiveDateRange =
          EffectiveDateRange.builder()
              .low(DateUtils.parseProvincialDateTimeFormatToInstantDateTime(province, low))
              .high(DateUtils.parseProvincialDateTimeFormatToInstantDateTime(province, high))
              .build();
    }
    super.setEffectiveDateRange(effectiveDateRange);
  }

  /** Populates provincial provider role */
  @XPathTarget
  public void populateProviderRole(
      @XPathTarget.Binding(xPath = "registrationRequest/subject/healthCareProvider/code/@code")
          String roleCode,
      @XPathTarget.Binding(
              xPath = "registrationRequest/subject/healthCareProvider/code/@nullFlavor")
          String nullFlavorRoleCode) {

    Optional.ofNullable(roleCode)
        .or(() -> Optional.ofNullable(nullFlavorRoleCode))
        .flatMap(
            code ->
                this.codeableConceptService.findSystemRoleCodingByProvincialRoleCode(
                    province, code))
        .ifPresent(providerCode -> setProviderRole(providerCode));
  }

  @XPathTarget
  public void bindProviderSpecialities(
      @XPathTarget.Binding(xPath = "registrationRequest/subject/healthCareProvider/relatedTo[x]")
          BindMany<ProviderSpecialityMapper> binder) {

    if (Objects.isNull(binder)) {
      return;
    }
    binder
        .apply(ProviderSpecialityMapper::new)
        .map(
            providerSpeciality ->
                Optional.ofNullable(providerSpeciality.getCode())
                    .or(() -> Optional.ofNullable(providerSpeciality.getNullFlavorCode()))
                    .flatMap(
                        specialityCode ->
                            this.codeableConceptService
                                .findSystemSpecialityRoleCodingByProvincialSpecialityRoleCode(
                                    province, specialityCode)))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .forEach(this::setProviderSpeciality);
  }
}
