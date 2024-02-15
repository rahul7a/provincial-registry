package com.lblw.vphx.phms.transformation.response.transformers.patientconsent;

import static com.lblw.vphx.phms.common.utils.XmlParsingUtils.extractExactXmlTreeForGivenTagName;

import com.lblw.vphx.phms.common.constants.ErrorConstants;
import com.lblw.vphx.phms.common.constants.HL7Constants;
import com.lblw.vphx.phms.common.databind.XPathBinder;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.patient.consent.response.hl7v3.ProvincialPatientConsentResponse;
import com.lblw.vphx.phms.transformation.exceptions.TransformationException;
import com.lblw.vphx.phms.transformation.response.ResponseTransformer;
import com.lblw.vphx.phms.transformation.response.transformers.patientconsent.mappers.ProvincialPatientConsentResponseMapper;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import reactor.core.publisher.Mono;

/** This class helps transformation service to transform response for patientConsentResponse */
@Component
@Slf4j
public class PatientConsentResponseXPathTransformer
    implements ResponseTransformer<ProvincialPatientConsentResponse> {

  /**
   * Transform response into domain object. Response could be xml, hl7-v2 or hl7-v3 etc.
   *
   * @param response Response could be xml, hl7-v2 or hl7-v3 etc. In current implementation only
   *     hl7-v3 is implemented
   * @return {@link ProvincialPatientConsentResponse}
   * @throws TransformationException occurs if transformation of XML response failed
   */
  @Override
  public Mono<ProvincialPatientConsentResponse> transform(String response) {

    return Mono.fromCallable(
        () -> {
          if (StringUtils.isBlank(response)) {
            log.error("Null/empty/blank response body.");
            throw new IllegalArgumentException("Response body is required");
          }
          return transformXMLPayloadToPatientConsentResponse(response);
        });
  }
  /**
   * Transforms XML response to ProvincialPatientConsentResponse
   *
   * @param response XML {@link String}
   * @return {@link ProvincialPatientConsentResponse} transformed response
   */
  private ProvincialPatientConsentResponse transformXMLPayloadToPatientConsentResponse(
      String response) {

    Element envelope = null;
    try {
      envelope = createRootResponseEnvelope(response);
    } catch (ParserConfigurationException | SAXException | IOException e) {
      log.error(
          String.format(
              ErrorConstants.EXCEPTION_MESSAGE_TRANSFORM_XML_RESPONSE,
              PatientConsentResponseXPathTransformer.class.getName()),
          e);
      ExceptionUtils.rethrow(e);
    }

    var xPathBinder = new XPathBinder(envelope);
    ProvincialPatientConsentResponse provincialPatientConsentResponse =
        new ProvincialPatientConsentResponseMapper();

    xPathBinder.bind(provincialPatientConsentResponse);
    String consentToken = extractExactXmlTreeForGivenTagName(response, HL7Constants.CONSENT_EVENT);
    provincialPatientConsentResponse.getProvincialResponsePayload().setConsentToken(consentToken);
    return provincialPatientConsentResponse;
  }

  @Override
  public Province getProvince() {
    return Province.QC;
  }
}
