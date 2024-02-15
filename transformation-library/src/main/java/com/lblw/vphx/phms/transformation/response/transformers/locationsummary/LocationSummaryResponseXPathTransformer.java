package com.lblw.vphx.phms.transformation.response.transformers.locationsummary;

import com.lblw.vphx.phms.common.coding.services.CodeableConceptService;
import com.lblw.vphx.phms.common.constants.ErrorConstants;
import com.lblw.vphx.phms.common.databind.XPathBinder;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.location.response.hl7v3.ProvincialLocationSearchResponse;
import com.lblw.vphx.phms.transformation.exceptions.TransformationException;
import com.lblw.vphx.phms.transformation.response.ResponseTransformer;
import com.lblw.vphx.phms.transformation.response.transformers.locationsummary.mappers.LocationSummaryResponseMapper;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LocationSummaryResponseXPathTransformer
    implements ResponseTransformer<ProvincialLocationSearchResponse> {
  private final CodeableConceptService codeableConceptService;

  /**
   * Public constructor.
   *
   * @param codeableConceptService {@link CodeableConceptService}
   */
  public LocationSummaryResponseXPathTransformer(CodeableConceptService codeableConceptService) {
    this.codeableConceptService = codeableConceptService;
  }

  /**
   * Transform response into domain object. Response could be xml, hl7-v2 or hl7-v3 etc.
   *
   * @param response Response could be xml, hl7-v2 or hl7-v3 etc. In current implementation only
   *     hl7-v3 is implemented
   * @return {@link ProvincialLocationSearchResponse}
   * @throws TransformationException occurs if transformation of XML response failed
   */
  @Override
  public Mono<ProvincialLocationSearchResponse> transform(String response) {

    return Mono.fromCallable(
        () -> {
          if (StringUtils.isBlank(response)) {
            log.error("Null/empty/blank response body.");
            throw new IllegalArgumentException("Response body is required");
          }
          return transformXMLPayloadToLocationSummaryResponse(response);
        });
  }

  /**
   * Transforms XML response to ProvincialPatientConsentResponse
   *
   * @param response XML {@link String}
   * @return {@link ProvincialLocationSearchResponse} transformed response
   */
  private ProvincialLocationSearchResponse transformXMLPayloadToLocationSummaryResponse(
      String response) {
    Element envelope = null;
    try {
      envelope = createRootResponseEnvelope(response);
    } catch (ParserConfigurationException | SAXException | IOException e) {
      log.error(
          String.format(
              ErrorConstants.EXCEPTION_MESSAGE_TRANSFORM_XML_RESPONSE,
              LocationSummaryResponseXPathTransformer.class.getName()),
          e);
      ExceptionUtils.rethrow(e);
    }

    var xPathBinder = new XPathBinder(envelope);
    ProvincialLocationSearchResponse provincialLocationSearchResponse =
        new LocationSummaryResponseMapper(codeableConceptService);

    xPathBinder.bind(provincialLocationSearchResponse);
    return provincialLocationSearchResponse;
  }

  /** {@inheritDoc} */
  @Override
  public Province getProvince() {
    return Province.QC;
  }
}
