package com.lblw.vphx.phms.common.exceptions;

/** InternalTimeoutException class to manage all Internal Timeout Exception. */
public class InternalTimeoutException extends RuntimeException {
  public InternalTimeoutException(String message, Throwable ex) {
    super(message, ex);
  }

  public InternalTimeoutException(String message) {
    super(message);
  }
}
