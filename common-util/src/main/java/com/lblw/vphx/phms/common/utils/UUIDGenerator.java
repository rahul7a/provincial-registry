package com.lblw.vphx.phms.common.utils;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/** Generates a UUID */
@Slf4j
@Component
public class UUIDGenerator {

  /**
   * It creates unique identifier number.
   *
   * @return generated random UUID
   */
  @NonNull
  public String generateUUID() {
    return UUID.randomUUID().toString();
  }
}
