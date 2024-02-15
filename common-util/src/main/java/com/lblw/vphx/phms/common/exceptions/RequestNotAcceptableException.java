package com.lblw.vphx.phms.common.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Custom exception for the Invalid Province */
@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
public class RequestNotAcceptableException extends RuntimeException {

  public RequestNotAcceptableException(String message) {
    super(message);
  }

  public RequestNotAcceptableException(Exception e) {
    super(e);
  }
}
