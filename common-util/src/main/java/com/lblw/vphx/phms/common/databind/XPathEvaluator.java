package com.lblw.vphx.phms.common.databind;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Internal utility class. A node used for caching intermediate results during the XML-to-object
 * binding process. The node has a value and can have child nodes, each of which is identified by
 * the segment as a string key. Composition of which builds a cache node tree
 */
@Data
@Builder
class CacheNode {
  private Object value;

  @Builder.Default private Map<String, CacheNode> children = new ConcurrentHashMap<>();
}

/**
 * Internal utility class for an xPath evaluator that provides methods to evaluate xPath expressions
 * on an XML Element.
 */
@Slf4j
class XPathEvaluator implements IXPathEvaluator {
  /** The root XML context to read and evaluate from. Can be root or segment of an XML document */
  private final CacheNode root;

  /** Reference to the Document @{see XPathEvaluator#root} belongs to */
  private final Document document;

  /**
   * Constructs a new XPathEvaluator instance using the provided XML element.
   *
   * @param xml the root XML context to read and evaluate from. Can be root or segment of an XML
   *     document
   */
  public XPathEvaluator(Element xml) {
    this.root = CacheNode.builder().value(xml).build();
    this.document = xml.getOwnerDocument();
  }

  /**
   * Evaluates xPath segment with different xPath language patterns. Delegates each pattern to
   * dedicated method
   *
   * @param element the context from where the segment needs to be evaluated from.
   * @param segment the segment to be evaluated.
   * @return the evaluated segment.
   */
  private static Optional<?> evaluate(Element element, String segment) {
    if (StringUtils.isBlank(segment)) {
      return Optional.of(element);
    }

    if (segment.startsWith("@")) {
      return Optional.of(evaluateSegmentAttributeValue(element, segment));
    }

    if (segment.contains("[@")) {
      return evaluateNextSegmentWithAttributeValue(element, segment);
    }

    if (segment.endsWith("text()")) {
      return Optional.of(evaluateSegmentTextBody(element));
    }

    if (segment.endsWith("[x]")) {
      return Optional.of(evaluateNextSegments(element, segment));
    }

    return evaluateNextSegment(element, child -> child.getNodeName().equals(segment));
  }

  /**
   * Evaluates xPath segment with pattern 'tag[x]'
   *
   * @param element the context from where the segment needs to be evaluated from.
   * @param segment the segment to be evaluated.
   * @return list of evaluated Element. Can be empty.
   */
  private static List<Element> evaluateNextSegments(Element element, String segment) {
    String path = segment.substring(0, segment.indexOf("[x]"));
    var nodeList = element.getChildNodes();
    return Stream.iterate(0, i -> i < nodeList.getLength(), i -> i + 1)
        .map(nodeList::item)
        .filter(childNode -> childNode.getNodeType() == Node.ELEMENT_NODE)
        .map(Element.class::cast)
        .filter(child -> child.getNodeName().equals(path))
        .collect(Collectors.toList());
  }

  /**
   * Evaluates xPath segment with pattern 'text()'.
   *
   * @param element the context from where the segment needs to be evaluated from.
   * @return text content from the context.
   */
  private static String evaluateSegmentTextBody(Element element) {
    NodeList childNodes = element.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);
      if (node.getNodeType() == Node.TEXT_NODE) {
        return node.getTextContent().trim();
      }
    }

    return StringUtils.EMPTY;
  }

  /**
   * Evaluates xPath segment with pattern '@attr'.
   *
   * @param element the context from where the segment needs to be evaluated from.
   * @param segment the segment to be evaluated.
   * @return attribute value.
   */
  private static String evaluateSegmentAttributeValue(Element element, String segment) {
    String attr = segment.substring(1);
    return element.getAttribute(attr);
  }

  /**
   * Evaluates xPath segment with pattern '[@attr=value]'
   *
   * @param element the context from where the segment needs to be evaluated from.
   * @param segment the segment to be evaluated.
   * @return the next segment that matches attribute value given in segment.
   */
  private static Optional<Element> evaluateNextSegmentWithAttributeValue(
      Element element, String segment) {
    String path = segment.substring(0, segment.indexOf("["));
    String attr = segment.substring(segment.indexOf("@") + 1, segment.indexOf("="));
    String value = segment.substring(segment.indexOf("=") + 2, segment.length() - 2);
    return evaluateNextSegment(
        element,
        child -> child.getNodeName().equals(path) && child.getAttribute(attr).equals(value));
  }

  private static Optional<Element> evaluateNextSegment(
      Element element, Predicate<Element> predicate) {
    NodeList childNodes = element.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element childElement = (Element) node;
        if (predicate.test(childElement)) {
          return Optional.of(childElement);
        }
      }
    }
    return Optional.empty();
  }

  /** {@inheritDoc} */
  @Override
  public <T> T evaluate(String xPath, Class<T> clazz) {
    CacheNode currentNode = root;
    Object lastResult = root.getValue();

    for (String segment : xPath.split("/")) {
      if (!currentNode.getChildren().containsKey(segment)) {
        CacheNode newNode = CacheNode.builder().build();
        currentNode.getChildren().put(segment, newNode);
      }

      currentNode = currentNode.getChildren().get(segment);

      if (currentNode.getValue() == null) {
        // org.w3c.dom is not thread-safe, even for reads
        synchronized (document) {
          if (lastResult instanceof List) {
            lastResult =
                ((List<?>) lastResult)
                    .stream()
                        .map(Element.class::cast)
                        .map(singleResult -> XPathEvaluator.evaluate(singleResult, segment))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .flatMap(
                            unwrappedResult -> {
                              if (unwrappedResult instanceof List) {
                                return ((List<?>) unwrappedResult).stream();
                              }
                              return Stream.of(unwrappedResult);
                            })
                        .collect(Collectors.toList());
          } else {
            lastResult = XPathEvaluator.evaluate((Element) lastResult, segment).orElse(null);
          }
        }
        currentNode.setValue(lastResult);
      } else {
        lastResult = currentNode.getValue();
      }

      if (lastResult == null) {
        return null;
      } else if (!(lastResult instanceof Element || lastResult instanceof List)) {
        break;
      }
    }

    return clazz.cast(currentNode.getValue());
  }
}
