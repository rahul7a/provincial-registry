package com.lblw.vphx.phms.common.databind;

interface IXPathEvaluator {
  /**
   * An xPath evaluator. Evaluates an xPath expression to an expected type.
   *
   * <pre>{@code
   * Given the following XML document:
   *   <book category="children">
   *     <title lang="en">Harry Potter and the Philosopher's Stone</title>
   *     <title lang="fr">Harry Potter a l'ecole des sorciers</title>
   *     <author>J.K. Rowling</author>
   *     <year>2005</year>
   *     <price>29.99</price>
   *     Excerpt
   *   </book>
   * }</pre>
   *
   * <p>Consider Element root = above xml document
   *
   * <p>To get the 'title' element: {@code xPathEvaluator.evaluate("book/title/"); or
   * XPathEvaluator.evaluate(root, "title"); returns Element or null}
   *
   * <p>To get the 'title' element with 'fr' lang: {@code
   * xPathEvaluator.evaluate("book/title[@lang=\"fr\"]"); returns String}
   *
   * <p>To get all the 'title' elements : {@code xPathEvaluator.evaluate("book/title[x]"); returns
   * List<Element>}
   *
   * <p>To get all the 'title' text : {@code xPathEvaluator.evaluate("book/title[x]/text()");
   * returns List<String>}
   *
   * <p>To get the value of category attribute 'children': {@code
   * xPathEvaluator.evaluate("book/@category"); returns String}
   *
   * <p>To get the text value 'Excerpt': {@code xPathEvaluator.evaluate("book/text()"); returns
   * String}
   *
   * <p>To get book element: {@code xPathEvaluator.evaluate("book/"); returns Element}
   *
   * <p>To get self root element: {code xPathEvaluator.evaluate(""); return Element}
   *
   * @param xPath the XPath expression to evaluate
   * @param clazz the expected response type. Can be String.class, Element.class, List.class
   * @return the result of evaluating the segment on the element, or null if the segment could not
   *     be evaluated
   */
  <T> T evaluate(String xPath, Class<T> clazz);
}
