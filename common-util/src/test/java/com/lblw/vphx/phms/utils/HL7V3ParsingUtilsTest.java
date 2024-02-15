package com.lblw.vphx.phms.utils;

import com.lblw.vphx.phms.common.utils.HL7V3ParsingUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HL7V3ParsingUtilsTest {

  @Test
  void parseQuantityInDays() {
    Double value = HL7V3ParsingUtils.convertToDays(20.0, "d");
    Assertions.assertNotNull(value);
    Assertions.assertEquals(20.0, value);
  }

  @Test
  void parseQuantityInWeeks() {
    Double value = HL7V3ParsingUtils.convertToDays(3.0, "wk");
    Assertions.assertNotNull(value);
    Assertions.assertEquals((double) Math.round(7 * 3.0), value);
  }

  @Test
  void parseQuantityInMonths() {
    Double value = HL7V3ParsingUtils.convertToDays(2.0, "mo");
    Assertions.assertNotNull(value);
    Assertions.assertEquals((double) Math.round(2.0 * (365.0 / 12)), value);
  }

  @Test
  void parseQuantityInYears() {
    Double value = HL7V3ParsingUtils.convertToDays(2.0, "a");
    Assertions.assertNotNull(value);
    Assertions.assertEquals((double) Math.round(365 * 2.0), value);
  }

  @Test
  void parseQuantityWhenUnitIsNull() {
    Assertions.assertNull(HL7V3ParsingUtils.convertToDays(2.0, null));
  }

  @Test
  void parseQuantityWhenValueIsNull() {
    Assertions.assertNull(HL7V3ParsingUtils.convertToDays(null, "d"));
  }

  @Test
  void parseQuantityWhenUnitIsNotMatched() {
    Assertions.assertNull(HL7V3ParsingUtils.convertToDays(2.0, "x"));
  }

  @Test
  void whenLowIsPresent_ThenBuildDurationString() {
    String low = "2022-11-01";
    Assertions.assertEquals(
        "From: 2022-11-01", HL7V3ParsingUtils.buildDurationString(low, null, null, null));
  }

  @Test
  void whenLowAndDurationValueAndDurationUnitIsPresent_ThenBuildDurationString() {
    String low = "2022-11-01";
    String durationValue = "6";
    String durationUnit = "months";
    Assertions.assertEquals(
        "From: 2022-11-01 For: 6 months",
        HL7V3ParsingUtils.buildDurationString(low, durationValue, durationUnit, null));
  }

  @Test
  void whenLowAndDurationValueIsPresent_ThenBuildBlankDurationString() {
    String low = "2022-11-01";
    String durationValue = "6";
    Assertions.assertEquals(
        "From: 2022-11-01", HL7V3ParsingUtils.buildDurationString(low, durationValue, null, null));
  }

  @Test
  void whenDurationValueAndDurationUnitIsPresent_ThenBuildDurationString() {
    String durationValue = "6";
    String durationUnit = "months";
    Assertions.assertEquals(
        "For: 6 months",
        HL7V3ParsingUtils.buildDurationString(null, durationValue, durationUnit, null));
  }

  @Test
  void whenLowAndHighIsPresent_ThenBuildDurationString() {
    String low = "2022-11-01";
    String high = "2023-05-31";
    Assertions.assertEquals(
        "From: 2022-11-01 Until: 2023-05-31",
        HL7V3ParsingUtils.buildDurationString(low, null, null, high));
  }

  @Test
  void whenDurationValueAndDurationUnitAndHighIsPresent_ThenBuildDurationString() {
    String durationValue = "6";
    String durationUnit = "months";
    String high = "2023-05-31";
    Assertions.assertEquals(
        "For: 6 months Until: 2023-05-31",
        HL7V3ParsingUtils.buildDurationString(null, durationValue, durationUnit, high));
  }

  @Test
  void whenLowAndDurationValueAndDurationUnitAndHighIsPresent_ThenBuildDurationString() {
    String low = "2022-11-01";
    String durationValue = "6";
    String durationUnit = "months";
    String high = "2023-05-31";
    Assertions.assertEquals(
        "From: 2022-11-01 For: 6 months Until: 2023-05-31",
        HL7V3ParsingUtils.buildDurationString(low, durationValue, durationUnit, high));
  }

  @Test
  void whenAllParametersNull_ThenBuildBlankDurationString() {
    Assertions.assertEquals(
        StringUtils.EMPTY, HL7V3ParsingUtils.buildDurationString(null, null, null, null));
  }
}
