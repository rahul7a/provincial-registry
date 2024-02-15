package com.lblw.vphx.phms.common.databind;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

class XPathEvaluatorTest {

  private static final String XML_STRING =
      "<root>"
          + "<book category=\"children\">\n"
          + "    <title lang=\"en\">Harry Potter and the Philosopher's Stone</title>\n"
          + "    <title lang=\"fr\">Harry Potter a l'ecole des sorciers</title>\n"
          + "    <author>J.K. Rowling</author>\n"
          + "    <year>2005</year>\n"
          + "    <price>29.99</price>\n"
          + "    <excerpt></excerpt>\n"
          + "</book>"
          + "<book category=\"children\">\n"
          + "    <title lang=\"en\">Harry Potter and the Chamber of Secrets</title>\n"
          + "    <title lang=\"fr\">Harry Potter et la Chambre des Secrets</title>\n"
          + "    <author>J.K. Rowling</author>\n"
          + "    <year>2002</year>\n"
          + "    <price>29.99</price>\n"
          + "</book>"
          + "</root>";

  private static XPathEvaluator xPathEvaluator;

  @BeforeAll
  static void beforeAll() throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Element root =
        builder
            .parse(new ByteArrayInputStream(XML_STRING.getBytes(StandardCharsets.UTF_8)))
            .getDocumentElement();
    xPathEvaluator = new XPathEvaluator(root);
  }

  @Nested
  class GivenXMLElement_whenEvaluate {
    @Test
    void testEvaluateAnElement() {
      Element title = xPathEvaluator.evaluate("book/title/", Element.class);
      assertNotNull(title);
      assertEquals("en", title.getAttribute("lang"));
      assertEquals("Harry Potter and the Philosopher's Stone", title.getTextContent().trim());
    }

    @Test
    void testEvaluateAllSiblingElements() {
      var titles = xPathEvaluator.evaluate("book/title[x]", List.class);
      assertNotNull(titles);
      assertInstanceOf(List.class, titles);
      titles.forEach(title -> assertInstanceOf(Element.class, title));
      assertEquals(2, ((List<Element>) titles).stream().count());
    }

    @Test
    void testEvaluateAllNestedElementsFromSiblingElements() {
      var languages = xPathEvaluator.evaluate("book/title[x]/@lang", List.class);
      assertNotNull(languages);
      assertInstanceOf(List.class, languages);
      assertEquals(List.of("en", "fr"), languages);
    }

    @Test
    void testEvaluateAllDeeplyNestedElementsFromSiblingElements() {
      var languages = xPathEvaluator.evaluate("book[x]/title[x]/text()", List.class);
      assertNotNull(languages);
      assertInstanceOf(List.class, languages);
      assertEquals(
          List.of(
              "Harry Potter and the Philosopher's Stone",
              "Harry Potter a l'ecole des sorciers",
              "Harry Potter and the Chamber of Secrets",
              "Harry Potter et la Chambre des Secrets"),
          languages);
    }

    @Test
    void testEvaluateAnElementMatchingAttribute() {
      Element title = xPathEvaluator.evaluate("book/title[@lang=\"fr\"]", Element.class);
      assertNotNull(title);
      assertEquals("fr", title.getAttribute("lang"));
      assertEquals("Harry Potter a l'ecole des sorciers", title.getTextContent().trim());
    }

    @Test
    void testEvaluateAnAttribute() {
      String category = xPathEvaluator.evaluate("book/@category", String.class);
      assertNotNull(category);
      assertEquals("children", category);
    }

    @Test
    void testEvaluateSelfElement() {
      Element self = xPathEvaluator.evaluate("book/", Element.class);
      assertNotNull(self);
      assertEquals("book", self.getNodeName());
    }

    @Test
    void testEvaluateTextContent() {
      String text = xPathEvaluator.evaluate("book/year/text()", String.class);
      assertNotNull(text);
      assertEquals("2005", text);
    }

    @Test
    void testEvaluateEmptyTextContent() {
      String text = xPathEvaluator.evaluate("book/excerpt/text()", String.class);
      assertNotNull(text);
      assertEquals(Strings.EMPTY, text);
    }

    @Test
    void testEvaluateRootElement() {
      Element root = xPathEvaluator.evaluate("/", Element.class);
      assertNotNull(root);
      assertEquals("root", root.getNodeName());
    }
  }
}
