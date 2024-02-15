package com.lblw.vphx.phms.transformation.response.transformers.mappers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AcknowledgementDetailsMapperTest {

  private static AcknowledgementDetailsMapper acknowledgementDetailsMapper;

  @BeforeAll
  static void buildAcknowledgementDetail() {
    acknowledgementDetailsMapper = new AcknowledgementDetailsMapper();
  }

  @Test
  void whenBinding_thenSetAcknowledgementDetailsFields() {

    // ACT
    acknowledgementDetailsMapper.bindCode("code");
    acknowledgementDetailsMapper.bindText("text");
    acknowledgementDetailsMapper.bindLocation("location");

    // Assert
    Assertions.assertEquals("code", acknowledgementDetailsMapper.getCode());
    Assertions.assertEquals("text", acknowledgementDetailsMapper.getText());
    Assertions.assertEquals("location", acknowledgementDetailsMapper.getLocation());
  }

  @Test
  void givenTypeCodeAttribute_whenBinding_thenSetAcknowledgementDetailsFields() {
    acknowledgementDetailsMapper.bindTypeCode("typeCode", null);
    Assertions.assertEquals("typeCode", acknowledgementDetailsMapper.getTypeCode());
  }

  @Test
  void givenTypeCodeElement_whenBinding_thenSetAcknowledgementDetailsFields() {
    acknowledgementDetailsMapper.bindTypeCode(null, "ackTypeCode");
    Assertions.assertEquals("ackTypeCode", acknowledgementDetailsMapper.getTypeCode());
  }
}
