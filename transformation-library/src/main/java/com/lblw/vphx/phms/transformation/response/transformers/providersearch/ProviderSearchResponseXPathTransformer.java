package com.lblw.vphx.phms.transformation.response.transformers.providersearch;

import com.lblw.vphx.phms.common.coding.services.CodeableConceptService;
import com.lblw.vphx.phms.common.constants.ErrorConstants;
import com.lblw.vphx.phms.common.databind.XPathBinder;
import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.provider.response.hl7v3.ProvincialProviderSearchResponse;
import com.lblw.vphx.phms.transformation.exceptions.TransformationException;
import com.lblw.vphx.phms.transformation.response.ResponseTransformer;
import com.lblw.vphx.phms.transformation.response.helpers.CommonResponseXPathTransformerHelper;
import com.lblw.vphx.phms.transformation.response.transformers.providersearch.mappers.ProvincialProviderSearchResponseMapper;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import reactor.core.publisher.Mono;

/** This class helps transformation service to transform response for providerSearchResponse */
@Component
@Slf4j
public class ProviderSearchResponseXPathTransformer
    implements ResponseTransformer<ProvincialProviderSearchResponse> {

  private final CodeableConceptService codeableConceptService;
  private final CommonResponseXPathTransformerHelper commonResponseXPathTransformerHelper;

  public ProviderSearchResponseXPathTransformer(
      CodeableConceptService codeableConceptService,
      CommonResponseXPathTransformerHelper commonResponseXPathTransformerHelper) {
    this.codeableConceptService = codeableConceptService;
    this.commonResponseXPathTransformerHelper = commonResponseXPathTransformerHelper;
  }

  /**
   * Transform response into domain object. Response could be xml, hl7-v2 or hl7-v3 etc.
   *
   * @param response Response could be xml, hl7-v2 or hl7-v3 etc. In current implementation only
   *     hl7-v3 is implemented
   * @return {@link ProvincialProviderSearchResponse}
   * @throws TransformationException occurs if transformation of XML response failed
   */
  @Override
  public Mono<ProvincialProviderSearchResponse> transform(String response) {
    return Mono.fromCallable(
        () -> {
          if (StringUtils.isBlank(response)) {
            log.error("Null/empty/blank response body.");
            throw new IllegalArgumentException("Response body is required");
          }
          return transformXMLPayloadToProviderSearchResponse(response);
        });
  }

  /**
   * Transforms XML response to ProvincialProviderSearchResponse
   *
   * @param response XML {@link String}
   * @return {@link ProvincialProviderSearchResponse} transformed response
   */
  private ProvincialProviderSearchResponse transformXMLPayloadToProviderSearchResponse(
      String response) {

    Element envelope = null;
    try {
      envelope = createRootResponseEnvelope(response);
    } catch (ParserConfigurationException | SAXException | IOException e) {
      log.error(
          String.format(
              ErrorConstants.EXCEPTION_MESSAGE_TRANSFORM_XML_RESPONSE,
              ProviderSearchResponseXPathTransformer.class.getName()),
          e);
      ExceptionUtils.rethrow(e);
    }

    var xPathBinder = new XPathBinder(envelope);
    ProvincialProviderSearchResponse provincialProviderSearchResponse =
        new ProvincialProviderSearchResponseMapper(
            getProvince(), codeableConceptService, commonResponseXPathTransformerHelper);

    xPathBinder.bind(provincialProviderSearchResponse);
    return provincialProviderSearchResponse;
  }

  @Override
  public Province getProvince() {
    return Province.QC;
  }
}
