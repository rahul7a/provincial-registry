package com.lblw.vphx.phms.transformation.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/** This class holds the common methods used by all test classes */
public class TestUtils {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  static {
    objectMapper.registerModule(new JavaTimeModule());
  }

  /**
   * Helps load a json file into the given class object
   *
   * @param pathToJsonFile Path to json file
   * @param classs classs type to cast the json into
   * @return A class representing the json file whose path is given
   * @throws IOException
   */
  public static Object loadJsonToClass(String pathToJsonFile, Class classs) throws IOException {
    final InputStream resourceAsStream =
        TestUtils.class.getClassLoader().getResourceAsStream(pathToJsonFile);
    assertThat(resourceAsStream).isNotNull();
    return objectMapper.readValue(resourceAsStream, classs);
  }

  /**
   * Reads the contents of the given file and returns them as String.
   *
   * @param fileName XML File Name
   * @return contents of the xml file in string format
   * @throws IOException if an I/O error occurs reading from the file or a malformed or unmappable
   *     byte sequence is read
   */
  public static String loadXmlFileContentsAsString(String fileName) throws IOException {
    Path path = Paths.get("src/test/resources", fileName);
    return Files.readString(path);
  }

  /**
   * It takes a string of XML and returns a Document object
   *
   * @param xmlStr The XML string that you want to convert to a Document object.
   * @return A Document object.
   */
  public static Document convertStringToDocument(String xmlStr) throws Exception {
    return DocumentBuilderFactory.newInstance()
        .newDocumentBuilder()
        .parse(new InputSource(new StringReader(xmlStr)));
  }
}
