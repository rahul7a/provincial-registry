package com.lblw.vphx.phms.common.utils;

import java.io.StringWriter;
import java.util.AbstractList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** This utility class contains methods that are useful in parsing / reading an xml elements. */
@Component
@Slf4j
public class XmlParsingUtils {

  private static final String SINGLE_SPACE = " ";

  /**
   * This function recursively traverses down the given XML tree, based on the given multiple
   * descendant names and checking if any of the descendants are nil elements or not.
   *
   * <p><html> <b>Example 1</b> Given following xml tree <br>
   *
   * <pre>{@code
   * <root>
   *     <a>
   *         <b>
   *             <c>
   *                 <d>
   *                     <e></e>
   *                 </d>
   *             </c>
   *         </b>
   *     </a>
   * </root>
   * }</pre>
   *
   * and root is passed as {@code tree} and an array [a,c,d] as {@code descendantNames}, then
   * following tree is returned
   *
   * <pre>{@code
   * <d>
   *      <e></e>
   * </d>
   * }</pre>
   *
   * <br>
   * <b>Example 2</b> The caller must ensure uniqueness of descendant name path. For following tree
   * <br>
   *
   * <pre>{@code
   * <root>
   *    <a>
   *        <b>
   *            <c>abc</c>
   *        </b>
   *        <d>
   *            <c>def</c>
   *        </d>
   *    </a>
   * </root>
   * }</pre>
   *
   * and root is passed as {@code tree} and an array [a,d,c] as {@code descendantNames}, then
   * following tree is returned
   *
   * <pre>{@code
   * <c>def</c>
   *
   * }</pre>
   *
   * @param tree XML tree to be traversed.
   * @param descendantNames An array of descendant names i.e. one of the child elements of the root
   *     node of the tree
   * @return Returns:
   *     <ol>
   *       <li>the element that represents the XML tree structure of the element whose name is in
   *           the last of the descendentNames; <br>
   *       <li>the {@code tree} back in case {@code descendantNames} is null;<br>
   *       <li>null if any of the descendant element is Nil. i.e. has an attribute {@code
   *           xsi:nil="true"}; or {@code tree} is null<br>
   *     </ol>
   */
  @Nullable
  public static Element getDeepestDescendantElementTree(Element tree, String... descendantNames) {
    if (tree == null) {
      return null;
    }
    if (descendantNames == null || descendantNames.length == 0) {
      return tree;
    }
    Element subTree = tree;
    for (String descendantName : descendantNames) {
      if (isElementNil(subTree)) {
        return null;
      }

      subTree = (Element) subTree.getElementsByTagName(descendantName).item(0);
    }
    return subTree;
  }

  /**
   * This behaves similar to {@link #getDeepestDescendantElementTree(Element, String...)}, except is
   * designed to be used in a more special situation.
   *
   * <p><html> <b>Example 1</b> Given following special xml tree, wherein when
   * document.getElementsById("b") is applied at the root tree, then only the first {@code <b>} with
   * attribute 'attr_b_a' will be returned. This method is designed to return the {@code <b>} tag
   * with attribute 'attr_b'.</b> <br>
   *
   * <pre>{@code
   * <root>
   *     <a>
   *         <b attr_b_a="true">
   *         </b>
   *     </a>
   *     <b attr_b="true">
   *         <c></c>
   *     </b
   * </root>
   * }</pre>
   *
   * When root is passed as {@code tree}, {@code attributeName} is passed as 'attr_b' and 'b' as
   * {@code descendantNames}, then following tree is returned
   *
   * <pre>{@code
   * <b attr_b="true">
   *     <c></c>
   * </b
   * }</pre>
   *
   * @param tree XML tree to be traversed.
   * @param attributeName name of the attribute expected on the deepest element
   * @param descendantName A descendant name i.e. one of the child elements of the root node of the
   *     tree
   * @return Returns:
   *     <ol>
   *       <li>the element that represents the XML tree structure of the element whose name is in
   *           the last of the descendentNames; <br>
   *       <li>the {@code tree} back in case {@code descendantNames} is null;<br>
   *       <li>null if any of the descendant element is Nil. i.e. has an attribute {@code
   *           xsi:nil="true"}; or {@code tree} is null<br>
   *     </ol>
   */
  @Nullable
  public static Element getDescendantElementTreeWithGivenAttribute(
      Element tree, String attributeName, String descendantName) {
    if (tree == null) {
      return null;
    }
    NodeList nodeList = tree.getElementsByTagName(descendantName);
    if (nodeList == null) {
      return null;
    }
    for (int index = 0; index < nodeList.getLength(); index++) {
      final Element currentElement = (Element) nodeList.item(index);
      if (!currentElement.hasAttribute(attributeName)) {
        continue;
      }
      return currentElement;
    }
    return null;
  }

  /**
   * Checks whether the current element is nil or not. <b>Example 2</b> The caller must ensure
   * uniqueness of descendant name path. For following tree * <br>
   * <b> Example, </b>
   *
   * <pre>{@code
   * <root>
   *    <b xsi:nil="true/>
   * </root>
   *
   * }</pre>
   *
   * Returns false for {@code <root>} element; but true Element {@code <b>};
   *
   * @param element Element whose nil-ability status is requested.
   * @return Returns:
   *     <ol>
   *       <li>true if the element has an attribute {@code xsi:nil="true"}
   *       <li>the element does not exists
   *     </ol>
   */
  public static boolean isElementNil(Element element) {
    if (element == null) {
      return true;
    }
    final Node nilAttribute = element.getAttributes().getNamedItem("xsi:nil");
    if (nilAttribute != null) {
      final String nilAttributeValue = nilAttribute.getNodeValue();
      return Boolean.TRUE.toString().equalsIgnoreCase(nilAttributeValue);
    }
    return false;
  }

  /**
   * Returns the value of the attribute {@code attributeName}, which belongs to an element which
   * <b>must be identified uniquely </b> by the array of descendents ( specified by {@code
   * descendantNames}), of the root tree indicated by {@code nodeTree}
   *
   * @param nodeTree Root of the tree which must be traversed
   * @param attributeName Name of the attribute whose value is needed
   * @param descendantNames An array of descendent element names, which when traversed in order of
   *     array index, within the {@code nodeTree}, uniquely identify the descendant element, to
   *     which the {@code attributeName} belongs
   * @return Returns:
   *     <ol>
   *       <li>Value of the {@code attributeName}
   *       <li>null in case the any of the descendants do not exist
   *     </ol>
   */
  public static String getAttributeValueFromTheGivenTree(
      Element nodeTree, String attributeName, String... descendantNames) {
    final Element deepestDescendantElementTree =
        getDeepestDescendantElementTree(nodeTree, descendantNames);
    if (deepestDescendantElementTree == null) {
      return null;
    }
    final Node attributeNode =
        deepestDescendantElementTree.getAttributes().getNamedItem(attributeName);
    if (attributeNode == null) {
      return null;
    }
    return attributeNode.getNodeValue();
  }

  /**
   * Returns the text contents of the last element in the array of {@code descendantNames}, of the
   * specified XML tree represented by {@code nodeTree}. The caller must ensure that the {@code
   * descendantNames} are specified in unique and correct order for desired behavior
   *
   * @param nodeTree Root of the tree which must be traversed
   * @param descendantNames An array of descendent element names, which must be traversed in order
   *     of the array index within the {@code nodeTree}.
   * @return Returns:
   *     <ol>
   *       <li>the text contents of the element that represents the XML tree structure of the
   *           element whose name is in the last of the descendantNames; <br>
   *       <li>the text contents of the root element of the {@code nodeTree} in case {@code
   *           descendantNames} is null;<br>
   *       <li>null if any of the descendant element is Nil. i.e. has an attribute {@code *
   *           xsi:nil="true"}; or {@code tree} is null<br>
   *     </ol>
   */
  public static String getElementTextFromTheGivenTree(Element nodeTree, String... descendantNames) {
    final Element deepestDescendantElementTree =
        getDeepestDescendantElementTree(nodeTree, descendantNames);
    if (deepestDescendantElementTree == null) {
      return null;
    }
    String textContent = deepestDescendantElementTree.getTextContent();
    return textContent == null ? null : textContent.trim();
  }

  /**
   * Returns as a String the xml between {@code tagName} Precondition: tagName must occur just once
   * within the xml. P.S.: This currently only correctly processes those tag names whose begin tag
   * is of type
   *
   * <pre>{@code
   *    <tagName attribute1='value1'>
   * }
   *
   * but <b>incorrectly</b> processes tag names of type
   *
   * <pre>{@code
   *    <tagName> .... </tagName>
   * <root>
   *
   * In case there is a requirement in future to tackle the latter case, then this function must be enhanced.
   *
   * @param xmlTreeString {@link String}
   * @param tagName {@link String }
   * @return xmlblob between the tagName
   */
  public static String extractExactXmlTreeForGivenTagName(String xmlTreeString, String tagName) {
    if (StringUtils.isBlank(xmlTreeString) || StringUtils.isBlank(tagName)) {
      return null;
    }
    String beginTag = "<" + tagName + SINGLE_SPACE;
    String endTag = "</" + tagName + ">";
    int lengthOfEndTag = endTag.length();
    int startPositionOfTag = xmlTreeString.indexOf(beginTag);
    if (startPositionOfTag <= 0) {
      return null;
    }
    int endPositionOfTag = xmlTreeString.lastIndexOf(endTag) + lengthOfEndTag;
    return xmlTreeString.substring(startPositionOfTag, endPositionOfTag).trim();
  }

  /**
   * This function recursively traverses down the given XML tree breadth first drilling down
   * matching direct descendants matched by {@code descendantNames}
   *
   * <p><html> <b>Example 1</b> Given following xml tree <br>
   *
   * <pre>{@code
   * <root>
   *     <a>
   *         <b>
   *             <c>
   *                 <d>
   *                     <e></e>
   *                 </d>
   *             </c>
   *         </b>
   *     </a>
   * </root>
   * }</pre>
   *
   * and root is passed as {@code tree} and an array [a, b, c, d] as {@code descendantNames}, then
   * following tree is returned
   *
   * <pre>{@code
   * <d>
   *      <e></e>
   * </d>
   * }</pre>
   *
   * <br>
   * <b>Example 2</b> The caller must ensure descendant names are direct descendants and not deep
   * descendants. For following tree <br>
   *
   * <pre>{@code
   * <root>
   *    <a>
   *        <b>
   *            <c>
   *                <d>
   *                    <c>def</c>
   *                </d>
   *            </c>
   *        </b>
   *    </a>
   * </root>
   * }</pre>
   *
   * and root is passed as {@code tree} and an array [a, c] as {@code descendantNames}, then
   * traversal breaks and null is returned
   *
   * @param tree XML tree to be traversed.
   * @param descendantNames An array of direct descendant names i.e. in order; as relative path
   * @return Returns:
   *     <ol>
   *       <li>the element that represents the XML tree structure of the element whose name is in
   *           the last of the descendentNames; <br>
   *       <li>the {@code tree} back in case {@code descendantNames} is null;<br>
   *       <li>null if any of the descendant element is Nil. i.e. has an attribute {@code
   *           xsi:nil="true"}; or {@code tree} is null<br>
   *     </ol>
   */
  @Nullable
  public static Element getDescendantElementTreeByExactPath(
      Element tree, String... descendantNames) {
    if (tree == null) {
      return null;
    }
    if (descendantNames == null || descendantNames.length == 0) {
      return tree;
    }
    var currentDescendant = tree;
    for (var descendantName : descendantNames) {
      if (isElementNil(currentDescendant)) {
        return null;
      }
      var children = currentDescendant.getChildNodes();
      currentDescendant =
          IntStream.range(0, children.getLength())
              .mapToObj(children::item)
              .filter(
                  node ->
                      node instanceof Element
                          && ((Element) node).getTagName().equals(descendantName))
              .map(Element.class::cast)
              .findFirst()
              .orElse(null);
    }
    return currentDescendant;
  }

  /**
   * This function recursively traverses down the given XML tree, based on the given multiple
   * descendant names and checking if any of the descendants are nil elements or not.
   *
   * <p><html> <b>Example 1</b> Given following xml tree <br>
   *
   * <pre>{@code
   * <root>
   *     <a>
   *         <b>
   *             <c>
   *                 <d>
   *                     <e></e>
   *                 </d>
   *             </c>
   *         </b>
   *     </a>
   * </root>
   * }</pre>
   *
   * and root is passed as {@code tree} and an array [a,c,d] as {@code descendantNames}, then
   * following tree is returned
   *
   * <pre>{@code
   * <d>
   *      <e></e>
   * </d>
   * }</pre>
   *
   * <br>
   * <b>Example 2</b> The caller must ensure uniqueness of descendant name path. For following tree
   * <br>
   *
   * <pre>{@code
   * <root>
   *    <a>
   *        <b>
   *            <c>abc</c>
   *        </b>
   *        <d>
   *            <c>def</c>
   *        </d>
   *    </a>
   * </root>
   * }</pre>
   *
   * and root is passed as {@code tree} and an array [a,d,c] as {@code descendantNames}, then
   * following tree is returned
   *
   * <pre>{@code
   * <c>def</c>
   *
   * }</pre>
   *
   * @param tree XML tree to be traversed.
   * @param descendantNames An array of descendant names i.e. one of the child elements of the root
   *     node of the tree
   * @return Returns:list of elements that represents the XML tree structure of the element whose
   *     name is in * the last of the descendentNames;
   */
  public static List<Element> getDeepestDescendantElementChildren(
      Element tree, String... descendantNames) {
    if (isElementNil(tree)) {
      return Collections.emptyList();
    }
    var subTree = tree;
    int index = 0;
    while (index < descendantNames.length - 1) {
      subTree = (Element) subTree.getElementsByTagName(descendantNames[index]).item(0);
      if (isElementNil(subTree)) {
        return Collections.emptyList();
      }
      index++;
    }
    var nodeList = subTree.getElementsByTagName(descendantNames[index]);
    return new AbstractList<>() {
      public int size() {
        return nodeList.getLength();
      }

      public Element get(int index) {
        Element item = (Element) nodeList.item(index);
        if (item == null) throw new IndexOutOfBoundsException();
        return item;
      }
    };
  }

  /**
   * This function recursively traverses down the given XML tree, based on the given multiple names
   * and checking if any of are nil elements or not.
   *
   * <p><html> <b>Example 1</b> Given following xml tree <br>
   *
   * <pre>{@code
   * <root>
   *     <a>
   *         <b>
   *             <c>
   *                 <d>
   *                     <e></e>
   *                 </d>
   *                 <d>
   *                     <e></e>
   *                 </d>
   *             </c>
   *         </b>
   *     </a>
   * </root>
   * }</pre>
   *
   * and root is passed as {@code tree} and an array [a,c,d] as {@code descendantNames}, then
   * following tree is returned
   *
   * <pre>{@code
   * <d>
   *      <e></e>
   * </d>
   * <d>
   *      <e></e>
   * </d>
   * }</pre>
   *
   * <br>
   * <b>Example 2</b> The caller must ensure uniqueness of descendant name path. For following tree
   * <br>
   *
   * <pre>{@code
   * <root>
   *    <a>
   *        <b>
   *            <c>abc</c>
   *        </b>
   *        <b>
   *            <c>def</c>
   *        </b>
   *    </a>
   * </root>
   * }</pre>
   *
   * and root is passed as {@code tree} and an array [a,d,c] as {@code descendantNames}, then
   * following tree is returned
   *
   * <pre>{@code
   * <c>abc</c>
   * <c>def</c>
   *
   * }</pre>
   *
   * @param tree XML tree to be traversed.
   * @param descendantNames An array of descendant names i.e. one of the child elements of the root
   *     node of the tree
   * @return Returns:list of elements that represents the XML tree structure of the element whose
   *     name is in * the first of the descendantName; empty list if not found in path
   */
  public static List<Element> getDeepestDescendantElementChildrenByExactPath(
      Element tree, String childTag, String... descendantNames) {
    var nodeElement = getDescendantElementTreeByExactPath(tree, descendantNames);
    if (nodeElement == null) return Collections.emptyList();
    var children = nodeElement.getChildNodes();
    return IntStream.range(0, children.getLength())
        .mapToObj(children::item)
        .filter(node -> node instanceof Element && ((Element) node).getTagName().equals(childTag))
        .map(Element.class::cast)
        .collect(Collectors.toList());
  }

  /**
   * Converts a signed XML request from a {@link Document} to a {@link String}
   *
   * @param document {@link Document} representation of the signed XML request. Return empty string
   *     if input is null
   * @return {@link String} conversion of the signed {@link Document}
   * @throws TransformerException when unable to transform to string
   */
  public String convertXMLDocumentToString(Document document) throws TransformerException {

    if (document == null) {
      log.info("Document to convert to string was null. Returning empty string.");
      return "";
    }

    // Translate the XML into a string to return
    StringWriter stringWriter = new StringWriter();
    TransformerFactory transformerFactory = TransformerFactory.newInstance();

    // fixes XXE attack vulnerability as recommended by sonar scan
    transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

    transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    transformer.transform(new DOMSource(document), new StreamResult(stringWriter));

    return stringWriter.toString().trim();
  }
}
