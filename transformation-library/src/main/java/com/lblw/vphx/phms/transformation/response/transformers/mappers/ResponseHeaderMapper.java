package com.lblw.vphx.phms.transformation.response.transformers.mappers;

import com.lblw.vphx.phms.common.databind.XPathTarget;
import com.lblw.vphx.phms.domain.common.response.hl7v3.ResponseHeader;

public class ResponseHeaderMapper extends ResponseHeader {
  @XPathTarget
  public void bindTransactionId(
      @XPathTarget.Binding(xPath = "cais:TransactionId/text()") String transactionId) {
    super.setTransactionId(transactionId);
  }

  @XPathTarget
  public void bindSessionId(
      @XPathTarget.Binding(xPath = "cais:SessionID/text()") String sessionId) {
    super.setSessionId(sessionId);
  }

  @XPathTarget
  public void bindTrackingId(
      @XPathTarget.Binding(xPath = "cais:TrackingId/text()") String trackingId) {
    super.setTrackingId(trackingId);
  }
}
