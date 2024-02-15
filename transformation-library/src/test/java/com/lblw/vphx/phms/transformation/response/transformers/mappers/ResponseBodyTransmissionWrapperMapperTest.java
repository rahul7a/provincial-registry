package com.lblw.vphx.phms.transformation.response.transformers.mappers;

import com.lblw.vphx.phms.common.databind.BindMany;
import com.lblw.vphx.phms.domain.common.response.hl7v3.AcknowledgementDetails;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ResponseBodyTransmissionWrapperMapperTest {

  private static ResponseBodyTransmissionWrapperMapper responseBodyTransmissionWrapperMapper;

  @BeforeAll
  static void buildQueryAcknowledgementMapper() {
    responseBodyTransmissionWrapperMapper = new ResponseBodyTransmissionWrapperMapper();
  }

  @Test
  void whenBinding_thenSetTransmissionUniqueIdentifier() {
    responseBodyTransmissionWrapperMapper.bindTransmissionUniqueIdentifier(
        "transmissionUniqueIdentifier");
    Assertions.assertEquals(
        "transmissionUniqueIdentifier",
        responseBodyTransmissionWrapperMapper.getTransmissionUniqueIdentifier());
  }

  @Test
  void whenBinding_thenSetAcknowledgeTypeCode() {
    responseBodyTransmissionWrapperMapper.bindAcknowledgeTypeCode("acknowledgeTypeCode");
    Assertions.assertEquals(
        "acknowledgeTypeCode", responseBodyTransmissionWrapperMapper.getAcknowledgeTypeCode());
  }

  @Test
  void whenBinding_thenSetAcknowledgementDetails() {

    BindMany<AcknowledgementDetails> bindMany =
        (supplier) -> {
          AcknowledgementDetails testAcknowledgementDetials1 =
              AcknowledgementDetails.builder()
                  .code("code")
                  .typeCode("typeCode")
                  .text("sampleText")
                  .location("sampleLocation")
                  .build();

          var testAcknowledgementDetials2 =
              AcknowledgementDetails.builder()
                  .code("testcode")
                  .typeCode("testTypeCode")
                  .text("testText")
                  .location("testLocation")
                  .build();

          return Stream.of(testAcknowledgementDetials1, testAcknowledgementDetials2);
        };

    responseBodyTransmissionWrapperMapper.bindAcknowledgementDetails(bindMany);
    Assertions.assertEquals(
        Arrays.asList(
            AcknowledgementDetails.builder()
                .code("code")
                .typeCode("typeCode")
                .text("sampleText")
                .location("sampleLocation")
                .build(),
            AcknowledgementDetails.builder()
                .code("testcode")
                .typeCode("testTypeCode")
                .text("testText")
                .location("testLocation")
                .build()),
        responseBodyTransmissionWrapperMapper.getAcknowledgementDetails());
  }
}
