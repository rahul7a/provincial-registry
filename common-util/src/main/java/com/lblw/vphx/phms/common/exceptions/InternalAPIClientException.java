package com.lblw.vphx.phms.common.exceptions;

public class InternalAPIClientException extends RuntimeException {
  private final String message;

  public InternalAPIClientException(String message) {
    super(message);
    this.message = message;
  }

  public InternalAPIClientException(String message, Throwable ex) {
    super(message, ex);
    this.message = message;
  }

  @Override
  public String getMessage() {
    return message;
  }
}
