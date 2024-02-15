package com.lblw.vphx.phms.transformation.response.transformers;

import com.lblw.vphx.phms.common.utils.DateUtils;
import com.lblw.vphx.phms.domain.common.Address;
import com.lblw.vphx.phms.domain.common.Gender;
import com.lblw.vphx.phms.domain.common.SystemIdentifier;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.common.response.*;
import com.lblw.vphx.phms.domain.common.response.hl7v3.AcknowledgementDetails;
import com.lblw.vphx.phms.domain.common.response.hl7v3.QueryAcknowledgement;
import com.lblw.vphx.phms.domain.common.response.hl7v3.ResponseBodyTransmissionWrapper;
import com.lblw.vphx.phms.domain.common.response.hl7v3.ResponseHeader;
import com.lblw.vphx.phms.domain.patient.request.ProvincialPatientSearchCriteria;
import com.lblw.vphx.phms.domain.patient.response.ProvincialPatientProfile;
import com.lblw.vphx.phms.domain.patient.response.hl7v3.ProvincialPatientSearchResponse;
import com.lblw.vphx.phms.domain.patient.response.hl7v3.ProvincialPatientSearchResponseControlAct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;

/**
 * This class is factory for creating Expected Test Responses for assertions. It builds instances of
 * {@link ProvincialPatientSearchResponse} from different properties file for each use case.
 */
public class ExpectedTestResponseFactory {

  /**
   * This builds {@link ProvincialPatientSearchResponse} instances to be used as expectations while
   * asserting for each use case specified by {@code caseName}
   *
   * @param caseName Name of the use case.
   * @return Prepared {@link ProvincialPatientSearchResponse} that is expectation for assertions
   */
  public ProvincialPatientSearchResponse buildExpectedProvincialPatientSearchResponse(
      String caseName) {
    String expectedPropertiesFile = null;
    switch (caseName) {
      case "patient_not_found":
        expectedPropertiesFile = "patientprofile/expectation/patient_not_found.properties";
        break;

      case "acknowledge_AE_queryack_QE":
        expectedPropertiesFile = "patientprofile/expectation/acknowledge_AE_queryack_QE.properties";
        break;

      case "ecartmin_success":
        expectedPropertiesFile = "patientprofile/expectation/ecartmin_success.properties";
        break;

      case "find_candidate_patient_found_response":
        expectedPropertiesFile =
            "patientprofile/expectation/find_candidate_patient_found_response.properties";
        break;

      case "missmand":
        expectedPropertiesFile = "patientprofile/expectation/missmand.properties";
        break;

      case "success":
        expectedPropertiesFile = "patientprofile/expectation/success.properties";
        break;

      case "unsval_date_creation_time_must_be_smaller":
        expectedPropertiesFile =
            "patientprofile/expectation/unsval_date_creation_time_must_be_smaller.properties";
        break;

      case "unsval_wrong_accept_ack_code":
        expectedPropertiesFile =
            "patientprofile/expectation/unsval_wrong_accept_ack_code.properties";
        break;

      case "deceased":
        expectedPropertiesFile = "patientprofile/expectation/deceased.properties";
        break;

      case "transmissionid":
        expectedPropertiesFile = "patientprofile/expectation/transmissionid.properties";
        break;

      case "multiple_acknowledgement_details":
        expectedPropertiesFile =
            "patientprofile/expectation/multiple_acknowledgement_details.properties";
        break;

      default:
        throw new IllegalStateException();
    }

    return buildExpectedProvincialPatientSearchResponse(
        loadExpectedProperties(expectedPropertiesFile));
  }

  private Properties loadExpectedProperties(String propertyPath) {
    Properties prop = new Properties();
    try {
      prop.load(
          ExpectedTestResponseFactory.class.getClassLoader().getResourceAsStream(propertyPath));
    } catch (IOException e) {
      // do nothing. This is test.
    }
    return prop;
  }

  private ProvincialPatientSearchResponse buildExpectedProvincialPatientSearchResponse(
      Properties properties) {
    return ProvincialPatientSearchResponse.builder()
        .responseHeader(buildResponseHeader(properties))
        .responseControlAct(buildResponseBodyControlAct(properties))
        .responseBodyTransmissionWrapper(buildResponseBodyTransmissionWrapper(properties))
        .build();
  }

  private Integer getMatchingIndex(Properties properties, String s) {
    final Object obj = properties.get(s);
    if (obj == null) {
      return null;
    }
    return (int) Float.parseFloat((String) obj);
  }

  private String get(Properties properties, String s) {
    final Object obj = properties.get(s);
    if (obj == null || ((String) obj).equalsIgnoreCase("null")) {
      return null;
    }
    return (String) obj;
  }

  private ProvincialPatientProfile buildProvincialPatientProfile(Properties properties) {

    final String provincialPatientProfile = get(properties, "ProvincialPatientProfile");
    if (provincialPatientProfile == null) {
      return ProvincialPatientProfile.builder().build();
    }
    final String gender = get(properties, "ProvincialPatientProfile.gender");
    return ProvincialPatientProfile.builder()
        .firstName(get(properties, "ProvincialPatientProfile.firstName"))
        .lastName(get(properties, "ProvincialPatientProfile.lastName"))
        .dateOfBirth(
            DateUtils.parseSystemDateFormatToLocalDate(
                get(properties, "ProvincialPatientProfile.dateOfBirth")))
        .gender(StringUtils.isBlank(gender) ? null : Gender.valueOf(gender))
        .deceasedDate(
            DateUtils.parseSystemDateFormatToLocalDate(
                get(properties, "ProvincialPatientProfile.deceasedDate")))
        .deceasedIndicator(
            Boolean.valueOf(get(properties, "ProvincialPatientProfile.deceasedIndicator")))
        .motherFirstName(get(properties, "ProvincialPatientProfile.motherFirstName"))
        .motherLastName(get(properties, "ProvincialPatientProfile.motherLastName"))
        .fatherFirstName(get(properties, "ProvincialPatientProfile.fatherFirstName"))
        .fatherLastName(get(properties, "ProvincialPatientProfile.fatherLastName"))
        .address(buildExpectedAddressInResponse(properties))
        .matchingIndex(getMatchingIndex(properties, "ProvincialPatientProfile.matchingIndex"))
        .provincialHealthNumber(buildProvincialHealthNumber(properties))
        .identifier(buildProvincialIdentifier(properties))
        .build();
  }

  private ResponseBodyTransmissionWrapper buildResponseBodyTransmissionWrapper(
      Properties properties) {
    return ResponseBodyTransmissionWrapper.builder()
        .acknowledgeTypeCode(get(properties, "ResponseBodyTransmissionWrapper.ackTypeCode"))
        .acknowledgementDetails(buildExpectedAcknowledgementDetails(properties))
        .transmissionUniqueIdentifier(
            get(properties, "ResponseBodyTransmissionWrapper.transmissionUUID"))
        .build();
  }

  private List<AcknowledgementDetails> buildExpectedAcknowledgementDetails(Properties properties) {
    final String acknowledgementDetailsExists = get(properties, "AcknowledgementDetails");
    if (acknowledgementDetailsExists == null) {
      return null;
    }
    int numberOfAcknowledgementDetails = 0;
    try {
      numberOfAcknowledgementDetails = Integer.parseInt(acknowledgementDetailsExists);
    } catch (NumberFormatException nfe) {
      numberOfAcknowledgementDetails = 0;
    }
    if (numberOfAcknowledgementDetails == 0) {
      return List.of(
          AcknowledgementDetails.builder()
              .typeCode(get(properties, "AcknowledgementDetails.typeCode"))
              .code(get(properties, "AcknowledgementDetails.code"))
              .location(get(properties, "AcknowledgementDetails.location"))
              .text(get(properties, "AcknowledgementDetails.text"))
              .build());
    }
    List<AcknowledgementDetails> acknowledgementDetails = new ArrayList<>();
    for (int index = 0; index < numberOfAcknowledgementDetails; index++) {
      String propertyPrefix = "AcknowledgementDetails[" + index + "]";
      final AcknowledgementDetails acknowledgementDetailInstance =
          AcknowledgementDetails.builder()
              .typeCode(get(properties, propertyPrefix + ".typeCode"))
              .code(get(properties, propertyPrefix + ".code"))
              .location(get(properties, propertyPrefix + ".location"))
              .text(get(properties, propertyPrefix + ".text"))
              .build();
      acknowledgementDetails.add(acknowledgementDetailInstance);
    }
    return acknowledgementDetails;
  }

  private ResponseHeader buildResponseHeader(Properties properties) {
    return ResponseHeader.builder()
        .sessionId(get(properties, "ResponseHeader.sessionId"))
        .trackingId(get(properties, "ResponseHeader.trackingId"))
        .transactionId(get(properties, "ResponseHeader.transactionId"))
        .build();
  }

  private SystemIdentifier buildProvincialHealthNumber(Properties properties) {
    return SystemIdentifier.builder()
        .assigner(get(properties, "ProvincialHealthNumber.assigner"))
        .type(SystemIdentifier.IDENTIFIER_TYPE.HEALTH_NUMBER)
        .value(get(properties, "ProvincialHealthNumber.value"))
        .system(get(properties, "ProvincialHealthNumber.system"))
        .build();
  }

  private SystemIdentifier buildProvincialIdentifier(Properties properties) {
    return SystemIdentifier.builder()
        .system(get(properties, "ProvincialIdentifier.system"))
        .value(get(properties, "ProvincialIdentifier.value"))
        .type(SystemIdentifier.IDENTIFIER_TYPE.PATIENT)
        .assigner(get(properties, "ProvincialIdentifier.assigner"))
        .build();
  }

  private ProvincialRequestControl buildProvincialRequestControl(Properties properties) {
    return ProvincialRequestControl.builder()
        .requestId(get(properties, "ProvincialRequestControl.correlationId"))
        .pharmacy(null)
        .build();
  }

  private Address buildExpectedAddressInResponse(Properties properties) {
    return Address.builder()
        .country(get(properties, "AddressInResponse.country"))
        .province(get(properties, "AddressInResponse.state"))
        .postalCode(get(properties, "AddressInResponse.postalCode"))
        .city(get(properties, "AddressInResponse.city"))
        .streetAddressLine(get(properties, "AddressInResponse.streetAddressLine"))
        .build();
  }

  private Address buildAddressInCriteria(Properties properties) {
    return Address.builder()
        .country(get(properties, "AddressInCriteria.country"))
        .province(get(properties, "AddressInCriteria.state"))
        .postalCode(get(properties, "AddressInCriteria.postalCode"))
        .city(get(properties, "AddressInCriteria.city"))
        .streetAddressLine(get(properties, "AddressInCriteria.streetAddressLine"))
        .build();
  }

  private ProvincialPatientSearchResponseControlAct buildResponseBodyControlAct(
      Properties properties) {
    return ProvincialPatientSearchResponseControlAct.builder()
        .eventRoot(get(properties, "ProvincialPatientSearchResponseBodyControlAct.rootId"))
        .eventCorrelationId(
            get(properties, "ProvincialPatientSearchResponseBodyControlAct.eventRootUid"))
        .provincialPatientSearchCriteria(buildProvincialPatientSearchCriteriaInResponse(properties))
        .build();
  }

  private ProvincialPatientSearchCriteria buildProvincialPatientSearchCriteriaInResponse(
      Properties properties) {

    final String patientSearchCriteria = get(properties, "PatientSearchCriteria");
    if (patientSearchCriteria.equalsIgnoreCase("null")) {
      return ProvincialPatientSearchCriteria.builder()
          .provincialRequestControl(buildProvincialRequestControl(properties))
          .build();
    }
    final String gender = get(properties, "PatientSearchCriteria.gender");
    return ProvincialPatientSearchCriteria.builder()
        .firstName(get(properties, "PatientSearchCriteria.firstName"))
        .lastName(get(properties, "PatientSearchCriteria.lastName"))
        .dateOfBirth(
            DateUtils.parseSystemDateFormatToLocalDate(
                get(properties, "PatientSearchCriteria.dateOfBirth")))
        .gender(StringUtils.isBlank(gender) ? null : Gender.valueOf(gender))
        .motherFirstName(get(properties, "PatientSearchCriteria.motherFirstName"))
        .motherLastName(get(properties, "PatientSearchCriteria.motherLastName"))
        .fatherFirstName(get(properties, "PatientSearchCriteria.fatherFirstName"))
        .fatherLastName(get(properties, "PatientSearchCriteria.fatherLastName"))
        .address(buildAddressInCriteria(properties))
        .provincialHealthNumber(get(properties, "PatientSearchCriteria.provincialHealthNumber"))
        .provincialRequestControl(buildProvincialRequestControl(properties))
        .build();
  }

  private QueryAcknowledgement buildQueryAck(Properties properties) {
    return QueryAcknowledgement.builder()
        .queryResponseCode(get(properties, "QueryAck.queryResponseCode"))
        .resultCurrentQuantity(get(properties, "QueryAck.resultCurrentQuantity"))
        .resultRemainingQuantity(get(properties, "QueryAck.resultRemainQuantity"))
        .resultTotalQuantity(get(properties, "QueryAck.resultTotalQuantity"))
        .build();
  }

  private ProvincialResponseAcknowledgement buildProvincialResponseAcknowledgement(
      Properties properties) {
    final String responseAck =
        get(properties, "ProvincialResponseAcknowledgement.responseAcknowledgement");
    return ProvincialResponseAcknowledgement.builder()
        .operationOutcome(
            OperationOutcome.builder()
                .status(
                    StringUtils.isBlank(responseAck)
                        ? null
                        : ResponseStatus.builder()
                            .text("")
                            .code(Status.valueOf(responseAck))
                            .build())
                .build())
        .auditEvent(
            AuditEvent.builder()
                .provincialRequestControl(
                    ProvincialRequestControl.builder()
                        .requestId(
                            get(
                                properties,
                                "ProvincialResponseAcknowledgement.requestCorrelationId"))
                        .build())
                .build())
        .build();
  }
}
