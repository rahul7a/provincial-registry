package com.lblw.vphx.phms.transformation.response.transformers.mappers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DetectedIssueMapperTest {

  private static DetectedIssueMapper detectedIssueMapper;

  @BeforeAll
  static void buildDetectedIssueMapper() {
    detectedIssueMapper = new DetectedIssueMapper();
  }

  @Test
  void whenBinding_thenSetDetectedIssues() {

    detectedIssueMapper.bindEventCode("eventCode");
    detectedIssueMapper.bindEventText("eventText");

    Assertions.assertEquals("eventCode", detectedIssueMapper.getEventCode());
    Assertions.assertEquals("eventText", detectedIssueMapper.getEventText());
  }
}
