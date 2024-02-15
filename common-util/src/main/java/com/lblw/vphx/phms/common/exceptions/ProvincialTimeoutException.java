package com.lblw.vphx.phms.common.exceptions;

/** ProvincialTimeoutException class to manage all Provincial Timeout Exception. */
public class ProvincialTimeoutException extends RuntimeException {
  public ProvincialTimeoutException(String message, Throwable ex) {
    super(message, ex);
  }

  public ProvincialTimeoutException(String message) {
    super(message);
  }
}
