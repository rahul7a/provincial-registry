package com.lblw.vphx.phms.transformation.request;

import com.lblw.vphx.phms.common.coding.services.CodeableConceptService;
import com.lblw.vphx.phms.common.constants.HL7Constants;
import com.lblw.vphx.phms.domain.coding.Coding;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.request.hl7v3.Request;
import com.lblw.vphx.phms.domain.provider.request.ProviderIdentifierType;
import com.lblw.vphx.phms.domain.provider.request.ProvincialProviderSearchCriteria;
import com.lblw.vphx.phms.domain.provider.request.hl7v3.ProvincialProviderSearchRequest;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.context.Context;

/**
 * Sets variable in {@link Context} to use in Find Provider Query transaction for the Quebec
 * province.
 */
public class ProvincialProviderSearchContextBuilder implements RequestContextBuilder {

  private final CodeableConceptService codeableConceptService;

  /**
   * @param codeableConceptService {@link CodeableConceptService} provides provincial reference data
   *     of provider and speciality codes
   */
  public ProvincialProviderSearchContextBuilder(CodeableConceptService codeableConceptService) {
    this.codeableConceptService = codeableConceptService;
  }

  /**
   * sets contexts for following criteria: <br>
   * 1. Provider role code.<br>
   * 2. provider role speciality code <br>
   * 3. Provider search by an identifier which could be license, billing or provider number.
   *
   * @param context {@link Context context from which template gets data used in transformation}
   * @param request {@link Request}
   */
  @Override
  public void setContext(Context context, Request<?> request) {

    var provincialProviderSearchCriteria =
        ((ProvincialProviderSearchRequest) request).getProvincialRequestPayload();
    var province =
        request.getProvincialRequestPayload().getProvincialRequestControl().getProvince();
    var roleCoding =
        codeableConceptService.findProvincialRoleCodingBySystemRoleCode(
            province, provincialProviderSearchCriteria.getRoleCode());
    if (provincialProviderSearchCriteria.getProviderIdentifierType() != null) {
      switch (provincialProviderSearchCriteria.getProviderIdentifierType()) {
        case PROVINCIAL_PROVIDER_ID:
          // NIU-I
          setProviderContextUsingNIU(context, provincialProviderSearchCriteria);
          break;
        case BILLING:
          setProviderContextUsingBillingOID(context, provincialProviderSearchCriteria);
          break;
        case LICENSE:
          setProviderContextUsingLicence(context, provincialProviderSearchCriteria, roleCoding);
          break;
        case SIN:
          setProviderContextUsingSocialInsuranceNumber(context, provincialProviderSearchCriteria);
          break;
      }
    }
    setProviderRoleCode(context, roleCoding);
    setProviderSpecialityCode(context, provincialProviderSearchCriteria, province);
  }

  /**
   * This method sets the context for ProviderIdentifierType.SIN {@link ProviderIdentifierType}
   *
   * @param context {@link Context context from which template gets data used in transformation}
   * @param provincialProviderSearchCriteria {@link ProvincialProviderSearchCriteria}
   */
  private void setProviderContextUsingSocialInsuranceNumber(
      Context context, ProvincialProviderSearchCriteria provincialProviderSearchCriteria) {
    context.setVariable(HL7Constants.PROVIDER_ID_ROOT, HL7Constants.SIN_ROOT);
    context.setVariable(
        HL7Constants.PROVIDER_ID_EXTENSION,
        provincialProviderSearchCriteria.getProviderIdentifierValue());
  }

  /**
   * This method sets the context for ProviderIdentifierType.BILLING {@link ProviderIdentifierType}
   *
   * @param context {@link Context context from which template gets data used in transformation}
   * @param provincialProviderSearchCriteria {@link ProvincialProviderSearchCriteria}
   */
  private void setProviderContextUsingBillingOID(
      Context context, ProvincialProviderSearchCriteria provincialProviderSearchCriteria) {
    context.setVariable(HL7Constants.PROVIDER_ID_ROOT, HL7Constants.BILLING_OID);
    context.setVariable(
        HL7Constants.PROVIDER_ID_EXTENSION,
        provincialProviderSearchCriteria.getProviderIdentifierValue());
  }

  /**
   * This method sets the context for ProviderIdentifierType.PROVINCIAL_PROVIDER_ID {@link
   * ProviderIdentifierType}
   *
   * @param context {@link Context context from which template gets data used in transformation}
   * @param provincialProviderSearchCriteria {@link ProvincialProviderSearchCriteria}
   */
  private void setProviderContextUsingNIU(
      Context context, ProvincialProviderSearchCriteria provincialProviderSearchCriteria) {
    context.setVariable(HL7Constants.PROVIDER_ID_ROOT, HL7Constants.NIU_OID);
    context.setVariable(
        HL7Constants.PROVIDER_ID_EXTENSION,
        provincialProviderSearchCriteria.getProviderIdentifierValue());
  }

  /**
   * This method sets the context for ProviderIdentifierType.LICENSE {@link ProviderIdentifierType}
   *
   * @param context {@link Context context from which template gets data used in transformation}
   * @param provincialProviderSearchCriteria {@link ProvincialProviderSearchCriteria}
   * @param roleCoding {@link Optional<Coding>}
   */
  private void setProviderContextUsingLicence(
      Context context,
      ProvincialProviderSearchCriteria provincialProviderSearchCriteria,
      Optional<Coding> roleCoding) {

    roleCoding.ifPresent(
        coding -> {
          context.setVariable(HL7Constants.PROVIDER_ID_ROOT, coding.getCodingIdentifier());
          context.setVariable(
              HL7Constants.PROVIDER_ID_EXTENSION,
              provincialProviderSearchCriteria.getProviderIdentifierValue());
        });
  }
  /**
   * This method sets the context for Provider role type
   *
   * @param context {@link Context context from which template gets data used in transformation}
   * @param roleCoding {@link Optional<Coding>}
   */
  private void setProviderRoleCode(Context context, Optional<Coding> roleCoding) {
    roleCoding.ifPresent(
        coding -> context.setVariable(HL7Constants.PROVIDER_ROLE_CODE, coding.getCode()));
  }

  /**
   * This method sets the context for Provider speciality role type
   *
   * @param context {@link Context context from which template gets data used in transformation}
   * @param provincialProviderSearchCriteria {@link ProvincialProviderSearchCriteria}
   * @param province {@link Province}
   */
  private void setProviderSpecialityCode(
      Context context,
      ProvincialProviderSearchCriteria provincialProviderSearchCriteria,
      Province province) {

    if (StringUtils.isNotBlank(provincialProviderSearchCriteria.getRoleSpecialityCode())) {
      codeableConceptService
          .findProvincialSpecialityRoleCodingBySystemSpecialityRoleCode(
              province, provincialProviderSearchCriteria.getRoleSpecialityCode())
          .ifPresent(
              provincialSpecialityCoding ->
                  context.setVariable(
                      HL7Constants.PROVIDER_SPECIALITY_CODE, provincialSpecialityCoding.getCode()));
    }
  }
}
