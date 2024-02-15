package com.lblw.vphx.phms.transformation.response.transformers.patientsearch.mappers;

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
import com.lblw.vphx.phms.domain.patient.response.ProvincialPatientProfile;
import com.lblw.vphx.phms.domain.patient.response.hl7v3.ProvincialPatientSearchResponse;
import com.lblw.vphx.phms.transformation.response.helpers.CommonResponseXPathTransformerHelper;
import java.util.Objects;

public class ProvincialPatientSearchResponseMapper extends ProvincialPatientSearchResponse {
  private final CommonResponseXPathTransformerHelper commonResponseXPathTransformerHelper;

  public ProvincialPatientSearchResponseMapper(
      CommonResponseXPathTransformerHelper commonResponseXPathTransformerHelper) {
    this.commonResponseXPathTransformerHelper = commonResponseXPathTransformerHelper;
  }

  @XPathTarget
  public void bindResponseHeader(
      @XPathTarget.Binding(xPath = "s:Header") BindOne<ResponseHeader> binder) {
    super.setResponseHeader(
        commonResponseXPathTransformerHelper.buildResponseHeaderBuilder(binder));
  }

  @XPathTarget
  public void bindResponseBodyTransmissionWrapper(
      @XPathTarget.Binding(xPath = "s:Body/PRPA_IN101104CA")
          BindOne<ResponseBodyTransmissionWrapper> binder) {
    super.setResponseBodyTransmissionWrapper(
        commonResponseXPathTransformerHelper.buildTransmissionWrapper(binder));
  }

  @XPathTarget
  public void bindDetectedIssues(
      @XPathTarget.Binding(xPath = "s:Body/PRPA_IN101104CA/controlActEvent/subjectOf[x]")
          BindMany<DetectedIssue> binder) {
    super.setDetectedIssues(commonResponseXPathTransformerHelper.buildDetectedIssue(binder));
  }

  @XPathTarget
  public void bindProvincialResponsePayload(
      @XPathTarget.Binding(xPath = "s:Body/PRPA_IN101104CA/controlActEvent")
          BindOne<ProvincialPatientProfile> binder,
      @XPathTarget.Binding(xPath = "s:Body/PRPA_IN101104CA/controlActEvent/id/@extension")
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
          ProvincialPatientProfile.builder()
              .provincialResponseAcknowledgement(provincialResponseAcknowledgement)
              .build());
      return;
    }
    var provincialPatientProfile = binder.apply(ProvincialPatientProfileMapper::new);
    provincialPatientProfile.setProvincialResponseAcknowledgement(
        provincialResponseAcknowledgement);
    super.setProvincialResponsePayload(provincialPatientProfile);
  }

  @XPathTarget
  public void bindQueryAck(
      @XPathTarget.Binding(xPath = "s:Body/PRPA_IN101104CA/controlActEvent/queryAck")
          BindOne<QueryAcknowledgement> binder) {
    super.setQueryAcknowledgement(commonResponseXPathTransformerHelper.buildQueryAck(binder));
  }
}
