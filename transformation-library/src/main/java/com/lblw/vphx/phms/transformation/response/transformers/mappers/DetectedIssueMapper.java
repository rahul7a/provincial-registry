package com.lblw.vphx.phms.transformation.response.transformers.mappers;

import com.lblw.vphx.phms.common.databind.XPathTarget;
import com.lblw.vphx.phms.domain.common.response.hl7v3.DetectedIssue;

public class DetectedIssueMapper extends DetectedIssue {

  @XPathTarget
  public void bindEventCode(
      @XPathTarget.Binding(xPath = "detectedIssueEvent/code/@code") String eventCode) {
    super.setEventCode(eventCode);
  }

  @XPathTarget
  public void bindEventText(
      @XPathTarget.Binding(xPath = "detectedIssueEvent/text/text()") String eventText) {
    super.setEventText(eventText);
  }
}
