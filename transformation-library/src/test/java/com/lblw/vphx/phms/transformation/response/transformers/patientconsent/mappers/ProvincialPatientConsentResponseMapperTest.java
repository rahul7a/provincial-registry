package com.lblw.vphx.phms.transformation.response.transformers.patientconsent.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.lblw.vphx.phms.common.constants.HL7Constants;
import com.lblw.vphx.phms.common.databind.BindMany;
import com.lblw.vphx.phms.common.databind.BindOne;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.SystemIdentifier;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.common.response.AuditEvent;
import com.lblw.vphx.phms.domain.common.response.ProvincialResponseAcknowledgement;
import com.lblw.vphx.phms.domain.common.response.hl7v3.*;
import com.lblw.vphx.phms.domain.patient.consent.response.ProvincialPatientConsent;
import com.lblw.vphx.phms.transformation.response.transformers.mappers.ResponseHeaderMapper;
import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProvincialPatientConsentResponseMapperTest {
  private ProvincialPatientConsentResponseMapper provincialPatientConsentResponseMapper;

  @BeforeEach
  void buildProvincialPatientConsentResponseMapper() {
    provincialPatientConsentResponseMapper = new ProvincialPatientConsentResponseMapper();
  }

  @Test
  void whenBinding_thenSetResponseHeader() {
    BindOne<ResponseHeader> bindOne =
        (supplier) -> {
          return ResponseHeaderMapper.builder()
              .transactionId("transactionId")
              .trackingId("trackingId")
              .sessionId("sessionId")
              .build();
        };
    provincialPatientConsentResponseMapper.bindResponseHeader(bindOne);

    assertEquals(
        "trackingId", provincialPatientConsentResponseMapper.getResponseHeader().getTrackingId());
    assertEquals(
        "transactionId",
        provincialPatientConsentResponseMapper.getResponseHeader().getTransactionId());
    assertEquals(
        "sessionId", provincialPatientConsentResponseMapper.getResponseHeader().getSessionId());
  }

  @Test
  void givenNullResponseHeaderBinder_whenBinding_thenNotSetResponseHeader() {
    BindOne<ResponseHeader> bindOne = null;
    provincialPatientConsentResponseMapper.bindResponseHeader(bindOne);

    assertNull(provincialPatientConsentResponseMapper.getResponseHeader());
  }

  @Test
  void whenBinding_thenSetResponseBodyTransmissionWrapper() {

    BindOne<ResponseBodyTransmissionWrapper> bindOne =
        (supplier) -> {
          return ResponseBodyTransmissionWrapper.builder()
              .transmissionUniqueIdentifier("transmissionUniqueIdentifier")
              .acknowledgeTypeCode("AE")
              .acknowledgementDetails(
                  Arrays.asList(
                      AcknowledgementDetails.builder()
                          .code("E")
                          .text("text")
                          .typeCode("typeCode")
                          .location("location")
                          .build(),
                      AcknowledgementDetails.builder()
                          .code("E")
                          .text("acknowledgementText")
                          .typeCode("typeCode")
                          .location("location")
                          .build()))
              .build();
        };

    provincialPatientConsentResponseMapper.bindResponseBodyTransmissionWrapper(bindOne);

    assertEquals(
        "transmissionUniqueIdentifier",
        provincialPatientConsentResponseMapper
            .getResponseBodyTransmissionWrapper()
            .getTransmissionUniqueIdentifier());
    assertEquals(
        "AE",
        provincialPatientConsentResponseMapper
            .getResponseBodyTransmissionWrapper()
            .getAcknowledgeTypeCode());

    assertEquals(
        Arrays.asList(
            AcknowledgementDetails.builder()
                .code("E")
                .text("text")
                .typeCode("typeCode")
                .location("location")
                .build(),
            AcknowledgementDetails.builder()
                .code("E")
                .text("acknowledgementText")
                .typeCode("typeCode")
                .location("location")
                .build()),
        provincialPatientConsentResponseMapper
            .getResponseBodyTransmissionWrapper()
            .getAcknowledgementDetails());
  }

  @Test
  void givenNullResponseBodyTransmissionWrapper_thenNotSetResponseBodyTransmissionWrapper() {
    BindOne<ResponseBodyTransmissionWrapper> bindOne = null;
    provincialPatientConsentResponseMapper.bindResponseBodyTransmissionWrapper(bindOne);

    assertNull(provincialPatientConsentResponseMapper.getResponseBodyTransmissionWrapper());
  }

  @Test
  void whenBinding_thenSetProvincialResponsePayload() {

    var startDateTime = Instant.now();
    var endDateTime = Instant.now();

    String requestId = "requestID";
    BindOne<ProvincialPatientConsent> bindOne =
        (supplier) -> {
          return ProvincialPatientConsent.builder()
              .identifier(
                  SystemIdentifier.builder()
                      .type(SystemIdentifier.IDENTIFIER_TYPE.PATIENT)
                      .value("8000000300")
                      .assigner(HL7Constants.QC)
                      .system(HL7Constants.NIU_U)
                      .build())
              .consentValidityStartDateTime(startDateTime)
              .consentValidityEndDateTime(endDateTime)
              .provincialResponseAcknowledgement(
                  ProvincialResponseAcknowledgement.builder()
                      .auditEvent(
                          AuditEvent.builder()
                              .provincialRequestControl(
                                  ProvincialRequestControl.builder()
                                      .province(Province.QC)
                                      .requestId(requestId)
                                      .build())
                              .build())
                      .build())
              .build();
        };
    provincialPatientConsentResponseMapper.bindProvincialResponsePayload(bindOne, "requestID");

    assertEquals(
        SystemIdentifier.builder()
            .type(SystemIdentifier.IDENTIFIER_TYPE.PATIENT)
            .value("8000000300")
            .assigner(HL7Constants.QC)
            .system(HL7Constants.NIU_U)
            .build(),
        provincialPatientConsentResponseMapper.getProvincialResponsePayload().getIdentifier());

    assertEquals(
        startDateTime,
        provincialPatientConsentResponseMapper
            .getProvincialResponsePayload()
            .getConsentValidityStartDateTime());
    assertEquals(
        endDateTime,
        provincialPatientConsentResponseMapper
            .getProvincialResponsePayload()
            .getConsentValidityEndDateTime());

    assertEquals(
        ProvincialResponseAcknowledgement.builder()
            .auditEvent(
                AuditEvent.builder()
                    .provincialRequestControl(
                        ProvincialRequestControl.builder()
                            .province(Province.QC)
                            .requestId(requestId)
                            .build())
                    .build())
            .build(),
        provincialPatientConsentResponseMapper
            .getProvincialResponsePayload()
            .getProvincialResponseAcknowledgement());
  }

  @Test
  void givenNullPatientConsentBinder_whenBinding_thenSetProvincialResponsePayload() {

    String requestId = "requestID";
    BindOne<ProvincialPatientConsent> bindOne = null;

    provincialPatientConsentResponseMapper.bindProvincialResponsePayload(bindOne, "requestID");
    assertEquals(
        ProvincialResponseAcknowledgement.builder()
            .auditEvent(
                AuditEvent.builder()
                    .provincialRequestControl(
                        ProvincialRequestControl.builder()
                            .province(Province.QC)
                            .requestId(requestId)
                            .build())
                    .build())
            .build(),
        provincialPatientConsentResponseMapper
            .getProvincialResponsePayload()
            .getProvincialResponseAcknowledgement());
  }

  @Test
  void whenBinding_thenSetDetectedIssues() {
    BindMany<DetectedIssue> bindMany =
        (supplier) -> {
          DetectedIssue detectedIssue1 =
              DetectedIssue.builder()
                  .eventCode("VALIDAT")
                  .eventText("Vous devez fournir une seule date de référence")
                  .build();

          DetectedIssue detectedIssue2 =
              DetectedIssue.builder()
                  .eventCode("MISSMAND")
                  .eventText("Paramètre obligatoire manquant")
                  .build();

          return Stream.of(detectedIssue1, detectedIssue2);
        };
    provincialPatientConsentResponseMapper.bindDetectedIssues(bindMany);

    Assertions.assertEquals(
        Arrays.asList(
            DetectedIssue.builder()
                .eventCode("VALIDAT")
                .eventText("Vous devez fournir une seule date de référence")
                .build(),
            DetectedIssue.builder()
                .eventCode("MISSMAND")
                .eventText("Paramètre obligatoire manquant")
                .build()),
        provincialPatientConsentResponseMapper.getDetectedIssues());
  }

  @Test
  void givenNullDetectedIssues_thenNotSetDetectedIssues() {
    BindMany<DetectedIssue> bindMany = null;
    provincialPatientConsentResponseMapper.bindDetectedIssues(bindMany);
    assertNull(provincialPatientConsentResponseMapper.getDetectedIssues());
  }

  @Test
  void whenBinding_thenSetQueryAck() {

    BindOne<QueryAcknowledgement> bindOne =
        (supplier) -> {
          return QueryAcknowledgement.builder()
              .queryResponseCode("OK")
              .resultTotalQuantity("1")
              .resultCurrentQuantity("1")
              .resultRemainingQuantity("0")
              .build();
        };
    provincialPatientConsentResponseMapper.bindQueryAck(bindOne);

    assertEquals(
        "OK",
        provincialPatientConsentResponseMapper.getQueryAcknowledgement().getQueryResponseCode());
    assertEquals(
        "1",
        provincialPatientConsentResponseMapper.getQueryAcknowledgement().getResultTotalQuantity());
    assertEquals(
        "1",
        provincialPatientConsentResponseMapper
            .getQueryAcknowledgement()
            .getResultCurrentQuantity());
    assertEquals(
        "0",
        provincialPatientConsentResponseMapper
            .getQueryAcknowledgement()
            .getResultRemainingQuantity());
  }

  @Test
  void givenNullQueryAckConsentBinder_thenNotSetQueryAck() {
    BindOne<QueryAcknowledgement> bindOne = null;
    provincialPatientConsentResponseMapper.bindQueryAck(bindOne);
    assertNull(provincialPatientConsentResponseMapper.getQueryAcknowledgement());
  }
}
