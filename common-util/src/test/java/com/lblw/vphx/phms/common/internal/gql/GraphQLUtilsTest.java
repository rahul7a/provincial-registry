package com.lblw.vphx.phms.common.internal.gql;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lblw.vphx.phms.common.constants.CommonConstants;
import com.lblw.vphx.phms.common.internal.gql.parser.config.GraphQLParserConfig;
import com.lblw.vphx.phms.domain.prescription.PrescriptionTransaction;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {GraphQLParserConfig.class, GraphQLUtils.class})
class GraphQLUtilsTest {

  @Autowired private GraphQLUtils graphQLUtils;

  @Test
  @DisplayName(
      "Tests parsedData function for given GQL Response and PrescriptionTransactionParser ")
  void givenGQLResponse_whenParsedDataForPrescriptionTrasaction_returnsPrescriptionTransaction() {

    // expected
    GraphQLResponse expectedGraphQLResponse =
        buildPrescriptionTransactionMockResponse("/mocks/PrescriptionTransaction/success.json");

    // actual
    PrescriptionTransaction actualResponse =
        graphQLUtils.parseData(
            expectedGraphQLResponse.getData().get(CommonConstants.PRESCRIPTION_TRANSACTION),
            PrescriptionTransaction.class);

    Assertions.assertEquals("62a0e374ad23912353ee9371", actualResponse.getSystemIdentifier());
    Assertions.assertEquals("20230119143401.345+0000", actualResponse.getCreatedDate());
    Assertions.assertEquals(
        "62a0e374ad23912353ee9372", actualResponse.getPrescription().getSystemIdentifier());
    Assertions.assertEquals(
        "616ee78eeec0906382d6ff9b",
        actualResponse.getPrescription().getPrescriber().getSystemIdentifier());
    Assertions.assertEquals("T 1 CP", String.join(" ", actualResponse.getPrescription().getSig().getCode()));
    Assertions.assertEquals("TAKE 1 CAPSULE", actualResponse.getPrescription().getSig().getDescriptionEn());
    Assertions.assertEquals("PRENDRE 1 CAPSULE", actualResponse.getPrescription().getSig().getDescriptionFr());
    Assertions.assertEquals(
            "PAVILLON DU CENTRE HOSPITALIER",
            actualResponse.getPrescription().getPrescriber().getPrescriberProvincialName());
    Assertions.assertEquals(
            "1000016632",
            actualResponse.getPrescription().getPrescriber().getPrescriberProvincialLocationIdentifier());
    Assertions.assertFalse(actualResponse.getPrescription().getPrescriber().getOutOfProvince());
    Assertions.assertEquals(
        "62a0e365c3a71822a9a7f4ab", actualResponse.getPrescription().getPatient().getSystemId());
    Assertions.assertEquals("02269080", actualResponse.getMedication().getDin());
    Assertions.assertEquals(
        "PRODUCT_FILLING", actualResponse.getAuditDetails().get(0).getWorkFlowStatus());
    Assertions.assertEquals(
        "PRODUCT_VERIFICATION", actualResponse.getAuditDetails().get(1).getWorkFlowStatus());
    Assertions.assertEquals("CLINICAL_VERIFICATION",actualResponse.getAuditDetails().get(2).getWorkFlowStatus());
    Assertions.assertEquals(
        "2022-11-30T22:11:20.447Z", actualResponse.getAuditDetails().get(0).getAuditDateTime());
    Assertions.assertEquals(
        "2022-11-30T22:11:24.833Z", actualResponse.getAuditDetails().get(1).getAuditDateTime());
    Assertions.assertEquals(
            "2023-03-01T06:32:51.822Z", actualResponse.getAuditDetails().get(2).getAuditDateTime());
    Assertions.assertEquals(
        "540f0368-81a8-4db6-80e7-c0373cbce328",
        actualResponse.getAuditDetails().get(0).getUserId());
    Assertions.assertEquals(
        "540f0368-81a8-4db6-80e7-c0373cbce328",
        actualResponse.getAuditDetails().get(1).getUserId());
    Assertions.assertEquals(
            "540f0368-81a8-4db6-80e7-c0373cbce329",
            actualResponse.getAuditDetails().get(2).getUserId());
    Assertions.assertEquals(
            "SUBMIT", actualResponse.getPrescription().getWorkflowType());
  }

  @Test
  @DisplayName("Tests parsedData function for given GQL Response and DefaultParser ")
  void givenGQLResponse_whenParsedDataForAnyOtherType_returnsDesiredParsedData() {

    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode objectNode = objectMapper.createObjectNode();
    objectNode.put("key1", "value1");
    objectNode.put("key2", 2);

    Map<String, Object> expectedResponse = Map.of("key1", "value1", "key2", 2);

    Map<String, Object> actualResponse =
        (Map<String, Object>) graphQLUtils.parseData(objectNode, Object.class);

    Assertions.assertEquals(expectedResponse, actualResponse);
  }

  @Test
  @DisplayName("Tests getSchemaFromFileName function for valid file name ")
  void givenFileNameAndFileExists_thenShouldReturnString() {
    Assertions.assertDoesNotThrow(
        () -> graphQLUtils.getSchemaFromFileName(CommonConstants.PRESCRIPTION_TRANSACTION_QUERY));
    String stringQuery =
        graphQLUtils.getSchemaFromFileName(CommonConstants.PRESCRIPTION_TRANSACTION_QUERY);
    Assertions.assertNotNull(stringQuery);
    Assertions.assertInstanceOf(String.class, stringQuery);
  }

  @Test
  @DisplayName("Tests getSchemaFromFileName function for invalid file name ")
  void givenFileNameAndFileDoesnNotExists_thenShouldThrowException() {
    Assertions.assertThrows(
        Exception.class, () -> graphQLUtils.getSchemaFromFileName("Unknown Query File"));
  }

  @Test
  @DisplayName("Validates convertToString function for given JsonNode ")
  void givenJsonNode_thenShouldReturnDesiredString() throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode objectNode = objectMapper.createObjectNode();
    objectNode.put("key1", "value1");
    objectNode.put("key2", 2);

    String expectedString = objectMapper.writeValueAsString(objectNode);

    String actualString = graphQLUtils.convertToString(objectNode);

    Assertions.assertEquals(expectedString, actualString);
  }

  @Test
  @DisplayName("Test convertStringObject function for given String")
  void convertStringToObject() throws JsonProcessingException {

    Map<String, Object> expectedValue = Map.of("key1", "1");

    String inputString = " { \"key1\" : \"1\" }";

    Map<String, Object> actualResponse =
        (Map<String, Object>) graphQLUtils.convertStringToObject(inputString, Map.class);

    expectedValue.entrySet().stream()
        .forEach(
            entry ->
                Assertions.assertEquals(
                    expectedValue.get(entry.getKey()), actualResponse.get(entry.getKey())));
  }

  /**
   * @param resource
   * @return the predefined JSON Request.
   */
  private String resourceAsString(String resource) {
    try {
      return IOUtils.toString(this.getClass().getResourceAsStream(resource), UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Method to prep test data.
   *
   * @param jsonDataPath
   * @return {@link GraphQLResponse}
   */
  private GraphQLResponse buildPrescriptionTransactionMockResponse(String jsonDataPath) {
    String mockData = resourceAsString(jsonDataPath);
    ObjectMapper objectMapper = new ObjectMapper();
    GraphQLResponse response = null;
    try {
      response = objectMapper.convertValue(objectMapper.readTree(mockData), GraphQLResponse.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    return response;
  }
}
