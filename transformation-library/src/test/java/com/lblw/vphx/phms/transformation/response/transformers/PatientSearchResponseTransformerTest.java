package com.lblw.vphx.phms.transformation.response.transformers;

import static org.assertj.core.api.Assertions.assertThat;

import com.lblw.vphx.phms.domain.common.MessageProcess;
import com.lblw.vphx.phms.domain.common.response.hl7v3.AcknowledgementDetails;
import com.lblw.vphx.phms.domain.patient.response.hl7v3.ProvincialPatientSearchResponse;
import com.lblw.vphx.phms.transformation.response.helpers.CommonResponseXPathTransformerHelper;
import com.lblw.vphx.phms.transformation.response.transformers.patientsearch.PatientSearchResponseXPathTransformer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

/**
 * This class contains tests which verify various transformations of the HL7 responses received for
 * Provincial Patient Search are properly populated into the {@link ProvincialPatientSearchResponse}
 * instances.
 */
class PatientSearchResponseTransformerTest {
  private ExpectedTestResponseFactory expectedTestResponseFactory =
      new ExpectedTestResponseFactory();
  private PatientSearchResponseXPathTransformer patientSearchResponseTransformer;

  @BeforeEach
  public void beforeEachTest() {
    patientSearchResponseTransformer =
        new PatientSearchResponseXPathTransformer(new CommonResponseXPathTransformerHelper());
  }

  @Test
  void whenPatientNotFound_ThenValidateResponse() throws Exception {

    testTemplate(
        "patientprofile/dsq.response/patient_not_found.xml",
        "patient_not_found",
        "La date de naissance doit être antérieure ou égale à la date du jour.");
  }

  @Test
  void whenAcknowledgeCodeIsAE_AndQueryAckIsQE_ThenValidateTransformationResponse()
      throws Exception {

    testTemplate(
        "patientprofile/dsq.response/acknowledge_AE_queryack_QE.xml",
        "acknowledge_AE_queryack_QE",
        null);
  }

  @Test
  void whenEcartminSuccess_ThenValidateTransformationResponse() throws Exception {

    testTemplate(
        "patientprofile/dsq.response/ecartmin_success.xml", "ecartmin_success", "Écart mineur");
  }

  @Test
  void whenDeceasedTimePresent_ThenValidateTransformationResponse() throws Exception {

    testTemplate("patientprofile/dsq.response/deceased.xml", "deceased", null);
  }

  @Test
  void whenFindCandidatePatientFoundResponse_ThenValidateTransformationResponse() throws Exception {

    testTemplate(
        "patientprofile/dsq.response/find_candidate_patient_found_response.xml",
        "find_candidate_patient_found_response",
        null);
  }

  @Test
  void whenMissmandResponse_ThenValidateTransformationResponse() throws Exception {

    testTemplate(
        "patientprofile/dsq.response/missmand.xml",
        "missmand",
        "Les critères de recherche de base n'ont pas été saisis. Ces critères sont: le NIU seul, le\n"
            + "                            NAM seul ou dans un ensemble, le nom, le prénom, la date de naissance et le sexe de\n"
            + "                            l'individu.");
  }

  @Test
  void whenSuccessResponse_ThenValidateTransformationResponse() throws Exception {

    testTemplate("patientprofile/dsq.response/success.xml", "success", "Écart mineur");
  }

  @Test
  void whenUnsvalDateCreationTimeMustBeSmallerResponse_ThenValidateTransformationResponse()
      throws Exception {

    testTemplate(
        "patientprofile/dsq.response/unsval_date_creation_time_must_be_smaller.xml",
        "unsval_date_creation_time_must_be_smaller",
        null);
  }

  @Test
  void whenSucessResponse_ThenTransmissionIdIsPopulated() throws Exception {

    testTemplate(
        "patientprofile/dsq.response/transmissionid.xml", "transmissionid", "Écart mineur");
  }

  @Test
  void whenMultipleAcknowledgementDetailsInResponse_ThenAllAcknowledgementDetailsArePopulated()
      throws Exception {

    testTemplate(
        "patientprofile/dsq.response/multiple_acknowledgement_details.xml",
        "multiple_acknowledgement_details",
        null);
  }

  @Test
  void whenUnsvalWrongAcceptAckCodeResponse_ThenValidateTransformationResponse() throws Exception {

    testTemplate(
        "patientprofile/dsq.response/unsval_wrong_accept_ack_code.xml",
        "unsval_wrong_accept_ack_code",
        null);
  }

  private void testTemplate(String hl7XmlResponse, String useCaseName, String detectedIssueText)
      throws Exception {
    String hl7_response = loadHL7ResponseForTestingFrom(hl7XmlResponse);

    final ProvincialPatientSearchResponse expectedProvincialPatientSearchResponse =
        expectedTestResponseFactory.buildExpectedProvincialPatientSearchResponse(useCaseName);

    StepVerifier.create(
            patientSearchResponseTransformer
                .transform(hl7_response)
                .contextWrite(
                    Context.of(MessageProcess.class, MessageProcess.PATIENT_SEARCH).readOnly()))
        .assertNext(
            actualResponse -> {
              ProvincialPatientSearchResponse observedProvincialPatientSearchResponse =
                  (ProvincialPatientSearchResponse) actualResponse;
              assertThat(observedProvincialPatientSearchResponse).isNotNull();

              performAssertions(
                  observedProvincialPatientSearchResponse,
                  expectedProvincialPatientSearchResponse,
                  detectedIssueText);
            })
        .verifyComplete();
  }

  /**
   * Performs assertion on the observed and the expected transformation objects
   *
   * @param observedProvincialPatientSearchResponse {@link ProvincialPatientSearchResponse} received
   *     from the code under test
   * @param expectedProvincialPatientSearchResponse {@link ProvincialPatientSearchResponse} expected
   * @param detectedIssueText
   */
  private void performAssertions(
      ProvincialPatientSearchResponse observedProvincialPatientSearchResponse,
      ProvincialPatientSearchResponse expectedProvincialPatientSearchResponse,
      String detectedIssueText) {

    assertThat(observedProvincialPatientSearchResponse.getResponseHeader())
        .isEqualToComparingFieldByFieldRecursively(
            expectedProvincialPatientSearchResponse.getResponseHeader());

    assertOnResponseBodyTransmissionWrapper(
        observedProvincialPatientSearchResponse, expectedProvincialPatientSearchResponse);
  }

  /**
   * Performs assertion on ResponseBodyTransmissionWrapper after extracting out running List of
   * {@link AcknowledgementDetails} as the recursive assertion function fails to assert on contents
   * of the lists, which is of relevance for this tests.
   *
   * @param observedProvincialPatientSearchResponse Observed {@link ProvincialPatientSearchResponse}
   * @param expectedProvincialPatientSearchResponse Expected {@link ProvincialPatientSearchResponse}
   */
  private void assertOnResponseBodyTransmissionWrapper(
      ProvincialPatientSearchResponse observedProvincialPatientSearchResponse,
      ProvincialPatientSearchResponse expectedProvincialPatientSearchResponse) {
    final List<AcknowledgementDetails> observedAcknowledgementDetails =
        observedProvincialPatientSearchResponse
            .getResponseBodyTransmissionWrapper()
            .getAcknowledgementDetails();
    observedProvincialPatientSearchResponse
        .getResponseBodyTransmissionWrapper()
        .setAcknowledgementDetails(null);

    final List<AcknowledgementDetails> expectedAcknowledgementDetails =
        expectedProvincialPatientSearchResponse
            .getResponseBodyTransmissionWrapper()
            .getAcknowledgementDetails();
    expectedProvincialPatientSearchResponse
        .getResponseBodyTransmissionWrapper()
        .setAcknowledgementDetails(null);

    if (observedAcknowledgementDetails != null && expectedAcknowledgementDetails != null) {
      assertThat(observedAcknowledgementDetails)
          .containsExactlyInAnyOrderElementsOf(expectedAcknowledgementDetails);
    }

    assertThat(observedProvincialPatientSearchResponse.getResponseBodyTransmissionWrapper())
        .isEqualToComparingFieldByFieldRecursively(
            expectedProvincialPatientSearchResponse.getResponseBodyTransmissionWrapper());
  }

  /**
   * Reads the contents of the given file and returns them as String.
   *
   * @param fileName XML File Name
   * @return contents of the xml file in string format
   * @throws IOException if an I/O error occurs reading from the file or a malformed or unmappable
   *     byte sequence is read
   */
  private String loadHL7ResponseForTestingFrom(String fileName) throws IOException {
    Path path = Paths.get("src/test/resources", fileName);
    return Files.readString(path);
  }
}
