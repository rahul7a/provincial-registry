package com.lblw.vphx.phms.common.utils;

import static com.lblw.vphx.phms.common.utils.DateUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.lblw.vphx.phms.domain.common.Province;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DateUtilsTest {

  @Nested
  @DisplayName("System date format to LocalDate")
  class SystemFormatToLocalDateTest {
    @Test
    void should_parse_valid_system_date_format() {
      var date = parseSystemDateFormatToLocalDate("1980-01-27");
      assertThat(date).isEqualTo(LocalDate.of(1980, 1, 27));
    }

    @Test
    void should_not_parse_invalid_system_date_format() {
      var date = parseSystemDateFormatToLocalDate("1980-0127");
      assertThat(date).isNull();
    }

    @Test
    void should_not_parse_null_system_date() {
      var date = parseSystemDateFormatToLocalDate(null);
      assertThat(date).isNull();
    }
  }

  @Nested
  @DisplayName("LocalDate format to System format")
  class LocalDateToSystemFormatTest {
    @Test
    void should_format_to_valid_system_date_format() {
      var date = formatToSystemDateFormat(LocalDate.of(1980, 1, 27));
      assertThat(date).isEqualTo("1980-01-27");
    }

    @Test
    void should_return_null_for_null_date() {
      assertNull(formatToSystemDateFormat(null));
    }
  }

  @Nested
  @DisplayName("QC date format to LocalDate")
  class QCFormatToLocalDateTest {
    @Test
    void should_parse_valid_QC_date_format() {
      var date = parseProvincialDateFormatToLocalDate(Province.QC, "19800127");
      assertThat(date).isEqualTo(LocalDate.of(1980, 1, 27));
    }

    @Test
    void should_parse_valid_QC_datetime_offset_format() {
      var date = parseProvincialDateTimeOffsetFormatToLocalDate(Province.QC, "20130921040000+0000");
      assertThat(date).isEqualTo(LocalDate.of(2013, 9, 21));
    }

    @Test
    void should_parse_invalid_QC_datetime_offset_format() {
      var date = parseProvincialDateTimeOffsetFormatToLocalDate(Province.QC, "20130921040000");
      assertThat(date).isNull();
    }

    @Test
    void should_not_parse_invalid_QC_date_format() {
      var date = parseProvincialDateFormatToLocalDate(Province.QC, "1980-0127");
      assertThat(date).isNull();
    }

    @Test
    void should_not_parse_null_QC_date() {
      var date = parseProvincialDateFormatToLocalDate(Province.QC, null);
      assertThat(date).isNull();
    }

    @Test
    void should_not_parse_unsupported_province() {
      Assertions.assertThrows(
          IllegalArgumentException.class,
          () -> parseProvincialDateFormatToLocalDate(Province.ON, "1980-0127"));
    }
  }

  @Nested
  @DisplayName("LocalDate format to QC format")
  class LocalDateToQCFormatTest {
    @Test
    void should_format_to_valid_system_date_format() {
      var localDate = LocalDate.of(1980, 1, 27);
      var date = formatToProvincialDateFormat(Province.QC, localDate);
      assertThat(date).isEqualTo("19800127");
    }

    @Test
    void should_return_null_for_null_date() {
      var date = formatToProvincialDateFormat(Province.QC, null);
      assertThat(date).isNull();
    }

    @Test
    void should_not_format_unsupported_province() {
      var localDate = LocalDate.of(1980, 1, 27);
      Assertions.assertThrows(
          IllegalArgumentException.class,
          () -> formatToProvincialDateFormat(Province.ON, localDate));
    }
  }

  @Nested
  @DisplayName("QC Date Time format to Instant Date Time")
  class QCFormatToInstantDateTimeTest {
    @Test
    void should_parse_valid_QC_date_format() {
      var dateTime = parseProvincialDateTimeFormatToInstantDateTime(Province.QC, "20210629000000");
      var localDate = LocalDateTime.of(2021, 6, 29, 4, 0, 0).toInstant(ZoneOffset.UTC);
      assertThat(dateTime).isEqualTo(localDate);
    }

    @Test
    void should_not_parse_invalid_QC_dateTime_format() {
      var date = parseProvincialDateTimeFormatToInstantDateTime(Province.QC, "20210629-000000");
      assertThat(date).isNull();
    }

    @Test
    void should_not_parse_null_QC_dateTime() {
      var date = parseProvincialDateTimeFormatToInstantDateTime(Province.QC, null);
      assertThat(date).isNull();
    }

    @Test
    void should_not_parse_unsupported_province() {
      Assertions.assertThrows(
          IllegalArgumentException.class,
          () -> parseProvincialDateTimeFormatToInstantDateTime(Province.ON, "20210629000000"));
    }

    @Test
    void should_parse_null_provincial_date() {
      var now = Instant.now();
      Assertions.assertThrows(
          IllegalArgumentException.class, () -> formatToProvincialDateTimeFormat(null, now));
    }

    @Test
    void should_parse_valid_provincial_date_format() {
      Instant instant = Instant.parse("2022-07-26T00:00:00.00Z");
      var dateTime = formatToProvincialDateTimeFormat(Province.QC, instant);
      assertThat(dateTime).isEqualTo("20220726000000+0000");
    }

    @Test
    void should_parse_unsupported_province() {
      var now = Instant.now();
      Assertions.assertThrows(
          IllegalArgumentException.class, () -> formatToProvincialDateTimeFormat(Province.ON, now));
    }

    @Test
    void should_parse_null_provincial_date_with_provincial_enablement() {
      var now = Instant.now();
      Assertions.assertThrows(
          IllegalArgumentException.class,
          () -> formatToProvincialEnablementDateTimeFormat(null, now));
    }

    @Test
    void should_parse_valid_provincial_date_format_with_provincial_enablement() {
      Instant instant = Instant.parse("2022-07-26T00:00:00.00Z");
      var dateTime = formatToProvincialEnablementDateTimeFormat(Province.QC, instant);
      assertThat(dateTime).isEqualTo("20220725200000.000-0400");
    }

    @Test
    void should_not_parse_date_with_hyphen_provincial_enablement_for_province_null() {
      var now = Instant.now();
      Assertions.assertThrows(
              IllegalArgumentException.class,
              () -> formatToProvincialEnablementHyphenDelimitedDateTimeFormat(null, now));
    }

    @Test
    void should_parse_valid_provincial_date_format_with_hyphen() {
      Instant instant = LocalDateTime.of(2022, 7, 26, 0, 0, 0).toInstant(ZoneOffset.UTC);
      var dateTime = formatToProvincialEnablementHyphenDelimitedDateTimeFormat(Province.QC, instant);
      assertThat(dateTime).isEqualTo("2022-07-25T20:00:00.000Z");
    }
  }

  @Nested
  @DisplayName("Date Time format to UTC Date Time")
  class QCFormatYrsToSecInUTCWithDateTest {
    @Test
    void should_parse_valid_utc_date_format() {
      var actualDate =
          toDateFormatYrsToSecInUTCWithDate(
              "2023-01-19T14:34:01.345Z", DateTimeFormatter.ofPattern("yyyyMMddHHmmss.SSSZ"));
      assertEquals("20230119143401.345+0000", actualDate);
    }

    @Test
    void should_return_null_for_date_empty() {
      var actualDate =
          toDateFormatYrsToSecInUTCWithDate("", DateTimeFormatter.ofPattern("yyyyMMddHHmmss.SSSZ"));
      assertEquals(null, actualDate);
    }

    @Test
    void should_return_null_id_date_formatter_is_null() {
      var actualDate = toDateFormatYrsToSecInUTCWithDate("2023-01-19T14:34:01.345Z", null);
      assertEquals(null, actualDate);
    }

    @Test
    void should_throw_exception_for_invalid_date() {
      var actualDate =
          toDateFormatYrsToSecInUTCWithDate(
              "2023-01-19T14.34:01.345Z", DateTimeFormatter.ofPattern("yyyyMMddHHmmss.SSSZ"));
      assertEquals(null, actualDate);
    }
  }
}
