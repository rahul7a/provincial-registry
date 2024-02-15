package com.lblw.vphx.phms.transformation.response.transformers.locationsummary.mappers;

import com.lblw.vphx.phms.common.coding.services.CodeableConceptService;
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
import com.lblw.vphx.phms.domain.location.response.ProvincialLocationSummaries;
import com.lblw.vphx.phms.domain.location.response.hl7v3.ProvincialLocationSearchResponse;
import com.lblw.vphx.phms.transformation.response.transformers.mappers.DetectedIssueMapper;
import com.lblw.vphx.phms.transformation.response.transformers.mappers.QueryAcknowledgementMapper;
import com.lblw.vphx.phms.transformation.response.transformers.mappers.ResponseBodyTransmissionWrapperMapper;
import com.lblw.vphx.phms.transformation.response.transformers.mappers.ResponseHeaderMapper;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LocationSummaryResponseMapper extends ProvincialLocationSearchResponse {
  private final CodeableConceptService codeableConceptService;

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
      @XPathTarget.Binding(xPath = "s:Body/PRLO_IN202011CAQC_V01")
          BindOne<ResponseBodyTransmissionWrapper> binder) {
    if (Objects.isNull(binder)) {
      return;
    }
    super.setResponseBodyTransmissionWrapper(
        binder.apply(ResponseBodyTransmissionWrapperMapper::new));
  }

  @XPathTarget
  public void bindQueryAcknowledgementMapper(
      @XPathTarget.Binding(xPath = "s:Body/PRLO_IN202011CAQC_V01/controlActEvent/queryAck")
          BindOne<QueryAcknowledgement> binder) {
    if (Objects.isNull(binder)) {
      return;
    }
    super.setQueryAcknowledgement(binder.apply(QueryAcknowledgementMapper::new));
  }

  @XPathTarget
  public void bindDetectedIssueMapper(
      @XPathTarget.Binding(xPath = "s:Body/PRLO_IN202011CAQC_V01/controlActEvent/subjectOf[x]")
          BindMany<DetectedIssue> binder) {
    if (Objects.isNull(binder)) {
      return;
    }
    super.setDetectedIssues(binder.apply(DetectedIssueMapper::new).collect(Collectors.toList()));
  }

  @XPathTarget
  public void bindProvincialResponsePayload(
      @XPathTarget.Binding(xPath = "s:Body/PRLO_IN202011CAQC_V01")
          BindOne<ProvincialLocationSummaries> binder,
      @XPathTarget.Binding(xPath = "s:Body/PRLO_IN202011CAQC_V01/controlActEvent")
          BindOne<Object> controlActElement,
      @XPathTarget.Binding(xPath = "s:Body/PRLO_IN202011CAQC_V01/controlActEvent/id/@extension")
          String requestId) {
    var locations =
        ProvincialLocationSummaries.builder().provincialLocations(Collections.emptyList()).build();
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
    if (Objects.isNull(controlActElement) || Objects.isNull(binder)) {
      locations.setProvincialResponseAcknowledgement(provincialResponseAcknowledgement);
      super.setProvincialResponsePayload(locations);
      return;
    }

    locations = binder.apply(() -> new ProvincialLocationSummariesMapper(codeableConceptService));
    locations.setProvincialResponseAcknowledgement(provincialResponseAcknowledgement);
    super.setProvincialResponsePayload(locations);
  }
}
