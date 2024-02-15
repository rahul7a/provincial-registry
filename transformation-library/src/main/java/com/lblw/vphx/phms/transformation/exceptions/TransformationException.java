package com.lblw.vphx.phms.transformation.exceptions;

/** Custom exception for the project. */
public class TransformationException extends RuntimeException {

  public TransformationException(String message, Throwable e) {
    super(message, e);
  }
}
