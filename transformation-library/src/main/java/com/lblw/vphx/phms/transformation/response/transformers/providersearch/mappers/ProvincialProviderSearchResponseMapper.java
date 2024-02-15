package com.lblw.vphx.phms.transformation.response.transformers.providersearch.mappers;

import com.lblw.vphx.phms.common.coding.services.CodeableConceptService;
import com.lblw.vphx.phms.common.constants.HL7Constants;
import com.lblw.vphx.phms.common.databind.BindMany;
import com.lblw.vphx.phms.common.databind.BindOne;
import com.lblw.vphx.phms.common.databind.XPathTarget;
import com.lblw.vphx.phms.domain.common.Gender;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.ProvincialProviderProfile;
import com.lblw.vphx.phms.domain.common.SystemIdentifier;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.common.response.AuditEvent;
import com.lblw.vphx.phms.domain.common.response.ProvincialResponseAcknowledgement;
import com.lblw.vphx.phms.domain.common.response.hl7v3.DetectedIssue;
import com.lblw.vphx.phms.domain.common.response.hl7v3.QueryAcknowledgement;
import com.lblw.vphx.phms.domain.common.response.hl7v3.ResponseBodyTransmissionWrapper;
import com.lblw.vphx.phms.domain.common.response.hl7v3.ResponseHeader;
import com.lblw.vphx.phms.domain.provider.request.ProvincialProviderSearchCriteria;
import com.lblw.vphx.phms.domain.provider.response.ProvincialProviderProfiles;
import com.lblw.vphx.phms.domain.provider.response.hl7v3.ProvincialProviderSearchResponse;
import com.lblw.vphx.phms.domain.provider.response.hl7v3.ProvincialProviderSearchResponseControlAct;
import com.lblw.vphx.phms.transformation.response.helpers.CommonResponseXPathTransformerHelper;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class ProvincialProviderSearchResponseMapper extends ProvincialProviderSearchResponse {

  private final Province province;
  private final CodeableConceptService codeableConceptService;
  private final CommonResponseXPathTransformerHelper commonResponseXPathTransformerHelper;

  public ProvincialProviderSearchResponseMapper(
      Province province,
      CodeableConceptService codeableConceptService,
      CommonResponseXPathTransformerHelper commonResponseXPathTransformerHelper) {
    this.province = province;
    this.codeableConceptService = codeableConceptService;
    this.commonResponseXPathTransformerHelper = commonResponseXPathTransformerHelper;
  }

  @XPathTarget
  public void bindResponseHeader(
      @XPathTarget.Binding(xPath = "s:Header") BindOne<ResponseHeader> binder) {
    super.setResponseHeader(
        commonResponseXPathTransformerHelper.buildResponseHeaderBuilder(binder));
  }

  @XPathTarget
  public void bindProvincialResponsePayload(
      @XPathTarget.Binding(xPath = "s:Body/PRPM_IN306011CA/controlActEvent/subject[x]")
          BindMany<ProvincialProviderProfile> binder,
      @XPathTarget.Binding(xPath = "s:Body/PRPM_IN306011CA/controlActEvent/id/@extension")
          String requestId,
      @XPathTarget.Binding(
              xPath =
                  "s:Body/PRPM_IN306011CA/controlActEvent/parameterList/providerID/value[@root=\"2.16.840.1.113883.4.272\"]/@extension")
          String sinIdentifier) {
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
      return;
    }
    var provincialPatientProfiles =
        binder
            .apply(() -> new ProvincialProviderProfileMapper(province, codeableConceptService))
            .collect(Collectors.toList());

    if (StringUtils.isNotEmpty(sinIdentifier)) {
      provincialPatientProfiles.forEach(
          provincialProviderProfile ->
              provincialProviderProfile.setSin(buildSINIdentifier(sinIdentifier)));
    }

    super.setProvincialResponsePayload(
        ProvincialProviderProfiles.builder()
            .provincialProviderProfiles(provincialPatientProfiles)
            .provincialResponseAcknowledgement(provincialResponseAcknowledgement)
            .build());
  }

  @XPathTarget
  public void bindResponseBodyTransmissionWrapper(
      @XPathTarget.Binding(xPath = "s:Body/PRPM_IN306011CA")
          BindOne<ResponseBodyTransmissionWrapper> binder) {
    super.setResponseBodyTransmissionWrapper(
        commonResponseXPathTransformerHelper.buildTransmissionWrapper(binder));
  }

  @XPathTarget
  public void bindResponseControlAct(
      @XPathTarget.Binding(xPath = "s:Body/PRPM_IN306011CA/controlActEvent/id/@extension")
          String requestId,
      @XPathTarget.Binding(xPath = "s:Body/PRPM_IN306011CA/controlActEvent/id/@root") String rootId,
      @XPathTarget.Binding(
              xPath =
                  "s:Body/PRPM_IN306011CA/controlActEvent/parameterList/administrativeGender/value/@code")
          String gender,
      @XPathTarget.Binding(
              xPath =
                  "s:Body/PRPM_IN306011CA/controlActEvent/parameterList/name/value/given/text()")
          String firstName,
      @XPathTarget.Binding(
              xPath =
                  "s:Body/PRPM_IN306011CA/controlActEvent/parameterList/name/value/family/text()")
          String lastName) {

    super.setResponseControlAct(
        ProvincialProviderSearchResponseControlAct.builder()
            .eventRoot(rootId)
            .eventCorrelationId(requestId)
            .provincialProviderSearchCriteria(
                ProvincialProviderSearchCriteria.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .gender(StringUtils.isEmpty(gender) ? null : Gender.valueOf(gender))
                    .build())
            .build());
  }

  @XPathTarget
  public void bindQueryAck(
      @XPathTarget.Binding(xPath = "s:Body/PRPM_IN306011CA/controlActEvent/queryAck")
          BindOne<QueryAcknowledgement> binder) {
    super.setQueryAcknowledgement(commonResponseXPathTransformerHelper.buildQueryAck(binder));
  }

  /**
   * Builds Sin Identifier for the given identifier value.
   *
   * @param identifierValue
   * @return a new instance of {@link SystemIdentifier }
   */
  private SystemIdentifier buildSINIdentifier(String identifierValue) {
    return SystemIdentifier.builder()
        .type(SystemIdentifier.IDENTIFIER_TYPE.PROVIDER_SIN_NUMBER)
        .value(identifierValue)
        .system(HL7Constants.SIN)
        .assigner(HL7Constants.QC)
        .build();
  }

  @XPathTarget
  public void bindDetectedIssues(
      @XPathTarget.Binding(xPath = "s:Body/PRPM_IN306011CA/controlActEvent/subjectOf[x]")
          BindMany<DetectedIssue> binder) {
    super.setDetectedIssues(commonResponseXPathTransformerHelper.buildDetectedIssue(binder));
  }
}
