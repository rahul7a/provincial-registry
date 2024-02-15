package com.lblw.vphx.phms.transformation.response;

import com.lblw.vphx.phms.domain.common.Province;
import com.lblw.vphx.phms.domain.common.response.ProvincialResponse;
import com.lblw.vphx.phms.domain.common.response.hl7v3.Response;
import com.lblw.vphx.phms.domain.common.response.hl7v3.ResponseControlAct;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import reactor.core.publisher.Mono;

/** This interface helps to transform response depending on the Transaction Type */
public interface ResponseTransformer<
    R extends Response<? extends ResponseControlAct, ? extends ProvincialResponse>> {
  /**
   * Transform response into domain object.
   *
   * @param hl7Response {@link String} Response is hl7-v3
   * @return {@link Response}after the transaction
   */
  Mono<R> transform(String hl7Response);

  /**
   * Returns implementation province
   *
   * @return {@link Province}
   */
  Province getProvince();

  /**
   * Default and common implementation that converts the incoming response to {@link Element}
   *
   * @param response HL7V3 XML response.
   * @return {@link Element} representing the root element tree of the response
   * @throws ParserConfigurationException thrown when any error while parsing.
   * @throws IOException thrown when any error while parsing.
   * @throws SAXException thrown when any error while parsing.
   */
  default Element createRootResponseEnvelope(String response)
      throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

    // fixes XXE attack vulnerability as recommended by sonar scan
    builderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    builderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

    builderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    DocumentBuilder builder = builderFactory.newDocumentBuilder();
    Document xmlDocument = builder.parse(new InputSource(new StringReader(response)));
    return xmlDocument.getDocumentElement();
  }
}
