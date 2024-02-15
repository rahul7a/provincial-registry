package com.lblw.vphx.phms.transformation.response.transformers.mappers;

import com.lblw.vphx.phms.common.databind.BindMany;
import com.lblw.vphx.phms.common.databind.XPathTarget;
import com.lblw.vphx.phms.domain.common.response.hl7v3.AcknowledgementDetails;
import com.lblw.vphx.phms.domain.common.response.hl7v3.ResponseBodyTransmissionWrapper;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class ResponseBodyTransmissionWrapperMapper extends ResponseBodyTransmissionWrapper {
  @XPathTarget
  public void bindTransmissionUniqueIdentifier(
      @XPathTarget.Binding(xPath = "acknowledgement/targetMessage/id/@root")
          String transmissionUniqueIdentifier) {
    if (StringUtils.isBlank(transmissionUniqueIdentifier)) {
      return;
    }
    super.setTransmissionUniqueIdentifier(transmissionUniqueIdentifier);
  }

  @XPathTarget
  public void bindAcknowledgeTypeCode(
      @XPathTarget.Binding(xPath = "acknowledgement/typeCode/@code") String acknowledgeTypeCode) {
    if (StringUtils.isBlank(acknowledgeTypeCode)) {
      return;
    }
    super.setAcknowledgeTypeCode(acknowledgeTypeCode);
  }

  @XPathTarget
  public void bindAcknowledgementDetails(
      @XPathTarget.Binding(xPath = "acknowledgement/acknowledgementDetail[x]")
          BindMany<AcknowledgementDetails> elements) {
    if (Objects.isNull(elements)) {
      return;
    }

    super.setAcknowledgementDetails(
        elements.apply(AcknowledgementDetailsMapper::new).collect(Collectors.toList()));
  }
}
