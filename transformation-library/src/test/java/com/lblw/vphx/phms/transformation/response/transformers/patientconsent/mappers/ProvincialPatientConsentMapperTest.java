package com.lblw.vphx.phms.transformation.response.transformers.patientconsent.mappers;

import com.lblw.vphx.phms.common.constants.HL7Constants;
import com.lblw.vphx.phms.domain.common.SystemIdentifier;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ProvincialPatientConsentMapperTest {

  private static ProvincialPatientConsentMapper provincialPatientConsentMapper;

  @BeforeAll
  static void buildProvincialPatientConsentMapper() {
    provincialPatientConsentMapper = new ProvincialPatientConsentMapper();
  }

  @Test
  void whenBinding_ThenSetConsentValidityStartDateTime() {
    provincialPatientConsentMapper.bindConsentValidityStartDateTime("20070101050000");
    Assertions.assertEquals(
        LocalDateTime.of(2007, 1, 1, 10, 0, 0).toInstant(ZoneOffset.UTC),
        provincialPatientConsentMapper.getConsentValidityStartDateTime());
  }

  @Test
  void whenBinding_ThenSetConsentValidityEndDateTime() {
    provincialPatientConsentMapper.bindConsentValidityEndDateTime("20211124215532");
    Assertions.assertEquals(
        LocalDateTime.of(2021, 11, 25, 2, 55, 32).toInstant(ZoneOffset.UTC),
        provincialPatientConsentMapper.getConsentValidityEndDateTime());
  }

  @Test
  void whenBinding_ThenSetIdentifier() {
    provincialPatientConsentMapper.bindIdentifier("8000000300");
    Assertions.assertEquals(
        SystemIdentifier.builder()
            .type(SystemIdentifier.IDENTIFIER_TYPE.PATIENT)
            .value("8000000300")
            .assigner(HL7Constants.QC)
            .system(HL7Constants.NIU_U)
            .build(),
        provincialPatientConsentMapper.getIdentifier());
  }
}
