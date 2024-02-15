package com.lblw.vphx.phms.common.coding.services;

import com.lblw.vphx.phms.domain.coding.Coding;
import com.lblw.vphx.phms.domain.common.Province;
import java.util.Optional;

public interface CodeableConceptService {

  Optional<Coding> findProvincialRoleCodingBySystemRoleCode(
      Province province, String systemRoleCode);

  Optional<Coding> findSystemRoleCodingByProvincialRoleCode(
      Province province, String provincialRoleCode);

  Optional<Coding> findProvincialSpecialityRoleCodingBySystemSpecialityRoleCode(
      Province province, String systemSpecialityCode);

  Optional<Coding> findSystemSpecialityRoleCodingByProvincialSpecialityRoleCode(
      Province province, String provincialSpecialityCode);

  Optional<Coding> findProvincialLocationTypeCodingBySystemCode(
      Province province, String systemLocationCode);

  Optional<Coding> findSystemLocationTypeCodingByProvincialCode(
      Province province, String provincialLocationCode);

  Optional<Coding> findProvincialPrescriptionAdministrationSiteCodingByProvincialCode(
      Province province, String provincialAdministrationSiteCode);

  Optional<Coding> findProvincialPrescriptionRouteOfAdminCodingByProvincialCode(
      Province province, String provincialRouteOfAdminCode);

  Optional<Coding> findProvincialPrescriptionIssueSeverityCodingByProvincialCode(
      Province province, String provincialIssueSeverityCode);

  Optional<Coding> findProvincialPrescriptionActDetectedIssueCodingByProvincialCode(
      Province province, String provincialActDetectedIssueCode);

  Optional<Coding> findProvincialPrescriptionIssuePriorityCodingByProvincialCode(
      Province province, String provincialIssuePriorityCode);

  Optional<Coding> findProvincialPrescriptionPackSizeUoMCodingByProvincialCode(
      Province province, String provincialPackSizeUoMCode);

  Optional<Coding> findProvincialPrescriptionDrugFormCodingByProvincialCode(
      Province province, String provincialDrugFormCode);

  Optional<Coding> findProvincialPrescriptionLocationTypeCodingByProvincialCode(
      Province province, String provincialLocationTypeCode, String provincialId);

  Optional<Coding> findProvincialPrescriptionActCareEventTypeCodingByProvincialCode(
      Province province, String provincialActCareEventType);

  Optional<Coding> findProvincialPrescriptionServiceDeliveryLocationRoleTypeCodingByProvincialCode(
      Province province, String provincialServiceLocationRoleTypeCode);

  Optional<Coding> findProvincialPrescriptionDurationLengthUnitCodingByProvincialCode(
      Province province, String provincialDurationLengthUnitCode);
}
