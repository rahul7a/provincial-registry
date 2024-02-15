package com.lblw.vphx.phms.transformation.response.transformers.patientsearch;

import com.lblw.vphx.phms.common.constants.ErrorConstants;
import com.lblw.vphx.phms.common.databind.XPathBinder;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.patient.response.hl7v3.ProvincialPatientSearchResponse;
import com.lblw.vphx.phms.transformation.exceptions.TransformationException;
import com.lblw.vphx.phms.transformation.response.ResponseTransformer;
import com.lblw.vphx.phms.transformation.response.helpers.CommonResponseXPathTransformerHelper;
import com.lblw.vphx.phms.transformation.response.transformers.patientsearch.mappers.ProvincialPatientSearchResponseMapper;
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
public class PatientSearchResponseXPathTransformer
    implements ResponseTransformer<ProvincialPatientSearchResponse> {
  private final CommonResponseXPathTransformerHelper commonResponseXPathTransformerHelper;

  public PatientSearchResponseXPathTransformer(
      CommonResponseXPathTransformerHelper commonResponseXPathTransformerHelper) {
    this.commonResponseXPathTransformerHelper = commonResponseXPathTransformerHelper;
  }

  /**
   * Transform response into domain object. Response could be xml, hl7-v2 or hl7-v3 etc.
   *
   * @param response Response could be xml, hl7-v2 or hl7-v3 etc. In current implementation only
   *     hl7-v3 is implemented
   * @return {@link ProvincialPatientSearchResponse}
   * @throws TransformationException occurs if transformation of XML response failed
   */
  @Override
  public Mono<ProvincialPatientSearchResponse> transform(String response) {
    return Mono.fromCallable(
        () -> {
          if (StringUtils.isBlank(response)) {
            log.error("Null/empty/blank response body.");
            throw new IllegalArgumentException("Response body is required");
          }
          return transformXMLPayloadToPatientSearchResponse(response);
        });
  }

  /**
   * Transforms XML response to ProvincialPatientSearchResponse
   *
   * @param response XML {@link String}
   * @return {@link ProvincialPatientSearchResponse} transformed response
   */
  private ProvincialPatientSearchResponse transformXMLPayloadToPatientSearchResponse(
      String response) {

    Element envelope = null;
    try {
      envelope = createRootResponseEnvelope(response);
    } catch (ParserConfigurationException | SAXException | IOException e) {
      log.error(
          String.format(
              ErrorConstants.EXCEPTION_MESSAGE_TRANSFORM_XML_RESPONSE,
              PatientSearchResponseXPathTransformer.class.getName()),
          e);
      ExceptionUtils.rethrow(e);
    }

    var xPathBinder = new XPathBinder(envelope);
    ProvincialPatientSearchResponse provincialPatientSearchResponse =
        new ProvincialPatientSearchResponseMapper(commonResponseXPathTransformerHelper);

    xPathBinder.bind(provincialPatientSearchResponse);
    return provincialPatientSearchResponse;
  }

  @Override
  public Province getProvince() {
    return Province.QC;
  }
}
