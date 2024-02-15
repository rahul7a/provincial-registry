package com.lblw.vphx.phms.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.lblw.vphx.phms.common.utils.XmlParsingUtils;
import java.io.IOException;
import java.io.StringReader;
import java.util.UUID;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** Unit Test For {@link XmlParsingUtils} that are useful in parsing / reading an xml elements. */
public class XmlParsingUtilsTest {

  private static String testInputSampleXMLString;
  private static Element testElement;
  private XmlParsingUtils xmlParsingUtils;

  @BeforeAll
  public static void beforeAll() throws IOException, ParserConfigurationException, SAXException {
    testInputSampleXMLString =
        TestUtils.loadXmlFileContentsAsString(
            "utils/xmlparsing/extractExactXmlTreeForGivenTagName/testinput/testinput.xml");
    testElement = getTestInputElementFromATestXml();
  }

  private static Element getTestInputElementFromATestXml()
      throws ParserConfigurationException, SAXException, IOException {
    String sampleXMLString =
        TestUtils.loadXmlFileContentsAsString(
            "utils/xmlparsing/getElementTextFromTheGivenTree/testinput/testinput.xml");

    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    builderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    DocumentBuilder builder = builderFactory.newDocumentBuilder();
    final Document xmlDocument = builder.parse(new InputSource(new StringReader(sampleXMLString)));
    return xmlDocument.getDocumentElement();
  }

  @BeforeEach
  public void init() {
    xmlParsingUtils = new XmlParsingUtils();
  }

  @Test
  void whenXmlTreeString_AndTagNameAreBlank_ThenNullIsReturned() {
    String result = xmlParsingUtils.extractExactXmlTreeForGivenTagName("", "");
    assertThat(result).isNull();

    result = xmlParsingUtils.extractExactXmlTreeForGivenTagName(null, null);
    assertThat(result).isNull();

    result = xmlParsingUtils.extractExactXmlTreeForGivenTagName(" ", null);
    assertThat(result).isNull();

    result = xmlParsingUtils.extractExactXmlTreeForGivenTagName(null, "  ");
    assertThat(result).isNull();

    result = xmlParsingUtils.extractExactXmlTreeForGivenTagName(UUID.randomUUID().toString(), "");
    assertThat(result).isNull();

    result = xmlParsingUtils.extractExactXmlTreeForGivenTagName("", UUID.randomUUID().toString());
    assertThat(result).isNull();
  }

  @Test
  void whenTagNameDoesNotExist_InTheGivenXml_ThenNullIsReceived() {
    final String result =
        xmlParsingUtils.extractExactXmlTreeForGivenTagName(
            testInputSampleXMLString, UUID.randomUUID().toString());
    assertThat(result).isNull();
  }

  @Test
  void whenTagNameExists_ThenTagIsExtracted() throws IOException {
    String result =
        xmlParsingUtils.extractExactXmlTreeForGivenTagName(
            testInputSampleXMLString, "consentEvent");
    String expectedResult =
        TestUtils.loadXmlFileContentsAsString(
            "utils/xmlparsing/extractExactXmlTreeForGivenTagName/expectation/consent.xml");
    assertThat(result).isEqualTo(expectedResult);
  }

  @Test
  void whenDeepestDescendentElementTreeIsNull_ThenNullIsReturned() {
    final String elementTextFromTheGivenTree =
        xmlParsingUtils.getElementTextFromTheGivenTree(mock(Element.class), null);
    assertThat(elementTextFromTheGivenTree).isNull();
  }

  @Test
  void whenDescendantNameExists_ThenItsTextIsReturned() {
    final String elementTextFromTheGivenTree =
        xmlParsingUtils.getElementTextFromTheGivenTree(testElement, "cais:SessionID");
    assertThat(elementTextFromTheGivenTree).isEqualTo("f96ac3679be54a6db4df037006d0c");
  }

  @Test
  void whenDescendentNameIsSubstringOfAnActualTagname_ThenEmptyStringIsReturned() {
    final String elementTextFromTheGivenTree =
        xmlParsingUtils.getElementTextFromTheGivenTree(testElement, "consent");
    assertThat(elementTextFromTheGivenTree).isNull();
  }

  @Test
  void whenDescendantName_HasNoText_ThenEmptyStringIsReturned() {
    final String elementTextFromTheGivenTree =
        xmlParsingUtils.getElementTextFromTheGivenTree(testElement, "processingModeCode");
    assertThat(elementTextFromTheGivenTree).isEmpty();
  }

  @Test
  void whenDescendantName_HasNullText_ThenNullIsReturned() {
    final String elementTextFromTheGivenTree =
        xmlParsingUtils.getElementTextFromTheGivenTree(testElement, "personalRelationshipCode");
    assertThat(elementTextFromTheGivenTree).isEmpty();
  }
}
