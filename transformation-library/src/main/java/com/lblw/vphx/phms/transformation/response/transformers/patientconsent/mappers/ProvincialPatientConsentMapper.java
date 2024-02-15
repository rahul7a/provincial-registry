package com.lblw.vphx.phms.transformation.response.transformers.patientconsent.mappers;

import com.lblw.vphx.phms.common.constants.HL7Constants;
import com.lblw.vphx.phms.common.databind.XPathTarget;
import com.lblw.vphx.phms.common.utils.DateUtils;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.SystemIdentifier;
import com.lblw.vphx.phms.domain.patient.consent.response.ProvincialPatientConsent;
import org.apache.commons.lang3.StringUtils;

public class ProvincialPatientConsentMapper extends ProvincialPatientConsent {

  @XPathTarget
  public void bindConsentValidityStartDateTime(
      @XPathTarget.Binding(xPath = "effectiveTime/low/@value")
          String consentValidityStartDateTime) {

    super.setConsentValidityStartDateTime(
        DateUtils.parseProvincialDateTimeFormatToInstantDateTime(
            Province.QC, consentValidityStartDateTime));
  }

  @XPathTarget
  public void bindConsentValidityEndDateTime(
      @XPathTarget.Binding(xPath = "effectiveTime/high/@value") String consentValidityEndDateTime) {
    super.setConsentValidityEndDateTime(
        DateUtils.parseProvincialDateTimeFormatToInstantDateTime(
            Province.QC, consentValidityEndDateTime));
  }

  @XPathTarget
  public void bindIdentifier(
      @XPathTarget.Binding(
              xPath = "subject1/patient/id[@root=\"2.16.840.1.113883.4.56\"]/@extension")
          String identifier) {
    if (StringUtils.isBlank(identifier)) {
      return;
    }
    super.setIdentifier(
        SystemIdentifier.builder()
            .type(SystemIdentifier.IDENTIFIER_TYPE.PATIENT)
            .value(identifier)
            .assigner(HL7Constants.QC)
            .system(HL7Constants.NIU_U)
            .build());
  }
}
