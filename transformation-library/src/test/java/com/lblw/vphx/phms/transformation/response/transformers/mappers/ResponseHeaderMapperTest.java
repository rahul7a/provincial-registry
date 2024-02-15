package com.lblw.vphx.phms.transformation.response.transformers.mappers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ResponseHeaderMapperTest {

  private static ResponseHeaderMapper responseHeaderMapper;

  @BeforeAll
  static void buildResponseHeaderMapper() {
    responseHeaderMapper = new ResponseHeaderMapper();
  }

  @Test
  void whenBinding_thenSetResponseHeader() {

    responseHeaderMapper.bindTransactionId("transactionId");
    responseHeaderMapper.bindSessionId("sessionId");
    responseHeaderMapper.bindTrackingId("trackingId");

    Assertions.assertEquals("transactionId", responseHeaderMapper.getTransactionId());
    Assertions.assertEquals("sessionId", responseHeaderMapper.getSessionId());
    Assertions.assertEquals("trackingId", responseHeaderMapper.getTrackingId());
  }
}
