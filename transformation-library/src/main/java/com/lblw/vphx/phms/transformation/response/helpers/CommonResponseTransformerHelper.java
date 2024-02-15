package com.lblw.vphx.phms.transformation.response.helpers;

import static com.lblw.vphx.phms.common.utils.XmlParsingUtils.*;

import com.lblw.vphx.phms.common.constants.HL7Constants;
import com.lblw.vphx.phms.domain.common.response.hl7v3.*;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** Helper class to handle common retrievals from HL7V3 response. */
@Component
@Slf4j
public class CommonResponseTransformerHelper {

  /**
   * Builds an instance of {@link ResponseHeader} from given {@code s:Header} xml element.
   *
   * @param envelope {@link Element} represents XML tree in memory for {@code s:Header}
   * @return Prepared {@link ResponseHeader} from the {@code header}
   */
  public ResponseHeader buildResponseHeaderBuilder(Element envelope) {
    var header = getDeepestDescendantElementTree(envelope, HL7Constants.S_HEADER);
    ResponseHeader.ResponseHeaderBuilder responseHeaderBuilder = ResponseHeader.builder();
    responseHeaderBuilder.trackingId(retrieveElementValue(header, HL7Constants.TRACKING_ID));
    responseHeaderBuilder.transactionId(retrieveElementValue(header, HL7Constants.TRANSACTION_ID));
    responseHeaderBuilder.sessionId(retrieveElementValue(header, HL7Constants.SESSION_ID));
    return responseHeaderBuilder.build();
  }

  /**
   * Builds an instance of {@link ResponseBodyTransmissionWrapper} from given {@code
   * acknowledgement} xml element.
   *
   * @param envelope {@link Element} represents XML tree in memory for {@code acknowledgement}
   * @return Prepared {@link ResponseBodyTransmissionWrapper} from the {@code acknowledgement}.
   */
  public ResponseBodyTransmissionWrapper buildTransmissionWrapper(Element envelope) {

    var acknowledgement = getDeepestDescendantElementTree(envelope, HL7Constants.ACKNOWLEDGEMENT);
    return ResponseBodyTransmissionWrapper.builder()
        .transmissionUniqueIdentifier(
            getAttributeValueFromTheGivenTree(
                acknowledgement, HL7Constants.ROOT, HL7Constants.TARGET_MESSAGE, HL7Constants.ID))
        .acknowledgeTypeCode(
            getAttributeValueFromTheGivenTree(
                acknowledgement, HL7Constants.CODE, HL7Constants.TYPE_CODE))
        .acknowledgementDetails(buildAcknowledgementDetails(acknowledgement))
        .build();
  }

  /**
   * Builds an list of {@link AcknowledgementDetails} from given {@code acknowledgement} xml
   * element.
   *
   * @param acknowledgement {@link Element} represents XML tree in memory for {@code
   *     acknowledgement}
   * @return Prepared {@link List} of {@link AcknowledgementDetails}, can be null
   */
  public List<AcknowledgementDetails> buildAcknowledgementDetails(Element acknowledgement) {
    if (acknowledgement == null) {
      return Collections.emptyList();
    }

    NodeList acknowledgementDetailsList =
        acknowledgement.getElementsByTagName(HL7Constants.ACKNOWLEDGEMENT_DETAIL);

    if (acknowledgementDetailsList == null || acknowledgementDetailsList.getLength() == 0) {
      return Collections.emptyList();
    }

    List<AcknowledgementDetails> acknowledgementDetails = new ArrayList<>();
    for (int index = 0; index < acknowledgementDetailsList.getLength(); index++) {
      acknowledgementDetails.add(
          buildAnAcknowledgementDetail((Element) acknowledgementDetailsList.item(index)));
    }
    return acknowledgementDetails;
  }

  /**
   * Builds an instance of {@link AcknowledgementDetails} from given {@code <acknowledgementDetail>}
   * xml element.
   *
   * @param acknowledgementDetail {@link Element} represents XML tree in memory for {@code
   *     <acknowledgementDetail>} xml element
   * @return Prepared {@link AcknowledgementDetails} from the {@code acknowledgementDetail}
   */
  @NonNull
  public AcknowledgementDetails buildAnAcknowledgementDetail(
      @NonNull Element acknowledgementDetail) {
    var typeCode = getAttributeValueFromTheGivenTree(acknowledgementDetail, HL7Constants.TYPE_CODE);
    if (typeCode == null) {
      typeCode =
          getAttributeValueFromTheGivenTree(
              acknowledgementDetail, HL7Constants.CODE, HL7Constants.TYPE_CODE);
    }
    var code =
        getAttributeValueFromTheGivenTree(
            acknowledgementDetail, HL7Constants.CODE, HL7Constants.CODE);
    var text = getElementTextFromTheGivenTree(acknowledgementDetail, HL7Constants.TEXT);
    var location = getElementTextFromTheGivenTree(acknowledgementDetail, HL7Constants.LOCATION);

    return AcknowledgementDetails.builder()
        .typeCode(typeCode)
        .code(code)
        .text(text)
        .location(location)
        .build();
  }

  /**
   * Builds an instance of {@link QueryAcknowledgement} from given {@code controlActEvent} xml
   * element.
   *
   * @param envelope {@link Element} represents XML tree in memory for {@code <controlActEvent>}
   * @return Prepared {@link QueryAcknowledgement}
   */
  public QueryAcknowledgement buildQueryAck(Element envelope) {
    var controlActEvent = getDeepestDescendantElementTree(envelope, HL7Constants.CONTROL_ACT_EVENT);
    var queryAckElement = getDeepestDescendantElementTree(controlActEvent, HL7Constants.QUERY_ACK);

    String queryResponseCode =
        getAttributeValueFromTheGivenTree(
            queryAckElement, HL7Constants.CODE, HL7Constants.QUERY_RESPONSE_CODE);
    String resultCurrentQuantity =
        getAttributeValueFromTheGivenTree(
            queryAckElement, HL7Constants.VALUE, HL7Constants.RESULT_CURRENT_QUANTITY);
    String resultRemainingQuantity =
        getAttributeValueFromTheGivenTree(
            queryAckElement, HL7Constants.VALUE, HL7Constants.RESULT_REMAINING_QUANTITY);
    String resultTotalQuantity =
        getAttributeValueFromTheGivenTree(
            queryAckElement, HL7Constants.VALUE, HL7Constants.RESULT_TOTAL_QUANTITY);

    return QueryAcknowledgement.builder()
        .queryResponseCode(queryResponseCode)
        .resultCurrentQuantity(resultCurrentQuantity)
        .resultRemainingQuantity(resultRemainingQuantity)
        .resultTotalQuantity(resultTotalQuantity)
        .build();
  }

  /**
   * Builds a list of {@link DetectedIssue} from given {@code subjectElement} xml element
   *
   * @param envelope {@link Element} represents XML tree in memory for {@code s:envelope}
   * @return Prepared list of {@link DetectedIssue} from envelope
   */
  public List<DetectedIssue> buildDetectedIssue(Element envelope) {
    return getDeepestDescendantElementChildren(envelope, HL7Constants.SUBJECT_OF).stream()
        .map(
            subjectOf1Element ->
                getDescendantElementTreeByExactPath(
                    subjectOf1Element, HL7Constants.DETECTED_ISSUE_EVENT))
        .filter(Objects::nonNull)
        .map(
            detectedIssueEvent ->
                DetectedIssue.builder()
                    .eventCode(
                        getAttributeValueFromTheGivenTree(
                            detectedIssueEvent, HL7Constants.CODE, HL7Constants.CODE))
                    .eventText(
                        getElementTextFromTheGivenTree(detectedIssueEvent, HL7Constants.TEXT))
                    .build())
        .collect(Collectors.toList());
  }

  /**
   * Retrieve values from element by tag name.
   *
   * @param element {@link Element} to search tags
   * @param tagName to be looked for inside the element
   * @return value from the element, empty if it is not found
   */
  public String retrieveElementValue(Element element, String tagName) {
    return Optional.of(element.getElementsByTagName(tagName))
        .map(item -> (item.getLength() > 0) ? item.item(0).getTextContent().trim() : "")
        .orElse("");
  }
}
