package com.lblw.vphx.phms.transformation.response.transformers;

import static org.assertj.core.api.Assertions.assertThat;

import com.lblw.vphx.phms.domain.common.MessageProcess;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.request.ProvincialRequestControl;
import com.lblw.vphx.phms.domain.common.response.AuditEvent;
import com.lblw.vphx.phms.domain.common.response.ProvincialResponseAcknowledgement;
import com.lblw.vphx.phms.domain.common.response.hl7v3.AcknowledgementDetails;
import com.lblw.vphx.phms.domain.patient.consent.response.ProvincialPatientConsent;
import com.lblw.vphx.phms.domain.patient.consent.response.hl7v3.ProvincialPatientConsentResponse;
import com.lblw.vphx.phms.transformation.response.transformers.patientconsent.PatientConsentResponseXPathTransformer;
import com.lblw.vphx.phms.transformation.utils.TestUtils;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.lang.NonNull;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

/**
 * Test cases to unit test {@link ProvincialPatientConsentResponse} ProvincialPatientConsentResponse
 */
public class PatientConsentResponseTransformerTest {

  private PatientConsentResponseXPathTransformer patientConsentResponseTransformer;
  private String requestCorrelationId;
  private String provincialSystemRawRequest;
  private String provincialSystemRawResponse;
  private String provincialSystemRawRequestTime;
  private String provincialSystemRawResponseTime;

  /**
   * Creates an instance of {@link ProvincialPatientConsent} from json success response
   * expectations.
   *
   * @param pathToJsonFile Path to the expectation json file. For instance,
   *     "patientconsent/expectation/success.json"
   * @param requestCorrelationId request correlation id
   * @param provincialSystemRawRequest provincial System Raw Request
   * @param provincialSystemRawResponse provincial System Raw Response
   * @param provincialSystemRawRequestTime provincial System Raw Request time
   * @param provincialSystemRawResponseTime provincial System Raw Response time
   * @return {@link ProvincialPatientConsent} expected instance of ProvincialPatientConsent
   */
  public static ProvincialPatientConsentResponse getProvincialPatientConsentExpectations(
      @NonNull String pathToJsonFile,
      String requestCorrelationId,
      Province province,
      String provincialSystemRawRequest,
      String provincialSystemRawResponse,
      String provincialSystemRawRequestTime,
      String provincialSystemRawResponseTime) {

    ProvincialPatientConsentResponse provincialPatientConsentResponse = null;
    try {
      provincialPatientConsentResponse =
          (ProvincialPatientConsentResponse)
              TestUtils.loadJsonToClass(pathToJsonFile, ProvincialPatientConsentResponse.class);
    } catch (IOException e) {
      Assertions.assertEquals(true, e.getMessage().isEmpty());
    }

    // Populating the @JsonIgnore

    ProvincialResponseAcknowledgement provincialResponseAcknowledgement =
        new ProvincialResponseAcknowledgement();
    provincialResponseAcknowledgement.setAuditEvent(
        AuditEvent.builder()
            .provincialRequestControl(
                ProvincialRequestControl.builder()
                    .province(province)
                    .requestId(requestCorrelationId)
                    .build())
            .rawRequest(provincialSystemRawRequest)
            .requestTimestamp(provincialSystemRawRequestTime)
            .rawResponse(provincialSystemRawResponse)
            .responseTimestamp(provincialSystemRawResponseTime)
            .build());

    provincialPatientConsentResponse
        .getProvincialResponsePayload()
        .setProvincialResponseAcknowledgement(provincialResponseAcknowledgement);

    return provincialPatientConsentResponse;
  }

  @BeforeEach
  public void beforeEach() {
    patientConsentResponseTransformer = new PatientConsentResponseXPathTransformer();
  }

  @Test
  void whenSuccessXmlResponse_ThenSuccessResponseIsReceived() throws Exception {
    String pathToJsonFile = "patientconsent/expectation/success.json";
    requestCorrelationId = "eventCorrelationId";
    final ProvincialPatientConsentResponse expectedProvincialPatientConsentResponse =
        getProvincialPatientConsentExpectations(
            pathToJsonFile,
            requestCorrelationId,
            Province.QC,
            provincialSystemRawRequest,
            provincialSystemRawResponse,
            provincialSystemRawRequestTime,
            provincialSystemRawResponseTime);

    testTemplate(
        "patientconsent/dsq.response/success.xml", null, expectedProvincialPatientConsentResponse);
  }

  @Test
  void whenNotValidEffectiveDateTime_ThenNoConsentToken() throws Exception {
    String pathToJsonFile = "patientconsent/expectation/notvalideffectivedatetime.json";
    requestCorrelationId = "eventCorrelationId";
    final ProvincialPatientConsentResponse expectedProvincialPatientConsentResponse =
        getProvincialPatientConsentExpectations(
            pathToJsonFile,
            requestCorrelationId,
            Province.QC,
            provincialSystemRawRequest,
            provincialSystemRawResponse,
            provincialSystemRawRequestTime,
            provincialSystemRawResponseTime);

    testTemplate(
        "patientconsent/dsq.response/notvalid_effectivedatetime.xml",
        "Vous ne pouvez pas utiliser une date de référence inférieure à 2007-01-01.",
        expectedProvincialPatientConsentResponse);
  }

  private void testTemplate(
      String hl7XmlResponse,
      String detectedIssueText,
      final ProvincialPatientConsentResponse expectedProvincialPatientConsentResponse)
      throws Exception {
    String hl7_response = TestUtils.loadXmlFileContentsAsString(hl7XmlResponse);
    StepVerifier.create(
            patientConsentResponseTransformer
                .transform(hl7_response)
                .contextWrite(
                    Context.of(MessageProcess.class, MessageProcess.PATIENT_CONSENT).readOnly()))
        .assertNext(
            actualResponse -> {
              ProvincialPatientConsentResponse observedProvincialPatientConsentResponse =
                  (ProvincialPatientConsentResponse) actualResponse;
              assertThat(observedProvincialPatientConsentResponse).isNotNull();
              try {
                performAssertions(
                    observedProvincialPatientConsentResponse,
                    expectedProvincialPatientConsentResponse,
                    detectedIssueText);
              } catch (IOException e) {
                e.printStackTrace();
              }
            })
        .verifyComplete();
  }

  /**
   * Performs assertion on the observed and the expected transformation objects
   *
   * @param observedProvincialPatientConsentResponse {@link ProvincialPatientConsentResponse}
   *     received from the code under test
   * @param expectedProvincialPatientConsentResponse {@link ProvincialPatientConsentResponse}
   *     expected
   * @param detectedIssueText
   */
  private void performAssertions(
      ProvincialPatientConsentResponse observedProvincialPatientConsentResponse,
      ProvincialPatientConsentResponse expectedProvincialPatientConsentResponse,
      String detectedIssueText)
      throws IOException {

    assertThat(observedProvincialPatientConsentResponse.getResponseHeader())
        .isEqualToComparingFieldByFieldRecursively(
            expectedProvincialPatientConsentResponse.getResponseHeader());

    assertOnResponseBodyTransmissionWrapper(
        observedProvincialPatientConsentResponse, expectedProvincialPatientConsentResponse);

    assertThat(observedProvincialPatientConsentResponse.getResponseControlAct())
        .isEqualTo(expectedProvincialPatientConsentResponse.getResponseControlAct());

    observedProvincialPatientConsentResponse.getDetectedIssues().stream()
        .findFirst()
        .ifPresent(
            detectedIssue -> assertThat(detectedIssue.getEventText()).isEqualTo(detectedIssueText));

    assertThat(observedProvincialPatientConsentResponse.getProvincialResponsePayload())
        .isEqualToComparingFieldByFieldRecursively(
            expectedProvincialPatientConsentResponse.getProvincialResponsePayload());
  }

  /**
   * Performs assertion on ResponseBodyTransmissionWrapper after extracting out running List of
   * {@link AcknowledgementDetails} as the recursive assertion function fails to assert on contents
   * of the lists, which is of relevance for this tests.
   *
   * @param observedProvincialPatientConsentResponse Observed {@link
   *     ProvincialPatientConsentResponse}
   * @param expectedProvincialPatientConsentResponse Expected {@link
   *     ProvincialPatientConsentResponse}
   */
  private void assertOnResponseBodyTransmissionWrapper(
      ProvincialPatientConsentResponse observedProvincialPatientConsentResponse,
      ProvincialPatientConsentResponse expectedProvincialPatientConsentResponse) {
    final List<AcknowledgementDetails> observedAcknowledgementDetails =
        observedProvincialPatientConsentResponse
            .getResponseBodyTransmissionWrapper()
            .getAcknowledgementDetails();
    observedProvincialPatientConsentResponse
        .getResponseBodyTransmissionWrapper()
        .setAcknowledgementDetails(null);

    final List<AcknowledgementDetails> expectedAcknowledgementDetails =
        expectedProvincialPatientConsentResponse
            .getResponseBodyTransmissionWrapper()
            .getAcknowledgementDetails();
    expectedProvincialPatientConsentResponse
        .getResponseBodyTransmissionWrapper()
        .setAcknowledgementDetails(null);

    if (observedAcknowledgementDetails != null && expectedAcknowledgementDetails != null) {
      assertThat(observedAcknowledgementDetails)
          .containsExactlyInAnyOrderElementsOf(expectedAcknowledgementDetails);
    }

    assertThat(observedProvincialPatientConsentResponse.getResponseBodyTransmissionWrapper())
        .isEqualToComparingFieldByFieldRecursively(
            expectedProvincialPatientConsentResponse.getResponseBodyTransmissionWrapper());
  }
}
