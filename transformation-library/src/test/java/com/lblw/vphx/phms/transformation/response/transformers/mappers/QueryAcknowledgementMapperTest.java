package com.lblw.vphx.phms.transformation.response.transformers.mappers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class QueryAcknowledgementMapperTest {

  private static QueryAcknowledgementMapper queryAcknowledgementMapper;

  @BeforeAll
  static void buildQueryAcknowledgementMapper() {
    queryAcknowledgementMapper = new QueryAcknowledgementMapper();
  }

  @Test
  void whenBinding_thenSetQueryAcknowledgement() {
    queryAcknowledgementMapper.bindQueryResponseCode("queryResponseCode");
    queryAcknowledgementMapper.bindResultTotalQuantity("resultTotalQuantity");
    queryAcknowledgementMapper.bindResultCurrentQuantity("resultCurrentQuantity");
    queryAcknowledgementMapper.bindResultRemainingQuantity("resultRemainingQuantity");

    Assertions.assertEquals("queryResponseCode", queryAcknowledgementMapper.getQueryResponseCode());
    Assertions.assertEquals(
        "resultTotalQuantity", queryAcknowledgementMapper.getResultTotalQuantity());
    Assertions.assertEquals(
        "resultCurrentQuantity", queryAcknowledgementMapper.getResultCurrentQuantity());
    Assertions.assertEquals(
        "resultRemainingQuantity", queryAcknowledgementMapper.getResultRemainingQuantity());
  }
}
