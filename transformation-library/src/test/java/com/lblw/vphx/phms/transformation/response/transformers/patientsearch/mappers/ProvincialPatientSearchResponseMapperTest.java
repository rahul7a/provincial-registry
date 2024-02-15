package com.lblw.vphx.phms.transformation.response.transformers.patientsearch.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.lblw.vphx.phms.common.databind.BindMany;
import com.lblw.vphx.phms.common.databind.BindOne;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.common.response.AuditEvent;
import com.lblw.vphx.phms.domain.common.response.ProvincialResponseAcknowledgement;
import com.lblw.vphx.phms.domain.common.response.hl7v3.*;
import com.lblw.vphx.phms.domain.patient.response.ProvincialPatientProfile;
import com.lblw.vphx.phms.transformation.response.helpers.CommonResponseXPathTransformerHelper;
import com.lblw.vphx.phms.transformation.response.transformers.mappers.ResponseHeaderMapper;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ProvincialPatientSearchResponseMapperTest {
  private ProvincialPatientSearchResponseMapper provincialPatientSearchResponseMapper;

  @BeforeEach
  void buildProvincialPatientSearchResponseMapper() {
    provincialPatientSearchResponseMapper =
        new ProvincialPatientSearchResponseMapper(new CommonResponseXPathTransformerHelper());
  }

  @Test
  void whenBinding_thenSetResponseHeader() {
    BindOne<ResponseHeader> bindOne =
        (supplier) ->
            ResponseHeaderMapper.builder()
                .transactionId("transactionId")
                .trackingId("trackingId")
                .sessionId("sessionId")
                .build();
    provincialPatientSearchResponseMapper.bindResponseHeader(bindOne);

    assertEquals(
        "trackingId", provincialPatientSearchResponseMapper.getResponseHeader().getTrackingId());
    assertEquals(
        "transactionId",
        provincialPatientSearchResponseMapper.getResponseHeader().getTransactionId());
    assertEquals(
        "sessionId", provincialPatientSearchResponseMapper.getResponseHeader().getSessionId());
  }

  @Test
  void givenNullResponseHeaderBinder_whenBinding_thenNotSetResponseHeader() {
    BindOne<ResponseHeader> bindOne = null;
    provincialPatientSearchResponseMapper.bindResponseHeader(bindOne);

    assertNull(provincialPatientSearchResponseMapper.getResponseHeader());
  }

  @Test
  void whenBinding_thenSetResponseBodyTransmissionWrapper() {
    var responseBodyTransmissionWrapper =
        ResponseBodyTransmissionWrapper.builder()
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
    BindOne<ResponseBodyTransmissionWrapper> bindOne =
        (supplier) -> responseBodyTransmissionWrapper;

    provincialPatientSearchResponseMapper.bindResponseBodyTransmissionWrapper(bindOne);

    assertThat(responseBodyTransmissionWrapper)
        .usingRecursiveComparison()
        .isEqualTo(provincialPatientSearchResponseMapper.getResponseBodyTransmissionWrapper());
  }

  @Test
  void givenNullResponseBodyTransmissionWrapper_thenNotSetResponseBodyTransmissionWrapper() {
    BindOne<ResponseBodyTransmissionWrapper> bindOne = null;
    provincialPatientSearchResponseMapper.bindResponseBodyTransmissionWrapper(bindOne);

    assertNull(provincialPatientSearchResponseMapper.getResponseBodyTransmissionWrapper());
  }

  @Test
  void whenBinding_thenSetProvincialResponsePayload() {

    String requestId = "requestID";
    BindOne<ProvincialPatientProfile> bindOne =
        (supplier) -> ProvincialPatientProfile.builder().build();
    provincialPatientSearchResponseMapper.bindProvincialResponsePayload(bindOne, "requestID");

    assertEquals(
        ProvincialPatientProfile.builder().build(),
        provincialPatientSearchResponseMapper.getProvincialResponsePayload());

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
        provincialPatientSearchResponseMapper
            .getProvincialResponsePayload()
            .getProvincialResponseAcknowledgement());
  }

  @Test
  void givenNullProvincialPatientProfileBinder_whenBinding_thenSetProvincialResponsePayload() {

    String requestId = "requestID";
    BindOne<ProvincialPatientProfile> bindOne = null;

    provincialPatientSearchResponseMapper.bindProvincialResponsePayload(bindOne, "requestID");
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
        provincialPatientSearchResponseMapper
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
    provincialPatientSearchResponseMapper.bindDetectedIssues(bindMany);

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
        provincialPatientSearchResponseMapper.getDetectedIssues());
  }

  @Test
  void givenNullDetectedIssues_thenNotSetDetectedIssues() {
    BindMany<DetectedIssue> bindMany = null;
    provincialPatientSearchResponseMapper.bindDetectedIssues(bindMany);
    assertNull(provincialPatientSearchResponseMapper.getDetectedIssues());
  }

  @Test
  void whenBinding_thenSetQueryAck() {
    var queryAcknowledgement =
        QueryAcknowledgement.builder()
            .queryResponseCode("OK")
            .resultTotalQuantity("1")
            .resultCurrentQuantity("1")
            .resultRemainingQuantity("0")
            .build();
    BindOne<QueryAcknowledgement> bindOne = (supplier) -> queryAcknowledgement;
    provincialPatientSearchResponseMapper.bindQueryAck(bindOne);
    assertThat(queryAcknowledgement)
        .usingRecursiveComparison()
        .isEqualTo(provincialPatientSearchResponseMapper.getQueryAcknowledgement());
  }
}
