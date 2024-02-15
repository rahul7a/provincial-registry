package com.lblw.vphx.phms.common.utils;

import static com.lblw.vphx.phms.domain.common.DateFormat.*;

import com.lblw.vphx.phms.common.constants.CommonConstants;
import com.lblw.vphx.phms.domain.common.DateFormat;
import com.lblw.vphx.phms.domain.common.Province;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.EnumMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

@Slf4j
/**
 * Provides utility methods to format and parse provincial and system dates. Uses date format
 * defined in {@link PhmsDateFormat}
 */
public class DateUtils {

  /** Date formatter used across all VPHX subdomains. */
  private static final DateTimeFormatter SYSTEM_DATE_FORMATTER =
      DateTimeFormatter.ofPattern(SYSTEM_DATE_FORMAT);
  /** List of date formatter of supported provinces used in provincial messaging. */
  private static final Map<Province, DateTimeFormatter> provinceDateFormatterMap =
      new EnumMap<>(Province.class);
  /** List of date time formatter of supported provinces used in provincial messaging. */
  private static final Map<Province, DateTimeFormatter> provinceDateTimeFormatterMap =
      new EnumMap<>(Province.class);
  /** Map of ZoneId of supported provinces used in provincial messaging. */
  private static final Map<Province, ZoneId> provinceZoneIdMap = new EnumMap<>(Province.class);
  /** List of date time formatter of supported provinces used in provincial messaging. */
  private static final Map<Province, DateTimeFormatter> provinceDateTimeWithOffsetFormatterMap =
      new EnumMap<>(Province.class);

  /** Initializes {@link DateTimeFormatter} of supported provinces. */
  static {
    provinceDateFormatterMap.put(Province.QC, DateTimeFormatter.ofPattern(QC_DATE_FORMAT));
    provinceDateTimeFormatterMap.put(Province.QC, DateTimeFormatter.ofPattern(QC_DATE_TIME_FORMAT));
    provinceZoneIdMap.put(Province.QC, ZoneId.of("Canada/Eastern"));
    provinceDateTimeWithOffsetFormatterMap.put(
        Province.QC, DateTimeFormatter.ofPattern(DateFormat.QC_DATE_TIME_FORMAT_WITH_OFFSET));
  }

  private DateUtils() {}

  /**
   * Parses system date to local date
   *
   * @param dateInSystemFormat {@link String}
   * @return {@link LocalDate}
   */
  public static LocalDate parseSystemDateFormatToLocalDate(String dateInSystemFormat) {

    if (StringUtils.isBlank(dateInSystemFormat)) {
      return null;
    }

    try {
      return LocalDate.parse(dateInSystemFormat, SYSTEM_DATE_FORMATTER);
    } catch (DateTimeParseException ex) {
      log.error("Unable to format  dateInSystemFormat= " + dateInSystemFormat);
      return null;
    }
  }

  /**
   * Parses provincial date to local date
   *
   * @param province {@link Province}
   * @param dateInProvincialFormat {@link String}
   * @return {@link LocalDate}
   */
  public static LocalDate parseProvincialDateFormatToLocalDate(
      Province province, String dateInProvincialFormat) {
    var dateFormatter = provinceDateFormatterMap.get(province);
    if (StringUtils.isBlank(dateInProvincialFormat)) {
      return null;
    }
    if (dateFormatter == null) {
      throw new IllegalArgumentException(
          String.format(
              CommonConstants.DATE_FORMATTER_NOT_SUPPORTED_FOR_PROVINCE, province.name()));
    }
    try {
      return LocalDate.parse(dateInProvincialFormat, dateFormatter);
    } catch (DateTimeParseException ex) {
      log.error("Unable to format  dateInProvincialFormat= " + dateInProvincialFormat);
      return null;
    }
  }

  /**
   * Parses provincial date time with offset to local date
   *
   * @param province {@link Province}
   * @param dateTimeInProvincialFormat {@link String}
   * @return {@link LocalDate}, will return null if null/blank, or unable to format.
   */
  public static LocalDate parseProvincialDateTimeOffsetFormatToLocalDate(
      Province province, String dateTimeInProvincialFormat) {
    var dateFormatter = provinceDateTimeWithOffsetFormatterMap.get(province);
    if (StringUtils.isBlank(dateTimeInProvincialFormat)) {
      return null;
    }
    if (dateFormatter == null) {
      throw new IllegalArgumentException(
          String.format(
              CommonConstants.DATE_FORMATTER_NOT_SUPPORTED_FOR_PROVINCE, province.name()));
    }
    try {
      var instant =
          ZonedDateTime.of(
                  LocalDateTime.parse(dateTimeInProvincialFormat, dateFormatter), ZoneOffset.UTC)
              .toInstant();

      return LocalDate.ofInstant(instant, ZoneOffset.UTC);
    } catch (DateTimeParseException ex) {
      log.error("Unable to format  dateTimeInProvincialFormat= " + dateTimeInProvincialFormat);
      return null;
    }
  }

  /**
   * Formats local date to system date
   *
   * @param localDate {@link LocalDate}
   * @return date in system format
   */
  public static String formatToSystemDateFormat(LocalDate localDate) {
    if (localDate == null) {
      return null;
    }
    return SYSTEM_DATE_FORMATTER.format(localDate);
  }

  /**
   * Formats local date to provincial date
   *
   * @param province {@link Province}
   * @param localDate {@link LocalDate}
   * @return date in provincial format
   */
  public static String formatToProvincialDateFormat(Province province, LocalDate localDate) {

    var dateFormatter = provinceDateFormatterMap.get(province);
    if (localDate == null) {
      return null;
    }
    if (dateFormatter == null) {
      throw new IllegalArgumentException(
          String.format(
              CommonConstants.DATE_FORMATTER_NOT_SUPPORTED_FOR_PROVINCE, province.name()));
    }
    return dateFormatter.format(localDate);
  }

  /**
   * Parses provincial date time to instant date time
   *
   * @param province {@link Province}
   * @param dateTimeInProvincialFormat can be null{@link String}
   * @return {@link Instant}
   */
  @Nullable
  public static Instant parseProvincialDateTimeFormatToInstantDateTime(
      Province province, String dateTimeInProvincialFormat) {
    if (StringUtils.isBlank(dateTimeInProvincialFormat)) {
      return null;
    }
    var dateTimeFormatter = provinceDateTimeFormatterMap.get(province);
    if (dateTimeFormatter == null) {
      throw new IllegalArgumentException(
          "dateTime formatter not supported for province= " + province.name());
    }
    try {
      return ZonedDateTime.of(
              LocalDateTime.parse(dateTimeInProvincialFormat, dateTimeFormatter),
              provinceZoneIdMap.get(province))
          .toInstant();
    } catch (DateTimeParseException ex) {
      log.error("Unable to format  dateTimeInProvincialFormat= " + dateTimeInProvincialFormat);
      return null;
    }
  }

  /**
   * Formats formatted date to LocalDate.
   *
   * @param inputDate {@link String}
   * @param fromFormatPattren {@link String}
   * @return LocalDate
   */
  public static LocalDate formatDateToLocalDate(String inputDate, String fromFormatPattren) {
    if (StringUtils.isBlank(inputDate) || StringUtils.isBlank(fromFormatPattren)) {
      return null;
    }
    try {
      return LocalDate.parse(inputDate, DateTimeFormatter.ofPattern(fromFormatPattren));
    } catch (DateTimeParseException ex) {
      log.error("Unable to format  date= " + fromFormatPattren);
      return null;
    }
  }
  /**
   * Formats local date to provincial datetime
   *
   * @param instant {@link Instant}
   * @return String in provincial dateTime format
   */
  public static String formatToProvincialDateTimeFormat(Province province, Instant instant) {
    validateProvince(province);
    var dateFormatter = provinceDateTimeWithOffsetFormatterMap.get(province);
    validateDateFormatter(province, dateFormatter);
    return dateFormatter.withZone(ZoneOffset.UTC).format(instant);
  }

  /**
   * Formats given date and time in DateTimeFormatter.ISO_OFFSET_DATE_TIME to required format
   *
   * @param date in data time format
   * @param outputFormat {@link DateTimeFormatter}
   * @return String in given output format
   */
  public static String toDateFormatYrsToSecInUTCWithDate(
      String date, DateTimeFormatter outputFormat) {
    if (StringUtils.isBlank(date) || outputFormat == null) {
      return null;
    }
    try {
      OffsetDateTime offsetDateTime = OffsetDateTime.parse(date);
      return outputFormat.format(offsetDateTime).replace("T", "");
    } catch (DateTimeParseException ex) {
      log.error("Unable to format  date= " + outputFormat);
      return null;
    }
  }

  /**
   * Formats local date to provincial enablement datetime
   *
   * @param province {@link Province}
   * @param instant {@link Instant}
   * @return String in provincial enablement dateTime format
   */
  public static String formatToProvincialEnablementDateTimeFormat(
      Province province, Instant instant) {
    validateProvince(province);
    var dateFormatter =
        DateTimeFormatter.ofPattern(PROVINCIAL_ENABLEMENT_DATE_TIME_FORMAT_WITH_OFFSET);
    // TODO: Need to confirm from pe ,do we need offset as ZoneOffset.UTC or with particular zone
    // offset
    return dateFormatter.withZone(provinceZoneIdMap.get(province)).format(instant);
  }

  /**
   * Formats local date to provincial enablement hyphen delimited datetime
   *
   * @param province {@link Province}
   * @param instant {@link Instant}
   * @return String in provincial enablement hyphen delimited dateTime format
   */
  public static String formatToProvincialEnablementHyphenDelimitedDateTimeFormat(
      Province province, Instant instant) {
    validateProvince(province);
    var dateFormatter =
        DateTimeFormatter.ofPattern(
            PROVINCIAL_ENABLEMENT_HYPHEN_DELIMITED_DATE_TIME_FORMAT_WITH_OFFSET);
    return dateFormatter.withZone(provinceZoneIdMap.get(province)).format(instant);
  }

  /**
   * validate province object ,if its null ,will throw exception
   *
   * @param province {@link Province}
   */
  private static void validateProvince(Province province) {
    if (province == null) {
      throw new IllegalArgumentException(" province is passed as null");
    }
  }

  /**
   * validate date formatter object ,if its null ,will throw exception
   *
   * @param province {@link Province}
   * @param dateFormatter {@link DateTimeFormatter}
   */
  private static void validateDateFormatter(Province province, DateTimeFormatter dateFormatter) {
    if (dateFormatter == null) {
      throw new IllegalArgumentException(
          "DateTime formatter for province=" + province.name() + " is not supported");
    }
  }

  /**
   * Drop-in replacement for Instant.now() that also considers offsets from current context {@link
   * Province} when applicable
   *
   * @param province {@link Province}
   * @return {@link Instant}
   */
  public static Instant getCurrentTimeStamp(Province province) {
    return ZonedDateTime.now(provinceZoneIdMap.get(province)).toInstant();
  }
}
