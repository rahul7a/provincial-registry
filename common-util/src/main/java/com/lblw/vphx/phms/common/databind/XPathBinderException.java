package com.lblw.vphx.phms.common.databind;

/** Custom Runtime exception for XPathBinder interface */
public class XPathBinderException extends RuntimeException {
  public XPathBinderException(String message, Throwable cause) {
    super(message, cause);
  }

  public XPathBinderException(Throwable cause) {
    super(cause);
  }
}
