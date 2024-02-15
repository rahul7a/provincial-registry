package com.lblw.vphx.phms.transformation.response.transformers.mappers;

import com.lblw.vphx.phms.common.databind.XPathTarget;
import com.lblw.vphx.phms.domain.common.response.hl7v3.AcknowledgementDetails;
import org.apache.commons.lang3.StringUtils;

public class AcknowledgementDetailsMapper extends AcknowledgementDetails {
  @XPathTarget
  public void bindTypeCode(
      @XPathTarget.Binding(xPath = "@typeCode") String typeCode,
      @XPathTarget.Binding(xPath = "typeCode/@code") String ackDetailsTypeCode) {

    if (StringUtils.isNotBlank(ackDetailsTypeCode)) {
      super.setTypeCode(ackDetailsTypeCode);
    } else {
      super.setTypeCode(typeCode);
    }
  }

  @XPathTarget
  public void bindCode(@XPathTarget.Binding(xPath = "code/@code") String code) {
    super.setCode(code);
  }

  @XPathTarget
  public void bindText(@XPathTarget.Binding(xPath = "text/text()") String text) {
    super.setText(text);
  }

  @XPathTarget
  public void bindLocation(@XPathTarget.Binding(xPath = "location/text()") String location) {
    super.setLocation(location);
  }
}
