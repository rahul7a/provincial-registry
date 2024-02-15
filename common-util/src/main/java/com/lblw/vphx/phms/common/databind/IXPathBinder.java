package com.lblw.vphx.phms.common.databind;

interface IXPathBinder {
  /**
   * An xPath to object binder. Implementations to make use of {@link XPathTarget} and {@link
   * XPathTarget.Binding} annotations as meta annotation for defining xPath and its target to bind
   * to
   *
   * <p>Simple Usage Example: Given XML
   *
   * <pre>{@code
   * <root>
   *     <a>Target</a>
   * </root>
   * }</pre>
   *
   * <p>Define Mapper:
   *
   * <pre>{@code
   * class Pojo {
   *     public String a;
   * }
   *
   * class PojoMapper extends Pojo {
   *     @XPathTarget
   *     public void bindA(@XPathTarget.Binding(xPath = "root/a") String a) {
   *        this.a = a;
   *     }
   * }
   * }</pre>
   *
   * <p>Bind:
   *
   * <pre>{@code
   * IXPathBinder xPathBinder;
   * Pojo pojo = new PojoMapper();
   * xPathBinder.bind(pojo);
   * print(pojo.a)
   * }</pre>
   *
   * Will bind "Target" to pojo.a
   *
   * @param instance The instance of the class to bind to. Mutates the instance
   */
  void bind(Object instance);
}
