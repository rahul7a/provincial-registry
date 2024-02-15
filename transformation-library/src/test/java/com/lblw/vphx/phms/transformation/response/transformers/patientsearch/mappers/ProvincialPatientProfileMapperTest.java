package com.lblw.vphx.phms.transformation.response.transformers.patientsearch.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.lblw.vphx.phms.common.constants.HL7Constants;
import com.lblw.vphx.phms.common.databind.BindMany;
import com.lblw.vphx.phms.domain.common.Address;
import com.lblw.vphx.phms.domain.common.Gender;
import com.lblw.vphx.phms.domain.common.SystemIdentifier;
import java.time.LocalDate;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ProvincialPatientProfileMapperTest {
  private static final String MTH = "MTH";
  private static final String FTH = "FTH";
  ProvincialPatientProfileMapper provincialPatientProfileMapper;

  @BeforeEach
  void buildPProvincialPatientProfileMapper() {
    provincialPatientProfileMapper = new ProvincialPatientProfileMapper();
  }

  @Test
  void whenBinding_given_identifierIsNotBlank_thenSetIdentifier() {
    String identifierValue = "identifier";

    provincialPatientProfileMapper.bindIdentifier(identifierValue);
    assertThat(
            SystemIdentifier.builder()
                .type(SystemIdentifier.IDENTIFIER_TYPE.PATIENT)
                .value(identifierValue)
                .assigner(HL7Constants.QC)
                .system(HL7Constants.NIU_U)
                .build())
        .usingRecursiveComparison()
        .isEqualTo(provincialPatientProfileMapper.getIdentifier());
  }

  @Test
  void whenBinding_given_identifierIsNull_thenNotSetIdentifier() {
    String identifierValue = null;

    provincialPatientProfileMapper.bindIdentifier(identifierValue);
    assertNull(provincialPatientProfileMapper.getIdentifier());
  }

  @Test
  void whenBinding_given_identifierIsBlank_thenNotSetIdentifier() {
    String identifierValue = "";

    provincialPatientProfileMapper.bindIdentifier(identifierValue);
    assertNull(provincialPatientProfileMapper.getIdentifier());
  }

  @Test
  void whenBinding_given_ProvincialHealthNumberIsNotBlank_thenSetProvincialHealthNumber() {
    String provincialHealthNumberValue = "provincialHealthNumber";
    provincialPatientProfileMapper.bindProvincialHealthNumber(provincialHealthNumberValue);

    assertThat(
            SystemIdentifier.builder()
                .type(SystemIdentifier.IDENTIFIER_TYPE.HEALTH_NUMBER)
                .value(provincialHealthNumberValue)
                .system(HL7Constants.NAM)
                .assigner(HL7Constants.QC)
                .build())
        .usingRecursiveComparison()
        .isEqualTo(provincialPatientProfileMapper.getProvincialHealthNumber());
  }

  @Test
  void whenBinding_given_ProvincialHealthNumberIsNull_thenNotSetProvincialHealthNumber() {
    String provincialHealthNumberValue = null;
    provincialPatientProfileMapper.bindProvincialHealthNumber(provincialHealthNumberValue);
    assertNull(provincialPatientProfileMapper.getProvincialHealthNumber());
  }

  @Test
  void whenBinding_given_ProvincialHealthNumberIsBlank_thenNotSetProvincialHealthNumber() {
    String identifierValue = "";
    provincialPatientProfileMapper.bindProvincialHealthNumber(identifierValue);
    assertNull(provincialPatientProfileMapper.getIdentifier());
  }

  @Test
  void whenBinding_given_FirstName_thenSetFirstName() {
    String firstName = "firstName";
    provincialPatientProfileMapper.bindFirstName(firstName);
    assertEquals(firstName, provincialPatientProfileMapper.getFirstName());
  }

  @Test
  void whenBinding_given_LastName_thenSetLastName() {
    String lastName = "lastName";
    provincialPatientProfileMapper.bindLastName(lastName);
    assertEquals(lastName, provincialPatientProfileMapper.getLastName());
  }

  @Test
  void whenBinding_given_DateOfBirthIsNotBlank_thenSetDateOfBirth() {
    String dateOfBirth = "19960604";
    provincialPatientProfileMapper.bindDateOfBirth(dateOfBirth);

    assertEquals(LocalDate.of(1996, 6, 4), provincialPatientProfileMapper.getDateOfBirth());
  }

  @Test
  void whenBinding_given_DateOfBirthIsNull_thenNotSetDateOfBirth() {
    String dateOfBirth = null;
    provincialPatientProfileMapper.bindDateOfBirth(dateOfBirth);
    assertNull(provincialPatientProfileMapper.getDateOfBirth());
  }

  @Test
  void whenBinding_given_DateOfBirthIsBlank_thenNotSetDateOfBirth() {
    String dateOfBirth = "";
    provincialPatientProfileMapper.bindDateOfBirth(dateOfBirth);
    assertNull(provincialPatientProfileMapper.getDateOfBirth());
  }

  @Test
  void whenBinding_given_GenderIsNotBlank_thenSetGender() {
    String gender = "M";
    provincialPatientProfileMapper.bindGender(gender);

    assertEquals(Gender.M, provincialPatientProfileMapper.getGender());
  }

  @Test
  void whenBinding_given_GenderIsNull_thenNotSetGender() {
    String gender = null;
    provincialPatientProfileMapper.bindGender(gender);
    assertNull(provincialPatientProfileMapper.getGender());
  }

  @Test
  void whenBinding_given_GenderIsBlank_thenNotSetGender() {
    String gender = "";
    provincialPatientProfileMapper.bindGender(gender);
    assertNull(provincialPatientProfileMapper.getGender());
  }

  @Test
  void
      whenBinding_given_DeceasedDateIsNotBlankAndDeceasedIndicatorIsTrue_thenSetDeceasedDateAndSetDeceasedIndicator() {
    String deceasedDate = "20220404";
    String deceasedIndicator = "true";
    provincialPatientProfileMapper.bindDeceasedDate(deceasedDate, deceasedIndicator);

    assertEquals(LocalDate.of(2022, 4, 4), provincialPatientProfileMapper.getDeceasedDate());
    assertTrue(provincialPatientProfileMapper.getDeceasedIndicator());
  }

  @Test
  void
      whenBinding_given_DeceasedDateIsNotBlankAndDeceasedIndicatorIsFalse_thenNeitherSetDeceasedDateNorSetDeceasedIndicator() {
    String deceasedDate = "20220404";
    String deceasedIndicator = "false";
    provincialPatientProfileMapper.bindDeceasedDate(deceasedDate, deceasedIndicator);

    assertNull(provincialPatientProfileMapper.getDeceasedDate());
    assertNull(provincialPatientProfileMapper.getDeceasedIndicator());
  }

  @Test
  void
      whenBinding_given_DeceasedDateIsBlankAndDeceasedIndicatorIsTrue_thenNeitherSetDeceasedDateNorSetDeceasedIndicator() {
    String deceasedDate = "";
    String deceasedIndicator = "true";
    provincialPatientProfileMapper.bindDeceasedDate(deceasedDate, deceasedIndicator);

    assertNull(provincialPatientProfileMapper.getDeceasedDate());
    assertNull(provincialPatientProfileMapper.getDeceasedIndicator());
  }

  @Test
  void
      whenBinding_given_DeceasedDateIsNullAndDeceasedIndicatorIsTrue_thenNeitherSetDeceasedDateNorSetDeceasedIndicator() {
    String deceasedDate = null;
    String deceasedIndicator = "true";
    provincialPatientProfileMapper.bindDeceasedDate(deceasedDate, deceasedIndicator);

    assertNull(provincialPatientProfileMapper.getDeceasedDate());
    assertNull(provincialPatientProfileMapper.getDeceasedIndicator());
  }

  @Test
  void
      whenBinding_given_PersonalRelationshipBinderNotNull_thenSetMotherFistNameAndLastNameAndSetFatherFirstNameAndLastName() {
    var father = new PersonalRelationshipMapper();
    father.bindCode(FTH);
    father.bindFirstName("fatherFistName");
    father.bindLastName("fatherLastName");
    var mother = new PersonalRelationshipMapper();
    mother.bindCode(MTH);
    mother.bindFirstName("motherFistName");
    mother.bindLastName("motherLastName");
    BindMany<PersonalRelationshipMapper> bindMany = (supplier) -> Stream.of(father, mother);
    provincialPatientProfileMapper.bindPersonalRelationship(bindMany);
    assertEquals("fatherFistName", provincialPatientProfileMapper.getFatherFirstName());
    assertEquals("fatherLastName", provincialPatientProfileMapper.getFatherLastName());

    assertEquals("motherFistName", provincialPatientProfileMapper.getMotherFirstName());
    assertEquals("motherLastName", provincialPatientProfileMapper.getMotherLastName());
  }

  @Test
  void
      whenBinding_givenPersonalRelationshipBinderIsNull_thenSetMotherFistNameAndLastNameAndSetFatherFirstNameAndLastName() {

    BindMany<PersonalRelationshipMapper> bindMany = null;
    provincialPatientProfileMapper.bindPersonalRelationship(bindMany);
    assertNull(provincialPatientProfileMapper.getFatherFirstName());
    assertNull(provincialPatientProfileMapper.getFatherLastName());

    assertNull(provincialPatientProfileMapper.getMotherFirstName());
    assertNull(provincialPatientProfileMapper.getMotherLastName());
  }

  @Test
  void whenBinding_givenNotAllAddressFieldBlank_thenSetAddress() {
    String streetAddressLine = "streetAddressLine";
    String city = "city";
    String province = "province";
    String country = "country";
    String postalCode = "postalCode";
    provincialPatientProfileMapper.bindAddress(
        streetAddressLine, city, province, country, postalCode);
    assertThat(
            Address.builder()
                .streetAddressLine(streetAddressLine)
                .city(city)
                .country(country)
                .province(province)
                .postalCode(postalCode)
                .build())
        .usingRecursiveComparison()
        .isEqualTo(provincialPatientProfileMapper.getAddress());
  }

  @Test
  void whenBinding_givenAllAddressFieldBlank_thenSetAddress() {
    String streetAddressLine = "";
    String city = null;
    String province = "";
    String country = null;
    String postalCode = "";
    provincialPatientProfileMapper.bindAddress(
        streetAddressLine, city, province, country, postalCode);
    assertNull(provincialPatientProfileMapper.getAddress());
  }
}
