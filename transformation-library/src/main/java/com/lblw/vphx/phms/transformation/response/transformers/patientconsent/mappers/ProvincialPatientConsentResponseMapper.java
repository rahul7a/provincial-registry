package com.lblw.vphx.phms.transformation.response.transformers.patientconsent.mappers;

import com.lblw.vphx.phms.common.databind.BindMany;
import com.lblw.vphx.phms.common.databind.BindOne;
import com.lblw.vphx.phms.common.databind.XPathTarget;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.common.response.AuditEvent;
import com.lblw.vphx.phms.domain.common.response.ProvincialResponseAcknowledgement;
import com.lblw.vphx.phms.domain.common.response.hl7v3.DetectedIssue;
import com.lblw.vphx.phms.domain.common.response.hl7v3.QueryAcknowledgement;
import com.lblw.vphx.phms.domain.common.response.hl7v3.ResponseBodyTransmissionWrapper;
import com.lblw.vphx.phms.domain.common.response.hl7v3.ResponseHeader;
import com.lblw.vphx.phms.domain.patient.consent.response.ProvincialPatientConsent;
import com.lblw.vphx.phms.domain.patient.consent.response.hl7v3.ProvincialPatientConsentResponse;
import com.lblw.vphx.phms.transformation.response.transformers.mappers.DetectedIssueMapper;
import com.lblw.vphx.phms.transformation.response.transformers.mappers.QueryAcknowledgementMapper;
import com.lblw.vphx.phms.transformation.response.transformers.mappers.ResponseBodyTransmissionWrapperMapper;
import com.lblw.vphx.phms.transformation.response.transformers.mappers.ResponseHeaderMapper;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProvincialPatientConsentResponseMapper extends ProvincialPatientConsentResponse {

  @XPathTarget
  public void bindResponseHeader(
      @XPathTarget.Binding(xPath = "s:Header") BindOne<ResponseHeader> binder) {
    if (Objects.isNull(binder)) {
      return;
    }
    super.setResponseHeader(binder.apply(ResponseHeaderMapper::new));
  }

  @XPathTarget
  public void bindResponseBodyTransmissionWrapper(
      @XPathTarget.Binding(xPath = "s:Body/RCMR_IN010997CAQC_V01")
          BindOne<ResponseBodyTransmissionWrapper> binder) {
    if (Objects.isNull(binder)) {
      return;
    }
    super.setResponseBodyTransmissionWrapper(
        binder.apply(ResponseBodyTransmissionWrapperMapper::new));
  }

  @XPathTarget
  public void bindProvincialResponsePayload(
      @XPathTarget.Binding(
              xPath = "s:Body/RCMR_IN010997CAQC_V01/controlActEvent/subject/consentEvent")
          BindOne<ProvincialPatientConsent> binder,
      @XPathTarget.Binding(xPath = "s:Body/RCMR_IN010997CAQC_V01/controlActEvent/id/@extension")
          String requestId) {
    var provincialResponseAcknowledgement =
        ProvincialResponseAcknowledgement.builder()
            .auditEvent(
                AuditEvent.builder()
                    .provincialRequestControl(
                        ProvincialRequestControl.builder()
                            .province(Province.QC)
                            .requestId(requestId)
                            .build())
                    .build())
            .build();

    if (Objects.isNull(binder)) {
      super.setProvincialResponsePayload(
          ProvincialPatientConsent.builder()
              .provincialResponseAcknowledgement(provincialResponseAcknowledgement)
              .build());
      return;
    }
    var patientConsent = binder.apply(ProvincialPatientConsentMapper::new);
    patientConsent.setProvincialResponseAcknowledgement(provincialResponseAcknowledgement);
    super.setProvincialResponsePayload(patientConsent);
  }

  @XPathTarget
  public void bindDetectedIssues(
      @XPathTarget.Binding(xPath = "s:Body/RCMR_IN010997CAQC_V01/controlActEvent/subjectOf1[x]")
          BindMany<DetectedIssue> binder) {
    if (Objects.isNull(binder)) {
      return;
    }
    super.setDetectedIssues(binder.apply(DetectedIssueMapper::new).collect(Collectors.toList()));
  }

  @XPathTarget
  public void bindQueryAck(
      @XPathTarget.Binding(xPath = "s:Body/RCMR_IN010997CAQC_V01/controlActEvent/queryAck")
          BindOne<QueryAcknowledgement> binder) {
    if (Objects.isNull(binder)) {
      return;
    }
    super.setQueryAcknowledgement(binder.apply(QueryAcknowledgementMapper::new));
  }
}
