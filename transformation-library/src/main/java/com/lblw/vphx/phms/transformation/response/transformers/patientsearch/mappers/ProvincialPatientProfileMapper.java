package com.lblw.vphx.phms.transformation.response.transformers.patientsearch.mappers;

import com.lblw.vphx.phms.common.databind.BindMany;
import com.lblw.vphx.phms.common.databind.XPathTarget;
import com.lblw.vphx.phms.common.utils.DateUtils;
import com.lblw.vphx.phms.domain.common.Address;
import com.lblw.vphx.phms.domain.common.Gender;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.SystemIdentifier;
import com.lblw.vphx.phms.domain.patient.response.ProvincialPatientProfile;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public class ProvincialPatientProfileMapper extends ProvincialPatientProfile {
  private static final String MTH = "MTH";
  private static final String FTH = "FTH";
  private static final String PROBABILITY_MATCH_OBSERVATION = "ProbabilityMatchObservation";

  private static final String QC = "Qc";
  private static final String NIU_U = "NIU-U";
  private static final String NAM = "NAM";

  @XPathTarget
  public void bindIdentifier(
      @XPathTarget.Binding(
              xPath =
                  "subject/registrationEvent/subject/identifiedEntity/id[@root=\"2.16.840.1.113883.4.56\"]/@extension")
          String identifier) {
    if (StringUtils.isBlank(identifier)) {
      return;
    }

    super.setIdentifier(
        SystemIdentifier.builder()
            .type(SystemIdentifier.IDENTIFIER_TYPE.PATIENT)
            .value(identifier)
            .assigner(QC)
            .system(NIU_U)
            .build());
  }

  @XPathTarget
  public void bindProvincialHealthNumber(
      @XPathTarget.Binding(
              xPath =
                  "subject/registrationEvent/subject/identifiedEntity/id[@root=\"2.16.124.10.101.1.60.100\"]/@extension")
          String provincialHealthNumber) {
    if (StringUtils.isBlank(provincialHealthNumber)) {
      return;
    }

    super.setProvincialHealthNumber(
        SystemIdentifier.builder()
            .type(SystemIdentifier.IDENTIFIER_TYPE.HEALTH_NUMBER)
            .value(provincialHealthNumber)
            .system(NAM)
            .assigner(QC)
            .build());
  }

  @XPathTarget
  public void bindFirstName(
      @XPathTarget.Binding(
              xPath =
                  "subject/registrationEvent/subject/identifiedEntity/identifiedPerson/name/given/text()")
          String firstName) {
    super.setFirstName(firstName);
  }

  @XPathTarget
  public void bindLastName(
      @XPathTarget.Binding(
              xPath =
                  "subject/registrationEvent/subject/identifiedEntity/identifiedPerson/name/family/text()")
          String lastName) {
    super.setLastName(lastName);
  }

  @XPathTarget
  public void bindDateOfBirth(
      @XPathTarget.Binding(
              xPath =
                  "subject/registrationEvent/subject/identifiedEntity/identifiedPerson/birthTime/@value")
          String dateOfBirth) {
    if (StringUtils.isBlank(dateOfBirth)) {
      return;
    }
    super.setDateOfBirth(DateUtils.parseProvincialDateFormatToLocalDate(Province.QC, dateOfBirth));
  }

  @XPathTarget
  public void bindGender(
      @XPathTarget.Binding(
              xPath =
                  "subject/registrationEvent/subject/identifiedEntity/identifiedPerson/administrativeGenderCode/@code")
          String gender) {
    if (StringUtils.isBlank(gender)) {
      return;
    }
    super.setGender(Gender.valueOf(gender));
  }

  @XPathTarget
  public void bindDeceasedDate(
      @XPathTarget.Binding(
              xPath =
                  "subject/registrationEvent/subject/identifiedEntity/identifiedPerson/deceasedTime/@value")
          String deceasedDate,
      @XPathTarget.Binding(
              xPath =
                  "subject/registrationEvent/subject/identifiedEntity/identifiedPerson/deceasedInd/@value")
          String deceasedIndicator) {
    if (StringUtils.isBlank(deceasedDate)
        || !Boolean.TRUE.toString().equalsIgnoreCase(deceasedIndicator)) {
      return;
    }

    super.setDeceasedDate(
        DateUtils.parseProvincialDateFormatToLocalDate(Province.QC, deceasedDate));
    super.setDeceasedIndicator(Boolean.TRUE);
  }

  @XPathTarget
  public void bindPersonalRelationship(
      @XPathTarget.Binding(
              xPath =
                  "subject/registrationEvent/subject/identifiedEntity/identifiedPerson/personalRelationship[x]")
          BindMany<PersonalRelationshipMapper> binder) {
    if (Objects.isNull(binder)) {
      return;
    }

    binder.apply(PersonalRelationshipMapper::new).forEach(this::setFatherNameAndMotherName);
  }

  private void setFatherNameAndMotherName(PersonalRelationshipMapper personalRelationship) {
    if (MTH.equals(personalRelationship.getCode())) {
      super.setMotherFirstName(personalRelationship.getFirstName());
      super.setMotherLastName(personalRelationship.getLastName());
    } else if (FTH.equals(personalRelationship.getCode())) {
      super.setFatherFirstName(personalRelationship.getFirstName());
      super.setFatherLastName(personalRelationship.getLastName());
    }
  }

  @XPathTarget
  public void bindAddress(
      @XPathTarget.Binding(
              xPath =
                  "subject/registrationEvent/subject/identifiedEntity/identifiedPerson/addr/streetAddressLine/text()")
          String streetAddressLine,
      @XPathTarget.Binding(
              xPath =
                  "subject/registrationEvent/subject/identifiedEntity/identifiedPerson/addr/city/text()")
          String city,
      @XPathTarget.Binding(
              xPath =
                  "subject/registrationEvent/subject/identifiedEntity/identifiedPerson/addr/state/text()")
          String province,
      @XPathTarget.Binding(
              xPath =
                  "subject/registrationEvent/subject/identifiedEntity/identifiedPerson/addr/country/text()")
          String country,
      @XPathTarget.Binding(
              xPath =
                  "subject/registrationEvent/subject/identifiedEntity/identifiedPerson/addr/postalCode/text()")
          String postalCode) {
    if (StringUtils.isAllBlank(streetAddressLine, city, province, country, postalCode)) {
      return;
    }

    super.setAddress(
        Address.builder()
            .streetAddressLine(streetAddressLine)
            .city(city)
            .country(country)
            .province(province)
            .postalCode(postalCode)
            .build());
  }

  @XPathTarget
  public void bindMatchingIndex(
      @XPathTarget.Binding(
              xPath =
                  "subject/registrationEvent/subject/identifiedEntity/subjectOf/observationEvent/value/@value")
          String observationEventValue,
      @XPathTarget.Binding(
              xPath =
                  "subject/registrationEvent/subject/identifiedEntity/subjectOf/observationEvent/code/@code")
          String observationEventCode) {

    if (StringUtils.isBlank(observationEventValue)) {
      return;
    }

    if (!PROBABILITY_MATCH_OBSERVATION.equals(observationEventCode)) {
      return;
    }

    double matchingIdxDouble = Double.parseDouble(observationEventValue) * 100;
    super.setMatchingIndex((int) matchingIdxDouble);
  }
}
