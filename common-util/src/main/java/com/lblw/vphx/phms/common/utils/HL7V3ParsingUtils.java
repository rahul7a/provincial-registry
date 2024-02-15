package com.lblw.vphx.phms.common.utils;

import static com.lblw.vphx.phms.common.constants.HL7Constants.*;

import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/** This class helps to parse HL7V3 conversions */
public class HL7V3ParsingUtils {

  private static final String FROM_PREFIX = "From: ";
  private static final String FOR_PREFIX = "For: ";
  private static final String UNTIL_PREFIX = "Until: ";

  /**
   * private Constructor of {@link HL7V3ParsingUtils} can be accessed within the class, all methods
   * are static
   */
  private HL7V3ParsingUtils() {}

  /**
   * returns days with given value and unit
   *
   * @param value {@link Double}
   * @param unit {@link String}
   * @return {@link Double}
   */
  public static Double convertToDays(Double value, String unit) {
    if (value == null || unit == null) {
      return null;
    }

    switch (unit) {
      case SUPPLY_DAYS_UNIT_WEEKS:
        value = value * 7;
        break;
      case SUPPLY_DAYS_UNIT_MONTHS:
        value = value * (365.0 / 12);
        break;
      case SUPPLY_DAYS_UNIT_YEAR:
        value = value * 365;
        break;
      case SUPPLY_DAYS_UNIT_DAYS:
        break;
      default:
        return null;
    }
    return (double) Math.round(value);
  }

  /**
   * builds durationString based on input parameters
   *
   * @param low - Format yyyy-MM-dd
   * @param high - Format yyyy-MM-dd
   * @return {@link String}
   */
  public static String buildDurationString(
      @Nullable String low,
      @Nullable String durationValue,
      @Nullable String durationUnit,
      @Nullable String high) {
    var durationString = new StringBuilder();
    if (low != null) {
      durationString.append(FROM_PREFIX).append(low);
    }
    if (durationValue != null && durationUnit != null) {
      durationString
          .append(StringUtils.SPACE)
          .append(FOR_PREFIX)
          .append(durationValue)
          .append(StringUtils.SPACE)
          .append(durationUnit);
    }
    if (high != null) {
      durationString.append(StringUtils.SPACE).append(UNTIL_PREFIX).append(high);
    }
    return durationString.toString().trim();
  }
}
