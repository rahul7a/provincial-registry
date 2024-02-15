package com.lblw.vphx.phms.transformation.request;

import com.lblw.vphx.phms.common.coding.services.CodeableConceptService;
import com.lblw.vphx.phms.common.constants.HL7Constants;
import com.lblw.vphx.phms.domain.common.request.hl7v3.Request;
import com.lblw.vphx.phms.domain.location.request.LocationIdentifierType;
import com.lblw.vphx.phms.domain.location.request.ProvincialLocationSearchCriteria;
import com.lblw.vphx.phms.domain.location.request.hl7v3.ProvincialLocationSearchRequest;
import org.thymeleaf.context.Context;

/**
 * Sets variable in {@link Context} to use in Find Location Summary transaction for the Quebec
 * province.
 */
public class ProvincialLocationSummarySearchContextBuilder implements RequestContextBuilder {

  private final CodeableConceptService codeableConceptService;

  /**
   * Creates instance with required {@link CodeableConceptService}
   *
   * @param codeableConceptService {@link CodeableConceptService}
   */
  public ProvincialLocationSummarySearchContextBuilder(
      CodeableConceptService codeableConceptService) {
    this.codeableConceptService = codeableConceptService;
  }

  @Override
  public void setContext(Context context, Request<?> request) {
    var provincialLocationSearchCriteria =
        ((ProvincialLocationSearchRequest) request).getProvincialRequestPayload();

    setProvincialLocationType(context, provincialLocationSearchCriteria);

    if (provincialLocationSearchCriteria.getProvincialLocationIdentifierType() == null) {
      return;
    }
    switch (provincialLocationSearchCriteria.getProvincialLocationIdentifierType()) {
      case PERMIT_NUMBER:
        setLocationContextUsingPermitNumber(context, provincialLocationSearchCriteria);
        break;

      case PHARMACY_BILLING_NUMBER:
        setLocationContextUsingPharmacyBillingNumber(context, provincialLocationSearchCriteria);
        break;

      case PUBLIC_HEALTH_INSURANCE_PERMIT_NUMBER:
        setLocationContextUsingPublicHealthInsurancePermitNumber(
            context, provincialLocationSearchCriteria);
        break;
    }
  }

  /**
   * This method sets the context for ProvincialLocationIdentifierType.PERMIT_NUMBER {@link
   * LocationIdentifierType}
   *
   * @param context {@link Context context from which template gets data used in transformation}
   * @param provincialLocationSearchCriteria {@link ProvincialLocationSearchCriteria}
   */
  private void setLocationContextUsingPermitNumber(
      Context context, ProvincialLocationSearchCriteria provincialLocationSearchCriteria) {
    context.setVariable(HL7Constants.LOCATION_SUMMARY_ID_ROOT, HL7Constants.PERMIT_NUMBER_ROOT);
    context.setVariable(
        HL7Constants.LOCATION_SUMMARY_ID_EXTENSION,
        provincialLocationSearchCriteria.getProvincialLocationIdentifierValue());
  }

  /**
   * This method sets the context for ProvincialLocationIdentifierType.PHARMACY_BILLING_NUMBER
   * {@link LocationIdentifierType}
   *
   * @param context {@link Context context from which template gets data used in transformation}
   * @param provincialLocationSearchCriteria {@link ProvincialLocationSearchCriteria}
   */
  private void setLocationContextUsingPharmacyBillingNumber(
      Context context, ProvincialLocationSearchCriteria provincialLocationSearchCriteria) {
    context.setVariable(
        HL7Constants.LOCATION_SUMMARY_ID_ROOT, HL7Constants.PHARMACY_BILLING_NUMBER_ROOT);
    context.setVariable(
        HL7Constants.LOCATION_SUMMARY_ID_EXTENSION,
        provincialLocationSearchCriteria.getProvincialLocationIdentifierValue());
  }

  /**
   * This method sets the context for
   * ProvincialLocationIdentifierType.PUBLIC_HEALTH_INSURANCE_PERMIT_NUMBER {@link
   * LocationIdentifierType}
   *
   * @param context {@link Context context from which template gets data used in transformation}
   * @param provincialLocationSearchCriteria {@link ProvincialLocationSearchCriteria}
   */
  private void setLocationContextUsingPublicHealthInsurancePermitNumber(
      Context context, ProvincialLocationSearchCriteria provincialLocationSearchCriteria) {
    context.setVariable(
        HL7Constants.LOCATION_SUMMARY_ID_ROOT,
        HL7Constants.PUBLIC_HEALTH_INSURANCE_PERMIT_NUMBER_ROOT);
    context.setVariable(
        HL7Constants.LOCATION_SUMMARY_ID_EXTENSION,
        provincialLocationSearchCriteria.getProvincialLocationIdentifierValue());
  }

  /**
   * Finds provincial location type from given system location type and sets provincial location
   * type in template context.
   *
   * @param context {@link Context}
   * @param provincialLocationSearchCriteria {@link ProvincialLocationSearchCriteria}
   */
  private void setProvincialLocationType(
      Context context, ProvincialLocationSearchCriteria provincialLocationSearchCriteria) {
    var systemLocationType = provincialLocationSearchCriteria.getLocationType();
    var province = provincialLocationSearchCriteria.getProvincialRequestControl().getProvince();
    codeableConceptService
        .findProvincialLocationTypeCodingBySystemCode(province, systemLocationType)
        .ifPresent(
            provincialLocationType ->
                context.setVariable(
                    HL7Constants.PROVINCIAL_LOCATION_TYPE, provincialLocationType.getCode()));
  }
}
