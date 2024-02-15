package com.lblw.vphx.phms.transformation.response.transformers.mappers;

import com.lblw.vphx.phms.common.databind.XPathTarget;
import com.lblw.vphx.phms.domain.common.response.hl7v3.QueryAcknowledgement;

public class QueryAcknowledgementMapper extends QueryAcknowledgement {

  @XPathTarget
  public void bindQueryResponseCode(
      @XPathTarget.Binding(xPath = "queryResponseCode/@code") String queryResponseCode) {
    super.setQueryResponseCode(queryResponseCode);
  }

  @XPathTarget
  public void bindResultTotalQuantity(
      @XPathTarget.Binding(xPath = "resultTotalQuantity/@value") String resultTotalQuantity) {
    super.setResultTotalQuantity(resultTotalQuantity);
  }

  @XPathTarget
  public void bindResultCurrentQuantity(
      @XPathTarget.Binding(xPath = "resultCurrentQuantity/@value") String resultCurrentQuantity) {
    super.setResultCurrentQuantity(resultCurrentQuantity);
  }

  @XPathTarget
  public void bindResultRemainingQuantity(
      @XPathTarget.Binding(xPath = "resultRemainingQuantity/@value")
          String resultRemainingQuantity) {
    super.setResultRemainingQuantity(resultRemainingQuantity);
  }
}
