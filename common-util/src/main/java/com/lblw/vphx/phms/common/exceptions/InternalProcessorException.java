package com.lblw.vphx.phms.common.exceptions;

/**
 * InternalProcessorException class to manage RuntimeException.
 *
 * <p>Handled by {@link com.lblw.vphx.phms.common.handlers.DefaultExceptionHandler} and is thrown
 * with Error Code: EM.075
 */
public class InternalProcessorException extends RuntimeException {

  public InternalProcessorException(String message, Throwable ex) {
    super(message, ex);
  }

  public InternalProcessorException(String message) {
    super(message);
  }
}
