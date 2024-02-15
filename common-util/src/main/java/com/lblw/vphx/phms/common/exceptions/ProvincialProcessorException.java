package com.lblw.vphx.phms.common.exceptions;

/** ProvincialProcessorException class to manage RuntimeException. */
public class ProvincialProcessorException extends RuntimeException {
  public ProvincialProcessorException(String message, Throwable ex) {
    super(message, ex);
  }

  public ProvincialProcessorException(String message) {
    super(message);
  }
}
